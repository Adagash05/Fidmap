package com.amsal.fidmap.surveyFile.surveyResponse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse,Long> {

    List<SurveyResponse> findAllBySurveyId(Long surveyId);
}
