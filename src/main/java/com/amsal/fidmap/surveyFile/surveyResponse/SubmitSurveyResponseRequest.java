package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerRequest;

import java.util.List;

public record SubmitSurveyResponseRequest(
        List<SurveyAnswerRequest> answers
) {
}