package com.amsal.fidmap.decision;

import java.time.Instant;

/**
 * Raw, factual evidence for one request. Nullable fields mean "not provided";
 * the engine never invents values for them.
 */
public record PriorityEvidence(
        int supporters,
        int surveyResponses,
        Double surveyAverageRating,   // 1..5, null when no rating question was answered
        Instant lastActivityAt,       // null when unknown
        Integer urgency,              // 1..5 human input
        Integer strategic,            // 1..5 human input
        Integer effort                // 1..5 human input, shown separately, NOT part of the score
) {}
