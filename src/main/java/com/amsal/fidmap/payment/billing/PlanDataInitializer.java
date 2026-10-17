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
                1,
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
                1,
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
                10,
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
                10,
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
                10,
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
            Integer maxSurveys,
            boolean removeFidmapBranding,
            boolean privateBoards
    ) {

        Plan existing = planRepository.findByBillingPlan(billingPlan).orElse(null);

        if (existing != null) {
            // Plans created before surveys were limited have max_surveys = NULL,
            // which means "unlimited". The survey limit is defined here, so bring
            // existing rows in line. Only this field is synchronised; limits an
            // operator edited by hand are left alone.
            if (!java.util.Objects.equals(existing.getMaxSurveys(), maxSurveys)) {
                existing.setMaxSurveys(maxSurveys);
                planRepository.save(existing);
            }
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
                .maxSurveys(maxSurveys)
                .customBranding(true)
                .removeFidmapBranding(removeFidmapBranding)
                .privateBoards(privateBoards)
                .build();

        planRepository.save(plan);
    }
}