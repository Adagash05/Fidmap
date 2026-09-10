package com.amsal.fidmap.surveyFile.surveyAnswer;

public record SurveyAnswerRequest(
        Long questionId,
        String answer
) {
}
