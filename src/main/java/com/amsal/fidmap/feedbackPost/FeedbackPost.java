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

}
