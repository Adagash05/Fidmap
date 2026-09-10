package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SurveyResponseDto(
        Long id,
        Long surveyId,
        UUID userId,
        LocalDateTime submittedAt,
        List<SurveyAnswerDto> answers
) {
}