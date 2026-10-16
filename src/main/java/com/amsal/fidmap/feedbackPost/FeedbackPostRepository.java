package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedbackPostRepository extends JpaRepository<FeedbackPost, UUID> {

    /** Legacy unpaged lookup; merged (duplicate) requests are hidden. */
    @Query("SELECT f FROM FeedbackPost f WHERE f.board = :board AND f.mergedInto IS NULL")
    List<FeedbackPost> findAllByBoard(@Param("board") Board board);

    FeedbackPost findFeedbackPostById(UUID feedbackId);

    @Query("""
                SELECT COUNT(f)
                FROM FeedbackPost f
                JOIN f.workspace w
                WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    /** All requests of a workspace that are still canonical (not merged away). */
    @Query("SELECT f FROM FeedbackPost f WHERE f.workspace.id = :workspaceId AND f.mergedInto IS NULL")
    List<FeedbackPost> findCanonicalByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    List<FeedbackPost> findByWorkspaceId(UUID workspaceId);

    @Query("""
            SELECT f FROM FeedbackPost f
            WHERE f.board.id = :boardId
              AND f.mergedInto IS NULL
              AND (:status IS NULL OR f.status = :status)
              AND LOWER(f.title) LIKE :pattern ESCAPE '\\'
            """)
    Page<FeedbackPost> search(
            @Param("boardId") UUID boardId,
            @Param("status") FeedbackStatus status,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    List<FeedbackPost> findByRoadMapItemId(UUID roadmapItemId);

    List<FeedbackPost> findByMergedIntoId(UUID canonicalId);
}
