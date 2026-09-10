package com.amsal.fidmap.workspace;
import com.amsal.fidmap.board.Board;
import com.amsal.fidmap.changeLog.ChangeLog;
import com.amsal.fidmap.comment.Comment;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "workspaces",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workspace_slug",
                        columnNames = "slug"
                )
        }
)
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(nullable = false, unique = true, length = 63)
    private String slug;

    @ManyToMany
    @JoinTable(
            name = "workspace_end_users",
            joinColumns = @JoinColumn(name = "workspace_id"),
            inverseJoinColumns = @JoinColumn(name = "end_user_id")
    )
    private Set<EndUser> endUsers = new HashSet<>();

    @OneToMany(mappedBy = "workspace")
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "workspace")
    private List<Survey> surveys;

    @OneToMany(mappedBy = "workspace")
    private List<Comment> comments;

    @OneToMany(mappedBy = "workspace")
    private List<ChangeLog> changeLogs;

    @OneToMany(mappedBy = "workspace")
    private List<FeedbackPost> feedbackPosts;

    @OneToMany(mappedBy = "workspace")
    private List<User> user = new ArrayList<>();

//    @OneToMany(mappedBy = "workspace")
//    private Set<WorkspaceMembership> workspaceMemberships = new HashSet<>();

    public void addBoardToWorkspace(Board board) {
        board.setWorkspace(this);
        this.boards.add(board);

    }

    public void addUserToWorkspace(User user) {

        user.setWorkspace(this);
        this.user.add(user);

    }

}
