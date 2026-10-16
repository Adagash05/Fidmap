package com.amsal.fidmap.surveyFile.surveyAnswer;

import java.util.List;

/**
 * One answer to one question.
 * - TEXT / SINGLE_CHOICE / RATING: use {@code answer}.
 * - MULTIPLE_CHOICE: use {@code answers} (the selected option values).
 */
public record SurveyAnswerRequest(
        Long questionId,
        String answer,
        List<String> answers
) {
}
