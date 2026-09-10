package com.amsal.fidmap.surveyFile.surveyAnswer;


public record SurveyAnswerDto(
        Long id,
        Long questionId,
        String answer
) {
}
