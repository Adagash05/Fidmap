package com.amsal.fidmap.security;

import com.amsal.fidmap.user.Role;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {

    private final UserRepository userRepository;

    /**
     * Requires the authenticated user to belong to the workspace.
     *
     * Used for workspace-level reads such as plan entitlements.
     */
    public void requireWorkspaceAccess(UUID workspaceId) {

        if (workspaceId == null) {
            throw new AccessDeniedException("Workspace ID is required");
        }

        UUID userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new AccessDeniedException("Authenticated user not found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user not found"
                        )
                );

        if (!userRepository.existsByIdAndWorkspaceId(
                userId,
                workspaceId
        )) {
            throw new AccessDeniedException(
                    "You do not have access to this workspace"
            );
        }
    }

    /**
     * Requires the authenticated user to belong to the workspace
     * and be the workspace owner.
     *
     * Used for billing mutations.
     */
    public void requireBillingAccess(UUID workspaceId) {

        requireWorkspaceAccess(workspaceId);

        UUID userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user not found"
                        )
                );

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException(
                    "You do not have permission to manage billing"
            );
        }
    }
}