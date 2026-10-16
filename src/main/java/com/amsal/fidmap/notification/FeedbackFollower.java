package com.amsal.fidmap.notification;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * An end user who asked for, voted for or commented on a request and may be
 * told when it progresses. Following is implicit (the person took part), and
 * every email carries a one-click unsubscribe link which sets unsubscribedAt.
 */
@Entity
@Table(
        name = "feedback_followers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_follower_post_user",
                columnNames = {"feedback_post_id", "end_user_id"}
        ),
        indexes = @Index(name = "idx_follower_token", columnList = "unsubscribe_token")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackFollower {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_post_id", nullable = false)
    private FeedbackPost feedbackPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "end_user_id", nullable = false)
    private EndUser endUser;

    @Column(name = "unsubscribe_token", nullable = false, unique = true, length = 64)
    private String unsubscribeToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;
}
