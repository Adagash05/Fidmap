package com.amsal.fidmap.referral;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReferralConversionResponse {

    private UUID id;

    private UUID partnerId;

    private UUID workspaceId;

    private String providerOrderId;

    private String providerSubscriptionId;

    private String providerProductId;

    private String product;

    private long revenueAmountMinor;

    private String currency;

    private BigDecimal commissionPercentage;

    private long commissionAmountMinor;

    private ReferralConversionStatus status;

    private Instant createdAt;
}