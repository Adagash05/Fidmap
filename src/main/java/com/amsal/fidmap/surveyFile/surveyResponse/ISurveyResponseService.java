package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.surveyFile.surveyResult.SurveyResultsDto;

import java.util.List;

public interface ISurveyResponseService {

    ApiResponse<SurveyResponseDto> submitResponse(Long surveyId, SubmitSurveyResponseRequest request);

    ApiResponse<List<SurveyResponseDto>> getSurveyResponses(Long surveyId);
}