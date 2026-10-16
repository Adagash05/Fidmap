package com.amsal.fidmap.decision;

import com.amsal.fidmap.audit.AuditService;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.decision.DecisionDtos.*;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.feedbackPost.FeedbackStatus;
import com.amsal.fidmap.roadmap.Roadmap;
import com.amsal.fidmap.roadmap.RoadmapRepository;
import com.amsal.fidmap.roadmapItem.AddItemRequest;
import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.roadmapItem.RoadmapItemRepository;
import com.amsal.fidmap.roadmapItem.RoadmapItemService;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private static final int MAX_RATIONALE = 4000;

    private final EvidenceCollector collector;
    private final FeedbackPostRepository feedbackPostRepository;
    private final FeedbackPriorityInputRepository inputRepository;
    private final ProductDecisionRepository decisionRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final RoadmapItemService roadmapItemService;
    private final WorkspaceAuthorizationService authorization;
    private final AuditService auditService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    // ----- ranked list ------------------------------------------------------------------------

    /**
     * Ranked, paginated list of open requests with an explained score. Terminal
     * requests (COMPLETED / REJECTED) are excluded unless {@code includeClosed}.
     */
    @Transactional(readOnly = true)
    public PageResponse<PriorityRow> priorities(UUID workspaceId, Integer page, Integer size,
                                                boolean includeClosed, String sort) {

        authorization.requireStaff(workspaceId);

        Instant now = Instant.now();
        Map<UUID, ProductDecision> latest = latestDecisions(workspaceId);

        List<PriorityRow> rows = new ArrayList<>();

        for (EvidenceCollector.Collected c : collector.collect(workspaceId, null).values()) {

            String status = c.post().status();

            if (!includeClosed && ("COMPLETED".equals(status) || "REJECTED".equals(status))) {
                continue;
            }

            PriorityResult result = PriorityScoringEngine.score(c.evidence(), now);
            ProductDecision d = latest.get(c.post().id());

            rows.add(new PriorityRow(c.post().id(), c.post().title(), c.post().boardName(), status,
                    c.evidence().supporters(), c.evidence().surveyResponses(), result,
                    c.evidence().effort(), d == null ? null : d.getDecidedAction(),
                    c.post().roadmapItemId()));
        }

        Comparator<PriorityRow> byScore = Comparator
                .comparingInt((PriorityRow r) -> r.result().score()).reversed()
                .thenComparing(Comparator.comparingInt((PriorityRow r) -> r.supporters()).reversed())
                .thenComparing(r -> r.feedbackId().toString());

        Comparator<PriorityRow> order = "supporters".equals(sort)
                ? Comparator.comparingInt((PriorityRow r) -> r.supporters()).reversed()
                  .thenComparing(r -> r.feedbackId().toString())
                : byScore;

        rows.sort(order);

        var pageable = Paging.of(page, size, org.springframework.data.domain.Sort.unsorted());
        int from = (int) Math.min(pageable.getOffset(), rows.size());
        int to = Math.min(from + pageable.getPageSize(), rows.size());

        return PageResponse.of(new PageImpl<>(rows.subList(from, to), pageable, rows.size()), r -> r);
    }

    // ----- brief ------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DecisionBrief brief(UUID feedbackId) {

        FeedbackPost post = loadPost(feedbackId);
        UUID workspaceId = post.getWorkspace().getId();

        authorization.requireStaff(workspaceId);

        EvidenceCollector.Collected c = collector.collect(workspaceId, feedbackId).get(feedbackId);

        if (c == null) {
            // merged posts have no brief of their own
            throw new IllegalStateException("This request was merged into another one; open the canonical request");
        }

        PriorityResult result = PriorityScoringEngine.score(c.evidence(), Instant.now());

        List<DecisionView> history = decisionRepository.findHistory(feedbackId).stream()
                .map(DecisionService::view).toList();

        return new DecisionBrief(feedbackId, post.getTitle(), post.getDescription(),
                c.post().boardName(), c.post().status(), c.evidence(), result,
                new PriorityInputRequest(c.evidence().urgency(), c.evidence().strategic(), c.evidence().effort()),
                c.post().roadmapItemId(), history, linkedSurveys(feedbackId, workspaceId),
                feedbackPostRepository.findByMergedIntoId(feedbackId).stream()
                        .map(m -> new MergedRequest(m.getId(), m.getTitle(), m.getMergedAt()))
                        .toList());
    }

    // ----- inputs -----------------------------------------------------------------------------

    @Transactional
    public PriorityInputRequest saveInputs(UUID feedbackId, PriorityInputRequest request) {

        FeedbackPost post = loadPost(feedbackId);
        UUID workspaceId = post.getWorkspace().getId();

        User user = authorization.requireStaffWrite(workspaceId);

        requireScale("Urgency", request.urgency());
        requireScale("Strategic fit", request.strategic());
        requireScale("Effort", request.effort());

        FeedbackPriorityInput input = inputRepository.findByFeedbackPostId(feedbackId)
                .orElseGet(() -> FeedbackPriorityInput.builder()
                        .feedbackPostId(feedbackId).workspaceId(workspaceId).build());

        input.setUrgency(request.urgency());
        input.setStrategic(request.strategic());
        input.setEffort(request.effort());
        input.setUpdatedBy(user.getId());
        input.setUpdatedAt(Instant.now());

        inputRepository.save(input);

        return request;
    }

    // ----- decide -----------------------------------------------------------------------------

    @Transactional
    public DecisionView decide(UUID feedbackId, DecisionRequest request) {

        FeedbackPost post = loadPost(feedbackId);
        UUID workspaceId = post.getWorkspace().getId();

        User user = authorization.requireStaffWrite(workspaceId);

        if (request == null || request.action() == null) {
            throw new IllegalArgumentException("A decision action is required");
        }

        if (post.getMergedInto() != null) {
            throw new IllegalStateException("This request was merged; decide on the canonical request");
        }

        EvidenceCollector.Collected c = collector.collect(workspaceId, feedbackId).get(feedbackId);
        PriorityResult result = PriorityScoringEngine.score(c.evidence(), Instant.now());

        String rationale = request.rationale() == null ? null : request.rationale().trim();

        if (rationale != null && rationale.length() > MAX_RATIONALE) {
            throw new IllegalArgumentException("Rationale is too long (max " + MAX_RATIONALE + " characters)");
        }

        // Overriding the system recommendation, and every rejection, needs a human reason.
        boolean override = request.action() != result.recommendation();
        if ((override || request.action() == Recommendation.REJECT)
                && (rationale == null || rationale.isBlank())) {
            throw new IllegalArgumentException(
                    "A rationale is required when you override the recommendation or reject a request");
        }

        if (request.roadmapItemId() != null && request.newRoadmapItem() != null) {
            throw new IllegalArgumentException("Link an existing roadmap item or create a new one, not both");
        }

        boolean wantsRoadmap = request.roadmapItemId() != null || request.newRoadmapItem() != null;
        if (wantsRoadmap && request.action() != Recommendation.PRIORITIZE
                && request.action() != Recommendation.CONSIDER) {
            throw new IllegalArgumentException("Only prioritised or considered requests can go on the roadmap");
        }

        RoadmapItem item = null;

        if (request.roadmapItemId() != null) {
            item = roadmapItemRepository.findById(request.roadmapItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Roadmap item not found"));

            if (item.getRoadmap() == null || item.getRoadmap().getWorkspace() == null
                    || !workspaceId.equals(item.getRoadmap().getWorkspace().getId())) {
                throw new AccessDeniedException("You do not have access to this roadmap item");
            }

            post.setRoadMapItem(item);

        } else if (request.newRoadmapItem() != null) {
            Roadmap roadmap = roadmapRepository.findRoadmapByWorkspaceId(workspaceId);

            if (roadmap == null) {
                throw new IllegalStateException("This workspace has no roadmap yet");
            }

            RoadmapDraft draft = request.newRoadmapItem();
            AddItemRequest add = new AddItemRequest();
            add.setTitle(draft.title() == null || draft.title().isBlank() ? post.getTitle() : draft.title());
            add.setDescription(draft.description());
            add.setTargetDate(draft.targetDate());
            add.setFeedbackPostIds(List.of(feedbackId));

            var created = roadmapItemService.createRoadmapItem(roadmap.getId(), add).getData();
            item = roadmapItemRepository.findById(created.getId()).orElseThrow();
        }

        // Keep the public status in step with the decision (human-visible, reversible).
        if (request.action() == Recommendation.REJECT) {
            post.setStatus(FeedbackStatus.REJECTED);
        } else if (item != null && post.getStatus() == FeedbackStatus.OPEN) {
            post.setStatus(FeedbackStatus.PLANNED);
        }
        feedbackPostRepository.save(post);

        ProductDecision decision = ProductDecision.builder()
                .feedbackPost(post)
                .workspaceId(workspaceId)
                .recommendedAction(result.recommendation())
                .decidedAction(request.action())
                .rationale(rationale)
                .scoreSnapshot(result.score())
                .confidenceSnapshot(result.confidence())
                .scoringVersion(result.version())
                .evidenceSnapshot(snapshot(c.evidence()))
                .roadmapItem(item)
                .decidedBy(user.getId())
                .decidedAt(Instant.now())
                .build();

        ProductDecision saved = decisionRepository.save(decision);
        auditService.record(workspaceId, user.getId(), "DECISION_RECORDED", "feedback", feedbackId.toString(),
                request.action() + (override ? " (override of " + result.recommendation() + ")" : ""));
        return view(saved);
    }

    // ----- helpers ----------------------------------------------------------------------------

    private List<LinkedSurvey> linkedSurveys(UUID feedbackId, UUID workspaceId) {

        List<LinkedSurvey> out = new ArrayList<>();

        for (Object[] r : em.createQuery("""
                SELECT s.id, s.title, s.status, COUNT(r)
                FROM Survey s LEFT JOIN SurveyResponse r ON r.survey = s
                WHERE s.linkedFeedbackPost.id = :p AND s.workspace.id = :w
                GROUP BY s.id, s.title, s.status""", Object[].class)
                .setParameter("p", feedbackId).setParameter("w", workspaceId).getResultList()) {
            out.add(new LinkedSurvey((Long) r[0], (String) r[1], String.valueOf(r[2]), (Long) r[3]));
        }

        return out;
    }

    private FeedbackPost loadPost(UUID id) {
        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(id);

        if (post == null || post.getWorkspace() == null) {
            throw new FeedbackPostNotFoundException("feedback post is not found");
        }

        return post;
    }

    private Map<UUID, ProductDecision> latestDecisions(UUID workspaceId) {
        Map<UUID, ProductDecision> map = new HashMap<>();
        // ordered newest first; keep the first seen per post
        for (ProductDecision d : decisionRepository.findByWorkspace(workspaceId)) {
            map.putIfAbsent(d.getFeedbackPost().getId(), d);
        }
        return map;
    }

    private static void requireScale(String label, Integer v) {
        if (v != null && (v < 1 || v > 5)) {
            throw new IllegalArgumentException(label + " must be between 1 and 5");
        }
    }

    private static String snapshot(PriorityEvidence e) {
        return "supporters=" + e.supporters()
                + ";surveyResponses=" + e.surveyResponses()
                + ";surveyAvg=" + e.surveyAverageRating()
                + ";lastActivity=" + e.lastActivityAt()
                + ";urgency=" + e.urgency()
                + ";strategic=" + e.strategic()
                + ";effort=" + e.effort();
    }

    private static DecisionView view(ProductDecision d) {
        return new DecisionView(d.getId(), d.getRecommendedAction(), d.getDecidedAction(), d.getRationale(),
                d.getScoreSnapshot(), d.getConfidenceSnapshot(), d.getScoringVersion(),
                d.getRoadmapItem() == null ? null : d.getRoadmapItem().getId(),
                d.getDecidedBy(), d.getDecidedAt());
    }
}
