package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SurveyValidatorTest {

    @Test
    void choiceQuestionNeedsTwoUniqueOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> SurveyValidator.validateQuestion("Pick", QuestionType.SINGLE_CHOICE, List.of("A")));
        assertThrows(IllegalArgumentException.class,
                () -> SurveyValidator.validateQuestion("Pick", QuestionType.SINGLE_CHOICE, List.of("A", "a")));
        assertDoesNotThrow(() -> SurveyValidator.validateQuestion("Pick", QuestionType.MULTIPLE_CHOICE, List.of("A", "B")));
    }

    @Test
    void textQuestionCannotHaveOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> SurveyValidator.validateQuestion("Why?", QuestionType.TEXT, List.of("A", "B")));
        assertDoesNotThrow(() -> SurveyValidator.validateQuestion("Why?", QuestionType.TEXT, null));
    }

    @Test
    void blankTitleAndTooManyQuestionsRejected() {
        assertThrows(IllegalArgumentException.class, () -> SurveyValidator.validateSurvey(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> SurveyValidator.validateSurvey("T", SurveyValidator.MAX_QUESTIONS + 1));
    }
}
