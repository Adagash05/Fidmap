package com.amsal.fidmap.feedbackPost;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Removes everything that references a feedback post so it can be deleted
 * without foreign-key violations. All statements are scoped by the post id.
 */
@Component
public class FeedbackDependentsCleaner {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void deleteFor(UUID postId) {

        run("DELETE FROM Vote v WHERE v.feedbackPost.id = :id", postId);
        run("DELETE FROM Comment c WHERE c.feedbackPost.id = :id", postId);
        run("DELETE FROM FeedbackFollower f WHERE f.feedbackPost.id = :id", postId);
        run("DELETE FROM FeedbackNotification n WHERE n.feedbackPostId = :id", postId);
        run("DELETE FROM ProductDecision d WHERE d.feedbackPost.id = :id", postId);
        run("DELETE FROM FeedbackPriorityInput i WHERE i.feedbackPostId = :id", postId);
        run("DELETE FROM DuplicateSuggestion s WHERE s.sourcePostId = :id OR s.targetPostId = :id", postId);
        run("UPDATE FeedbackPost f SET f.mergedInto = NULL, f.mergedAt = NULL WHERE f.mergedInto.id = :id", postId);
        run("UPDATE Survey s SET s.linkedFeedbackPost = NULL WHERE s.linkedFeedbackPost.id = :id", postId);

        em.flush();
        em.clear();
    }

    private void run(String jpql, UUID postId) {
        em.createQuery(jpql).setParameter("id", postId).executeUpdate();
    }
}
