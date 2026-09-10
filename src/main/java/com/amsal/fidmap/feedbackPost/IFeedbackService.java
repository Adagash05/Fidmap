package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.apiResponse.ApiResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IFeedbackService {


    ApiResponse<FeedbackDto> createFeedbackPost(AddFeedback feedback, UUID boardId);


    ApiResponse<List<FeedbackDto>> getAllFeedbackByBoard(UUID boardId);


    ApiResponse<FeedbackDto> getFeedbackById(UUID boardId, UUID feedbackId);


    @Transactional
    ApiResponse<FeedbackDto> assignFeedbackToRoadmapItem(
            UUID feedbackId,
            UUID roadmapItemId
    );

    ApiResponse<FeedbackDto> editFeedback(UUID boardId, UUID feedbackId, UpdateFeedbackRequest request);

    ApiResponse<FeedbackDto> editFeedbackStatus(UUID boardId, UUID feedbackId, FeedbackStatus status);


    ApiResponse<Void> deleteFeedback(UUID boardId, UUID feedbackId);


}
