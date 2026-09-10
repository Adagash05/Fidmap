package com.amsal.fidmap.surveyFile.surveyOption;

import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String value;

    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;
}
