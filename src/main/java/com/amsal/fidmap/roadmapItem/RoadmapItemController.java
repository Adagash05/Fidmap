package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.roadmap.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roadmap/item")
public class RoadmapItemController {

    private final RoadmapItemService roadmapItemService;

    @PostMapping("/{roadmap-id}")
    public ResponseEntity<ApiResponse<RoadmapItemDto>> createRoadmapItem(
            @PathVariable("roadmap-id") Long roadmapId, @RequestBody AddItemRequest request
    ) {

        return ResponseEntity
                .status(OK)
                .body(roadmapItemService.createRoadmapItem(roadmapId, request));
    }

    @GetMapping("/roadmap/{roadmap-id}")
    public ResponseEntity<ApiResponse<List<RoadmapItemDto>>> getAllRoadmapItems(
            @PathVariable("roadmap-id") Long roadmapId
    ) {
        return ResponseEntity
                .status(OK)
                .body(roadmapItemService.getAllRoadmapItems(roadmapId));
    }

    @GetMapping("/{roadmap-item-id}")
    public ResponseEntity<ApiResponse<RoadmapItemDto>> getRoadmapItem(
            @PathVariable("roadmap-item-id") UUID roadmapItemId
    ) {

        return ResponseEntity
                .status(OK)
                .body(roadmapItemService.getRoadmapItem(roadmapItemId));
    }

    @PutMapping("/{roadmap-id}")
    public ResponseEntity<ApiResponse<RoadmapItemDto>> updateRoadmapItem(
            @PathVariable("roadmap-id") UUID roadmapItemId,
            @RequestBody UpdateItemRequest request
    ) {

        return ResponseEntity
                .status(OK)
                .body(roadmapItemService.updateRoadmapItem(roadmapItemId, request));
    }

    @PatchMapping("/{roadmap-id}")
    public ResponseEntity<ApiResponse<RoadmapItemDto>> editRoadmapItem(
            @PathVariable("roadmap-id") UUID roadmapItemId,
            @RequestParam("status") RoadMapStatus status
    ) {

        return ResponseEntity
                .status(OK)
                .body(roadmapItemService.editRoadmapItem(roadmapItemId, status));
    }

    @DeleteMapping("/{roadmap-id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoadmapItem(
            @PathVariable("roadmap-id") UUID roadmapItemId
    ) {

        roadmapItemService.deleteRoadmapItem(roadmapItemId);

        return ResponseEntity.ok().build();
    }
}

