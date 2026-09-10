package com.amsal.fidmap.surveyFile.surveyQuestion;

import com.amsal.fidmap.surveyFile.survey.Survey;
import com.amsal.fidmap.surveyFile.surveyOption.SurveyOption;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SurveyOption> options = new ArrayList<>();


    public void addOption(SurveyOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }
}