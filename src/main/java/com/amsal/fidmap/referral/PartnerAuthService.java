package com.amsal.fidmap.referral;

import com.amsal.fidmap.user.Role;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PartnerAuthService {

    private final ReferralInvitationRepository invitationRepository;
    private final ReferralPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void acceptInvitation(
            String token,
            String password
    ) {

        ReferralInvitation invitation =
                invitationRepository.findByToken(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid invitation"
                                )
                        );

        if (invitation.isAccepted()) {
            throw new IllegalStateException(
                    "This invitation has already been accepted"
            );
        }

        if (invitation.getExpiresAt() == null
                || invitation.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalStateException(
                    "This invitation has expired"
            );
        }

        ReferralPartner partner =
                invitation.getReferralPartner();

        if (partner == null) {
            throw new IllegalStateException(
                    "Referral partner not found"
            );
        }

        if (partner.getStatus()
                != ReferralPartnerStatus.ACTIVE) {

            throw new IllegalStateException(
                    "This referral partner account is inactive"
            );
        }

        if (partner.getUser() != null) {
            throw new IllegalStateException(
                    "This partner account has already been created"
            );
        }

        String email =
                invitation.getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (userRepository
                .findByEmailIgnoreCase(email)
                .isPresent()) {

            throw new IllegalStateException(
                    "An account with this email already exists"
            );
        }

        User user =
                User.builder()
                        .fullName(partner.getName())
                        .email(email)
                        .password(
                                passwordEncoder.encode(password)
                        )
                        .role(Role.PARTNER)
                        .build();

        user = userRepository.save(user);

        partner.setUser(user);

        partnerRepository.save(partner);

        invitation.setAccepted(true);
        invitation.setAcceptedAt(Instant.now());

        invitationRepository.save(invitation);
    }
}