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
                        name = "idx_subscription_paddle_subscription_id",
                        columnList = "paddle_subscription_id"
                ),
                @Index(
                        name = "idx_subscription_paddle_customer_id",
                        columnList = "paddle_customer_id"
                ),
                @Index(
                        name = "idx_subscription_paddle_transaction_id",
                        columnList = "paddle_transaction_id"
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
     * Each workspace should have exactly one subscription record.
     */
    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;


    /**
     * Paddle customer ID.
     *
     * Example:
     * ctm_01...
     *
     * Nullable during the free trial.
     */
    @Column(
            name = "paddle_customer_id"
    )
    private String paddleCustomerId;


    /**
     * Paddle recurring subscription ID.
     *
     * Used by Startup and Business.
     *
     * Nullable for:
     * - Free trial
     * - Lifetime
     */
    @Column(
            name = "paddle_subscription_id",
            unique = true
    )
    private String paddleSubscriptionId;


    /**
     * Paddle transaction ID.
     *
     * Primarily used for the Lifetime one-time purchase.
     *
     * Example:
     * txn_01...
     */
    @Column(
            name = "paddle_transaction_id"
    )
    private String paddleTransactionId;


    /**
     * Paddle price ID associated with the current plan.
     *
     * Startup:
     * pri_...
     *
     * Business:
     * pri_...
     *
     * Lifetime:
     * pri_...
     */
    @Column(name = "paddle_price_id")
    private String paddlePriceId;


    /**
     * Current FIDMAP billing plan.
     *
     * STARTUP
     * BUSINESS
     * LIFETIME
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingPlan plan;


    /**
     * Current billing/access status.
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
     * Nullable during trial and Lifetime.
     */
    @Column(name = "current_period_start")
    private Instant currentPeriodStart;


    /**
     * End of the current paid billing period.
     *
     * Nullable during trial and Lifetime.
     */
    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;


    /**
     * Whether a recurring subscription has been
     * scheduled for cancellation at the end
     * of the current billing period.
     */
    @Builder.Default
    @Column(
            name = "cancel_at_period_end",
            nullable = false
    )
    private boolean cancelAtPeriodEnd = false;


    /**
     * Start of the 7-day free trial.
     *
     * No Paddle payment is required.
     */
    @Column(name = "trial_starts_at")
    private Instant trialStartsAt;


    /**
     * End of the 7-day free trial.
     *
     * No Paddle payment is required.
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