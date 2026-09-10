//package com.amsal.fidmap.workSpaceMembership;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
//
//    boolean existsByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
//
//
//    Optional<WorkspaceMembership> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
//
//    boolean existsByUserIdAndWorkspaceIdAndRole(UUID userId, UUID workspaceId, WorkspaceRole role);
//}
////