package com.amsal.fidmap.vote;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.board.Board;
import com.amsal.fidmap.board.BoardRepository;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserResolver;
import com.amsal.fidmap.exception.BoardNotFoundException;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.notification.NotificationService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final FeedbackPostRepository feedbackPostRepository;
    private final BoardRepository boardRepository;
    private final EndUserResolver endUserResolver;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final NotificationService notificationService;

    /**
     * Toggles one vote per (request, end user). A vote on a request that was
     * merged into another request counts on the canonical request, and the
     * unique (post, end user) constraint plus recounting from the votes table
     * guarantee a person can never be counted twice.
     */
    @Transactional
    public VoteResponseDto toggleVote(UUID feedbackId, String endUserName, String endUserEmail) {

        FeedbackPost requested = feedbackPostRepository.findFeedbackPostById(feedbackId);

        if (requested == null) {
            throw new FeedbackPostNotFoundException("Feedback post not found");
        }

        FeedbackPost post = requested.getMergedInto() != null ? requested.getMergedInto() : requested;

        requirePublicOrStaff(post.getBoard());

        planEntitlementService.requireActiveAccess(post.getWorkspace().getId());

        EndUser endUser = endUserResolver.resolve(post.getWorkspace(), endUserName, endUserEmail);

        var existing = voteRepository.findByFeedbackPostIdAndEndUserId(post.getId(), endUser.getId());

        if (existing.isPresent()) {

            voteRepository.delete(existing.get());

        } else {

            Vote vote = new Vote();
            vote.setFeedbackPost(post);
            vote.setEndUser(endUser);
            vote.setCreatedAt(Instant.now());
            voteRepository.save(vote);

            notificationService.follow(post, endUser);
        }

        voteRepository.flush();

        // Vote rows are the source of truth; never increment/decrement blindly.
        long voteCount = voteRepository.countByFeedbackPostId(post.getId());

        post.setVoteCount(voteCount);
        feedbackPostRepository.save(post);

        boolean votedNow = voteRepository
                .findByFeedbackPostIdAndEndUserId(post.getId(), endUser.getId())
                .isPresent();

        return new VoteResponseDto(post.getId(), voteCount, votedNow);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BoardVoteStatusDto>> getBoardVoteStatuses(UUID boardId, String endUserEmail) {

        Board board = boardRepository.findBoardById(boardId);

        if (board == null) {
            throw new BoardNotFoundException("board does not exist");
        }

        requirePublicOrStaff(board);

        String normalized = endUserEmail == null ? "" : endUserEmail.trim().toLowerCase();

        List<BoardVoteStatusDto> statuses = voteRepository.findVoteStatusesByBoardId(boardId, normalized);

        return ApiResponse.success("Board vote statuses", statuses);
    }

    private void requirePublicOrStaff(Board board) {

        if (board != null
                && Boolean.FALSE.equals(board.getIsPublic())
                && !workspaceAuthorizationService.isStaffOf(board.getWorkspace().getId())) {
            throw new BoardNotFoundException("board does not exist");
        }
    }
}
