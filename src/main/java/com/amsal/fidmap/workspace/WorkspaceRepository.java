package com.amsal.fidmap.workspace;

import com.amsal.fidmap.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Workspace findWorkspaceById(UUID workspaceId);

    Optional<Workspace> findBySlug(String slug);

    boolean existsByUser(User user); //todo

}
