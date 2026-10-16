package com.amsal.fidmap.security;

import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.user.Role;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central tenant-isolation guard.
 *
 * Every authenticated operation that touches workspace-owned data must call one
 * of these methods with the workspace id that OWNS THE RESOURCE (resolved from
 * the loaded entity, never from a client-supplied path/body value alone).
 *
 * Roles: OWNER, ADMIN and MEMBER are workspace staff. SUPER_ADMIN has no
 * implicit cross-tenant data access here; platform administration uses its own
 * explicitly protected endpoints. PARTNER users never get workspace access.
 */
@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {

    private static final Set<Role> STAFF_ROLES =
            EnumSet.of(Role.OWNER, Role.ADMIN, Role.MEMBER);

    private static final Set<Role> MANAGER_ROLES =
            EnumSet.of(Role.OWNER, Role.ADMIN);

    private final UserRepository userRepository;
    private final PlanEntitlementService planEntitlementService;

    /**
     * Requires the authenticated user to belong to the workspace.
     *
     * Used for workspace-level reads such as plan entitlements.
     */
    public void requireWorkspaceAccess(UUID workspaceId) {

        loadMember(workspaceId);
    }

    /**
     * Requires an authenticated OWNER/ADMIN/MEMBER of the workspace.
     */
    public User requireStaff(UUID workspaceId) {

        User user = loadMember(workspaceId);

        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new AccessDeniedException(
                    "You do not have permission to manage this workspace"
            );
        }

        return user;
    }

    /**
     * Requires an authenticated OWNER or ADMIN of the workspace.
     */
    public User requireManager(UUID workspaceId) {

        User user = loadMember(workspaceId);

        if (!MANAGER_ROLES.contains(user.getRole())) {
            throw new AccessDeniedException(
                    "Only a workspace owner or admin can do this"
            );
        }

        return user;
    }

    /**
     * Staff access AND an active trial/subscription. Use for every
     * mutation performed by team members.
     */
    public User requireStaffWrite(UUID workspaceId) {

        User user = requireStaff(workspaceId);

        planEntitlementService.requireActiveAccess(workspaceId);

        return user;
    }

    /**
     * Manager access AND an active trial/subscription.
     */
    public User requireManagerWrite(UUID workspaceId) {

        User user = requireManager(workspaceId);

        planEntitlementService.requireActiveAccess(workspaceId);

        return user;
    }

    /**
     * Returns true when the current request is an authenticated staff member
     * of the workspace. Never throws; used for public endpoints that expose
     * more to staff (e.g. private boards, draft changelog entries).
     */
    public boolean isStaffOf(UUID workspaceId) {

        if (workspaceId == null) {
            return false;
        }

        UUID userId = SecurityUtils.getCurrentUserIdOrNull();

        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .filter(user -> STAFF_ROLES.contains(user.getRole()))
                .filter(user -> user.getWorkspace() != null
                        && workspaceId.equals(user.getWorkspace().getId()))
                .isPresent();
    }

    /**
     * Requires the authenticated user to belong to the workspace
     * and be the workspace owner.
     *
     * Used for billing mutations.
     */
    public void requireBillingAccess(UUID workspaceId) {

        User user = loadMember(workspaceId);

        if (user.getRole() != Role.OWNER) {
            throw new AccessDeniedException(
                    "You do not have permission to manage billing"
            );
        }
    }

    /**
     * The authenticated user, loaded fresh from the database.
     */
    public User currentUser() {

        UUID userId = SecurityUtils.getCurrentUserIdOrNull();

        if (userId == null) {
            throw new AccessDeniedException("Authenticated user not found");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new AccessDeniedException("Authenticated user not found")
                );
    }

    private User loadMember(UUID workspaceId) {

        if (workspaceId == null) {
            throw new AccessDeniedException("Workspace ID is required");
        }

        User user = currentUser();

        if (user.getWorkspace() == null
                || !workspaceId.equals(user.getWorkspace().getId())) {
            throw new AccessDeniedException(
                    "You do not have access to this workspace"
            );
        }

        return user;
    }
}
