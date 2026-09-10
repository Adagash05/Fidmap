package com.amsal.fidmap.surveyFile.surveyQuestion;

import java.util.List;

public record CreateQuestionRequest(
        String question,
        QuestionType type,
        Integer position,
        List<String> options
) {}
