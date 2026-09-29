package com.amsal.fidmap.referral;

import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralPartnerRepository partnerRepository;
    private final ReferralConversionRepository conversionRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * Attributes a workspace to a referral partner.
     *
     * First-touch attribution:
     * once a workspace has a partner, it is never overwritten.
     */
    @Transactional
    public void attributeWorkspace(
            UUID workspaceId,
            String referralCode
    ) {

        if (workspaceId == null
                || referralCode == null
                || referralCode.isBlank()) {

            return;
        }

        Workspace workspace =
                workspaceRepository
                        .findWorkspaceById(workspaceId);

        if (workspace == null) {
            return;
        }

        /*
         * First-touch attribution.
         *
         * Never overwrite an existing partner.
         */
        if (workspace.getReferralPartner() != null) {
            return;
        }

        String normalizedCode =
                referralCode
                        .trim()
                        .toUpperCase(Locale.ROOT);

        ReferralPartner partner =
                partnerRepository
                        .findByReferralCodeIgnoreCase(
                                normalizedCode
                        )
                        .orElse(null);

        /*
         * Invalid code should not break signup.
         */
        if (partner == null) {
            return;
        }

        /*
         * Inactive partners cannot receive
         * new customers.
         */
        if (partner.getStatus()
                != ReferralPartnerStatus.ACTIVE) {

            return;
        }

        workspace.setReferralPartner(partner);

        workspaceRepository.save(workspace);
    }

    /**
     * Records a verified paid Polar order.
     *
     * This method is idempotent.
     */
    @Transactional
    public void recordPaidOrder(
            UUID workspaceId,
            String providerOrderId,
            String providerSubscriptionId,
            String providerProductId,
            String product,
            long revenueAmountMinor,
            String currency
    ) {

        if (workspaceId == null
                || providerOrderId == null
                || providerOrderId.isBlank()) {

            return;
        }

        /*
         * Never create duplicate commissions.
         */
        if (conversionRepository
                .existsByProviderOrderId(
                        providerOrderId
                )) {

            return;
        }

        Workspace workspace =
                workspaceRepository
                        .findWorkspaceById(workspaceId);

        if (workspace == null) {
            return;
        }

        ReferralPartner partner =
                workspace.getReferralPartner();

        /*
         * Customer was not referred.
         */
        if (partner == null) {
            return;
        }

        /*
         * Do not generate new commissions for
         * inactive partners on future orders.
         *
         * Existing conversions remain untouched.
         */
        if (partner.getStatus()
                != ReferralPartnerStatus.ACTIVE) {

            return;
        }

        if (revenueAmountMinor <= 0) {
            return;
        }

        BigDecimal percentage =
                partner.getCommissionPercentage();

        if (percentage == null
                || percentage.signum() <= 0) {

            return;
        }

        /*
         * Example:
         *
         * revenue = 4900
         * commission = 30%
         *
         * 4900 × 30 / 100 = 1470
         */
        long commissionAmountMinor =
                BigDecimal.valueOf(
                                revenueAmountMinor
                        )
                        .multiply(percentage)
                        .divide(
                                BigDecimal.valueOf(100),
                                0,
                                RoundingMode.HALF_UP
                        )
                        .longValueExact();

        ReferralConversion conversion =
                ReferralConversion.builder()
                        .partner(partner)
                        .workspace(workspace)
                        .providerOrderId(
                                providerOrderId
                        )
                        .providerSubscriptionId(
                                providerSubscriptionId
                        )
                        .providerProductId(
                                providerProductId
                        )
                        .product(product)
                        .revenueAmountMinor(
                                revenueAmountMinor
                        )
                        .currency(
                                currency == null
                                        ? "USD"
                                        : currency
                                        .toUpperCase(
                                                Locale.ROOT
                                        )
                        )
                        .commissionPercentage(
                                percentage
                        )
                        .commissionAmountMinor(
                                commissionAmountMinor
                        )
                        .status(
                                ReferralConversionStatus.EARNED
                        )
                        .build();

        conversionRepository.save(conversion);
    }
}