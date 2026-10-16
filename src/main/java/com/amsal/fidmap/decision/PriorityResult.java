package com.amsal.fidmap.decision;

import java.util.List;

public record PriorityResult(
        String version,
        int score,
        int scoreLow,
        int scoreHigh,
        Confidence confidence,
        Recommendation recommendation,
        String reason,
        List<ScoreComponent> components,
        List<String> missingEvidence
) {}
