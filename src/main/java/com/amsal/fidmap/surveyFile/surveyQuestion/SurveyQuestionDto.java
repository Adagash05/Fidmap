package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.surveyFile.survey.Survey;

import com.amsal.fidmap.surveyFile.surveyOption.SurveyOptionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SurveyQuestionDto {

    private Long id;

    private String question;

    private QuestionType type;

    private Integer position;

    private List<SurveyOptionDto> list;

//    public SurveyQuestionDto(Long id, String question, QuestionType type, Integer position, List<SurveyOptionDto> list) {


//    }
}
