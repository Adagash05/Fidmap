package com.amsal.fidmap.user;

import com.amsal.fidmap.workspace.WorkspaceDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFirstUserRequest {

    private AddFirstUser firstUser;

    private WorkspaceDto dto;
}
