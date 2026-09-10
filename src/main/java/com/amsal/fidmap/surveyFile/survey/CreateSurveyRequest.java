package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.CreateQuestionRequest;

import java.util.List;

public record CreateSurveyRequest(
        String title,
        String description,
        List<CreateQuestionRequest> questions
) {}
