package com.amsal.fidmap.referral;

import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "referral_conversions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_referral_conversion_order",
                        columnNames = "provider_order_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_referral_conversion_partner",
                        columnList = "partner_id"
                ),
                @Index(
                        name = "idx_referral_conversion_workspace",
                        columnList = "workspace_id"
                ),
                @Index(
                        name = "idx_referral_conversion_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "partner_id",
            nullable = false
    )
    private ReferralPartner partner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workspace_id",
            nullable = false
    )
    private Workspace workspace;

    /**
     * Polar order ID.
     *
     * One Polar order can create only one referral conversion.
     */
    @Column(
            name = "provider_order_id",
            nullable = false,
            unique = true,
            length = 255
    )
    private String providerOrderId;

    @Column(
            name = "provider_subscription_id",
            length = 255
    )
    private String providerSubscriptionId;

    @Column(
            name = "provider_product_id",
            nullable = false,
            length = 255
    )
    private String providerProductId;

    @Column(
            nullable = false,
            length = 100
    )
    private String product;

    /**
     * Amount in the smallest currency unit.
     *
     * Example:
     * $49.00 = 4900
     */
    @Column(
            name = "revenue_amount_minor",
            nullable = false
    )
    private Long revenueAmountMinor;

    @Column(
            nullable = false,
            length = 10
    )
    private String currency;

    /**
     * Commission percentage frozen at conversion time.
     *
     * Example:
     * 30.00 = 30%
     */
    @Column(
            name = "commission_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal commissionPercentage;

    /**
     * Commission amount in smallest currency unit.
     */
    @Column(
            name = "commission_amount_minor",
            nullable = false
    )
    private Long commissionAmountMinor;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private ReferralConversionStatus status =
            ReferralConversionStatus.EARNED;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void onCreate() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralPayoutStatus payoutStatus = ReferralPayoutStatus.PENDING;

    @Column
    private Instant paidAt;

    @Column(length = 255)
    private String payoutReference;

    @Column(length = 1000)
    private String payoutNote;
}