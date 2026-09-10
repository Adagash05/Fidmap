package com.amsal.fidmap.feedbackPost;

import org.springframework.stereotype.Service;

@Service
public class FeedbackMapper {

    public FeedbackPost toFeedbackPost(AddFeedback feedback) {

        FeedbackPost post = new FeedbackPost();

        post.setTitle(feedback.getTitle());
        post.setDescription(feedback.getDescription());

        // New feedback starts as OPEN.
        // Staff can later move it to PLANNED, IN_PROGRESS,
        // COMPLETED, or REJECTED.
        post.setStatus(FeedbackStatus.OPEN);

        post.setVoteCount(0L);

        return post;
    }

    public FeedbackDto toFeedbackDto(FeedbackPost post) {

        FeedbackDto dto = new FeedbackDto();

        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setStatus(post.getStatus());
        dto.setVoteCount(post.getVoteCount());
        dto.setCreatedAt(post.getCreatedAt());

        if (post.getEndUser() != null) {
            dto.setEndUserName(post.getEndUser().getName());
        }

        if (post.getRoadMapItem() != null) {
            dto.setRoadmapItemId(post.getRoadMapItem().getId());
        }

        return dto;
    }
}

