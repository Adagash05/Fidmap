package com.amsal.fidmap.surveyFile.surveyResponse;

import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.surveyAnswer.SurveyAnswer;
import com.amsal.fidmap.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(
            mappedBy = "response",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();

    public void addAnswer(SurveyAnswer answer) {

        this.answers.add(answer);
        answer.setResponse(this);
    }
}