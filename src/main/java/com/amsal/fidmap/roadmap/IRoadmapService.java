package com.amsal.fidmap.roadmap;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface IRoadmapService {


    ApiResponse<RoadMapDto> getRoadmapByWorkspace(UUID workspaceId);


//    ApiResponse<List<RoadMapDto>> getRoadMap(Long roadmapId);

//    ApiResponse<Void> removeFromRoadMap(Long roadmapId);
}
