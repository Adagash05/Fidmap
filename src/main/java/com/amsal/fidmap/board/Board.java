package com.amsal.fidmap.board;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String description;

    private String slug;

    private Boolean isPublic = true;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<FeedbackPost> feedbackPosts = new ArrayList<>();

   @ManyToOne
   @JoinColumn(name = "workspace_id")
    private Workspace workspace;

   public void addFeedbackPost(FeedbackPost post) {
       post.setBoard(this);
       this.feedbackPosts.add(post);
   }





}
