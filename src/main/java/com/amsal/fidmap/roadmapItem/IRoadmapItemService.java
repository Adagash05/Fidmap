package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface IRoadmapItemService {

    ApiResponse<RoadmapItemDto> createRoadmapItem(Long roadmapId, AddItemRequest request);

    ApiResponse<List<RoadmapItemDto>> getAllRoadmapItems(Long roadmapId);

    ApiResponse<RoadmapItemDto> getRoadmapItem(UUID roadmapItemId);

    ApiResponse<RoadmapItemDto> updateRoadmapItem(UUID roadMapItemId,UpdateItemRequest request);


    ApiResponse<RoadmapItemDto> editRoadmapItem(UUID roadMapItemId,RoadMapStatus status);



//    ApiResponse<RoadmapDto> editRoadmapItemTitle(UUID roadMapItemId);
//    ApiResponse<RoadmapDto> editRoadmapItemDesc(UUID roadMapItemId);

    ApiResponse<Void> deleteRoadmapItem(UUID roadmapItemId);
}
