package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.roadmap.Roadmap;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private LocalDate targetDate;

    @CreatedDate
    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    private RoadMapStatus status;

    @ManyToOne
    @JoinColumn(name = "road_map_id")
    private Roadmap roadmap;

    @OneToMany(mappedBy = "roadMapItem")
    private List<FeedbackPost> feedbackPost;
}

