package com.amsal.fidmap.referral;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PartnerLoginRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}