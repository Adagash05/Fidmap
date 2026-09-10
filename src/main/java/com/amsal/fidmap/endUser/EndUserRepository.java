package com.amsal.fidmap.endUser;

import com.amsal.fidmap.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EndUserRepository extends JpaRepository<EndUser, UUID> {

    Optional<EndUser> findByGoogleSubject(String googleSubject);

    Optional<EndUser> findByEmail(String email);

    EndUser findEndUserByEmail(String email);

    EndUser findEndUserById(UUID endUserId);

    boolean existsByEmail(String email);

    @Query("""
                SELECT COUNT(e)
                FROM EndUser e
                JOIN e.workspaces w
                WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
//    EndUser findEndUserByIdAndWorkspace(Long endUserId, Workspace workspace);
}
