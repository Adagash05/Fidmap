package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface ISurveyService {

    ApiResponse<SurveyDto> createSurvey(UUID workspaceId, CreateSurveyRequest request);

    ApiResponse<SurveyDto> getSurvey(Long surveyId);

    ApiResponse<List<SurveyDto>> getWorkspaceSurveys(UUID workspaceId);

    ApiResponse<SurveyDto> updateSurvey(Long surveyId, UpdateSurveyRequest request);

    ApiResponse<SurveyDto> publishSurvey(Long surveyId);

    ApiResponse<SurveyDto> closeSurvey(Long surveyId);

    ApiResponse<Void> deleteSurvey(Long surveyId);
}