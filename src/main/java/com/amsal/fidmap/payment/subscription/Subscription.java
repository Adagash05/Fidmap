package com.amsal.fidmap.payment.subscription;

import com.amsal.fidmap.payment.billing.BillingPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscription_workspace",
                        columnNames = "workspace_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_subscription_workspace",
                        columnList = "workspace_id"
                ),
                @Index(
                        name = "idx_subscription_provider_subscription_id",
                        columnList = "provider_subscription_id"
                ),
                @Index(
                        name = "idx_subscription_provider_customer_id",
                        columnList = "provider_customer_id"
                ),
                @Index(
                        name = "idx_subscription_provider_transaction_id",
                        columnList = "provider_transaction_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * FIDMAP workspace that owns this billing record.
     *
     * Each workspace has exactly one local billing record.
     */
    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    /**
     * Billing provider customer ID.
     *
     * Provider-neutral so the FIDMAP domain is not coupled
     * to Polar or any future billing provider.
     */
    @Column(name = "provider_customer_id")
    private String providerCustomerId;

    /**
     * Billing provider subscription ID.
     *
     * Null for local trials and Lifetime purchases.
     */
    @Column(
            name = "provider_subscription_id",
            unique = true
    )
    private String providerSubscriptionId;

    /**
     * Billing provider transaction/order ID.
     *
     * Used to identify the payment associated with the
     * billing record, particularly for one-time purchases.
     */
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    /**
     * Billing provider product ID.
     *
     * Used to map a Polar product back to a FIDMAP billing plan.
     */
    @Column(name = "provider_product_id")
    private String providerProductId;

    /**
     * Current FIDMAP billing plan.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingPlan plan;

    /**
     * Current FIDMAP billing/access status.
     *
     * TRIALING
     * ACTIVE
     * PAST_DUE
     * PAUSED
     * CANCELED
     * EXPIRED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /**
     * Start of the current paid billing period.
     *
     * Nullable during the local trial and Lifetime.
     */
    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    /**
     * End of the current paid billing period.
     *
     * Nullable during the local trial and Lifetime.
     */
    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    /**
     * Whether a recurring subscription is scheduled
     * for cancellation at the end of the current period.
     */
    @Builder.Default
    @Column(
            name = "cancel_at_period_end",
            nullable = false
    )
    private boolean cancelAtPeriodEnd = false;

    /**
     * Start of the FIDMAP 7-day local trial.
     *
     * No Polar payment is required.
     */
    @Column(name = "trial_starts_at")
    private Instant trialStartsAt;

    /**
     * End of the FIDMAP 7-day local trial.
     *
     * No Polar payment is required.
     */
    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    /**
     * Record creation timestamp.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    /**
     * Last modification timestamp.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

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