package com.amsal.fidmap.workspace;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/{workspace-id}")
    public ResponseEntity<ApiResponse<WorkspaceDto>> getWorkspace(
            @PathVariable("workspace-id") UUID workspaceId
    ) {

        ApiResponse<WorkspaceDto> response =
                workspaceService.getWorkspace(workspaceId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<WorkspaceDto>> getWorkspaceBySlug(
            @PathVariable String slug
    ) {

        return ResponseEntity.ok(
                workspaceService.getWorkspaceBySlug(slug)
        );
    }
}

