package com.amsal.fidmap.surveyFile.surveyResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse,Long> {

    List<SurveyResponse> findAllBySurveyId(Long surveyId);

    Page<SurveyResponse> findBySurveyId(Long surveyId, Pageable pageable);

    long countBySurveyId(Long surveyId);

    boolean existsBySurveyIdAndEndUserId(Long surveyId, UUID endUserId);
}
