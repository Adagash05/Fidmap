package com.amsal.fidmap.decision;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure, deterministic, versioned priority scoring. No I/O, no clock reads
 * (the caller passes "now"), so identical inputs always give identical output.
 *
 * Components (0-100) and weights:
 *   demand 40  - distinct supporters (voters, commenters, author), diminishing returns
 *   survey 20  - linked survey responses (and average rating when asked)
 *   recency 10 - half-life 30 days since last activity
 *   urgency 15 - human input 1-5
 *   strategic 15 - human input 1-5
 *
 * Missing components are EXCLUDED and the remaining weights renormalised; the
 * score range [low, high] shows what the score would be if the missing
 * components were 0 or 100. Effort is deliberately NOT folded in: it is shown
 * next to the score so people can weigh value against cost themselves.
 */
public final class PriorityScoringEngine {

    public static final String VERSION = "v1";

    static final int W_DEMAND = 40;
    static final int W_SURVEY = 20;
    static final int W_RECENCY = 10;
    static final int W_URGENCY = 15;
    static final int W_STRATEGIC = 15;

    static final double HALF_LIFE_DAYS = 30.0;

    private PriorityScoringEngine() {
    }

    public static PriorityResult score(PriorityEvidence e, Instant now) {

        List<ScoreComponent> components = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        double weighted = 0;
        int presentWeight = 0;
        int totalWeight = W_DEMAND + W_SURVEY + W_RECENCY + W_URGENCY + W_STRATEGIC;

        // demand (always present: zero supporters is a real, low value)
        int supporters = Math.max(0, e.supporters());
        int demand = clamp(100.0 * Math.log(1 + supporters) / Math.log(21));
        components.add(new ScoreComponent("demand", "Customer demand", demand, W_DEMAND,
                round1(demand * W_DEMAND / 100.0),
                supporters + " distinct supporter(s)"));
        weighted += demand * W_DEMAND;
        presentWeight += W_DEMAND;

        // survey
        if (e.surveyResponses() > 0) {
            double countScore = 100.0 * Math.log(1 + e.surveyResponses()) / Math.log(11);
            double value = e.surveyAverageRating() == null
                    ? countScore
                    : 0.5 * Math.min(100, countScore) + 0.5 * ((e.surveyAverageRating() - 1) / 4.0 * 100.0);
            int v = clamp(value);
            components.add(new ScoreComponent("survey", "Survey evidence", v, W_SURVEY,
                    round1(v * W_SURVEY / 100.0),
                    e.surveyResponses() + " survey response(s)"
                            + (e.surveyAverageRating() == null ? ""
                            : ", average rating " + round1(e.surveyAverageRating()) + "/5")));
            weighted += v * W_SURVEY;
            presentWeight += W_SURVEY;
        } else {
            components.add(missingComponent("survey", "Survey evidence", W_SURVEY));
            missing.add("Link a survey and collect responses");
        }

        // recency
        if (e.lastActivityAt() != null) {
            double days = Math.max(0, Duration.between(e.lastActivityAt(), now).toHours() / 24.0);
            int v = clamp(100.0 * Math.pow(0.5, days / HALF_LIFE_DAYS));
            components.add(new ScoreComponent("recency", "Recent activity", v, W_RECENCY,
                    round1(v * W_RECENCY / 100.0),
                    "Last activity " + Math.round(days) + " day(s) ago"));
            weighted += v * W_RECENCY;
            presentWeight += W_RECENCY;
        } else {
            components.add(missingComponent("recency", "Recent activity", W_RECENCY));
        }

        // urgency / strategic (human inputs)
        presentWeight += humanComponent(components, missing, "urgency", "Urgency", W_URGENCY, e.urgency());
        weighted += humanWeighted(e.urgency(), W_URGENCY);
        presentWeight += humanComponent(components, missing, "strategic", "Strategic fit", W_STRATEGIC, e.strategic());
        weighted += humanWeighted(e.strategic(), W_STRATEGIC);

        int score = clamp(weighted / presentWeight);
        int low = clamp(weighted / totalWeight);
        int high = clamp((weighted + 100.0 * (totalWeight - presentWeight)) / totalWeight);

        int humanOrSurvey = (e.surveyResponses() > 0 ? 1 : 0)
                + (e.urgency() != null ? 1 : 0)
                + (e.strategic() != null ? 1 : 0);

        Confidence confidence;
        if (supporters >= 10 && humanOrSurvey >= 2) {
            confidence = Confidence.HIGH;
        } else if (supporters >= 10 || (supporters >= 3 && humanOrSurvey >= 1)) {
            confidence = Confidence.MEDIUM;
        } else {
            confidence = Confidence.LOW;
        }

        Recommendation recommendation;
        String reason;

        if (confidence == Confidence.LOW || (supporters < 3 && e.surveyResponses() == 0)) {
            recommendation = Recommendation.INVESTIGATE;
            reason = "Not enough evidence to decide: gather more votes, link a survey, or add urgency/strategic inputs.";
        } else if (score >= 65) {
            recommendation = Recommendation.PRIORITIZE;
            reason = "Strong evidence (score " + score + ") with " + confidence.name().toLowerCase() + " confidence.";
        } else if (score >= 40) {
            recommendation = Recommendation.CONSIDER;
            reason = "Moderate evidence (score " + score + "); weigh against effort and strategy.";
        } else {
            recommendation = Recommendation.DEFER;
            reason = "Weak evidence so far (score " + score + "); revisit if demand grows.";
        }

        return new PriorityResult(VERSION, score, Math.min(low, score), Math.max(high, score),
                confidence, recommendation, reason, List.copyOf(components), List.copyOf(missing));
    }

    private static int humanComponent(List<ScoreComponent> out, List<String> missing,
                                      String key, String label, int weight, Integer input) {
        if (input == null) {
            out.add(missingComponent(key, label, weight));
            missing.add("Add " + label.toLowerCase() + " (1-5)");
            return 0;
        }
        int v = fromScale(input);
        out.add(new ScoreComponent(key, label, v, weight, round1(v * weight / 100.0),
                label + " rated " + input + "/5 by your team"));
        return weight;
    }

    private static double humanWeighted(Integer input, int weight) {
        return input == null ? 0 : fromScale(input) * (double) weight;
    }

    private static int fromScale(int input) {
        int bounded = Math.max(1, Math.min(5, input));
        return (bounded - 1) * 25;
    }

    private static ScoreComponent missingComponent(String key, String label, int weight) {
        return new ScoreComponent(key, label, null, weight, 0, "No evidence yet - excluded from the score");
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(100, Math.round(v)));
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
