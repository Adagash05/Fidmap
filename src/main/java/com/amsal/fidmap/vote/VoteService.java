package com.amsal.fidmap.vote;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRepository;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final FeedbackPostRepository feedbackPostRepository;
    private final EndUserRepository endUserRepository;

    @Transactional
    public ApiResponse<VoteDto> toggleVote(UUID feedbackPostId, VoteRequest request) {

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackPostId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("post not found");
        }

        EndUser endUser = endUserRepository.findEndUserByEmail(request.getEndUserEmail());
        if (endUser == null) {
            endUser = new EndUser();
            endUser.setName(request.getEndUserName());
            endUser.setEmail(request.getEndUserEmail());
            endUser.setWorkspaces(List.of(post.getWorkspace()));
            endUser = endUserRepository.save(endUser);
        }

        Optional<Vote> existingVote = voteRepository.findByFeedbackPostAndEndUser(post, endUser);

        boolean votedNow;
        if (existingVote.isPresent()) {
            voteRepository.delete(existingVote.get());
            post.setVoteCount(Math.max(0, post.getVoteCount() - 1));
            votedNow = false;
        } else {
            Vote vote = new Vote();
            vote.setFeedbackPost(post);
            vote.setEndUser(endUser);
            voteRepository.save(vote);
            post.setVoteCount(post.getVoteCount() + 1);
            votedNow = true;
        }

        feedbackPostRepository.save(post);

        VoteDto dto = new VoteDto();
        dto.setFeedbackPostId(post.getId());
        dto.setVoteCount(post.getVoteCount());
        dto.setVotedByCurrentEndUser(votedNow);

        return ApiResponse.success(votedNow ? "vote added" : "vote removed", dto);
    }

    @Transactional(readOnly = true)
    public ApiResponse<VoteDto> getVoteStatus(UUID feedbackPostId, String endUserEmail) {

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackPostId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("post not found");
        }

        boolean voted = false;
        if (endUserEmail != null) {
            EndUser endUser = endUserRepository.findEndUserByEmail(endUserEmail);
            if (endUser != null) {
                voted = voteRepository.findByFeedbackPostAndEndUser(post, endUser).isPresent();
            }
        }

        VoteDto dto = new VoteDto();
        dto.setFeedbackPostId(post.getId());
        dto.setVoteCount(post.getVoteCount());
        dto.setVotedByCurrentEndUser(voted);

        return ApiResponse.success("vote status", dto);
    }


    @Transactional
    public VoteResponseDto toggleVote(UUID feedbackId, String endUserName, String endUserEmail) {

        FeedbackPost feedbackPost = feedbackPostRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Feedback post not found with id: " + feedbackId));

        EndUser endUser = endUserRepository.findByEmail(endUserEmail)
                .orElseGet(() -> {

                    EndUser newEndUser = EndUser.builder()
                            .name(endUserName)
                            .email(endUserEmail)
                            .build();

                    return endUserRepository.save(newEndUser);
                });

        var existingVote = voteRepository.findByFeedbackPostIdAndEndUserId(
                feedbackPost.getId(),
                endUser.getId()
        );

        if (existingVote.isPresent()) {

            voteRepository.delete(existingVote.get());

        } else {

            Vote vote = new Vote();

            vote.setFeedbackPost(feedbackPost);
            vote.setEndUser(endUser);

            voteRepository.save(vote);
        }

        /*
         * Vote is the source of truth.
         * Recalculate the count after the insert/delete
         * rather than blindly incrementing/decrementing
         * FeedbackPost.voteCount.
         */
        long voteCount = voteRepository.countByFeedbackPostId(feedbackPost.getId());

        feedbackPost.setVoteCount(voteCount);

        feedbackPostRepository.save(feedbackPost);

        boolean votedByCurrentEndUser = voteRepository.findByFeedbackPostIdAndEndUserId(
                                feedbackPost.getId(),
                                endUser.getId()
                        )
                        .isPresent();

        return new VoteResponseDto(
                feedbackPost.getId(),
                voteCount,
                votedByCurrentEndUser
        );
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BoardVoteStatusDto>> getBoardVoteStatuses(UUID boardId, String endUserEmail) {

        List<BoardVoteStatusDto> statuses = voteRepository.findVoteStatusesByBoardId(boardId, endUserEmail);

        return ApiResponse.success("Board vote statuses", statuses);
    }
}
