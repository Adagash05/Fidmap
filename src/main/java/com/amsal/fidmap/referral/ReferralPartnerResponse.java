package com.amsal.fidmap.referral;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReferralPartnerResponse {

    private UUID id;

    private String name;

    private String email;

    private String referralCode;

    private String referralLink;

    private BigDecimal commissionPercentage;

    private ReferralPartnerStatus status;

    private long referredCustomers;

    private long revenueAmountMinor;

    private long commissionAmountMinor;

    private long pendingCommissionAmountMinor;

    private long paidCommissionAmountMinor;

    private long reversedCommissionAmountMinor;

    private boolean accountCreated;

    private Instant createdAt;

    private Instant updatedAt;
}