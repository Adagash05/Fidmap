package com.amsal.fidmap.roadmapItem;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateItemRequest {

    private String title;

    private String description;

    private LocalDate targetDate;

    private RoadMapStatus status;
}
