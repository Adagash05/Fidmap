package com.amsal.fidmap.referral;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralAdminService {

    private final ReferralPartnerRepository partnerRepository;
    private final ReferralConversionRepository conversionRepository;

    @Value("${fidmap.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public ReferralPartnerResponse createPartner(
            CreateReferralPartnerRequest request
    ) {

        String code = normalizeCode(
                request.getReferralCode()
        );

        if (partnerRepository.existsByReferralCodeIgnoreCase(code)) {
            throw new IllegalArgumentException(
                    "Referral code already exists"
            );
        }

        ReferralPartner partner =
                ReferralPartner.builder()
                        .name(request.getName().trim())
                        .email(normalizeEmail(request.getEmail()))
                        .referralCode(code)
                        .commissionPercentage(
                                request.getCommissionPercentage()
                                        .setScale(2)
                        )
                        .status(ReferralPartnerStatus.ACTIVE)
                        .build();

        partnerRepository.save(partner);

        return toResponse(partner);
    }

    @Transactional(readOnly = true)
    public List<ReferralPartnerResponse> getPartners() {

        return partnerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReferralPartnerResponse getPartner(UUID id) {

        return toResponse(getPartnerEntity(id));
    }

    @Transactional(readOnly = true)
    public ReferralPartner getPartnerEntity(UUID id) {

        return partnerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Referral partner not found"
                        )
                );
    }

    @Transactional
    public ReferralPartnerResponse updatePartner(
            UUID id,
            UpdateReferralPartnerRequest request
    ) {

        ReferralPartner partner = getPartnerEntity(id);

        if (request.getName() != null
                && !request.getName().isBlank()) {

            partner.setName(
                    request.getName().trim()
            );
        }

        if (request.getEmail() != null) {

            partner.setEmail(
                    normalizeEmail(request.getEmail())
            );
        }

        if (request.getCommissionPercentage() != null) {

            partner.setCommissionPercentage(
                    request.getCommissionPercentage()
                            .setScale(2)
            );
        }

        if (request.getStatus() != null) {

            partner.setStatus(
                    request.getStatus()
            );
        }

        partnerRepository.save(partner);

        return toResponse(partner);
    }

    @Transactional(readOnly = true)
    public List<ReferralConversionResponse> getConversions(
            UUID partnerId
    ) {

        if (!partnerRepository.existsById(partnerId)) {
            throw new IllegalArgumentException(
                    "Referral partner not found"
            );
        }

        return conversionRepository
                .findAllByPartnerIdOrderByCreatedAtDesc(partnerId)
                .stream()
                .map(this::toConversionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReferralAdminSummaryResponse getSummary() {

        List<ReferralPartner> partners =
                partnerRepository.findAll();

        long totalPartners = partners.size();

        long activePartners =
                partners.stream()
                        .filter(p ->
                                p.getStatus()
                                        == ReferralPartnerStatus.ACTIVE
                        )
                        .count();

        long referredCustomers = 0;
        long totalConversions = 0;
        long totalRevenue = 0;
        long totalCommission = 0;
        long pendingCommission = 0;
        long paidCommission = 0;
        long reversedCommission = 0;

        for (ReferralPartner partner : partners) {

            UUID partnerId = partner.getId();

            referredCustomers +=
                    conversionRepository
                            .countReferredCustomers(partnerId);

            totalConversions +=
                    conversionRepository
                            .countByPartnerId(partnerId);

            totalRevenue += defaultLong(
                    conversionRepository.sumRevenue(partnerId)
            );

            totalCommission += defaultLong(
                    conversionRepository.sumCommission(partnerId)
            );

            pendingCommission += defaultLong(
                    conversionRepository.sumPendingCommission(
                            partnerId
                    )
            );

            paidCommission += defaultLong(
                    conversionRepository.sumPaidCommission(
                            partnerId
                    )
            );

            reversedCommission += defaultLong(conversionRepository.sumReversedCommission(
                            partnerId
                    ));
        }

        return new ReferralAdminSummaryResponse(
                totalPartners,
                activePartners,
                referredCustomers,
                totalConversions,
                totalRevenue,
                totalCommission,
                pendingCommission,
                paidCommission,
                reversedCommission
        );
    }

    private ReferralPartnerResponse toResponse(
            ReferralPartner partner
    ) {

        UUID partnerId = partner.getId();

        long customers =
                conversionRepository.countReferredCustomers(
                        partnerId
                );

        long revenue =
                defaultLong(
                        conversionRepository.sumRevenue(
                                partnerId
                        )
                );

        long commission =
                defaultLong(
                        conversionRepository.sumCommission(
                                partnerId
                        )
                );

        long pending =
                defaultLong(
                        conversionRepository.sumPendingCommission(
                                partnerId
                        )
                );

        long paid =
                defaultLong(
                        conversionRepository.sumPaidCommission(
                                partnerId
                        )
                );

        long reversed =
                defaultLong(
                        conversionRepository.sumReversedCommission(
                                partnerId
                        )
                );

        return ReferralPartnerResponse.builder()
                .id(partner.getId())
                .name(partner.getName())
                .email(partner.getEmail())
                .referralCode(partner.getReferralCode())
                .referralLink(
                        buildReferralLink(
                                partner.getReferralCode()
                        )
                )
                .commissionPercentage(
                        partner.getCommissionPercentage()
                )
                .status(partner.getStatus())
                .referredCustomers(customers)
                .revenueAmountMinor(revenue)
                .commissionAmountMinor(commission)
                .pendingCommissionAmountMinor(pending)
                .paidCommissionAmountMinor(paid)
                .reversedCommissionAmountMinor(reversed)
                .accountCreated(partner.getUser() != null)
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }

    private ReferralConversionResponse toConversionResponse(
            ReferralConversion conversion
    ) {

        return ReferralConversionResponse.builder()
                .id(conversion.getId())
                .partnerId(
                        conversion.getPartner().getId()
                )
                .workspaceId(
                        conversion.getWorkspace().getId()
                )
                .providerOrderId(
                        conversion.getProviderOrderId()
                )
                .providerSubscriptionId(
                        conversion.getProviderSubscriptionId()
                )
                .providerProductId(
                        conversion.getProviderProductId()
                )
                .product(
                        conversion.getProduct()
                )
                .revenueAmountMinor(
                        conversion.getRevenueAmountMinor()
                )
                .currency(
                        conversion.getCurrency()
                )
                .commissionPercentage(
                        conversion.getCommissionPercentage()
                )
                .commissionAmountMinor(
                        conversion.getCommissionAmountMinor()
                )
                .status(
                        conversion.getStatus()
                )
                .createdAt(
                        conversion.getCreatedAt()
                )
                .build();
    }

    private String normalizeCode(String code) {

        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String buildReferralLink(String code) {

        String base =
                frontendUrl == null
                        ? ""
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