package com.amsal.fidmap.notification;

import com.amsal.fidmap.config.FidmapConfig;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Customer feedback loop.
 *
 * - Followers: people who submitted / voted / commented follow a request.
 * - Events: a status change or roadmap progress records ONE notification per
 *   (request, person, event). The unique constraint + existence check make the
 *   same event idempotent.
 * - Delivery: a scheduled dispatcher hands due notifications to the email
 *   provider. A row becomes SENT only after the provider returned a message id.
 *   Failures are retried with backoff and finally marked GAVE_UP.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 50;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FeedbackFollowerRepository followerRepository;
    private final FeedbackNotificationRepository notificationRepository;
    private final EmailService emailService;
    private final FidmapConfig fidmapConfig;

    /** Idempotent. Safe to call on every submit / vote / comment. */
    @Transactional
    public void follow(FeedbackPost post, EndUser endUser) {

        if (post == null || endUser == null || post.getId() == null || endUser.getId() == null) {
            return;
        }

        if (followerRepository.existsByFeedbackPostIdAndEndUserId(post.getId(), endUser.getId())) {
            return;
        }

        followerRepository.save(
                FeedbackFollower.builder()
                        .feedbackPost(post)
                        .endUser(endUser)
                        .unsubscribeToken(newToken())
                        .createdAt(Instant.now())
                        .build()
        );
    }

    /**
     * Records one pending notification per active follower for this event.
     *
     * @return number of NEW notifications recorded (0 on a repeated event)
     */
    @Transactional
    public int queueEvent(FeedbackPost post, String eventKey, String subject, String headline) {

        if (post == null || post.getWorkspace() == null) {
            return 0;
        }

        int created = 0;

        for (FeedbackFollower follower : followerRepository.findActiveByPostId(post.getId())) {

            UUID endUserId = follower.getEndUser().getId();

            if (notificationRepository.existsByFeedbackPostIdAndEndUserIdAndEventKey(
                    post.getId(), endUserId, eventKey)) {
                continue;
            }

            notificationRepository.save(
                    FeedbackNotification.builder()
                            .workspaceId(post.getWorkspace().getId())
                            .feedbackPostId(post.getId())
                            .endUserId(endUserId)
                            .eventKey(eventKey)
                            .subject(truncate(subject, 300))
                            .headline(truncate(headline, 300))
                            .status(NotificationStatus.PENDING)
                            .attempts(0)
                            .createdAt(Instant.now())
                            .nextAttemptAt(Instant.now())
                            .build()
            );

            created++;
        }

        return created;
    }

    @Transactional
    public boolean unsubscribe(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        return followerRepository.findByUnsubscribeToken(token)
                .map(follower -> {
                    if (follower.getUnsubscribedAt() == null) {
                        follower.setUnsubscribedAt(Instant.now());
                    }
                    return true;
                })
                .orElse(false);
    }

    @Scheduled(fixedDelayString = "${fidmap.notifications.dispatch-interval-ms:60000}")
    public void dispatchScheduled() {

        try {
            dispatchDue();
        } catch (RuntimeException e) {
            log.warn("Notification dispatch run failed: {}", e.getMessage());
        }
    }

    /**
     * Sends due notifications. Each notification is isolated so one bad
     * recipient never blocks the rest.
     *
     * @return number of notifications the provider accepted in this run
     */
    public int dispatchDue() {

        List<FeedbackNotification> due =
                notificationRepository.findDue(Instant.now(), PageRequest.of(0, BATCH_SIZE));

        int sent = 0;

        for (FeedbackNotification notification : due) {

            if (sendOne(notification.getId())) {
                sent++;
            }
        }

        return sent;
    }

    /**
     * Not annotated @Transactional on purpose: it is called from the scheduler
     * inside this same bean (a self-call would bypass the proxy), so the state
     * change is persisted with an explicit save and no DB transaction is held
     * open across the network call to the email provider.
     */
    public boolean sendOne(UUID notificationId) {

        FeedbackNotification notification = notificationRepository.findById(notificationId).orElse(null);

        if (notification == null
                || notification.getStatus() == NotificationStatus.SENT
                || notification.getStatus() == NotificationStatus.GAVE_UP) {
            return false;
        }

        boolean sent = attempt(notification);

        notificationRepository.save(notification);

        return sent;
    }

    private boolean attempt(FeedbackNotification notification) {

        FeedbackFollower follower = followerRepository
                .findActiveByPostId(notification.getFeedbackPostId())
                .stream()
                .filter(f -> f.getEndUser().getId().equals(notification.getEndUserId()))
                .findFirst()
                .orElse(null);

        if (follower == null) {
            // Unsubscribed (or post removed) after the event was recorded.
            notification.setStatus(NotificationStatus.GAVE_UP);
            notification.setLastError("Recipient is no longer subscribed");
            return false;
        }

        notification.setAttempts(notification.getAttempts() + 1);

        try {

            String messageId = emailService.send(
                    follower.getEndUser().getEmail(),
                    notification.getSubject(),
                    renderHtml(notification, follower)
            );

            notification.setProviderMessageId(messageId);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setLastError(null);
            notification.setNextAttemptAt(null);

            return true;

        } catch (RuntimeException e) {

            notification.setLastError(truncate(e.getMessage(), 500));

            if (notification.getAttempts() >= MAX_ATTEMPTS) {
                notification.setStatus(NotificationStatus.GAVE_UP);
                notification.setNextAttemptAt(null);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setNextAttemptAt(
                        Instant.now().plus(backoff(notification.getAttempts()))
                );
            }

            return false;
        }
    }

    static Duration backoff(int attempts) {
        // 2, 4, 8, 16 minutes ...
        return Duration.ofMinutes(1L << Math.min(attempts, 6));
    }

    private String renderHtml(FeedbackNotification notification, FeedbackFollower follower) {

        String unsubscribeUrl = fidmapConfig.getFrontendUrl()
                + "/unsubscribe?token=" + follower.getUnsubscribeToken();

        return """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; color: #111827;">
                <div style="max-width: 560px; margin: 32px auto; padding: 24px;">
                  <h2 style="margin: 0 0 16px;">%s</h2>
                  <p style="margin: 0 0 24px;">You are receiving this because you took part in this request.</p>
                  <p style="font-size: 12px; color: #6b7280;">
                    <a href="%s">Stop receiving updates about this request</a>
                  </p>
                </div></body></html>
                """.formatted(
                EmailService.escape(notification.getHeadline()),
                unsubscribeUrl
        );
    }

    private static String newToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String truncate(String value, int max) {

        if (value == null) {
            return "";
        }

        return value.length() <= max ? value : value.substring(0, max);
    }
}
