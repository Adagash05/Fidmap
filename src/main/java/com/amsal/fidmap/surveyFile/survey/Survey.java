package com.amsal.fidmap.surveyFile.survey;

import com.amsal.fidmap.surveyFile.surveyQuestion.SurveyQuestion;
import com.amsal.fidmap.workspace.Workspace;
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
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @OneToMany(
            mappedBy = "survey",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SurveyQuestion> questions = new ArrayList<>();

    public void addQuestion(SurveyQuestion question) {

        this.questions.add(question);
        question.setSurvey(this);
    }
}