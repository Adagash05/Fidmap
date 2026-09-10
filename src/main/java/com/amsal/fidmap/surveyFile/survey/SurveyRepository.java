package com.amsal.fidmap.surveyFile.survey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey,Long> {

    List<Survey> findAllByWorkspaceId(UUID workspaceId);
}
