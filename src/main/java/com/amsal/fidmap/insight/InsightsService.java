package com.amsal.fidmap.insight;

import com.amsal.fidmap.duplicate.TextSimilarity;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Deterministic, explainable insights. Themes are recurring title terms
 * (no ML, no external calls); every theme lists the requests behind it so a
 * person can verify it.
 */
@Service
@RequiredArgsConstructor
public class InsightsService {

    static final int WEEKS = 8;
    static final int MAX_THEMES = 12;

    private final FeedbackPostRepository postRepository;
    private final WorkspaceAuthorizationService authorization;

    @PersistenceContext
    private EntityManager em;

    public record ThemePost(UUID id, String title, long votes) {}

    public record Theme(String term, int requestCount, long totalVotes, List<ThemePost> topRequests) {}

    public record WeekPoint(LocalDate weekStart, long newRequests, long newVotes) {}

    public record Insights(int openRequests, Map<String, Long> byStatus, List<Theme> themes,
                           List<WeekPoint> weekly, String method) {}

    @Transactional(readOnly = true)
    public Insights insights(UUID workspaceId) {

        authorization.requireStaff(workspaceId);

        List<FeedbackPost> posts = postRepository.findCanonicalByWorkspaceId(workspaceId);

        Map<String, Long> byStatus = new TreeMap<>();
        Map<String, List<FeedbackPost>> byTerm = new HashMap<>();
        int open = 0;

        for (FeedbackPost p : posts) {

            String status = p.getStatus() == null ? "OPEN" : p.getStatus().name();
            byStatus.merge(status, 1L, Long::sum);

            if (status.equals("COMPLETED") || status.equals("REJECTED")) {
                continue;
            }

            open++;

            for (String term : TextSimilarity.tokens(p.getTitle())) {
                byTerm.computeIfAbsent(term, k -> new ArrayList<>()).add(p);
            }
        }

        List<Theme> themes = byTerm.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(e -> {
                    List<FeedbackPost> list = e.getValue().stream()
                            .sorted(Comparator.comparingLong(InsightsService::votes).reversed()
                                    .thenComparing(p -> p.getId().toString()))
                            .toList();
                    long total = list.stream().mapToLong(InsightsService::votes).sum();
                    return new Theme(e.getKey(), list.size(), total,
                            list.stream().limit(5)
                                    .map(p -> new ThemePost(p.getId(), p.getTitle(), votes(p))).toList());
                })
                .sorted(Comparator.comparingInt(Theme::requestCount).reversed()
                        .thenComparing(Comparator.comparingLong(Theme::totalVotes).reversed())
                        .thenComparing(Theme::term))
                .limit(MAX_THEMES)
                .toList();

        return new Insights(open, byStatus, themes, weekly(workspaceId, posts),
                "Themes are title terms shared by 2+ open requests. Weekly counts use creation dates.");
    }

    private List<WeekPoint> weekly(UUID workspaceId, List<FeedbackPost> posts) {

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate thisWeek = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate first = thisWeek.minusWeeks(WEEKS - 1L);

        long[] requests = new long[WEEKS];
        long[] votes = new long[WEEKS];

        for (FeedbackPost p : posts) {
            bucket(requests, first, p.getCreatedAt());
        }

        List<Instant> voteTimes = em.createQuery("""
                SELECT v.createdAt FROM Vote v
                WHERE v.feedbackPost.workspace.id = :w AND v.createdAt >= :from""", Instant.class)
                .setParameter("w", workspaceId)
                .setParameter("from", first.atStartOfDay().toInstant(ZoneOffset.UTC))
                .getResultList();

        for (Instant t : voteTimes) {
            bucket(votes, first, t.atZone(ZoneOffset.UTC).toLocalDate());
        }

        List<WeekPoint> out = new ArrayList<>();

        for (int i = 0; i < WEEKS; i++) {
            out.add(new WeekPoint(first.plusWeeks(i), requests[i], votes[i]));
        }

        return out;
    }

    private static void bucket(long[] counts, LocalDate first, LocalDate date) {

        if (date == null || date.isBefore(first)) {
            return;
        }

        int idx = (int) ChronoUnit.WEEKS.between(first, date);

        if (idx >= 0 && idx < counts.length) {
            counts[idx]++;
        }
    }

    private static long votes(FeedbackPost p) {
        return p.getVoteCount() == null ? 0 : p.getVoteCount();
    }
}
