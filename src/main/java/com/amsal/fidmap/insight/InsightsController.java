package com.amsal.fidmap.insight;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<InsightsService.Insights>> get(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("insights", insightsService.insights(workspaceId)));
    }
}
