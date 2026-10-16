package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
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

    private UUID linkedFeedbackPostId;

    private long responseCount;
}
