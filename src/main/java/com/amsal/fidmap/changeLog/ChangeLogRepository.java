package com.amsal.fidmap.changeLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, UUID> {

    @Query("""
            SELECT c
            FROM ChangeLog c
            WHERE c.workspace.id = :workspaceId
            """)
    List<ChangeLog> findAllChangeLogByWorkspace(
            @Param("workspaceId") UUID workspaceId
    );

    ChangeLog findChangeLogById(UUID changeLogId);

    @Query("""
            SELECT COUNT(c)
            FROM ChangeLog c
            JOIN c.workspace w
            WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}