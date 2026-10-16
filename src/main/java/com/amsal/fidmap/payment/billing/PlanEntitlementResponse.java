package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PlanEntitlementResponse(

        BillingPlan billingPlan,

        String planName,

        String billingInterval,

        Integer maxTeamMembers,

        Integer maxBoards,

        Integer maxFeedbackPosts,

        Integer maxEndUsers,

        Integer maxRoadmapItems,

        Integer maxChangelogEntries,

        boolean customBranding,

        boolean removeFidmapBranding,

        boolean privateBoards,

        /** Effective status after applying trial/period expiry (never stale). */
        SubscriptionStatus status,

        /** Server-computed: may this workspace currently use write features? */
        boolean accessActive,

        /** Machine-readable reason, e.g. TRIAL, ACTIVE, LIFETIME, TRIAL_EXPIRED, PERIOD_ENDED. */
        String accessReason,

        /** Trial end or current period end, when applicable. */
        Instant accessEndsAt
) {
}