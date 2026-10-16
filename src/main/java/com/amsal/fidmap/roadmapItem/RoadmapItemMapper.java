package com.amsal.fidmap.roadmapItem;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoadmapItemMapper {

    public RoadmapItem toRoadMapItem(AddItemRequest request) {

        RoadmapItem item = new RoadmapItem();

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setTargetDate(request.getTargetDate());
        item.setStatus(request.getStatus() == null ? RoadMapStatus.PLANNED : request.getStatus());
        item.setCreatedAt(LocalDate.now());

        return item;
    }

    public RoadmapItemDto toRoadMapItemDto(RoadmapItem item) {

        RoadmapItemDto dto = new RoadmapItemDto();

        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setTargetDate(item.getTargetDate());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());

        List<com.amsal.fidmap.feedbackPost.FeedbackPost> linked = item.getFeedbackPost();

        dto.setFeedbackPostIds(
                linked == null
                        ? List.of()
                        : linked.stream().map(com.amsal.fidmap.feedbackPost.FeedbackPost::getId).toList()
        );

        return dto;
    }
}
