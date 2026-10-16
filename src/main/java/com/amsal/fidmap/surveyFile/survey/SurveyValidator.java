package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.QuestionType;

import java.util.HashSet;
import java.util.List;

/** Server-side validation of survey definitions. */
public final class SurveyValidator {

    public static final int MAX_QUESTIONS = 30;
    public static final int MAX_OPTIONS = 20;

    private SurveyValidator() {
    }

    public static void validateSurvey(String title, int questionCount) {

        if (title == null || title.isBlank() || title.trim().length() > 200) {
            throw new IllegalArgumentException("A survey title (max 200 characters) is required");
        }

        if (questionCount > MAX_QUESTIONS) {
            throw new IllegalArgumentException("A survey can have at most " + MAX_QUESTIONS + " questions");
        }
    }

    public static void validateQuestion(String text, QuestionType type, List<String> options) {

        if (text == null || text.isBlank() || text.trim().length() > 500) {
            throw new IllegalArgumentException("Each question needs text (max 500 characters)");
        }

        if (type == null) {
            throw new IllegalArgumentException("Each question needs a type");
        }

        boolean choice = type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;

        if (choice) {

            if (options == null || options.size() < 2 || options.size() > MAX_OPTIONS) {
                throw new IllegalArgumentException(
                        "Choice questions need between 2 and " + MAX_OPTIONS + " options"
                );
            }

            var seen = new HashSet<String>();

            for (String option : options) {

                if (option == null || option.isBlank() || option.trim().length() > 200) {
                    throw new IllegalArgumentException("Options must be 1-200 characters");
                }

                if (!seen.add(option.trim().toLowerCase())) {
                    throw new IllegalArgumentException("Options must be unique");
                }
            }

        } else if (options != null && !options.isEmpty()) {
            throw new IllegalArgumentException("Only choice questions can have options");
        }
    }
}
