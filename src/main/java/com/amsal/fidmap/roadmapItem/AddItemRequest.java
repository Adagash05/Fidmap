package com.amsal.fidmap.roadmapItem;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AddItemRequest {

    private String title;

    private String description;

    /**
     * Calendar date (yyyy-MM-dd). Optional. It is a plain date, never a
     * timestamp, so it can not shift by a day through timezone conversion.
     */
    private LocalDate targetDate;

    private RoadMapStatus status;

    /** Requests to link to the new item (must belong to the same workspace). */
    private List<UUID> feedbackPostIds = new ArrayList<>();

    /**
     * Backward compatibility: the previous frontend sent
     * {@code feedbackPost: [{id}]}. Both shapes are accepted.
     */
    private List<FeedbackRef> feedbackPost = new ArrayList<>();

    @Getter
    @Setter
    public static class FeedbackRef {
        private UUID id;
    }

    public List<UUID> resolvedFeedbackPostIds() {

        List<UUID> ids = new ArrayList<>();

        if (feedbackPostIds != null) {
            ids.addAll(feedbackPostIds);
        }

        if (feedbackPost != null) {
            for (FeedbackRef ref : feedbackPost) {
                if (ref != null && ref.getId() != null) {
                    ids.add(ref.getId());
                }
            }
        }

        return ids.stream().distinct().toList();
    }
}
