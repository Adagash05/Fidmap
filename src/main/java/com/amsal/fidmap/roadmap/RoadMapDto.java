package com.amsal.fidmap.roadmap;

import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.roadmapItem.RoadmapItemDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoadMapDto {

    private Long id;

    private List<RoadmapItemDto> roadmapItems;
}
