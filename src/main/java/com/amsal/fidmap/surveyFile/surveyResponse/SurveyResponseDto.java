package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyResponseDto(
        Long id,
        Long surveyId,
        String respondentName,
        String respondentEmail,
        LocalDateTime submittedAt,
        List<SurveyAnswerDto> answers
) {
}
