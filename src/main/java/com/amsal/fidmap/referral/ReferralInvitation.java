package com.amsal.fidmap.referral;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "referral_invitations",
        indexes = {
                @Index(name = "idx_referral_invitation_token", columnList = "token"),
                @Index(name = "idx_referral_invitation_email", columnList = "email")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referral_partner_id", nullable = false)
    private ReferralPartner referralPartner;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean accepted;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant acceptedAt;
}