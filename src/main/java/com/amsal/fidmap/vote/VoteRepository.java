package com.amsal.fidmap.vote;

import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote,Long> {
    Optional<Vote> findByFeedbackPostAndEndUser(FeedbackPost post, EndUser endUser);

    //todo
//    @Query("SELECT SUM(")
//    Vote countAllVotesByEndUserAndFeedbackPost(EndUser endUser, FeedbackPost post);

    Optional<Vote> findByFeedbackPostIdAndEndUserId(UUID feedbackPostId, UUID endUserId );
    long countByFeedbackPostId(UUID feedbackPostId);

    @Query("SELECT v.endUser.id FROM Vote v WHERE v.feedbackPost.id = :postId")
    List<UUID> findVoterIds(@Param("postId") UUID postId);

    @Query("SELECT v FROM Vote v WHERE v.feedbackPost.id = :postId")
    List<Vote> findAllByPostId(@Param("postId") UUID postId);

    @Query("""
    SELECT new com.amsal.fidmap.vote.BoardVoteStatusDto(
        fp.id,
        fp.voteCount,
        CASE WHEN COUNT(v) > 0 THEN true ELSE false END
    )
    FROM FeedbackPost fp
    LEFT JOIN Vote v
        ON v.feedbackPost = fp
        AND LOWER(v.endUser.email) = :endUserEmail
    WHERE fp.board.id = :boardId
    GROUP BY fp.id, fp.voteCount
    ORDER BY fp.id
""")
    List<BoardVoteStatusDto> findVoteStatusesByBoardId(
            @Param("boardId") UUID boardId,
            @Param("endUserEmail") String endUserEmail
    );

}
