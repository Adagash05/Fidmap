package com.amsal.fidmap.referral;

import java.time.Instant;
import java.util.UUID;

public record ReferralInvitationResponse(

        UUID id,

        String email,

        String invitationUrl,

        Instant expiresAt,

        boolean accepted
) {
}