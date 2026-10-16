package com.amsal.fidmap.workspace;

import com.amsal.fidmap.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Workspace findWorkspaceById(UUID workspaceId);

    Optional<Workspace> findBySlug(String slug);

    boolean existsByUser(User user); //todo

    /**
     * Idempotently links an end user to a workspace through the owning side of
     * the workspace_end_users join table without loading the whole collection.
     */
    @Modifying
    @Query(value = """
            INSERT INTO workspace_end_users (workspace_id, end_user_id)
            VALUES (:workspaceId, :endUserId)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int linkEndUser(
            @Param("workspaceId") UUID workspaceId,
            @Param("endUserId") UUID endUserId
    );

}
