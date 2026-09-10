package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.RoadmapNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.roadmap.Roadmap;
import com.amsal.fidmap.roadmap.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RoadmapItemService implements IRoadmapItemService {

    private final RoadmapItemRepository roadmapItemRepository;
    private final RoadmapItemMapper roadmapItemMapper;
    private final RoadmapRepository roadmapRepository;
    private final PlanEntitlementService planEntitlementService;

    @Override
    public ApiResponse<RoadmapItemDto> createRoadmapItem(Long roadmapId, AddItemRequest request) {

        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if (roadmap == null) {
            throw new RoadmapNotFoundException("roadmap is not found");
        }

        //Plan check
        long currentItems = roadmapItemRepository.countByRoadmapId(roadmapId);
        planEntitlementService.checkRoadmapItemLimit(roadmap.getWorkspace().getId(), currentItems);


        RoadmapItem item = roadmapItemMapper.toRoadMapItem(request);

        //might add roadmap repo to save roadmap later if bug exists
        roadmap.addRoadmapItem(item);


        roadmapItemRepository.save(item);


        var dto = roadmapItemMapper.toRoadMapItemDto(item);

        return ApiResponse.success("item is added successfully", dto);
    }

    @Override
    public ApiResponse<List<RoadmapItemDto>> getAllRoadmapItems(Long roadmapId) {

        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if (roadmap == null) {
            throw new RoadmapNotFoundException("roadmap is not found");
        }

        List<RoadmapItemDto> dtoList = roadmapItemRepository.findAllByRoadmap(roadmap)
                .stream()
                .map(roadmapItemMapper::toRoadMapItemDto)
                .toList();


        return ApiResponse.success("retrieve roadmap items", dtoList);
    }

    @Override
    public ApiResponse<RoadmapItemDto> getRoadmapItem(UUID roadmapItemId) {

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("item is not found please try again later"));


        var dto = roadmapItemMapper.toRoadMapItemDto(roadmapItem);

        return ApiResponse.success("retrieve roadmap item", dto);

    }

    @Override
    public ApiResponse<RoadmapItemDto> updateRoadmapItem(UUID roadmapItemId, UpdateItemRequest request) {

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("item is not found please try again later"));

        roadmapItem.setTitle(request.getTitle());
        roadmapItem.setDescription(request.getDescription());
        roadmapItem.setTargetDate(request.getTargetDate());
        roadmapItem.setStatus(request.getStatus());

        roadmapItemRepository.save(roadmapItem);

        var dto = roadmapItemMapper.toRoadMapItemDto(roadmapItem);

        return ApiResponse.success("update roadmap item successfully", dto);

    }

    @Override
    public ApiResponse<RoadmapItemDto> editRoadmapItem(UUID roadmapItemId, RoadMapStatus status) {

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("item is not found please try again later"));


        roadmapItem.setStatus(status);

        roadmapItemRepository.save(roadmapItem);

        var dto = roadmapItemMapper.toRoadMapItemDto(roadmapItem);

        return ApiResponse.success("update roadmap item status successfully", dto);

    }

    @Override
    public ApiResponse<Void> deleteRoadmapItem(UUID roadmapItemId) {

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("item is not found please try again later"));


        roadmapItemRepository.delete(roadmapItem);

        return ApiResponse.success("deleted item successfully", null);
    }
}
