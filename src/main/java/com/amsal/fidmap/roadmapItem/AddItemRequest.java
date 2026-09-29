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

    //todo the target date is not implemented in the frontend creating,so add it
    private LocalDate targetDate;

    private RoadMapStatus status;

}

