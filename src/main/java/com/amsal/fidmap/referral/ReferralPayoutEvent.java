package com.amsal.fidmap.referral;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referral_payout_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralPayoutEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversion_id", nullable = false)
    private ReferralConversion conversion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralPayoutStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralPayoutStatus newStatus;

    @Column(length = 255)
    private String payoutReference;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    /*
     * Store the admin user ID if your current authentication
     * architecture makes this straightforward.
     */
    private UUID changedByUserId;
}