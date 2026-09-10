package com.amsal.fidmap.endUser;

import com.amsal.fidmap.comment.Comment;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.vote.Vote;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "end_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String googleSubject;

    private String profilePicture;

    @Builder.Default
    private boolean emailVerified = false;

    @OneToMany(mappedBy = "endUser")
    private List<Comment> comments;

    @OneToMany(mappedBy = "endUser")
    private List<FeedbackPost> feedbackPosts;

    @OneToMany(mappedBy = "endUser")
    private List<Vote> votes;

//    @JsonBackReference
    @ManyToMany(mappedBy = "endUsers")
    private List<Workspace> workspaces;

}
