package com.amsal.fidmap.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReferralPayoutEventRepository
        extends JpaRepository<ReferralPayoutEvent, UUID> {

    List<ReferralPayoutEvent>
    findAllByConversionIdOrderByCreatedAtDesc(
            UUID conversionId
    );
}