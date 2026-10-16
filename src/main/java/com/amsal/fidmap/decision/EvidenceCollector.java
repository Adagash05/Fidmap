package com.amsal.fidmap.decision;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Gathers factual evidence for every canonical (non-merged) request of ONE
 * workspace using a fixed number of bulk queries (no per-post queries).
 * Every query is filtered by workspace id.
 */
@Component
public class EvidenceCollector {

    @PersistenceContext
    private EntityManager em;

    public record PostRow(UUID id, String title, String boardName, String status,
                          Instant createdAt, UUID roadmapItemId) {}

    public record Collected(PostRow post, PriorityEvidence evidence, FeedbackPriorityInput input) {}

    @Transactional(readOnly = true)
    public Map<UUID, Collected> collect(UUID workspaceId, UUID onlyPostId) {

        String postFilter = onlyPostId == null ? "" : " AND p.id = :pid";

        var postQuery = em.createQuery("""
                SELECT p.id, p.title, b.name, p.status, p.createdAt, ri.id, eu.id
                FROM FeedbackPost p
                LEFT JOIN p.board b
                LEFT JOIN p.roadMapItem ri
                LEFT JOIN p.endUser eu
                WHERE p.workspace.id = :w AND p.mergedInto IS NULL""" + postFilter, Object[].class)
                .setParameter("w", workspaceId);
        if (onlyPostId != null) postQuery.setParameter("pid", onlyPostId);

        Map<UUID, PostRow> posts = new LinkedHashMap<>();
        Map<UUID, Set<UUID>> supporters = new HashMap<>();
        Map<UUID, Instant> last = new HashMap<>();

        for (Object[] r : postQuery.getResultList()) {
            UUID id = (UUID) r[0];
            Instant created = r[4] == null ? null : ((LocalDate) r[4]).atStartOfDay().toInstant(ZoneOffset.UTC);
            posts.put(id, new PostRow(id, (String) r[1], (String) r[2],
                    r[3] == null ? null : r[3].toString(), created, (UUID) r[5]));
            Set<UUID> set = supporters.computeIfAbsent(id, k -> new HashSet<>());
            if (r[6] != null) set.add((UUID) r[6]);
            touch(last, id, created);
        }

        if (posts.isEmpty()) {
            return Map.of();
        }

        var voteQuery = em.createQuery("""
                SELECT v.feedbackPost.id, v.endUser.id, v.createdAt
                FROM Vote v
                WHERE v.feedbackPost.workspace.id = :w AND v.feedbackPost.mergedInto IS NULL"""
                + postFilter.replace("p.id", "v.feedbackPost.id"), Object[].class)
                .setParameter("w", workspaceId);
        if (onlyPostId != null) voteQuery.setParameter("pid", onlyPostId);

        for (Object[] r : voteQuery.getResultList()) {
            UUID id = (UUID) r[0];
            if (r[1] != null) supporters.computeIfAbsent(id, k -> new HashSet<>()).add((UUID) r[1]);
            touch(last, id, (Instant) r[2]);
        }

        var commentQuery = em.createQuery("""
                SELECT c.feedbackPost.id, c.endUser.id, c.createdAt
                FROM Comment c
                WHERE c.feedbackPost.workspace.id = :w AND c.feedbackPost.mergedInto IS NULL"""
                + postFilter.replace("p.id", "c.feedbackPost.id"), Object[].class)
                .setParameter("w", workspaceId);
        if (onlyPostId != null) commentQuery.setParameter("pid", onlyPostId);

        for (Object[] r : commentQuery.getResultList()) {
            UUID id = (UUID) r[0];
            if (r[1] != null) supporters.computeIfAbsent(id, k -> new HashSet<>()).add((UUID) r[1]);
            touch(last, id, r[2] == null ? null : ((LocalDate) r[2]).atStartOfDay().toInstant(ZoneOffset.UTC));
        }

        Map<UUID, Long> surveyCounts = new HashMap<>();
        var surveyCountQuery = em.createQuery("""
                SELECT s.linkedFeedbackPost.id, COUNT(r)
                FROM SurveyResponse r JOIN r.survey s
                WHERE s.workspace.id = :w AND s.linkedFeedbackPost IS NOT NULL
                GROUP BY s.linkedFeedbackPost.id""", Object[].class)
                .setParameter("w", workspaceId);
        for (Object[] r : surveyCountQuery.getResultList()) {
            surveyCounts.put((UUID) r[0], (Long) r[1]);
        }

        Map<UUID, double[]> ratings = new HashMap<>(); // {sum, count}
        var ratingQuery = em.createQuery("""
                SELECT s.linkedFeedbackPost.id, a.answer
                FROM SurveyAnswer a JOIN a.question q JOIN q.survey s
                WHERE s.workspace.id = :w AND s.linkedFeedbackPost IS NOT NULL
                  AND q.type = com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType.RATING""", Object[].class)
                .setParameter("w", workspaceId);
        for (Object[] r : ratingQuery.getResultList()) {
            try {
                double v = Double.parseDouble(String.valueOf(r[1]).trim());
                if (v >= 1 && v <= 5) {
                    double[] acc = ratings.computeIfAbsent((UUID) r[0], k -> new double[2]);
                    acc[0] += v;
                    acc[1] += 1;
                }
            } catch (NumberFormatException ignored) {
                // non-numeric legacy answers are not evidence
            }
        }

        Map<UUID, FeedbackPriorityInput> inputs = new HashMap<>();
        for (FeedbackPriorityInput in : em.createQuery(
                "SELECT i FROM FeedbackPriorityInput i WHERE i.workspaceId = :w", FeedbackPriorityInput.class)
                .setParameter("w", workspaceId).getResultList()) {
            inputs.put(in.getFeedbackPostId(), in);
        }

        Map<UUID, Collected> out = new LinkedHashMap<>();

        for (PostRow post : posts.values()) {
            UUID id = post.id();
            FeedbackPriorityInput in = inputs.get(id);
            double[] rating = ratings.get(id);
            long surveyN = surveyCounts.getOrDefault(id, 0L);

            PriorityEvidence evidence = new PriorityEvidence(
                    supporters.getOrDefault(id, Set.of()).size(),
                    (int) surveyN,
                    rating == null ? null : rating[0] / rating[1],
                    last.get(id),
                    in == null ? null : in.getUrgency(),
                    in == null ? null : in.getStrategic(),
                    in == null ? null : in.getEffort());

            out.put(id, new Collected(post, evidence, in));
        }

        return out;
    }

    private static void touch(Map<UUID, Instant> last, UUID id, Instant at) {
        if (at != null) last.merge(id, at, (a, b) -> a.isAfter(b) ? a : b);
    }
}
