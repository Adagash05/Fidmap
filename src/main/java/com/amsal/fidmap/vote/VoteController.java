package com.amsal.fidmap.vote;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/{feedback-id}")
    public ResponseEntity<ApiResponse<VoteResponseDto>> toggleVote(
            @PathVariable("feedback-id") UUID feedbackId,
            @RequestBody VoteRequest request
    ) {

        VoteResponseDto response = voteService.toggleVote(
                feedbackId,
                request.getEndUserName(),
                request.getEndUserEmail()
                );

        return ResponseEntity.ok(ApiResponse.success("Vote toggled successfully", response));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<ApiResponse<List<BoardVoteStatusDto>>> getBoardVoteStatuses(
            @PathVariable UUID boardId,
            @RequestParam String endUserEmail
    ) {

        return ResponseEntity.ok(voteService.getBoardVoteStatuses(boardId, endUserEmail));
    }
}