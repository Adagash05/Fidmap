package com.amsal.fidmap.feedbackPost;

import java.math.BigDecimal;
import java.util.UUID;

public record FeedbackPriorityResponse(
        UUID feedbackId,
        String title,
        int impactScore,
        int votes,
        int payingCustomers,
        BigDecimal affectedMrr,
        int salesOpportunities,
        int churnRiskCustomers,
        int estimatedEffort
) {}