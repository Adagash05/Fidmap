package com.amsal.fidmap.referral;

public record ReferralAdminSummaryResponse(

        long totalPartners,

        long activePartners,

        long referredCustomers,

        long totalConversions,

        long totalRevenueAmountMinor,

        long totalCommissionEarnedAmountMinor,

        long totalCommissionPendingAmountMinor,

        long totalCommissionPaidAmountMinor,

        long totalCommissionReversedAmountMinor
) {
}