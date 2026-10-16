package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOptionDto;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestionDto;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

public final class SurveyMapper {

    private SurveyMapper() {
    }

    public static SurveyQuestionDto toDto(SurveyQuestion question) {

        List<SurveyOptionDto> options = question.getOptions()
                .stream()
                .sorted(comparing(SurveyOption::getPosition, nullsLast(Comparator.naturalOrder())))
                .map(o -> new SurveyOptionDto(o.getId(), o.getValue(), o.getPosition()))
                .toList();

        return new SurveyQuestionDto(
                question.getId(),
                question.getQuestion(),
                question.getType(),
                question.getPosition(),
                options,
                Boolean.TRUE.equals(question.getRequired())
        );
    }

    public static SurveyDto toDto(Survey survey, long responseCount) {

        List<SurveyQuestionDto> questions = survey.getQuestions()
                .stream()
                .sorted(comparing(SurveyQuestion::getPosition, nullsLast(Comparator.naturalOrder())))
                .map(SurveyMapper::toDto)
                .toList();

        return new SurveyDto(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStatus(),
                survey.getPublishedAt(),
                questions,
                survey.getLinkedFeedbackPost() == null ? null : survey.getLinkedFeedbackPost().getId(),
                responseCount
        );
    }
}
