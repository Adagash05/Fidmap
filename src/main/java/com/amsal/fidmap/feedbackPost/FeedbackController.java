package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback/board")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/{board-id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> createPost(
            @RequestBody AddFeedback feedback,
            @PathVariable("board-id") UUID boardId
    ) {

        return ResponseEntity
                .status(CREATED)
                .body(feedbackService.createFeedbackPost(feedback, boardId));

    }

    @GetMapping("/{board-id}")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getAllFeedbackByBoard(@PathVariable("board-id") UUID boardId) {

        return ResponseEntity
                .status(OK)
                .body(feedbackService.getAllFeedbackByBoard(boardId));

    }

    @GetMapping("/{board-id}/feedback/{feedback-id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> getFeedbackById(
            @PathVariable("board-id") UUID boardId,
            @PathVariable("feedback-id") UUID feedbackId
    ) {


        return ResponseEntity
                .status(OK)
                .body(feedbackService.getFeedbackById(boardId, feedbackId));
    }


    @PatchMapping("/{feedback-id}/roadmap/{roadmap-item-id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> assignFeedbackToRoadmapItem(
            @PathVariable("feedback-id") UUID feedbackId,
            @PathVariable("roadmap-item-id") UUID roadmapItemId
    ) {

        return ResponseEntity.ok(
                feedbackService.assignFeedbackToRoadmapItem(
                        feedbackId,
                        roadmapItemId
                )
        );
    }



    @PutMapping("/{board-id}/feedback/{feedback-id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> editFeedback(
            @PathVariable("board-id") UUID boardId,
            @PathVariable("feedback-id") UUID feedbackId,
            @RequestBody UpdateFeedbackRequest request
    ) {
        return ResponseEntity
                .status(OK)
                .body(feedbackService.editFeedback(boardId, feedbackId, request));


    }

    @PatchMapping("/{board-id}/feedback/{feedback-id}/status")
    public ResponseEntity<ApiResponse<FeedbackDto>> editFeedbackStatus(
            @PathVariable("board-id") UUID boardId,
            @PathVariable("feedback-id") UUID feedbackId,
            @RequestParam("status") FeedbackStatus status) {

        return ResponseEntity
                .status(OK)
                .body(feedbackService.editFeedbackStatus(boardId, feedbackId, status));

    }

    @DeleteMapping("/{board-id}/feedback/{feedback-id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(
            @PathVariable("board-id") UUID boardId,
            @PathVariable("feedback-id") UUID feedbackId
    ) {
        return ResponseEntity
                .status(OK)
                .body(feedbackService.deleteFeedback(boardId, feedbackId));
    }
}
