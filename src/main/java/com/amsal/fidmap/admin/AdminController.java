package com.amsal.fidmap.admin;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.audit.AuditLog;
import com.amsal.fidmap.audit.AuditLogRepository;
import com.amsal.fidmap.audit.AuditService;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.user.Role;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Platform administration. SecurityConfig restricts /api/admin/** to SUPER_ADMIN;
 * each handler also checks the role (independent second check).
 * Workspace creation stays on the public registration flow (see docs).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    @PersistenceContext
    private EntityManager em;

    public record WorkspaceRow(UUID id, String name, String slug, String ownerEmail, long members,
                               String subscriptionStatus, String plan) {}

    public record AuditRow(UUID id, UUID workspaceId, UUID actorId, String action, String targetType,
                           String targetId, String detail, Instant createdAt) {}

    private void requireSuperAdmin() {
        UUID id = SecurityUtils.getCurrentUserIdOrNull();
        boolean ok = id != null && userRepository.findById(id)
                .map(u -> u.getRole() == Role.SUPER_ADMIN).orElse(false);
        if (!ok) {
            throw new AccessDeniedException("Super admin access required");
        }
    }

    @GetMapping("/workspaces")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PageResponse<WorkspaceRow>>> workspaces(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        requireSuperAdmin();
        var pageable = Paging.of(page, size, Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
        String like = Paging.likeContains(q);

        long total = em.createQuery("""
                SELECT COUNT(w) FROM Workspace w
                WHERE LOWER(w.name) LIKE :q ESCAPE '\\' OR LOWER(w.slug) LIKE :q ESCAPE '\\'""", Long.class)
                .setParameter("q", like).getSingleResult();

        List<Workspace> rows = em.createQuery("""
                SELECT w FROM Workspace w
                WHERE LOWER(w.name) LIKE :q ESCAPE '\\' OR LOWER(w.slug) LIKE :q ESCAPE '\\'
                ORDER BY w.name ASC, w.id ASC""", Workspace.class)
                .setParameter("q", like)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<WorkspaceRow> items = rows.stream().map(w -> {
            String owner = em.createQuery("""
                    SELECT u.email FROM User u WHERE u.workspace.id = :w AND u.role = :r ORDER BY u.email""", String.class)
                    .setParameter("w", w.getId()).setParameter("r", Role.OWNER)
                    .setMaxResults(1).getResultList().stream().findFirst().orElse(null);

            var sub = subscriptionRepository.findByWorkspaceId(w.getId());
            var state = PlanEntitlementService.evaluate(sub.orElse(null), Instant.now());

            return new WorkspaceRow(w.getId(), w.getName(), w.getSlug(), owner,
                    userRepository.countByWorkspaceId(w.getId()),
                    state.effectiveStatus() == null ? "NONE" : state.effectiveStatus().toString(),
                    sub.map(s -> String.valueOf(s.getPlan())).orElse(null));
        }).toList();

        auditService.record(null, SecurityUtils.getCurrentUserIdOrNull(), "ADMIN_LIST_WORKSPACES", "workspace", null,
                q == null ? null : "search=" + q);

        int totalPages = (int) Math.ceil(total / (double) pageable.getPageSize());

        return ResponseEntity.ok(ApiResponse.success("workspaces", new PageResponse<>(
                items, pageable.getPageNumber(), pageable.getPageSize(), total, totalPages,
                pageable.getPageNumber() + 1 < totalPages)));
    }

    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<PageResponse<AuditRow>>> audit(
            @RequestParam(required = false) UUID workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        requireSuperAdmin();
        var pageable = Paging.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));

        var result = workspaceId == null
                ? auditLogRepository.findAll(pageable)
                : auditLogRepository.findByWorkspaceId(workspaceId, pageable);

        return ResponseEntity.ok(ApiResponse.success("audit log", PageResponse.of(result,
                (AuditLog a) -> new AuditRow(a.getId(), a.getWorkspaceId(), a.getActorId(), a.getAction(),
                        a.getTargetType(), a.getTargetId(), a.getDetail(), a.getCreatedAt()))));
    }
}
