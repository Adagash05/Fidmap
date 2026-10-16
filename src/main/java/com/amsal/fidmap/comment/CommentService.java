package com.amsal.fidmap.comment;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRepository;
import com.amsal.fidmap.endUser.EndUserResolver;
import com.amsal.fidmap.exception.BoardNotFoundException;
import com.amsal.fidmap.notification.NotificationService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.exception.EndUserException;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final FeedbackPostRepository feedbackPostRepository;
    private final EndUserRepository endUserRepository;
    private final CommentMapper commentMapper;
    private final EndUserResolver endUserResolver;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ApiResponse<CommentDto> createComment(
            UUID feedbackPostId,
            AddComment addComment
    ) {

        FeedbackPost post =
                feedbackPostRepository.findFeedbackPostById(feedbackPostId);

        if (post == null) {
            throw new FeedbackPostNotFoundException(
                    "post is not found please try again later"
            );
        }

        requirePublicOrStaff(post);

        if (addComment == null || addComment.getEndUser() == null) {
            throw new IllegalArgumentException("Your name and email are required");
        }

        String message = addComment.getMessage() == null ? "" : addComment.getMessage().trim();

        if (message.isEmpty() || message.length() > 2000) {
            throw new IllegalArgumentException("A comment must be between 1 and 2000 characters");
        }

        planEntitlementService.requireActiveAccess(post.getWorkspace().getId());

        EndUser endUser = endUserResolver.resolve(
                post.getWorkspace(),
                addComment.getEndUser().getName(),
                addComment.getEndUser().getEmail()
        );

        Comment comment = new Comment();

        comment.setMessage(message);
        comment.setFeedbackPost(post);
        comment.setEndUser(endUser);
        comment.setWorkspace(post.getWorkspace());
        comment.setCreatedAt(LocalDate.now());

        Comment savedComment = commentRepository.save(comment);

        notificationService.follow(post, endUser);

        return ApiResponse.success("comment added", commentMapper.toCommentDto(savedComment));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CommentDto>> getAllCommentsByFeedbackPost(
            UUID feedbackPostId
    ) {

        FeedbackPost post =
                feedbackPostRepository.findFeedbackPostById(feedbackPostId);

        if (post == null) {
            throw new FeedbackPostNotFoundException(
                    "post is not found please try again later"
            );
        }

        requirePublicOrStaff(post);

        List<CommentDto> dtos =
                commentRepository
                        .findAllCommentByFeedbackPost(post)
                        .stream()
                        .map(commentMapper::toCommentDto)
                        .toList();

        return ApiResponse.success(
                "retrieve all comments",
                dtos
        );
    }

    @Override
    @Transactional
    public void deleteComment(
            UUID endUserId,
            UUID commentId
    ) {

        Comment comment =
                commentRepository.findCommentById(commentId);

        if (comment == null) {
            throw new FeedbackPostNotFoundException(
                    "comment is not found please try again later"
            );
        }

        EndUser endUser =
                endUserRepository.findEndUserById(endUserId);

        if (endUser == null) {
            throw new EndUserException(
                    "this user is not found"
            );
        }

        /*
         * Only the EndUser who owns the comment, or staff of the workspace
         * the comment belongs to, may delete it.
         */
        boolean owner = comment.getEndUser() != null
                && comment.getEndUser().getId().equals(endUserId);

        boolean staff = comment.getWorkspace() != null
                && workspaceAuthorizationService.isStaffOf(comment.getWorkspace().getId());

        if (!owner && !staff) {
            throw new EndUserException(
                    "you are not allowed to delete this comment"
            );
        }

        commentRepository.delete(comment);

    }

    private void requirePublicOrStaff(FeedbackPost post) {

        if (post.getBoard() != null
                && Boolean.FALSE.equals(post.getBoard().getIsPublic())
                && !workspaceAuthorizationService.isStaffOf(post.getWorkspace().getId())) {
            throw new FeedbackPostNotFoundException(
                    "post is not found please try again later"
            );
        }
    }
}
