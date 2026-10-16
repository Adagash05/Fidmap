package com.amsal.fidmap.surveyFile.survey;

import java.util.UUID;

public record UpdateSurveyRequest(
        String title,

        String description,

        /** PUT semantics: null clears the link. */
        UUID linkedFeedbackPostId
 ) {


}
