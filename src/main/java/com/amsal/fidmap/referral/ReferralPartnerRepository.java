package com.amsal.fidmap.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReferralPartnerRepository
        extends JpaRepository<ReferralPartner, UUID> {

    Optional<ReferralPartner> findByReferralCodeIgnoreCase(
            String referralCode
    );

    boolean existsByReferralCodeIgnoreCase(
            String referralCode
    );
    Optional<ReferralPartner> findByUserId(UUID userId);
}