package com.amsal.fidmap.roadmap;

import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany(
            mappedBy = "roadmap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RoadmapItem> roadmapItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(
            name = "workspace_id",
            nullable = false,
            unique = true
    )
    private Workspace workspace;

    public void addRoadmapItem(RoadmapItem item) {
        roadmapItems.add(item);
        item.setRoadmap(this);
    }

    public void removeRoadmapItem(RoadmapItem item) {
        roadmapItems.remove(item);
        item.setRoadmap(null);
    }
}
