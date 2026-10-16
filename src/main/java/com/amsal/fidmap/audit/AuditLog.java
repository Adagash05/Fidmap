package com.amsal.fidmap.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** Append-only record of sensitive actions. Never stores secrets, tokens or message bodies. */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_workspace", columnList = "workspace_id,created_at"),
        @Index(name = "idx_audit_created", columnList = "created_at")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(name = "target_type", length = 40)
    private String targetType;

    @Column(name = "target_id", length = 64)
    private String targetId;

    @Column(length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
