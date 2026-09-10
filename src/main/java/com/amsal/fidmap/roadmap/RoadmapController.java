package com.amsal.fidmap.roadmap;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

/**
 * Did not exist before. Without this, the frontend has no way to discover the
 * roadmap id that RoadmapItemController requires for every operation.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    // Every workspace has exactly one roadmap (see Roadmap.workspace @OneToOne),
    // created automatically in WorkspaceService.createWorkspace. This is the
    // endpoint the frontend actually needs.
    @GetMapping("/workspace/{workspace-id}")
    public ResponseEntity<ApiResponse<RoadMapDto>> getRoadmapByWorkspace(
            @PathVariable("workspace-id") UUID workspaceId
    ) {
        return ResponseEntity
                .status(OK)
                .body(roadmapService.getRoadmapByWorkspace(workspaceId));
    }
}
