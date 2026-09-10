package com.amsal.fidmap.payment.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanDataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {

        createIfMissing(
                BillingPlan.STARTUP_MONTHLY,
                3,
                10,
                1_000,
                5_000,
                100,
                100,
                false,
                false
        );

        createIfMissing(
                BillingPlan.STARTUP_YEARLY,
                3,
                10,
                1_000,
                5_000,
                100,
                100,
                false,
                false
        );

        createIfMissing(
                BillingPlan.BUSINESS_MONTHLY,
                10,
                null,
                10_000,
                25_000,
                null,
                null,
                true,
                true
        );

        createIfMissing(
                BillingPlan.BUSINESS_YEARLY,
                10,
                null,
                10_000,
                25_000,
                null,
                null,
                true,
                true
        );

        createIfMissing(
                BillingPlan.LIFETIME,
                10,
                null,
                10_000,
                25_000,
                null,
                null,
                true,
                true
        );
    }

    private void createIfMissing(
            BillingPlan billingPlan,
            Integer maxTeamMembers,
            Integer maxBoards,
            Integer maxFeedbackPosts,
            Integer maxEndUsers,
            Integer maxRoadmapItems,
            Integer maxChangelogEntries,
            boolean removeFidmapBranding,
            boolean privateBoards
    ) {

        if (planRepository.findByBillingPlan(billingPlan).isPresent()) {
            return;
        }

        Plan plan = Plan.builder()
                .billingPlan(billingPlan)
                .maxTeamMembers(maxTeamMembers)
                .maxBoards(maxBoards)
                .maxFeedbackPosts(maxFeedbackPosts)
                .maxEndUsers(maxEndUsers)
                .maxRoadmapItems(maxRoadmapItems)
                .maxChangelogEntries(maxChangelogEntries)
                .customBranding(true)
                .removeFidmapBranding(removeFidmapBranding)
                .privateBoards(privateBoards)
                .build();

        planRepository.save(plan);
    }
}