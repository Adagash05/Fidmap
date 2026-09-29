package com.amsal.fidmap.referral;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ReferralInvitationService {

    private final ReferralPartnerRepository partnerRepository;
    private final ReferralInvitationRepository invitationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ReferralInvitation createInvitation(
            ReferralPartner partner
    ) {

        String token = generateToken();

        ReferralInvitation invitation =
                ReferralInvitation.builder()
                        .referralPartner(partner)
                        .email(partner.getEmail())
                        .token(token)
                        .expiresAt(
                                Instant.now()
                                        .plus(7, ChronoUnit.DAYS)
                        )
                        .accepted(false)
                        .createdAt(Instant.now())
                        .build();

        return invitationRepository.save(invitation);
    }

    private String generateToken() {

        byte[] bytes = new byte[48];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /*
    *Hi Kamal,

I've set up your FIDMAP partner account.

Please use this invitation link to create your partner password:

https://app.fidmap.co/partner/accept-invitation?token=...

Once completed, you'll have access to your partner dashboard where you can see referrals, customer purchases, generated revenue and commissions.

Thanks,
Al-Amin
FIDMAP
    *  */
}