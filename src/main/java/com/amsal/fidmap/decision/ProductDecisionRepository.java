package com.amsal.fidmap.decision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductDecisionRepository extends JpaRepository<ProductDecision, UUID> {

    @Query("SELECT d FROM ProductDecision d WHERE d.feedbackPost.id = :postId ORDER BY d.decidedAt DESC")
    List<ProductDecision> findHistory(@Param("postId") UUID postId);

    @Query("SELECT d FROM ProductDecision d WHERE d.workspaceId = :w ORDER BY d.decidedAt DESC")
    List<ProductDecision> findByWorkspace(@Param("w") UUID workspaceId);
}
