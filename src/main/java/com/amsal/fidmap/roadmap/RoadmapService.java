package com.amsal.fidmap.roadmap;
// Replace your existing RoadmapService.java with this. Also add the two methods
// below to IRoadmapService.java, and add `Roadmap findRoadmapByWorkspaceId(UUID workspaceId);`
// to RoadmapRepository.java (Spring Data derives it from the Roadmap.workspace field).
//
// You'll also want RoadmapItemMapper's toRoadMapItemDto fixed (see BACKEND_ISSUES.md #11)
// so items actually carry their id/targetDate — RoadMapDto below depends on that.


import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.roadmapItem.RoadmapItemMapper;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService implements IRoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RoadmapItemMapper roadmapItemMapper;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<RoadMapDto> getRoadmapByWorkspace(UUID workspaceId) {

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
        if (workspace == null) {
            throw new WorkspaceNotFoundException("workspace not found");
        }

        Roadmap roadmap = roadmapRepository.findRoadmapByWorkspaceId(workspaceId);
        if (roadmap == null) {
            throw new WorkspaceNotFoundException("this workspace has no roadmap yet");
        }

        RoadMapDto dto = new RoadMapDto();
        dto.setId(roadmap.getId());
        dto.setRoadmapItems(
                roadmap.getRoadmapItems()
                        .stream()
                        .map(roadmapItemMapper::toRoadMapItemDto)
                        .toList()
        );

        return ApiResponse.success("roadmap found", dto);
    }
}

// --- RoadMapDto.java should be updated to hold DTOs, not entities: ---
// @Getter @Setter
// public class RoadMapDto {
//     private UUID id;
//     private List<RoadmapItemDto> roadmapItems;   // was List<RoadmapItem> — raw entities
// }                                                 // in a DTO risk lazy-loading serialization
//                                                    // errors and leak internal fields.