package com.amsal.fidmap.decision;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Request/response shapes for the decision module. */
public final class DecisionDtos {

    private DecisionDtos() {
    }

    public record PriorityInputRequest(Integer urgency, Integer strategic, Integer effort) {}

    public record RoadmapDraft(String title, String description, LocalDate targetDate) {}

    /**
     * @param roadmapItemId existing item (same workspace) to link, OR
     * @param newRoadmapItem a new item to create and link; never both
     */
    public record DecisionRequest(Recommendation action, String rationale,
                                  UUID roadmapItemId, RoadmapDraft newRoadmapItem) {}

    public record PriorityRow(UUID feedbackId, String title, String boardName, String status,
                              int supporters, int surveyResponses,
                              PriorityResult result, Integer effort,
                              Recommendation decidedAction, UUID roadmapItemId) {}

    public record DecisionView(UUID id, Recommendation recommendedAction, Recommendation decidedAction,
                               String rationale, int scoreSnapshot, Confidence confidence,
                               String scoringVersion, UUID roadmapItemId,
                               UUID decidedBy, Instant decidedAt) {}

    /** Underlying records the evidence comes from, so every claim can be traced. */
    public record LinkedSurvey(Long id, String title, String status, long responses) {}

    public record MergedRequest(UUID id, String title, Instant mergedAt) {}

    public record DecisionBrief(UUID feedbackId, String title, String description, String boardName,
                                String status, PriorityEvidence evidence, PriorityResult result,
                                PriorityInputRequest inputs, UUID roadmapItemId,
                                List<DecisionView> history,
                                List<LinkedSurvey> linkedSurveys,
                                List<MergedRequest> mergedRequests) {}
}
