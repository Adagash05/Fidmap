package com.amsal.fidmap.referral;

import com.amsal.fidmap.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerDashboardService {

    private final ReferralPartnerRepository partnerRepository;
    private final ReferralConversionRepository conversionRepository;

    @Value("${fidmap.frontend-url:https://fidmap.co}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public PartnerDashboardResponse getDashboard(User user) {

        ReferralPartner partner =
                getPartner(user);

        long referredCustomers =
                conversionRepository.countReferredCustomers(
                        partner.getId()
                );

        long conversions =
                conversionRepository.countByPartnerId(
                        partner.getId()
                );

        long pendingConversions =
                conversionRepository.countByPartnerIdAndPayoutStatus(
                        partner.getId(),
                        ReferralPayoutStatus.PENDING
                );

        long reversedConversions =
                conversionRepository.countByPartnerIdAndStatus(
                        partner.getId(),
                        ReferralConversionStatus.REVERSED
                );

        long revenue =
                defaultLong(
                        conversionRepository.sumRevenue(
                                partner.getId()
                        )
                );

        long earned =
                defaultLong(
                        conversionRepository.sumCommission(
                                partner.getId()
                        )
                );

        long pending =
                defaultLong(
                        conversionRepository.sumPendingCommission(
                                partner.getId()
                        )
                );

        long paid =
                defaultLong(
                        conversionRepository.sumPaidCommission(
                                partner.getId()
                        )
                );

        long reversed =
                defaultLong(
                        conversionRepository.sumReversedCommission(
                                partner.getId()
                        )
                );

        return new PartnerDashboardResponse(
                partner.getName(),
                partner.getEmail(),
                partner.getReferralCode(),
                buildReferralLink(
                        partner.getReferralCode()
                ),
                referredCustomers,
                conversions,
                pendingConversions,
                reversedConversions,
                revenue,
                earned,
                pending,
                paid,
                reversed,
                "USD"
        );
    }

    @Transactional(readOnly = true)
    public ReferralPartnerProfileResponse getProfile(
            User user
    ) {

        ReferralPartner partner =
                getPartner(user);

        return new ReferralPartnerProfileResponse(
                partner.getId(),
                partner.getName(),
                partner.getEmail(),
                partner.getReferralCode(),
                buildReferralLink(
                        partner.getReferralCode()
                ),
                partner.getCommissionPercentage(),
                partner.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<PartnerConversionResponse> getConversions(
            User user
    ) {

        ReferralPartner partner =
                getPartner(user);

        return conversionRepository
                .findAllByPartnerIdOrderByCreatedAtDesc(
                        partner.getId()
                )
                .stream()
                .map(c ->
                        new PartnerConversionResponse(
                                c.getId(),
                                c.getWorkspace().getId(),
                                c.getProduct(),
                                c.getRevenueAmountMinor(),
                                c.getCurrency(),
                                c.getCommissionPercentage(),
                                c.getCommissionAmountMinor(),
                                c.getStatus(),
                                c.getPayoutStatus(),
                                c.getCreatedAt(),
                                c.getPaidAt(),
                                c.getPayoutReference()
                        )
                )
                .toList();
    }

    private ReferralPartner getPartner(User user) {

        if (user == null || user.getId() == null) {
            throw new IllegalStateException(
                    "Authenticated partner user is missing"
            );
        }

        return partnerRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Partner account not found"
                        )
                );
    }

    private String buildReferralLink(String code) {

        String base =
                frontendUrl == null
                        ? "https://fidmap.co"
                        : frontendUrl.trim();

        while (base.endsWith("/")) {
            base = base.substring(
                    0,
                    base.length() - 1
            );
        }

        return base + "/?ref=" + code;
    }

    private long defaultLong(Long value) {

        return value == null
                ? 0L
                : value;
    }
}