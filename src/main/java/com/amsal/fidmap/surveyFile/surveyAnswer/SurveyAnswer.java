package com.amsal.fidmap.surveyFile.surveyAnswer;

import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.surveyFile.surveyResponse.SurveyResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;
}
