package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFeedback {

    private String title;

    private String description;

    private EndUserRequest endUser; //todo
}
