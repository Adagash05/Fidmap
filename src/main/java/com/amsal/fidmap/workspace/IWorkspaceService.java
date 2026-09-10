package com.amsal.fidmap.workspace;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.user.AddUserRequest;
import com.amsal.fidmap.user.User;

import java.util.UUID;

public interface IWorkspaceService {
    ApiResponse<WorkspaceDto> createWorkspace(WorkspaceDto workspaceDto, User user);
    ApiResponse<WorkspaceDto> getWorkspace(UUID workspaceId); // NEW
}