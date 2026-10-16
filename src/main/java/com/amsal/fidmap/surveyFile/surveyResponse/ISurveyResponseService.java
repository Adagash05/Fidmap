package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.surveyFile.survey.SurveyDto;
import com.amsal.fidmap.surveyFile.surveyResult.SurveyResultsDto;

public interface ISurveyResponseService {

    /** Public: a respondent identified by name + email answers a PUBLISHED survey. */
    ApiResponse<SurveyResponseDto> submitResponse(Long surveyId, SubmitSurveyResponseRequest request);

    /** Public: the survey definition, only while PUBLISHED. */
    ApiResponse<SurveyDto> getPublicSurvey(Long surveyId);

    /** Staff: paged individual responses. */
    ApiResponse<PageResponse<SurveyResponseDto>> getSurveyResponses(Long surveyId, Integer page, Integer size);

    /** Staff: analytics computed from stored answers. */
    ApiResponse<SurveyResultsDto> getResults(Long surveyId);
}
