package com.amsal.fidmap.roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap,Long> {
    Roadmap findRoadmapById(Long roadmapId);

    Roadmap findRoadmapByWorkspaceId(UUID workspaceId);
}
