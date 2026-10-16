package com.amsal.fidmap.decision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackPriorityInputRepository extends JpaRepository<FeedbackPriorityInput, UUID> {

    Optional<FeedbackPriorityInput> findByFeedbackPostId(UUID feedbackPostId);

    List<FeedbackPriorityInput> findByWorkspaceId(UUID workspaceId);
}
