package com.amsal.fidmap.user;

import com.amsal.fidmap.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);


    User findUserById(UUID userId);

    boolean existsByEmail(String email);

    User findUserByEmail(String email);

    List<User> findAllByWorkspace(Workspace workspace);

    boolean existsByEmailAndWorkspace(String email, Workspace workspace);

    boolean existsByIdAndWorkspaceId(UUID userId, UUID workspaceId);

    boolean existsByEmailAndWorkspaceId(String email, UUID workspaceId);

    @Query("""
                SELECT COUNT(u)
                FROM User u
                JOIN u.workspace w
                WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
