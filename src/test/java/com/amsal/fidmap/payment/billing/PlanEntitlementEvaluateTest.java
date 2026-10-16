package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import org.junit.jupiter.api.Test;

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
}
