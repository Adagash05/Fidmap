package com.amsal.fidmap.comment;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllCommentByFeedbackPost(FeedbackPost feedbackPost);

    Comment findCommentById(UUID commentId);
}
