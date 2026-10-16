package com.amsal.fidmap.duplicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DuplicateSuggestionRepository extends JpaRepository<DuplicateSuggestion, UUID> {

    Page<DuplicateSuggestion> findByWorkspaceIdAndStatus(UUID workspaceId, SuggestionStatus status, Pageable pageable);

    @Query("SELECT s.sourcePostId, s.targetPostId FROM DuplicateSuggestion s WHERE s.workspaceId = :w")
    List<Object[]> findAllPairs(@Param("w") UUID workspaceId);

    @Query("""
            SELECT s FROM DuplicateSuggestion s
            WHERE s.status = com.amsal.fidmap.duplicate.SuggestionStatus.PENDING
              AND (s.sourcePostId = :id OR s.targetPostId = :id)""")
    List<DuplicateSuggestion> findPendingInvolving(@Param("id") UUID postId);
}
