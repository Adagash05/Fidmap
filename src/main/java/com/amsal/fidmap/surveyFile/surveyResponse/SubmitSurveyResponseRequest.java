package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.endUser.EndUserRequest;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerRequest;

import java.util.List;

public record SubmitSurveyResponseRequest(
        EndUserRequest respondent,
        List<SurveyAnswerRequest> answers
) {
}
