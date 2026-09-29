package com.amsal.fidmap.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReferralInvitationRepository
        extends JpaRepository<ReferralInvitation, UUID> {

    Optional<ReferralInvitation> findByToken(String token);

    Optional<ReferralInvitation> findTopByEmailIgnoreCaseAndAcceptedFalseOrderByCreatedAtDesc(
            String email
    );
}