package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.exception.FeatureNotAvailableException;
import com.amsal.fidmap.exception.PlanLimitExceededException;
import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanEntitlementService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

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

        Plan plan = getPlan(workspaceId);

        if (!plan.isPrivateBoards()) {

            throw new FeatureNotAvailableException(
                    "Private boards are not available on your current plan."
            );
        }
    }
}