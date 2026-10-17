package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.board.Board;
import com.amsal.fidmap.comment.Comment;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.vote.Vote;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class FeedbackPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

//    @Enumerated(EnumType.STRING)
//    private Type type;
    @CreatedDate
    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;

    private Long voteCount = 0L;

    // Optional business-impact inputs (not used by the scoring engine).
    // Boxed + nullable + DB default so that (a) `ddl-auto: update` can add the
    // columns to a table that already has rows (a NOT NULL column without a
    // default cannot be added to a populated table; the column then never
    // exists and EVERY feedback_post query fails with HTTP 500) and (b) rows
    // that hold NULL still hydrate instead of throwing on a primitive.
    @Column(columnDefinition = "integer default 0")
    private Integer impactScore = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer payingCustomerCount = 0;

    @Column(precision = 15, scale = 2, columnDefinition = "numeric(15,2) default 0")
    private BigDecimal affectedMrr = BigDecimal.ZERO;

    @Column(columnDefinition = "integer default 0")
    private Integer salesOpportunityCount = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer churnRiskCount = 0;

    @Column(columnDefinition = "integer default 3")
    private Integer estimatedEffort = 3; // 1 = very easy, 5 = very hard

    @Column(columnDefinition = "integer default 0")
    private Integer demandGrowth = 0;


    //not null
    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "end_user_id")
    private EndUser endUser;

    @ManyToOne
    @JoinColumn(name = "road_map_item_id")
    private RoadmapItem roadMapItem;

    @OneToMany(mappedBy = "feedbackPost",fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "feedbackPost")
    private List<Vote> vote;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    /**
     * Set when this request was confirmed as a duplicate and merged into
     * another request. The row, its comments and its history are preserved;
     * its votes are re-pointed (de-duplicated) onto the canonical request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_into_id")
    private FeedbackPost mergedInto;

    @Column(name = "merged_at")
    private Instant mergedAt;

}
