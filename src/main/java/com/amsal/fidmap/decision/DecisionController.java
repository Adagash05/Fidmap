package com.amsal.fidmap.decision;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.decision.DecisionDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @GetMapping("/workspace/{workspaceId}/priorities")
    public ResponseEntity<ApiResponse<PageResponse<PriorityRow>>> priorities(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "false") boolean includeClosed,
            @RequestParam(defaultValue = "score") String sort) {

        return ResponseEntity.ok(ApiResponse.success("prioritised requests",
                decisionService.priorities(workspaceId, page, size, includeClosed, sort)));
    }

    @GetMapping("/feedback/{feedbackId}/brief")
    public ResponseEntity<ApiResponse<DecisionBrief>> brief(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok(ApiResponse.success("decision brief", decisionService.brief(feedbackId)));
    }

    @PutMapping("/feedback/{feedbackId}/inputs")
    public ResponseEntity<ApiResponse<PriorityInputRequest>> saveInputs(
            @PathVariable UUID feedbackId, @RequestBody PriorityInputRequest request) {
        return ResponseEntity.ok(ApiResponse.success("inputs saved", decisionService.saveInputs(feedbackId, request)));
    }

    @PostMapping("/feedback/{feedbackId}/decide")
    public ResponseEntity<ApiResponse<DecisionView>> decide(
            @PathVariable UUID feedbackId, @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("decision recorded", decisionService.decide(feedbackId, request)));
    }
}
