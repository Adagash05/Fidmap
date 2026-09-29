package com.amsal.fidmap.referral;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReferralPayoutRequest(

        @NotNull
        ReferralPayoutStatus status,

        @Size(max = 255)
        String payoutReference,

        @Size(max = 1000)
        String payoutNote
) {
}