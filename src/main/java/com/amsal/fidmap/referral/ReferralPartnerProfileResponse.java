package com.amsal.fidmap.referral;

import java.math.BigDecimal;
import java.util.UUID;

public record ReferralPartnerProfileResponse(

        UUID id,

        String name,

        String email,

        String referralCode,

        String referralLink,

        BigDecimal commissionPercentage,

        ReferralPartnerStatus status
) {
}