package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys/{surveyId}")
@RequiredArgsConstructor
public class SurveyResponseController {

    private final ISurveyResponseService responseService;

    @PostMapping("/responses")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> submitResponse(
            @PathVariable Long surveyId,
            @RequestBody SubmitSurveyResponseRequest request
    ) {

        return ResponseEntity.ok(
                responseService.submitResponse(
                        surveyId,
                        request
                )
        );
    }

    @GetMapping("/responses")
    public ResponseEntity<ApiResponse<List<SurveyResponseDto>>> getResponses(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(
                responseService.getSurveyResponses(
                        surveyId
                )
        );
    }
}
