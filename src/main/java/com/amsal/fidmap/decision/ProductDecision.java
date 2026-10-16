package com.amsal.fidmap.decision;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.roadmapItem.RoadmapItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A recorded human decision about a request. Keeps a snapshot of the score and
 * evidence at decision time so the rationale stays auditable later.
 */
@Entity
@Table(name = "product_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_post_id", nullable = false)
    private FeedbackPost feedbackPost;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recommendation recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recommendation decidedAction;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    private int scoreSnapshot;

    @Enumerated(EnumType.STRING)
    private Confidence confidenceSnapshot;

    private String scoringVersion;

    @Column(columnDefinition = "TEXT")
    private String evidenceSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_item_id")
    private RoadmapItem roadmapItem;

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;
}
