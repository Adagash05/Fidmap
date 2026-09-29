package com.amsal.fidmap.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferralConversionRepository
        extends JpaRepository<ReferralConversion, UUID> {

    boolean existsByProviderOrderId(String providerOrderId);

    Optional<ReferralConversion> findByProviderOrderId(
            String providerOrderId
    );

    List<ReferralConversion>
    findAllByPartnerIdOrderByCreatedAtDesc(
            UUID partnerId
    );

    long countByPartnerId(UUID partnerId);

    long countByPartnerIdAndStatus(
            UUID partnerId,
            ReferralConversionStatus status
    );

    long countByPartnerIdAndPayoutStatus(
            UUID partnerId,
            ReferralPayoutStatus payoutStatus
    );

    @Query("""
            SELECT COUNT(DISTINCT c.workspace.id)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            """)
    long countReferredCustomers(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.revenueAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            """)
    Long sumRevenue(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            """)
    Long sumCommission(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            AND c.payoutStatus = :payoutStatus
            """)
    Long sumCommissionByPartnerIdAndPayoutStatus(
            @Param("partnerId") UUID partnerId,
            @Param("payoutStatus") ReferralPayoutStatus payoutStatus
    );

    @Query("""
            SELECT COUNT(c)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            """)
    long countEarnedConversions(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COUNT(c)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.REVERSED
            """)
    long countReversedConversions(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.status = com.amsal.fidmap.referral.ReferralConversionStatus.REVERSED
            AND c.partner.id = :partnerId
            """)
    Long sumReversedCommission(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            AND c.payoutStatus = com.amsal.fidmap.referral.ReferralPayoutStatus.PENDING
            """)
    Long sumPendingCommission(
            @Param("partnerId") UUID partnerId
    );

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmountMinor), 0)
            FROM ReferralConversion c
            WHERE c.partner.id = :partnerId
            AND c.status = com.amsal.fidmap.referral.ReferralConversionStatus.EARNED
            AND c.payoutStatus = com.amsal.fidmap.referral.ReferralPayoutStatus.PAID
            """)
    Long sumPaidCommission(
            @Param("partnerId") UUID partnerId
    );
}