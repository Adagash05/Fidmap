package com.amsal.fidmap.vote;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "votes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"feedback_post_id", "end_user_id"})
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(
            message = "this cannot be null"
    )
    @ManyToOne
    @JoinColumn(name = "feedback_post_id")
    private FeedbackPost feedbackPost;

    @ManyToOne
    @JoinColumn(name = "end_user_id")
    private EndUser endUser;

    //voteRepository.countByFeedbackPostId(10L);
}
