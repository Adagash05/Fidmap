package com.amsal.fidmap.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackFollowerRepository extends JpaRepository<FeedbackFollower, UUID> {

    boolean existsByFeedbackPostIdAndEndUserId(UUID feedbackPostId, UUID endUserId);

    Optional<FeedbackFollower> findByUnsubscribeToken(String token);

    @Query("""
            SELECT f FROM FeedbackFollower f
            JOIN FETCH f.endUser
            WHERE f.feedbackPost.id = :postId AND f.unsubscribedAt IS NULL
            """)
    List<FeedbackFollower> findActiveByPostId(@Param("postId") UUID postId);

    @Modifying
    @Query("DELETE FROM FeedbackFollower f WHERE f.feedbackPost.id = :postId")
    int deleteAllByPostId(@Param("postId") UUID postId);
}
