//package com.amsal.fidmap.workSpaceMembership;
//
//import com.amsal.fidmap.exception.UserNotFoundException;
//import com.amsal.fidmap.exception.WorkspaceMembershipNotFoundException;
//import com.amsal.fidmap.exception.WorkspaceNotFoundException;
//import com.amsal.fidmap.security.SecurityUtils;
//import com.amsal.fidmap.user.User;
//import com.amsal.fidmap.user.UserRepository;
//import com.amsal.fidmap.workspace.Workspace;
//import com.amsal.fidmap.workspace.WorkspaceRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class WorkspaceMemberShipService implements IWorkspaceMembershipService {
//
//    private final UserRepository userRepository;
//    private final WorkspaceRepository workspaceRepository;
//    private final WorkspaceMembershipRepository workspaceMembershipRepository;
//
//
//    @Transactional
//    public void addUserToWorkspaceMembership(UUID userId, UUID workspaceId) {
//
//        User user = userRepository.findUserById(userId);
//        if (user == null) {
//            throw new UserNotFoundException("user not found");
//        }
//
//        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
//        if (workspace == null) {
//            throw new WorkspaceNotFoundException("workspace is not found");
//        }
//
//        boolean membership = workspaceMembershipRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
//        if (membership) {
//            throw new WorkspaceNotFoundException("this user already exists in this workspace");
//        }
//
//        WorkspaceMembership newMembership = new WorkspaceMembership();
////        newMembership.setUser(user);
////        newMembership.setWorkspace(workspace);
//
//        workspaceMembershipRepository.save(newMembership);
//
//    }
//
//    @Override
//    public WorkspaceMembership getMembership(UUID userId, UUID workspaceId) {
//
//        return workspaceMembershipRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
//                .orElseThrow(() ->
//                        new WorkspaceMembershipNotFoundException("User is not a member of this workspace")
//                );
//    }
//
//    @Override
//    public boolean isMember(UUID userId, UUID workspaceId) {
//
//        return workspaceMembershipRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
//    }
//
//    @Override
//    public boolean hasRole(UUID userId, UUID workspaceId, WorkspaceRole role) {
//
//        return workspaceMembershipRepository.existsByUserIdAndWorkspaceIdAndRole(userId, workspaceId, role);
//    }
//}
