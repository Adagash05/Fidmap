package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.exception.PlanLimitExceededException;
import com.amsal.fidmap.exception.SubscriptionRequiredException;
import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PlanEntitlementEvaluateTest {

    private static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");

    private static Subscription sub(SubscriptionStatus status, BillingPlan plan, Instant periodEnd, Instant trialEnd) {
        Subscription s = new Subscription();
        s.setStatus(status);
        s.setPlan(plan);
        s.setCurrentPeriodEnd(periodEnd);
        s.setTrialEndsAt(trialEnd);
        return s;
    }

    @Test
    void noRecordIsDenied() {
        var st = PlanEntitlementService.evaluate(null, NOW);
        assertFalse(st.active());
        assertEquals("NO_SUBSCRIPTION", st.reasonCode());
    }

    @Test
    void lifetimeActiveIsAllowed() {
        assertTrue(PlanEntitlementService.evaluate(sub(SubscriptionStatus.ACTIVE, BillingPlan.LIFETIME, null, null), NOW).active());
    }

    @Test
    void recurringActiveEndsAtPeriodEnd() {
        assertTrue(PlanEntitlementService.evaluate(
                sub(SubscriptionStatus.ACTIVE, BillingPlan.STARTUP_MONTHLY, NOW.plus(1, ChronoUnit.DAYS), null), NOW).active());
        var ended = PlanEntitlementService.evaluate(
                sub(SubscriptionStatus.ACTIVE, BillingPlan.STARTUP_MONTHLY, NOW.minus(1, ChronoUnit.SECONDS), null), NOW);
        assertFalse(ended.active());
        assertEquals("PERIOD_ENDED", ended.reasonCode());
    }

    @Test
    void trialEndsAtTrialEnd() {
        assertTrue(PlanEntitlementService.evaluate(
                sub(SubscriptionStatus.TRIALING, null, null, NOW.plus(2, ChronoUnit.DAYS)), NOW).active());
        var expired = PlanEntitlementService.evaluate(
                sub(SubscriptionStatus.TRIALING, null, null, NOW.minus(2, ChronoUnit.DAYS)), NOW);
        assertFalse(expired.active());
        assertEquals("TRIAL_EXPIRED", expired.reasonCode());
    }

    @Test
    void badStatesAreDenied() {
        for (SubscriptionStatus s : new SubscriptionStatus[]{
                SubscriptionStatus.PAST_DUE, SubscriptionStatus.PAUSED,
                SubscriptionStatus.CANCELED, SubscriptionStatus.EXPIRED}) {
            assertFalse(PlanEntitlementService.evaluate(sub(s, BillingPlan.STARTUP_MONTHLY, NOW.plus(5, ChronoUnit.DAYS), null), NOW).active(), s.name());
        }
    }

    // ----- survey limits (Startup 1, Business 10, Lifetime 10) -----------------------------------

    private static PlanEntitlementService serviceFor(BillingPlan billingPlan, SubscriptionStatus status,
                                                     Integer maxSurveys, Instant periodEnd) {
        SubscriptionRepository subscriptions = mock(SubscriptionRepository.class);
        PlanRepository plans = mock(PlanRepository.class);

        when(subscriptions.findByWorkspaceId(any())).thenReturn(Optional.of(sub(status, billingPlan, periodEnd, null)));
        when(plans.findByBillingPlan(billingPlan)).thenReturn(Optional.of(
                Plan.builder().billingPlan(billingPlan).maxTeamMembers(3).maxSurveys(maxSurveys).build()));

        return new PlanEntitlementService(subscriptions, plans);
    }

    private static final Instant FUTURE = Instant.now().plus(30, ChronoUnit.DAYS);

    @Test
    void startupAllowsExactlyOneSurvey() {
        var service = serviceFor(BillingPlan.STARTUP_MONTHLY, SubscriptionStatus.ACTIVE, 1, FUTURE);
        UUID ws = UUID.randomUUID();

        assertDoesNotThrow(() -> service.checkSurveyLimit(ws, 0));

        var e = assertThrows(PlanLimitExceededException.class, () -> service.checkSurveyLimit(ws, 1));
        assertTrue(e.getMessage().contains("1 survey."), e.getMessage());
        assertTrue(e.getMessage().contains("Startup"), e.getMessage());
    }

    @Test
    void businessAllowsTenSurveys() {
        var service = serviceFor(BillingPlan.BUSINESS_MONTHLY, SubscriptionStatus.ACTIVE, 10, FUTURE);
        UUID ws = UUID.randomUUID();

        assertDoesNotThrow(() -> service.checkSurveyLimit(ws, 9));
        assertThrows(PlanLimitExceededException.class, () -> service.checkSurveyLimit(ws, 10));
    }

    @Test
    void lifetimeSurveyLimitMirrorsBusinessAndIsNotExpired() {
        var service = serviceFor(BillingPlan.LIFETIME, SubscriptionStatus.ACTIVE, 10, null);
        UUID ws = UUID.randomUUID();

        assertDoesNotThrow(() -> service.checkSurveyLimit(ws, 9));
        assertThrows(PlanLimitExceededException.class, () -> service.checkSurveyLimit(ws, 10));
    }

    @Test
    void downgradedWorkspaceKeepsItsSurveysButCannotCreateMore() {
        // Business -> Startup with 5 existing surveys: nothing is deleted, creation is simply refused.
        var service = serviceFor(BillingPlan.STARTUP_MONTHLY, SubscriptionStatus.ACTIVE, 1, FUTURE);

        assertThrows(PlanLimitExceededException.class, () -> service.checkSurveyLimit(UUID.randomUUID(), 5));
    }

    @Test
    void nullLimitMeansUnlimited() {
        var service = serviceFor(BillingPlan.BUSINESS_MONTHLY, SubscriptionStatus.ACTIVE, null, FUTURE);

        assertDoesNotThrow(() -> service.checkSurveyLimit(UUID.randomUUID(), 10_000));
    }

    @Test
    void expiredWorkspaceIsRefusedBeforeTheLimitIsEvaluated() {
        var service = serviceFor(BillingPlan.BUSINESS_MONTHLY, SubscriptionStatus.ACTIVE, 10,
                Instant.now().minus(1, ChronoUnit.DAYS));

        var e = assertThrows(SubscriptionRequiredException.class, () -> service.checkSurveyLimit(UUID.randomUUID(), 0));
        assertEquals("PERIOD_ENDED", e.getReasonCode());
    }

    @Test
    void initializerBackfillsSurveyLimitOnExistingPlanRowsWithoutTouchingOtherFields() {
        PlanRepository plans = mock(PlanRepository.class);
        Plan legacy = Plan.builder().billingPlan(BillingPlan.STARTUP_MONTHLY).maxTeamMembers(7).maxSurveys(null).build();

        when(plans.findByBillingPlan(any())).thenAnswer(inv -> {
            BillingPlan p = inv.getArgument(0);
            return p == BillingPlan.STARTUP_MONTHLY
                    ? Optional.of(legacy)
                    : Optional.of(Plan.builder().billingPlan(p).maxSurveys(p.name().startsWith("STARTUP") ? 1 : 10).build());
        });

        new PlanDataInitializer(plans).run();

        assertEquals(1, legacy.getMaxSurveys());
        assertEquals(7, legacy.getMaxTeamMembers());
        verify(plans).save(legacy);
    }
}
