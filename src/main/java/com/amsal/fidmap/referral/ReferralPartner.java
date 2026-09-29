package com.amsal.fidmap.referral;

import com.amsal.fidmap.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "referral_partners",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_referral_partner_code",
                        columnNames = "referral_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_referral_partner_code",
                        columnList = "referral_code"
                ),
                @Index(
                        name = "idx_referral_partner_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            length = 120
    )
    private String name;

    @Column(length = 255)
    private String email;

    @Column(
            name = "referral_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String referralCode;

    /**
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

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private ReferralPartnerStatus status =
            ReferralPartnerStatus.ACTIVE;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @PrePersist
    void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {

        updatedAt = Instant.now();
    }
}