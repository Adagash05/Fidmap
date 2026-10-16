package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.exception.FeatureNotAvailableException;
import com.amsal.fidmap.exception.PlanLimitExceededException;
import com.amsal.fidmap.exception.SubscriptionRequiredException;
import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanEntitlementService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    private final Clock clock = Clock.systemUTC();

    /**
     * Result of evaluating whether a workspace may currently use
     * paid/write functionality. Pure function of the subscription record
     * and the current time so it is reproducible and unit-testable.
     */
    public record AccessState(
            boolean active,
            SubscriptionStatus effectiveStatus,
            String reasonCode,
            Instant accessEndsAt
    ) {
    }

    /**
     * Server-side source of truth for "is this workspace entitled right now".
     *
     * Rules (no grace periods are invented):
     * - no billing record                        -> denied (NO_SUBSCRIPTION)
     * - ACTIVE + LIFETIME                        -> allowed
     * - ACTIVE recurring, now < currentPeriodEnd -> allowed (also covers
     *   cancel-at-period-end, which stays ACTIVE until the period ends)
     * - ACTIVE recurring, now >= currentPeriodEnd-> denied (PERIOD_ENDED)
     * - TRIALING, now < trialEndsAt              -> allowed
     * - TRIALING, now >= trialEndsAt / no end    -> denied (TRIAL_EXPIRED)
     * - PAST_DUE, PAUSED, CANCELED, EXPIRED      -> denied
     */
    public static AccessState evaluate(Subscription subscription, Instant now) {

        if (subscription == null || subscription.getStatus() == null) {
            return new AccessState(false, SubscriptionStatus.EXPIRED, "NO_SUBSCRIPTION", null);
        }

        SubscriptionStatus status = subscription.getStatus();

        switch (status) {

            case ACTIVE -> {

                if (subscription.getPlan() == BillingPlan.LIFETIME) {
                    return new AccessState(true, status, "LIFETIME", null);
                }

                Instant end = subscription.getCurrentPeriodEnd();

                if (end != null && !now.isBefore(end)) {
                    return new AccessState(false, SubscriptionStatus.EXPIRED, "PERIOD_ENDED", end);
                }

                return new AccessState(true, status, "ACTIVE", end);
            }

            case TRIALING -> {

                Instant end = subscription.getTrialEndsAt();

                if (end == null || !now.isBefore(end)) {
                    return new AccessState(false, SubscriptionStatus.EXPIRED, "TRIAL_EXPIRED", end);
                }

                return new AccessState(true, status, "TRIAL", end);
            }

            case PAST_DUE -> {
                return new AccessState(false, status, "PAYMENT_PAST_DUE", null);
            }

            case PAUSED -> {
                return new AccessState(false, status, "SUBSCRIPTION_PAUSED", null);
            }

            case CANCELED -> {
                return new AccessState(false, status, "SUBSCRIPTION_CANCELED", null);
            }

            default -> {
                return new AccessState(false, SubscriptionStatus.EXPIRED, "SUBSCRIPTION_EXPIRED", null);
            }
        }
    }

    @Transactional(readOnly = true)
    public AccessState getAccessState(UUID workspaceId) {

        Subscription subscription = subscriptionRepository
                .findByWorkspaceId(workspaceId)
                .orElse(null);

        return evaluate(subscription, Instant.now(clock));
    }

    public boolean hasActiveAccess(UUID workspaceId) {
        return getAccessState(workspaceId).active();
    }

    /**
     * Gate for every protected write operation. Fails closed.
     */
    public void requireActiveAccess(UUID workspaceId) {

        if (workspaceId == null) {
            throw new SubscriptionRequiredException(
                    "NO_WORKSPACE",
                    "Workspace is required."
            );
        }

        AccessState state = getAccessState(workspaceId);

        if (!state.active()) {
            throw new SubscriptionRequiredException(
                    state.reasonCode(),
                    messageFor(state.reasonCode())
            );
        }
    }

    private static String messageFor(String reasonCode) {

        return switch (reasonCode) {
            case "TRIAL_EXPIRED" ->
                    "Your free trial has ended. Choose a plan to continue.";
            case "PERIOD_ENDED", "SUBSCRIPTION_EXPIRED" ->
                    "Your subscription has expired. Renew to continue.";
            case "SUBSCRIPTION_CANCELED" ->
                    "Your subscription was cancelled. Choose a plan to continue.";
            case "PAYMENT_PAST_DUE" ->
                    "Your last payment failed. Update your payment method to continue.";
            case "SUBSCRIPTION_PAUSED" ->
                    "Your subscription is paused.";
            default ->
                    "This workspace does not have an active plan.";
        };
    }

    /**
     * Returns the effective plan configuration
     * for the workspace.
     */
    public Plan getPlan(UUID workspaceId) {

        Subscription subscription = subscriptionRepository
                .findByWorkspaceId(workspaceId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Billing subscription not found"
                        )
                );

        return planRepository
                .findByBillingPlan(subscription.getPlan())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Plan configuration not found for: "
                                        + subscription.getPlan()
                        )
                );
    }

    public void checkBoardLimit(
            UUID workspaceId,
            long currentBoardCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxBoards();

        if (limit != null && currentBoardCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " boards."
            );
        }
    }

    public void checkTeamMemberLimit(
            UUID workspaceId,
            long currentMemberCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxTeamMembers();

        if (limit != null && currentMemberCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " team members."
            );
        }
    }

    public void checkFeedbackPostLimit(
            UUID workspaceId,
            long currentFeedbackCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxFeedbackPosts();

        if (limit != null && currentFeedbackCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " feedback posts."
            );
        }
    }

    public void checkRoadmapItemLimit(
            UUID workspaceId,
            long currentRoadmapItemCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxRoadmapItems();

        if (limit != null && currentRoadmapItemCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " roadmap items."
            );
        }
    }

    public void checkEndUserLimit(
            UUID workspaceId,
            long currentEndUserCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxEndUsers();

        if (limit != null && currentEndUserCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " end users."
            );
        }
    }

    public void checkChangelogEntryLimit(
            UUID workspaceId,
            long currentChangelogEntryCount
    ) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        Integer limit = plan.getMaxChangelogEntries();

        if (limit != null && currentChangelogEntryCount >= limit) {

            throw new PlanLimitExceededException(
                    "Your current plan allows a maximum of "
                            + limit
                            + " changelog entries."
            );
        }
    }

    /**
     * Requires the current plan to support private boards.
     */
    public void requirePrivateBoards(UUID workspaceId) {
        requireActiveAccess(workspaceId);


        Plan plan = getPlan(workspaceId);

        if (!plan.isPrivateBoards()) {

            throw new FeatureNotAvailableException(
                    "Private boards are not available on your current plan."
            );
        }
    }
}