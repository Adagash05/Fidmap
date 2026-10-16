package com.amsal.fidmap.duplicate;

import com.amsal.fidmap.audit.AuditService;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.notification.FeedbackFollower;
import com.amsal.fidmap.notification.FeedbackFollowerRepository;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.vote.Vote;
import com.amsal.fidmap.vote.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DuplicateService {

    /** Pairwise comparison is O(n^2); beyond this many open requests the scan refuses rather than stalls. */
    static final int MAX_SCAN_POSTS = 1500;
    static final double THRESHOLD = 0.45;

    private final DuplicateSuggestionRepository suggestionRepository;
    private final FeedbackPostRepository postRepository;
    private final VoteRepository voteRepository;
    private final FeedbackFollowerRepository followerRepository;
    private final WorkspaceAuthorizationService authorization;
    private final AuditService auditService;

    public record ScanResult(int created, int comparedPosts) {}

    public record SuggestionView(UUID id, double similarity, String reason, SuggestionStatus status,
                                 PostSummary source, PostSummary target) {}

    public record PostSummary(UUID id, String title, String description, Long votes, String status) {}

    public record MergeResult(UUID canonicalId, int votesMoved, int duplicateVotesDropped, long canonicalVoteCount) {}

    // ----- scan -------------------------------------------------------------------------------

    @Transactional
    public ScanResult scan(UUID workspaceId) {

        authorization.requireStaffWrite(workspaceId);

        List<FeedbackPost> posts = postRepository.findCanonicalByWorkspaceId(workspaceId).stream()
                .filter(p -> p.getStatus() == null || !isClosed(p))
                .toList();

        if (posts.size() > MAX_SCAN_POSTS) {
            throw new IllegalStateException("Too many open requests to scan at once (" + posts.size()
                    + "). Close or merge some requests first.");
        }

        Set<String> known = new HashSet<>();
        for (Object[] pair : suggestionRepository.findAllPairs(workspaceId)) {
            known.add(pair[0] + "|" + pair[1]);
        }

        int created = 0;

        for (int i = 0; i < posts.size(); i++) {
            for (int j = i + 1; j < posts.size(); j++) {

                FeedbackPost a = posts.get(i);
                FeedbackPost b = posts.get(j);

                double sim = TextSimilarity.similarity(a.getTitle(), a.getDescription(), b.getTitle(), b.getDescription());

                if (sim < THRESHOLD) {
                    continue;
                }

                // Normalised order: smaller id first, so (a,b) and (b,a) are one pair.
                boolean aFirst = a.getId().toString().compareTo(b.getId().toString()) < 0;
                FeedbackPost first = aFirst ? a : b;
                FeedbackPost second = aFirst ? b : a;

                if (!known.add(first.getId() + "|" + second.getId())) {
                    continue;
                }

                // Default direction: merge the less-voted into the more-voted.
                long va = votes(a);
                long vb = votes(b);
                FeedbackPost target = va >= vb ? a : b;
                FeedbackPost source = target == a ? b : a;

                suggestionRepository.save(DuplicateSuggestion.builder()
                        .workspaceId(workspaceId)
                        .sourcePostId(source.getId())
                        .targetPostId(target.getId())
                        .similarity(Math.round(sim * 100.0) / 100.0)
                        .reason("Shared terms: " + String.join(", ",
                                TextSimilarity.sharedTerms(a.getTitle(), b.getTitle())))
                        .status(SuggestionStatus.PENDING)
                        .createdAt(Instant.now())
                        .build());

                created++;
            }
        }

        return new ScanResult(created, posts.size());
    }

    // ----- list -------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<SuggestionView> list(UUID workspaceId, Integer page, Integer size) {

        authorization.requireStaff(workspaceId);

        var pageable = Paging.of(page, size, Sort.by(Sort.Order.desc("similarity"), Sort.Order.asc("id")));

        return PageResponse.of(
                suggestionRepository.findByWorkspaceIdAndStatus(workspaceId, SuggestionStatus.PENDING, pageable),
                s -> new SuggestionView(s.getId(), s.getSimilarity(), s.getReason(), s.getStatus(),
                        summary(s.getSourcePostId()), summary(s.getTargetPostId())));
    }

    // ----- resolve ----------------------------------------------------------------------------

    @Transactional
    public void dismiss(UUID suggestionId) {

        DuplicateSuggestion s = load(suggestionId);
        User user = authorization.requireStaffWrite(s.getWorkspaceId());

        requirePending(s);

        s.setStatus(SuggestionStatus.DISMISSED);
        s.setResolvedBy(user.getId());
        s.setResolvedAt(Instant.now());
        suggestionRepository.save(s);
    }

    /**
     * Confirms the suggestion and merges. {@code keepPostId} (optional) picks
     * which of the two becomes the canonical request; default is the suggested target.
     */
    @Transactional
    public MergeResult confirm(UUID suggestionId, UUID keepPostId) {

        DuplicateSuggestion s = load(suggestionId);
        User user = authorization.requireStaffWrite(s.getWorkspaceId());

        requirePending(s);

        UUID keepId = keepPostId == null ? s.getTargetPostId() : keepPostId;

        if (!keepId.equals(s.getTargetPostId()) && !keepId.equals(s.getSourcePostId())) {
            throw new IllegalArgumentException("keepPostId must be one of the two requests in the suggestion");
        }

        UUID dropId = keepId.equals(s.getTargetPostId()) ? s.getSourcePostId() : s.getTargetPostId();

        MergeResult result = merge(s.getWorkspaceId(), dropId, keepId);

        s.setStatus(SuggestionStatus.CONFIRMED);
        s.setResolvedBy(user.getId());
        s.setResolvedAt(Instant.now());
        suggestionRepository.save(s);

        auditService.record(s.getWorkspaceId(), user.getId(), "REQUEST_MERGED", "feedback", dropId.toString(),
                "merged into " + keepId);

        return result;
    }

    /**
     * Merge {@code dropId} into {@code keepId}.
     *
     * - votes are re-pointed; a voter who already voted for the canonical
     *   request is NOT counted twice (their duplicate vote row is deleted)
     * - the merged request, its comments and history are preserved
     * - followers keep following (on the canonical request)
     * - anything previously merged into the dropped request follows it
     */
    MergeResult merge(UUID workspaceId, UUID dropId, UUID keepId) {

        if (dropId.equals(keepId)) {
            throw new IllegalArgumentException("A request cannot be merged into itself");
        }

        FeedbackPost drop = requireInWorkspace(dropId, workspaceId);
        FeedbackPost keep = requireInWorkspace(keepId, workspaceId);

        if (drop.getMergedInto() != null || keep.getMergedInto() != null) {
            throw new IllegalStateException("One of these requests was already merged");
        }

        Set<UUID> keepVoters = new HashSet<>(voteRepository.findVoterIds(keepId));

        int moved = 0;
        int dropped = 0;

        for (Vote vote : voteRepository.findAllByPostId(dropId)) {

            UUID voter = vote.getEndUser() == null ? null : vote.getEndUser().getId();

            if (voter != null && keepVoters.contains(voter)) {
                voteRepository.delete(vote);
                dropped++;
            } else {
                vote.setFeedbackPost(keep);
                voteRepository.save(vote);
                if (voter != null) keepVoters.add(voter);
                moved++;
            }
        }

        voteRepository.flush();

        // Followers: move those not already following the canonical request.
        for (FeedbackFollower follower : followerRepository.findActiveByPostId(dropId)) {
            if (!followerRepository.existsByFeedbackPostIdAndEndUserId(keepId, follower.getEndUser().getId())) {
                follower.setFeedbackPost(keep);
                followerRepository.save(follower);
            }
        }

        for (FeedbackPost earlier : postRepository.findByMergedIntoId(dropId)) {
            earlier.setMergedInto(keep);
            postRepository.save(earlier);
        }

        if (keep.getRoadMapItem() == null && drop.getRoadMapItem() != null) {
            keep.setRoadMapItem(drop.getRoadMapItem());
        }

        long total = voteRepository.countByFeedbackPostId(keepId);
        keep.setVoteCount(total);
        drop.setVoteCount(0L);
        drop.setMergedInto(keep);
        drop.setMergedAt(Instant.now());

        postRepository.save(keep);
        postRepository.save(drop);

        // Any other open suggestion that involved the merged request is now moot.
        for (DuplicateSuggestion other : suggestionRepository.findPendingInvolving(dropId)) {
            other.setStatus(SuggestionStatus.OBSOLETE);
            suggestionRepository.save(other);
        }

        return new MergeResult(keepId, moved, dropped, total);
    }

    // ----- helpers ----------------------------------------------------------------------------

    private DuplicateSuggestion load(UUID id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found"));
    }

    private static void requirePending(DuplicateSuggestion s) {
        if (s.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("This suggestion was already resolved");
        }
    }

    private FeedbackPost requireInWorkspace(UUID id, UUID workspaceId) {
        FeedbackPost post = postRepository.findFeedbackPostById(id);

        if (post == null || post.getWorkspace() == null || !workspaceId.equals(post.getWorkspace().getId())) {
            throw new FeedbackPostNotFoundException("feedback post is not found");
        }

        return post;
    }

    private PostSummary summary(UUID id) {
        FeedbackPost p = postRepository.findFeedbackPostById(id);
        return p == null ? null
                : new PostSummary(p.getId(), p.getTitle(), p.getDescription(), p.getVoteCount(),
                p.getStatus() == null ? null : p.getStatus().name());
    }

    private static long votes(FeedbackPost p) {
        return p.getVoteCount() == null ? 0 : p.getVoteCount();
    }

    private static boolean isClosed(FeedbackPost p) {
        return p.getStatus() != null
                && (p.getStatus().name().equals("COMPLETED") || p.getStatus().name().equals("REJECTED"));
    }
}
