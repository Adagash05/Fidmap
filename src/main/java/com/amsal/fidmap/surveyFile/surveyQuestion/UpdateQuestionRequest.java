package com.amsal.fidmap.surveyFile.surveyQuestion;

import java.util.List;

public record UpdateQuestionRequest (

     String question,

     QuestionType type,

     Integer position,

     /** When non-null, replaces the question's options (draft surveys only). */
     List<String> options,

     Boolean required
) {
}
