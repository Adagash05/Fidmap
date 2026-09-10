package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AddItemRequest {

    private String title;

    private String description;

    private LocalDate targetDate;

    private RoadMapStatus status;

}

