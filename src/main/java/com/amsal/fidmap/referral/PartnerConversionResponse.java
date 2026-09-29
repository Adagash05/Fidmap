package com.amsal.fidmap.referral;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PartnerConversionResponse(

        UUID id,

        UUID workspaceId,

        String product,

        long revenueAmountMinor,

        String currency,

        BigDecimal commissionPercentage,

        long commissionAmountMinor,

        ReferralConversionStatus conversionStatus,

        ReferralPayoutStatus payoutStatus,

        Instant createdAt,

        Instant paidAt,

        String payoutReference
) {
}