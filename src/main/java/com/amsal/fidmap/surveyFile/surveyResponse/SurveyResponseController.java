package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.surveyFile.surveyResult.SurveyResultsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Staff-only: browse responses and analytics of one survey. */
@RestController
@RequestMapping("/api/surveys/{surveyId}")
@RequiredArgsConstructor
public class SurveyResponseController {

    private final ISurveyResponseService responseService;

    @GetMapping("/responses")
    public ResponseEntity<ApiResponse<PageResponse<SurveyResponseDto>>> getResponses(
            @PathVariable Long surveyId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {

        return ResponseEntity.ok(responseService.getSurveyResponses(surveyId, page, size));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<SurveyResultsDto>> getResults(
            @PathVariable Long surveyId
    ) {

        return ResponseEntity.ok(responseService.getResults(surveyId));
    }
}
