package com.amsal.fidmap.comment;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface ICommentService {

    ApiResponse<CommentDto> createComment(UUID feedbackPostId, AddComment addComment);

    ApiResponse<List<CommentDto>> getAllCommentsByFeedbackPost(UUID feedbackPostId);

    void deleteComment(UUID endUserId, UUID commentId);




}
