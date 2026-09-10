package com.amsal.fidmap.comment;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRequest;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddComment {

    private String message;

    private EndUserRequest endUser;

}
