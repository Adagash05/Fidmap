package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey-questions")
@RequiredArgsConstructor
public class SurveyQuestionManagementController {

    private final ISurveyQuestionService questionService;

    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<SurveyQuestionDto>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody UpdateQuestionRequest request
    ) {

        return ResponseEntity.ok(
                questionService.updateQuestion(
                        questionId,
                        request
                )
        );
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long questionId
    ) {

        return ResponseEntity.ok(
                questionService.deleteQuestion(
                        questionId
                )
        );
    }
}