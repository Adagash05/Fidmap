package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.apiResponse.ApiResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;

public interface ISurveyQuestionService {

    ApiResponse<SurveyQuestionDto> addQuestion(
            Long surveyId,
            CreateQuestionRequest request
    );

    ApiResponse<SurveyQuestionDto> updateQuestion(
            Long questionId,
            UpdateQuestionRequest request
    );

    ApiResponse<Void> deleteQuestion(
            Long questionId
    );
}
