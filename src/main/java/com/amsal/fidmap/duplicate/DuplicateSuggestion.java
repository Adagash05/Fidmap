package com.amsal.fidmap.duplicate;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A suggested duplicate pair. The pair is stored once (smaller id first) so a
 * dismissed pair never resurfaces. Nothing is merged until a human confirms.
 */
@Entity
@Table(name = "duplicate_suggestion",
        uniqueConstraints = @UniqueConstraint(name = "uk_dup_pair", columnNames = {"source_post_id", "target_post_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "source_post_id", nullable = false)
    private UUID sourcePostId;

    @Column(name = "target_post_id", nullable = false)
    private UUID targetPostId;

    private double similarity;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
