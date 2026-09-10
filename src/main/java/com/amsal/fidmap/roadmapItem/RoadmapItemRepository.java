package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.roadmap.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, UUID> {
    List<RoadmapItem> findAllByRoadmap(Roadmap roadmap);

    @Query("""
                SELECT COUNT(item)
                FROM RoadmapItem item
                JOIN item.roadmap r
                WHERE r.id = :roadmapId
            """)
    long countByRoadmapId(@Param("roadmapId") Long roadmapId);

}

