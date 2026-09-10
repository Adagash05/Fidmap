package com.amsal.fidmap.comment;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRepository;
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

        /*
         * Find the EndUser by email.
         *
         * If the visitor already exists, reuse the existing
         * EndUser instead of creating another one.
         *
         * If the visitor does not exist, create a new EndUser.
         */
        EndUser endUser =
                endUserRepository.findEndUserByEmail(
                        addComment.getEndUser().getEmail()
                );

        if (endUser == null) {

            endUser = new EndUser();

            endUser.setName(
                    addComment.getEndUser().getName()
            );

            endUser.setEmail(
                    addComment.getEndUser().getEmail()
            );

            endUser = endUserRepository.save(endUser);

        } else {

            /*
             * Keep the existing EndUser.
             *
             * Do not create another EndUser with the same email.
             */
        }

        Comment comment = new Comment();

        comment.setMessage(addComment.getMessage());
        comment.setFeedbackPost(post);
        comment.setEndUser(endUser);
        comment.setWorkspace(post.getWorkspace());
        comment.setCreatedAt(LocalDate.now());

        Comment savedComment =
                commentRepository.save(comment);

        CommentDto dto =
                commentMapper.toCommentDto(savedComment);

        return ApiResponse.success(
                "comment added",
                dto
        );
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
         * Only the EndUser who owns the comment should be able
         * to delete it.
         */
        if (!comment.getEndUser().getId().equals(endUserId)) {
            throw new EndUserException(
                    "you are not allowed to delete this comment"
            );
        }

        commentRepository.delete(comment);

    }
}

