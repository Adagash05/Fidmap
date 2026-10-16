package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.surveyFile.survey.SurveyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Public respondent flow (no staff login). Only PUBLISHED surveys are reachable. */
@RestController
@RequestMapping("/api/public/surveys/{surveyId}")
@RequiredArgsConstructor
public class PublicSurveyController {

    private final ISurveyResponseService responseService;

    @GetMapping
    public ResponseEntity<ApiResponse<SurveyDto>> getSurvey(@PathVariable Long surveyId) {

        return ResponseEntity.ok(responseService.getPublicSurvey(surveyId));
    }

    @PostMapping("/responses")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> submit(
            @PathVariable Long surveyId,
            @RequestBody SubmitSurveyResponseRequest request
    ) {

        return ResponseEntity.ok(responseService.submitResponse(surveyId, request));
    }
}
