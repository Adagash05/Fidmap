package com.amsal.fidmap.board;

import com.amsal.fidmap.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findAllBoardByWorkspace(Workspace workspace);

    Board findBoardById(UUID boardId);

    void deleteBoardById(UUID boardId);

    List<Board> findAllByWorkspaceIdAndIsPublicTrue(UUID workspaceId);

    @Query("""
                SELECT COUNT(b)
                FROM Board b
                JOIN b.workspace w
                WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}