package com.amsal.fidmap.surveyFile.surveyQuestion;


public record UpdateQuestionRequest (

     String question,

     QuestionType type,

     Integer position
) {
}
