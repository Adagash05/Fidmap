package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestionDto;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SurveyDto {

    private Long id;

    private String title;

    private String description;

    private SurveyStatus status;

    private LocalDateTime publishedAt;

    private List<SurveyQuestionDto> questions;
}
