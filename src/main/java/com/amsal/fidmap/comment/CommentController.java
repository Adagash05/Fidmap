package com.amsal.fidmap.comment;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{feedback-id}")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable("feedback-id") UUID feedbackPostId,
            @RequestBody AddComment addComment) {


        return ResponseEntity
                .status(CREATED)
                .body(commentService.createComment(feedbackPostId, addComment));

    }

    @GetMapping("/{feedback-id}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getAllCommentsByFeedbackPost(
            @PathVariable("feedback-id") UUID feedbackPostId
    ) {

        return ResponseEntity
                .status(OK)
                .body(commentService.getAllCommentsByFeedbackPost(feedbackPostId));
    }

    @DeleteMapping("end-user/{end-user-id}/comment/{comment-id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("end-user-id") UUID endUserId,
            @PathVariable("comment-id") UUID commentId
    ) {

        commentService.deleteComment(endUserId, commentId);

        return ResponseEntity.ok().build();
    }
}
