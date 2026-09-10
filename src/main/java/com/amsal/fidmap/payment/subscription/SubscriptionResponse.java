package com.amsal.fidmap.payment.subscription;


import com.amsal.fidmap.payment.billing.BillingPlan;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID workspaceId,
        BillingPlan plan,
        String planName,
        SubscriptionStatus status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd,
        Instant trialEndsAt
) {
}