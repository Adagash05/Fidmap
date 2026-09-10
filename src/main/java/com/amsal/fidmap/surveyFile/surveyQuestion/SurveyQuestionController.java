package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys/{surveyId}/questions")
@RequiredArgsConstructor
public class SurveyQuestionController {

    private final ISurveyQuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyQuestionDto>> addQuestion(
            @PathVariable Long surveyId,
            @RequestBody CreateQuestionRequest request
    ) {

        return ResponseEntity.ok(
                questionService.addQuestion(
                        surveyId,
                        request
                )
        );
    }
}