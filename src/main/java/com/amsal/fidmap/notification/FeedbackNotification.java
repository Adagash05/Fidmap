package com.amsal.fidmap.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per (request, recipient, event). The unique constraint is what
 * prevents the same event from ever producing two emails to one person.
 */
@Entity
@Table(
        name = "feedback_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_event",
                columnNames = {"feedback_post_id", "end_user_id", "event_key"}
        ),
        indexes = {
                @Index(name = "idx_notification_status", columnList = "status,next_attempt_at"),
                @Index(name = "idx_notification_workspace", columnList = "workspace_id,created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "feedback_post_id", nullable = false)
    private UUID feedbackPostId;

    @Column(name = "end_user_id", nullable = false)
    private UUID endUserId;

    @Column(name = "event_key", nullable = false, length = 120)
    private String eventKey;

    @Column(name = "subject", nullable = false, length = 300)
    private String subject;

    @Column(name = "headline", nullable = false, length = 300)
    private String headline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "provider_message_id", length = 120)
    private String providerMessageId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
