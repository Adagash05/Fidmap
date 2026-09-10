package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.payment.paddle.PaddleConfig;
import com.amsal.fidmap.payment.paddle.PaddleService;
import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import com.amsal.fidmap.payment.subscription.SubscriptionResponse;
import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final int TRIAL_DAYS = 7;

    private final SubscriptionRepository subscriptionRepository;
    private final PaddleConfig paddleConfig;
    private final PaddleService paddleService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final PlanEntitlementService planEntitlementService;

    /**
     * Creates the initial 7-day free trial for a workspace.
     * <p>
     * The trial is completely local.
     * <p>
     * No Paddle customer.
     * No Paddle subscription.
     * No payment method.
     * No checkout.
     */
    @Transactional
    public Subscription startTrial(UUID workspaceId) {

        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        if (subscriptionRepository.findByWorkspaceId(workspaceId).isPresent()) {

            throw new IllegalStateException("Workspace already has a billing subscription");
        }

        Instant now = Instant.now();

        Instant trialEndsAt = now.plus(TRIAL_DAYS, ChronoUnit.DAYS);

        Subscription subscription = Subscription.builder()
                .workspaceId(workspaceId)
                .plan(BillingPlan.STARTUP_MONTHLY)
                .status(SubscriptionStatus.TRIALING)
                .trialStartsAt(now)
                .trialEndsAt(trialEndsAt)
                .cancelAtPeriodEnd(false)
                .build();

        return subscriptionRepository.save(
                subscription
        );
    }

    /**
     * Creates the information required by the frontend
     * to open Paddle Checkout.
     * <p>
     * Paddle Checkout itself is opened by the frontend
     * using Paddle.js.
     */
    public CheckoutResponse createCheckout(
            UUID workspaceId,
            BillingPlan plan
    ) {

        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        if (plan == null) {
            throw new IllegalArgumentException("Billing plan is required");
        }

        workspaceAuthorizationService.requireBillingAccess(workspaceId);

        /*
         * A workspace should never purchase a "trial"
         * through Paddle.
         *
         * Trial is represented locally as:
         *
         * STARTUP + TRIALING
         */
        String priceId = getPriceId(plan);

        if (priceId == null || priceId.isBlank()) {

            throw new IllegalStateException(
                    "Paddle price ID is not configured for plan: " + plan);
        }

        String environment = paddleConfig
                        .getApiUrl()
                        .contains("sandbox")
                        ? "sandbox"
                        : "production";

        return new CheckoutResponse(
                workspaceId,
                plan,
                priceId,
                paddleConfig.getClientToken(),
                environment
        );
    }

    /**
     * Returns the configured Paddle price ID
     * for the selected billing plan.
     */
    private String getPriceId(BillingPlan plan) {

        return switch (plan) {

            case STARTUP_MONTHLY -> paddleConfig
                    .getPrices()
                    .getStartupMonthly();

            case STARTUP_YEARLY -> paddleConfig
                    .getPrices()
                    .getStartupYearly();

            case BUSINESS_MONTHLY -> paddleConfig
                    .getPrices()
                    .getBusinessMonthly();

            case BUSINESS_YEARLY -> paddleConfig
                    .getPrices()
                    .getBusinessYearly();

            case LIFETIME -> paddleConfig
                    .getPrices()
                    .getLifetime();
        };
    }

    /**
     * Returns the current billing state of a workspace.
     */
    @Transactional
    public SubscriptionResponse getSubscription(UUID workspaceId) {

        workspaceAuthorizationService.requireBillingAccess(workspaceId);

        Subscription subscription = subscriptionRepository.findByWorkspaceId(workspaceId)
                        .orElseThrow(() -> new IllegalStateException("Billing subscription not found"));

        /*
         * Automatically expire an ended trial.
         */
        if (subscription.getStatus() == SubscriptionStatus.TRIALING
                && subscription.getTrialEndsAt() != null
                && !Instant.now().isBefore(subscription.getTrialEndsAt())) {

            subscription.setStatus(SubscriptionStatus.EXPIRED);
        }

        return toResponse(subscription);
    }

    /**
     * Determines whether the workspace currently has
     * access to the application.
     */
    @Transactional
    public boolean hasAccess(UUID workspaceId) {

        Subscription subscription = subscriptionRepository.findByWorkspaceId(workspaceId)
                        .orElse(null);

        if (subscription == null) {
            return false;
        }

        /*
         * Active paid subscription or Lifetime.
         */
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {

            return true;
        }

        /*
         * Active 7-day trial.
         */
        if (subscription.getStatus() == SubscriptionStatus.TRIALING) {

            Instant trialEndsAt = subscription.getTrialEndsAt();

            if (trialEndsAt == null) {
                return false;
            }

            if (Instant.now().isBefore(trialEndsAt)) {
                return true;
            }

            subscription.setStatus(SubscriptionStatus.EXPIRED);

            return false;
        }

        /*
         * PAST_DUE, PAUSED, CANCELED and EXPIRED
         * currently have no access.
         *
         * This policy can be changed later.
         */
        return false;
    }

    /**
     * Returns the number of whole calendar days remaining
     * in the trial, rounded upward.
     */
    @Transactional
    public long getTrialDaysRemaining(UUID workspaceId) {

        workspaceAuthorizationService.requireBillingAccess(workspaceId);

        Subscription subscription = subscriptionRepository.findByWorkspaceId(workspaceId)
                        .orElseThrow(() -> new IllegalStateException("Billing subscription not found"));



        if (subscription.getStatus() != SubscriptionStatus.TRIALING || subscription.getTrialEndsAt() == null) {

            return 0;
        }

        Instant now = Instant.now();

        if (!now.isBefore(subscription.getTrialEndsAt())) {

            subscription.setStatus(SubscriptionStatus.EXPIRED);

            return 0;
        }

        long hoursRemaining = ChronoUnit.HOURS.between(now, subscription.getTrialEndsAt());

        /*
         * Round upward:
         *
         * 6 days 3 hours → 7
         * 2 days 1 hour  → 3
         * 10 hours       → 1
         */
        return (hoursRemaining + 23) / 24;
    }

    /**
     * Schedules cancellation of a recurring Paddle
     * subscription at the end of its current period.
     * <p>
     * Lifetime purchases cannot be canceled because
     * they are one-time purchases.
     */
    @Transactional
    public void cancelSubscription(UUID workspaceId) {

        workspaceAuthorizationService.requireBillingAccess(workspaceId);

        Subscription subscription = subscriptionRepository.findByWorkspaceId(workspaceId)
                        .orElseThrow(() -> new IllegalStateException("No subscription found"));


        if (subscription.getPlan() == BillingPlan.LIFETIME) {

            throw new IllegalStateException("Lifetime plans cannot be canceled");
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {

            throw new IllegalStateException("Only an active subscription can be canceled");
        }

        String paddleSubscriptionId = subscription.getPaddleSubscriptionId();

        if (paddleSubscriptionId == null || paddleSubscriptionId.isBlank()) {

            throw new IllegalStateException("Subscription has no Paddle subscription ID");
        }

        paddleService.cancelSubscription(paddleSubscriptionId);

        /*
         * This does NOT mean the subscription is canceled yet.
         *
         * Paddle will send the appropriate webhook.
         *
         * Until then, the user retains access.
         */
        subscription.setCancelAtPeriodEnd(true);
    }

    /**
     * Changes the plan of an existing active Paddle subscription.
     *
     * Paddle performs the actual billing change.
     * The Paddle webhook remains the source of truth and
     * updates the local Subscription entity.
     */
    @Transactional
    public void changePlan(
            UUID workspaceId,
            BillingPlan newPlan
    ) {

        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        if (newPlan == null) {
            throw new IllegalArgumentException("New billing plan is required");
        }

        workspaceAuthorizationService.requireBillingAccess(workspaceId);

        Subscription subscription = subscriptionRepository
                .findByWorkspaceId(workspaceId)
                .orElseThrow(() ->
                        new IllegalStateException("Billing subscription not found"));

        /*
         * Plan changes are only supported for active
         * Paddle recurring subscriptions.
         */
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active subscription can change plans");
        }

        /*
         * Lifetime is a one-time purchase.
         */
        if (subscription.getPlan() == BillingPlan.LIFETIME) {

            throw new IllegalStateException(
                    "Lifetime plans cannot be changed");
        }

        /*
         * Lifetime should be purchased separately.
         */
        if (newPlan == BillingPlan.LIFETIME) {

            throw new IllegalStateException(
                    "Lifetime cannot be selected through plan change");
        }

        /*
         * Prevent unnecessary Paddle API calls.
         */
        if (subscription.getPlan() == newPlan) {

            throw new IllegalStateException(
                    "Workspace is already subscribed to this plan");
        }

        String paddleSubscriptionId =
                subscription.getPaddleSubscriptionId();

        if (paddleSubscriptionId == null
                || paddleSubscriptionId.isBlank()) {

            throw new IllegalStateException(
                    "Subscription has no Paddle subscription ID");
        }

        String newPriceId = getPriceId(newPlan);

        if (newPriceId == null || newPriceId.isBlank()) {

            throw new IllegalStateException(
                    "Paddle price ID is not configured for plan: "
                            + newPlan);
        }

        /*
         * Paddle performs the actual plan change.
         *
         * Do NOT update subscription.plan here.
         *
         * subscription.updated webhook will update
         * the local database after Paddle confirms it.
         */
        paddleService.changeSubscriptionPlan(
                paddleSubscriptionId,
                newPriceId
        );
    }

    /**
     * Converts the database entity into an API response.
     */
    private SubscriptionResponse toResponse(
            Subscription subscription
    ) {

        BillingPlan plan = subscription.getPlan();

        return new SubscriptionResponse(
                subscription.getWorkspaceId(),
                plan,
                plan.getDisplayName(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isCancelAtPeriodEnd(),
                subscription.getTrialEndsAt()
        );
    }

    /**
     * Returns the effective feature and usage limits
     * for the workspace's current billing plan.
     *
     * Any authenticated staff member belonging to the
     * workspace can read this information.
     */
    @Transactional(readOnly = true)
    public PlanEntitlementResponse getEntitlements(UUID workspaceId) {

        workspaceAuthorizationService.requireWorkspaceAccess(workspaceId);

        Plan plan = planEntitlementService.getPlan(workspaceId);

        return PlanEntitlementResponse.builder()
                .billingPlan(plan.getBillingPlan())
                .planName(plan.getBillingPlan().getDisplayName())
                .billingInterval(plan.getBillingPlan().getBillingInterval())
                .maxTeamMembers(plan.getMaxTeamMembers())
                .maxBoards(plan.getMaxBoards())
                .maxFeedbackPosts(plan.getMaxFeedbackPosts())
                .maxEndUsers(plan.getMaxEndUsers())
                .maxRoadmapItems(plan.getMaxRoadmapItems())
                .maxChangelogEntries(plan.getMaxChangelogEntries())
                .customBranding(plan.isCustomBranding())
                .removeFidmapBranding(plan.isRemoveFidmapBranding())
                .privateBoards(plan.isPrivateBoards())
                .build();
    }
}
