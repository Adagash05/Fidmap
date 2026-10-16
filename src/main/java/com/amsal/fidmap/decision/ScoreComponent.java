package com.amsal.fidmap.decision;

/** One explained line of a priority score. {@code value} is null when the evidence is missing. */
public record ScoreComponent(
        String key,
        String label,
        Integer value,
        int weight,
        double contribution,
        String explanation
) {}
