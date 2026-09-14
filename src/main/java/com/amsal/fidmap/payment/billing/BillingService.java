package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.config.FidmapConfig;
import com.amsal.fidmap.payment.polar.PolarConfig;
import com.amsal.fidmap.payment.polar.PolarService;
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
    private final PolarConfig polarConfig;
    private final PolarService polarService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final PlanEntitlementService planEntitlementService;
    private final FidmapConfig fidmapConfig;

    /**
     * Creates the initial 7-day FIDMAP trial.
     *
     * The trial is completely local.
     *
     * No Polar customer.
     * No Polar subscription.
     * No payment method.
     * No Polar checkout.
     */
    @Transactional
    public Subscription startTrial(UUID workspaceId) {

        requireWorkspaceId(workspaceId);

        if (subscriptionRepository.findByWorkspaceId(workspaceId).isPresent()) {

            throw new IllegalStateException(
                    "Workspace already has a billing subscription"
            );
        }

        Instant now = Instant.now();

        Instant trialEndsAt =
                now.plus(
                        TRIAL_DAYS,
                        ChronoUnit.DAYS
                );

        Subscription subscription =
                Subscription.builder()
                        .workspaceId(workspaceId)
                        .plan(BillingPlan.STARTUP_MONTHLY)
                        .status(SubscriptionStatus.TRIALING)
                        .trialStartsAt(now)
                        .trialEndsAt(trialEndsAt)
                        .cancelAtPeriodEnd(false)
                        .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Creates a Polar hosted checkout session.
     *
     * The local subscription is NOT activated here.
     *
     * Polar webhook events are the source of truth for
     * successful payment and subscription activation.
     */
    public CheckoutResponse createCheckout(
            UUID workspaceId,
            BillingPlan plan
    ) {

        requireWorkspaceId(workspaceId);

        if (plan == null) {

            throw new IllegalArgumentException(
                    "Billing plan is required"
            );
        }

        workspaceAuthorizationService.requireBillingAccess(
                workspaceId
        );

        /*
         * The FIDMAP free trial is handled locally.
         *
         * If the frontend calls this endpoint, it creates
         * a paid Polar checkout for the selected product.
         */
        String productId =
                getProductId(plan);

        if (productId == null || productId.isBlank()) {

            throw new IllegalStateException(
                    "Polar product ID is not configured for plan: "
                            + plan
            );
        }

        String frontendUrl =
                fidmapConfig.getFrontendUrl();

        if (frontendUrl == null || frontendUrl.isBlank()) {

            throw new IllegalStateException(
                    "FIDMAP frontend URL is not configured"
            );
        }

        String successUrl =
                frontendUrl + "/billing/success";

        String returnUrl =
                frontendUrl + "/settings/billing";

        PolarService.PolarCheckoutResult checkout =
                polarService.createCheckout(
                        workspaceId,
                        productId,
                        successUrl,
                        returnUrl,
                        frontendUrl
                );

        return new CheckoutResponse(
                workspaceId,
                plan,
                checkout.checkoutId().toString(),
                checkout.checkoutUrl()
        );
    }

    /**
     * Resolves a FIDMAP billing plan to its Polar product ID.
     */
    private String getProductId(BillingPlan plan) {

        return switch (plan) {

            case STARTUP_MONTHLY ->
                    polarConfig
                            .getProducts()
                            .getStartupMonthly();

            case STARTUP_YEARLY ->
                    polarConfig
                            .getProducts()
                            .getStartupYearly();

            case BUSINESS_MONTHLY ->
                    polarConfig
                            .getProducts()
                            .getBusinessMonthly();

            case BUSINESS_YEARLY ->
                    polarConfig
                            .getProducts()
                            .getBusinessYearly();

            case LIFETIME ->
                    polarConfig
                            .getProducts()
                            .getLifetime();
        };
    }

    /**
     * Returns the current billing state of a workspace.
     */
    @Transactional
    public SubscriptionResponse getSubscription(
            UUID workspaceId
    ) {

        workspaceAuthorizationService.requireBillingAccess(
                workspaceId
        );

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Billing subscription not found"
                                )
                        );

        /*
         * Automatically expire an ended local trial.
         */
        if (subscription.getStatus()
                == SubscriptionStatus.TRIALING
                && subscription.getTrialEndsAt() != null
                && !Instant.now().isBefore(
                subscription.getTrialEndsAt()
        )) {

            subscription.setStatus(
                    SubscriptionStatus.EXPIRED
            );
        }

        return toResponse(subscription);
    }

    /**
     * Determines whether the workspace currently has
     * access to the application.
     */
    @Transactional
    public boolean hasAccess(UUID workspaceId) {

        requireWorkspaceId(workspaceId);

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElse(null);

        if (subscription == null) {
            return false;
        }

        /*
         * ACTIVE covers:
         *
         * - active recurring Polar subscriptions
         * - active Lifetime purchases
         */
        if (subscription.getStatus()
                == SubscriptionStatus.ACTIVE) {

            return true;
        }

        /*
         * Local 7-day trial.
         */
        if (subscription.getStatus()
                == SubscriptionStatus.TRIALING) {

            Instant trialEndsAt =
                    subscription.getTrialEndsAt();

            if (trialEndsAt == null) {
                return false;
            }

            if (Instant.now().isBefore(trialEndsAt)) {
                return true;
            }

            subscription.setStatus(
                    SubscriptionStatus.EXPIRED
            );

            return false;
        }

        /*
         * PAST_DUE, PAUSED, CANCELED and EXPIRED
         * currently have no access.
         */
        return false;
    }

    /**
     * Returns the number of days remaining in the
     * local 7-day trial, rounded upward.
     */
    @Transactional
    public long getTrialDaysRemaining(
            UUID workspaceId
    ) {

        workspaceAuthorizationService.requireBillingAccess(
                workspaceId
        );

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Billing subscription not found"
                                )
                        );

        if (subscription.getStatus()
                != SubscriptionStatus.TRIALING
                || subscription.getTrialEndsAt() == null) {

            return 0;
        }

        Instant now = Instant.now();

        if (!now.isBefore(
                subscription.getTrialEndsAt()
        )) {

            subscription.setStatus(
                    SubscriptionStatus.EXPIRED
            );

            return 0;
        }

        long hoursRemaining =
                ChronoUnit.HOURS.between(
                        now,
                        subscription.getTrialEndsAt()
                );

        return (hoursRemaining + 23) / 24;
    }

    /**
     * Schedules cancellation of a recurring Polar
     * subscription at the end of its current billing period.
     *
     * Polar remains the source of truth.
     *
     * The resulting subscription state is confirmed
     * through Polar webhook events.
     */
    @Transactional
    public void cancelSubscription(
            UUID workspaceId
    ) {

        workspaceAuthorizationService.requireBillingAccess(
                workspaceId
        );

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No subscription found"
                                )
                        );

        if (subscription.getPlan()
                == BillingPlan.LIFETIME) {

            throw new IllegalStateException(
                    "Lifetime plans cannot be canceled"
            );
        }

        if (subscription.getStatus()
                != SubscriptionStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active subscription can be canceled"
            );
        }

        String providerSubscriptionId =
                subscription.getProviderSubscriptionId();

        if (providerSubscriptionId == null
                || providerSubscriptionId.isBlank()) {

            throw new IllegalStateException(
                    "Subscription has no provider subscription ID"
            );
        }

        /*
         * Ask Polar to schedule the cancellation.
         */
        polarService.cancelSubscription(
                providerSubscriptionId
        );

        /*
         * Optimistically update only the cancellation flag.
         *
         * The subscription itself remains ACTIVE until
         * Polar confirms the resulting state.
         */
        subscription.setCancelAtPeriodEnd(true);

        subscriptionRepository.save(subscription);
    }

    /**
     * Changes the plan of an existing active recurring
     * Polar subscription.
     *
     * Polar performs the actual billing operation.
     *
     * The local Subscription entity is updated only after
     * the corresponding Polar webhook is received.
     */
    @Transactional
    public void changePlan(
            UUID workspaceId,
            BillingPlan newPlan
    ) {

        requireWorkspaceId(workspaceId);

        if (newPlan == null) {

            throw new IllegalArgumentException(
                    "New billing plan is required"
            );
        }

        workspaceAuthorizationService.requireBillingAccess(
                workspaceId
        );

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Billing subscription not found"
                                )
                        );

        /*
         * Plan changes are supported only for active
         * recurring subscriptions.
         */
        if (subscription.getStatus()
                != SubscriptionStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Only an active subscription can change plans"
            );
        }

        /*
         * Lifetime is a one-time purchase.
         */
        if (subscription.getPlan()
                == BillingPlan.LIFETIME) {

            throw new IllegalStateException(
                    "Lifetime plans cannot be changed"
            );
        }

        /*
         * Lifetime must be purchased through checkout.
         */
        if (newPlan == BillingPlan.LIFETIME) {

            throw new IllegalStateException(
                    "Lifetime cannot be selected through plan change"
            );
        }

        /*
         * Prevent unnecessary Polar API calls.
         */
        if (subscription.getPlan() == newPlan) {

            throw new IllegalStateException(
                    "Workspace is already subscribed to this plan"
            );
        }

        /*
         * Do not silently change a subscription that is
         * already scheduled for cancellation.
         */
        if (subscription.isCancelAtPeriodEnd()) {

            throw new IllegalStateException(
                    "Subscription is scheduled for cancellation"
            );
        }

        String providerSubscriptionId =
                subscription.getProviderSubscriptionId();

        if (providerSubscriptionId == null
                || providerSubscriptionId.isBlank()) {

            throw new IllegalStateException(
                    "Subscription has no provider subscription ID"
            );
        }

        String newProductId =
                getProductId(newPlan);

        if (newProductId == null
                || newProductId.isBlank()) {

            throw new IllegalStateException(
                    "Polar product ID is not configured for plan: "
                            + newPlan
            );
        }

        /*
         * Polar performs the actual plan change.
         *
         * Do NOT change subscription.plan here.
         *
         * The Polar webhook is the source of truth.
         */
        polarService.changeSubscriptionPlan(
                providerSubscriptionId,
                newProductId
        );
    }

    /**
     * Converts the database entity into an API response.
     */
    private SubscriptionResponse toResponse(
            Subscription subscription
    ) {

        BillingPlan plan =
                subscription.getPlan();

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
     * Returns the effective feature limits and capabilities
     * for the workspace's current billing plan.
     */
    @Transactional(readOnly = true)
    public PlanEntitlementResponse getEntitlements(
            UUID workspaceId
    ) {

        workspaceAuthorizationService.requireWorkspaceAccess(
                workspaceId
        );

        Plan plan =
                planEntitlementService.getPlan(
                        workspaceId
                );

        return PlanEntitlementResponse.builder()
                .billingPlan(
                        plan.getBillingPlan()
                )
                .planName(
                        plan.getBillingPlan()
                                .getDisplayName()
                )
                .billingInterval(
                        plan.getBillingPlan()
                                .getBillingInterval()
                )
                .maxTeamMembers(
                        plan.getMaxTeamMembers()
                )
                .maxBoards(
                        plan.getMaxBoards()
                )
                .maxFeedbackPosts(
                        plan.getMaxFeedbackPosts()
                )
                .maxEndUsers(
                        plan.getMaxEndUsers()
                )
                .maxRoadmapItems(
                        plan.getMaxRoadmapItems()
                )
                .maxChangelogEntries(
                        plan.getMaxChangelogEntries()
                )
                .customBranding(
                        plan.isCustomBranding()
                )
                .removeFidmapBranding(
                        plan.isRemoveFidmapBranding()
                )
                .privateBoards(
                        plan.isPrivateBoards()
                )
                .build();
    }

    private void requireWorkspaceId(
            UUID workspaceId
    ) {

        if (workspaceId == null) {

            throw new IllegalArgumentException(
                    "Workspace ID is required"
            );
        }
    }
}
