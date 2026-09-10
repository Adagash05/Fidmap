package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyManagementController {

    private final ISurveyService surveyService;

    @GetMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<SurveyDto>> getSurvey(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(
                surveyService.getSurvey(surveyId)
        );
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<SurveyDto>> updateSurvey(
            @PathVariable Long surveyId,
            @RequestBody UpdateSurveyRequest request
    ) {

        return ResponseEntity.ok(
                surveyService.updateSurvey(
                        surveyId,
                        request
                )
        );
    }

    @PatchMapping("/{surveyId}/publish")
    public ResponseEntity<ApiResponse<SurveyDto>> publishSurvey(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(
                surveyService.publishSurvey(surveyId)
        );
    }

    @PatchMapping("/{surveyId}/close")
    public ResponseEntity<ApiResponse<SurveyDto>> closeSurvey(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(
                surveyService.closeSurvey(surveyId)
        );
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<Void>> deleteSurvey(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(
                surveyService.deleteSurvey(surveyId)
        );
    }
}