package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.CreateQuestionRequest;

import java.util.List;
import java.util.UUID;

public record CreateSurveyRequest(
        String title,
        String description,
        List<CreateQuestionRequest> questions,
        UUID linkedFeedbackPostId
) {}
