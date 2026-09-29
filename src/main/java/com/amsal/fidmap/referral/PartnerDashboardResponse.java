package com.amsal.fidmap.referral;

import java.math.BigDecimal;

public record PartnerDashboardResponse(

        String partnerName,

        String email,

        String referralCode,

        String referralLink,

        long referredCustomers,

        long conversions,

        long pendingConversions,

        long reversedConversions,

        long revenueAmountMinor,

        long commissionEarnedAmountMinor,

        long commissionPendingAmountMinor,

        long commissionPaidAmountMinor,

        long commissionReversedAmountMinor,

        String currency
) {
}