package com.amsal.fidmap.feedbackPost;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class FeedbackDto {

    private UUID id;

    private String title;

    private String description;

    private FeedbackStatus status;

    private Long voteCount;

    private String endUserName;

    private UUID roadmapItemId;

    private LocalDate createdAt;
}

