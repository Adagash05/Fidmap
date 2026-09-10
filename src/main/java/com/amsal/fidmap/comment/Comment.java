package com.amsal.fidmap.comment;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String message;

    private Long votes = 0L;

    @CreatedDate
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "end_user_id")
    private EndUser endUser;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @NotNull(
            message = "this cannot be null"
    )
    @ManyToOne
    @JoinColumn(name = "feed_back_post_id")
    private FeedbackPost feedbackPost;


}
