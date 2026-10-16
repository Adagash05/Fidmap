package com.amsal.fidmap.invitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {

    Optional<TeamInvitation> findByTokenHash(String tokenHash);

    List<TeamInvitation> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);

    List<TeamInvitation> findByWorkspaceIdAndEmailIgnoreCaseAndStatus(UUID workspaceId, String email, InvitationStatus status);

    long countByWorkspaceIdAndStatus(UUID workspaceId, InvitationStatus status);
}
