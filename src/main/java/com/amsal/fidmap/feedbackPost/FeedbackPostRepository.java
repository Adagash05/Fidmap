package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedbackPostRepository extends JpaRepository<FeedbackPost, UUID> {

    List<FeedbackPost> findAllByBoard(Board board);

    FeedbackPost findFeedbackPostById(UUID feedbackId);

    @Query("""
                SELECT COUNT(f)
                FROM FeedbackPost f
                JOIN f.workspace w
                WHERE w.id = :workspaceId
            """)
    long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
