package com.amsal.fidmap.decision;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PriorityScoringEngineTest {

    private static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");

    private static PriorityEvidence ev(int supporters, int surveys, Double rating, Instant last,
                                       Integer urgency, Integer strategic) {
        return new PriorityEvidence(supporters, surveys, rating, last, urgency, strategic, null);
    }

    @Test
    void noEvidenceIsInvestigateWithLowConfidence() {
        PriorityResult r = PriorityScoringEngine.score(ev(0, 0, null, null, null, null), NOW);
        assertEquals(Confidence.LOW, r.confidence());
        assertEquals(Recommendation.INVESTIGATE, r.recommendation());
        assertEquals(0, r.score());
        assertEquals(60, r.scoreHigh()); // demand present (0); the other 60 weight points are missing
    }

    @Test
    void sameInputGivesSameOutput() {
        PriorityEvidence e = ev(12, 4, 4.5, NOW.minus(3, ChronoUnit.DAYS), 4, 5);
        assertEquals(PriorityScoringEngine.score(e, NOW), PriorityScoringEngine.score(e, NOW));
    }

    @Test
    void missingComponentsAreExcludedAndRenormalised() {
        PriorityResult r = PriorityScoringEngine.score(ev(20, 0, null, NOW, null, null), NOW);
        // demand=100 (weight 40), recency=100 (weight 10): only these are present -> 100
        assertEquals(100, r.score());
        assertTrue(r.scoreLow() < r.score() || r.scoreLow() == r.score());
        assertEquals(100, r.scoreHigh());
        assertFalse(r.missingEvidence().isEmpty());
    }

    @Test
    void strongEvidenceWithHumanInputsIsPrioritiseWithHighConfidence() {
        PriorityResult r = PriorityScoringEngine.score(ev(25, 6, 4.8, NOW.minus(2, ChronoUnit.DAYS), 5, 5), NOW);
        assertEquals(Confidence.HIGH, r.confidence());
        assertEquals(Recommendation.PRIORITIZE, r.recommendation());
        assertTrue(r.score() >= 65);
    }

    @Test
    void rejectIsNeverRecommendedBySystem() {
        for (int s = 0; s < 40; s += 3) {
            assertNotEquals(Recommendation.REJECT, PriorityScoringEngine.score(ev(s, s % 5, null, NOW, 1, 1), NOW).recommendation());
        }
    }

    @Test
    void recencyHalvesEveryThirtyDays() {
        PriorityResult fresh = PriorityScoringEngine.score(ev(5, 0, null, NOW, null, null), NOW);
        PriorityResult old = PriorityScoringEngine.score(ev(5, 0, null, NOW.minus(30, ChronoUnit.DAYS), null, null), NOW);
        int freshRecency = fresh.components().stream().filter(c -> c.key().equals("recency")).findFirst().get().value();
        int oldRecency = old.components().stream().filter(c -> c.key().equals("recency")).findFirst().get().value();
        assertEquals(100, freshRecency);
        assertEquals(50, oldRecency);
    }

    @Test
    void effortDoesNotChangeTheScore() {
        PriorityResult a = PriorityScoringEngine.score(new PriorityEvidence(8, 0, null, NOW, 3, 3, 1), NOW);
        PriorityResult b = PriorityScoringEngine.score(new PriorityEvidence(8, 0, null, NOW, 3, 3, 5), NOW);
        assertEquals(a.score(), b.score());
    }

    @Test
    void scoreStaysInsideItsRange() {
        PriorityResult r = PriorityScoringEngine.score(ev(6, 0, null, NOW, 2, null), NOW);
        assertTrue(r.scoreLow() <= r.score() && r.score() <= r.scoreHigh());
    }
}
