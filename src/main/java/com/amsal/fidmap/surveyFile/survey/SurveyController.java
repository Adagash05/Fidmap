package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final ISurveyService surveyService;

    @PostMapping
    public ResponseEntity<ApiResponse<SurveyDto>> createSurvey(
            @PathVariable UUID workspaceId,
            @RequestBody CreateSurveyRequest request
    ) {

        return ResponseEntity.ok(
                surveyService.createSurvey(
                        workspaceId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SurveyDto>>> getWorkspaceSurveys(
            @PathVariable UUID workspaceId
    ) {

        return ResponseEntity.ok(
                surveyService.getWorkspaceSurveys(
                        workspaceId
                )
        );
    }
}