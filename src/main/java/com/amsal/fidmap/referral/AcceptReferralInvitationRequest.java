package com.amsal.fidmap.referral;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptReferralInvitationRequest(

        @NotBlank
        String token,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}