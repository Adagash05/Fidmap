package com.amsal.fidmap.decision;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** Human-provided scoring inputs for one request (1-5 each, all optional). */
@Entity
@Table(name = "feedback_priority_input",
        uniqueConstraints = @UniqueConstraint(name = "uk_priority_input_post", columnNames = "feedback_post_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackPriorityInput {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "feedback_post_id", nullable = false)
    private UUID feedbackPostId;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    private Integer urgency;
    private Integer strategic;
    private Integer effort;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
