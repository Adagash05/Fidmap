package com.amsal.fidmap.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedbackNotificationRepository extends JpaRepository<FeedbackNotification, UUID> {

    boolean existsByFeedbackPostIdAndEndUserIdAndEventKey(
            UUID feedbackPostId, UUID endUserId, String eventKey);

    @Query("""
            SELECT n FROM FeedbackNotification n
            WHERE n.status IN (com.amsal.fidmap.notification.NotificationStatus.PENDING,
                               com.amsal.fidmap.notification.NotificationStatus.FAILED)
              AND (n.nextAttemptAt IS NULL OR n.nextAttemptAt <= :now)
            ORDER BY n.createdAt ASC
            """)
    List<FeedbackNotification> findDue(@Param("now") Instant now, Pageable pageable);

    Page<FeedbackNotification> findByWorkspaceId(UUID workspaceId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM FeedbackNotification n WHERE n.feedbackPostId = :postId")
    int deleteAllByPostId(@Param("postId") UUID postId);
}
