package com.amsal.fidmap.duplicate;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/duplicates")
@RequiredArgsConstructor
public class DuplicateController {

    private final DuplicateService duplicateService;

    @PostMapping("/workspace/{workspaceId}/scan")
    public ResponseEntity<ApiResponse<DuplicateService.ScanResult>> scan(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("scan complete", duplicateService.scan(workspaceId)));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<PageResponse<DuplicateService.SuggestionView>>> list(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("duplicate suggestions", duplicateService.list(workspaceId, page, size)));
    }

    @PostMapping("/{suggestionId}/confirm")
    public ResponseEntity<ApiResponse<DuplicateService.MergeResult>> confirm(
            @PathVariable UUID suggestionId,
            @RequestBody(required = false) Map<String, UUID> body) {
        UUID keep = body == null ? null : body.get("keepPostId");
        return ResponseEntity.ok(ApiResponse.success("merged", duplicateService.confirm(suggestionId, keep)));
    }

    @PostMapping("/{suggestionId}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismiss(@PathVariable UUID suggestionId) {
        duplicateService.dismiss(suggestionId);
        return ResponseEntity.ok(ApiResponse.success("dismissed", null));
    }
}
