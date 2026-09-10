package com.amsal.fidmap.payment.billing;

import lombok.Builder;

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

        boolean privateBoards
) {
}