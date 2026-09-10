package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoadmapItemDto {

    private UUID id;

    private String title;

    private String description;

    //the date that is expected to be launch is the target date
    private LocalDate targetDate;

    private RoadMapStatus status;

    private LocalDate createdAt;

//    private List<FeedbackPost> feedbackPost;
}
