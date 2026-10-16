package com.amsal.fidmap.notification;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FeedbackNotificationRepository notificationRepository;
    private final WorkspaceAuthorizationService authorization;

    public record NotificationView(UUID id, UUID feedbackPostId, String subject, String headline,
                                   NotificationStatus status, int attempts, String lastError,
                                   Instant createdAt, Instant sentAt) {}

    /** Public one-click unsubscribe (token is an unguessable per-follower secret). */
    @PostMapping("/api/public/notifications/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@RequestBody Map<String, String> body) {

        boolean ok = notificationService.unsubscribe(body == null ? null : body.get("token"));

        return ok
                ? ResponseEntity.ok(ApiResponse.success("You have been unsubscribed", null))
                : ResponseEntity.status(404).body(ApiResponse.error("This unsubscribe link is not valid"));
    }

    /** Staff delivery log for the workspace. */
    @GetMapping("/api/notifications/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<PageResponse<NotificationView>>> log(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        authorization.requireStaff(workspaceId);

        var pageable = Paging.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));

        return ResponseEntity.ok(ApiResponse.success("notifications",
                PageResponse.of(notificationRepository.findByWorkspaceId(workspaceId, pageable),
                        n -> new NotificationView(n.getId(), n.getFeedbackPostId(), n.getSubject(), n.getHeadline(),
                                n.getStatus(), n.getAttempts(), n.getLastError(), n.getCreatedAt(), n.getSentAt()))));
    }

    /** Re-queues a failed / given-up notification. */
    @PostMapping("/api/notifications/{id}/retry")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> retry(@PathVariable UUID id) {

        FeedbackNotification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        authorization.requireStaffWrite(n.getWorkspaceId());

        if (n.getStatus() != NotificationStatus.FAILED && n.getStatus() != NotificationStatus.GAVE_UP) {
            throw new IllegalStateException("Only failed notifications can be retried");
        }

        n.setStatus(NotificationStatus.PENDING);
        n.setAttempts(0);
        n.setNextAttemptAt(Instant.now());
        notificationRepository.save(n);

        return ResponseEntity.ok(ApiResponse.success("queued for retry", null));
    }
}
