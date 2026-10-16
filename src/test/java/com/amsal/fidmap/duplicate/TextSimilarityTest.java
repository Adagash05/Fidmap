package com.amsal.fidmap.duplicate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextSimilarityTest {

    @Test
    void nearDuplicateTitlesScoreAboveThreshold() {
        double s = TextSimilarity.similarity("Dark mode for dashboard", null, "Add dark mode to the dashboard", null);
        assertTrue(s >= DuplicateService.THRESHOLD, "similarity was " + s);
    }

    @Test
    void unrelatedTitlesScoreBelowThreshold() {
        double s = TextSimilarity.similarity("Export invoices as PDF", null, "Slack integration for alerts", null);
        assertTrue(s < DuplicateService.THRESHOLD, "similarity was " + s);
    }

    @Test
    void pluralsAreNormalised() {
        assertEquals(TextSimilarity.tokens("integration"), TextSimilarity.tokens("integrations"));
    }

    @Test
    void emptyTextIsZero() {
        assertEquals(0.0, TextSimilarity.similarity("", "", "anything", null));
    }
}
