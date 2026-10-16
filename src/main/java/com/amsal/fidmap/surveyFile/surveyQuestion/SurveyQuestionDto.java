package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.surveyFile.surveyOption.SurveyOptionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionDto {

    private Long id;

    private String question;

    private QuestionType type;

    private Integer position;

    /** Kept under its original JSON name for backward compatibility. */
    private List<SurveyOptionDto> list;

    private boolean required;

    /** Preferred name for new clients (same data as {@code list}). */
    public List<SurveyOptionDto> getOptions() {
        return list;
    }
}
