package com.amsal.fidmap.surveyFile.surveyResult;

import com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType;

import java.util.List;
import java.util.Map;

/**
 * Analytics computed from real stored answers only.
 */
public record SurveyResultsDto(
        Long surveyId,
        String title,
        long responseCount,
        List<QuestionResult> questions
) {

    public record QuestionResult(
            Long questionId,
            String question,
            QuestionType type,
            long answerCount,
            /** option value / rating -> count (choice and rating questions) */
            Map<String, Long> distribution,
            /** mean rating, only for RATING questions with at least one answer */
            Double averageRating,
            /** most recent free-text answers (TEXT questions, capped) */
            List<String> recentTextAnswers
    ) {
    }
}
