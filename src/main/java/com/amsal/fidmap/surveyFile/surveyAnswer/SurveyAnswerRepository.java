package com.amsal.fidmap.surveyFile.surveyAnswer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    /** [questionId, answer, count] for choice and rating questions. */
    @Query("""
            SELECT a.question.id, a.answer, COUNT(a)
            FROM SurveyAnswer a
            WHERE a.response.survey.id = :surveyId
              AND a.question.type <> com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType.TEXT
            GROUP BY a.question.id, a.answer
            """)
    List<Object[]> countAnswers(@Param("surveyId") Long surveyId);

    @Query("""
            SELECT a FROM SurveyAnswer a
            WHERE a.question.id = :questionId AND a.answer IS NOT NULL
            ORDER BY a.response.submittedAt DESC, a.id DESC
            """)
    List<SurveyAnswer> latestTextAnswers(@Param("questionId") Long questionId, Pageable pageable);
}
