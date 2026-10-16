package com.amsal.fidmap.roadmapItem;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.RoadmapNotFoundException;
import com.amsal.fidmap.feedbackPost.FeedbackPost;
import com.amsal.fidmap.feedbackPost.FeedbackPostRepository;
import com.amsal.fidmap.notification.NotificationService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.roadmap.Roadmap;
import com.amsal.fidmap.roadmap.RoadmapRepository;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RoadmapItemService implements IRoadmapItemService {

    static final int MAX_TITLE = 200;
    static final int MAX_DESCRIPTION = 5000;

    private final RoadmapItemRepository roadmapItemRepository;
    private final RoadmapItemMapper roadmapItemMapper;
    private final RoadmapRepository roadmapRepository;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final FeedbackPostRepository feedbackPostRepository;
    private final NotificationService notificationService;

    /**
     * Creates the item with its target date in the SAME request/transaction and
     * returns the persisted entity, so the response already carries the date.
     * (Root cause of the original bug was the creation form, which neither
     * showed nor sent the date; see REFACTOR_PROGRESS.md.)
     */
    @Transactional
    @Override
    public ApiResponse<RoadmapItemDto> createRoadmapItem(Long roadmapId, AddItemRequest request) {

        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if (roadmap == null) {
            throw new RoadmapNotFoundException("roadmap is not found");
        }

        UUID workspaceId = roadmap.getWorkspace().getId();

        workspaceAuthorizationService.requireStaffWrite(workspaceId);

        validate(request.getTitle(), request.getDescription());

        //Plan check
        long currentItems = roadmapItemRepository.countByRoadmapId(roadmapId);
        planEntitlementService.checkRoadmapItemLimit(workspaceId, currentItems);

        RoadmapItem item = roadmapItemMapper.toRoadMapItem(request);
        item.setTitle(item.getTitle().trim());
        item.setDescription(item.getDescription() == null ? null : item.getDescription().trim());

        roadmap.addRoadmapItem(item);

        RoadmapItem saved = roadmapItemRepository.saveAndFlush(item);

        // Link requests (same workspace only) so decision -> roadmap stays traceable.
        for (UUID feedbackId : request.resolvedFeedbackPostIds()) {

            FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);

            if (post == null
                    || post.getWorkspace() == null
                    || !workspaceId.equals(post.getWorkspace().getId())) {
                throw new AccessDeniedException("You do not have access to one of the selected requests");
            }

            post.setRoadMapItem(saved);
            feedbackPostRepository.save(post);
        }

        return ApiResponse.success("item is added successfully", roadmapItemMapper.toRoadMapItemDto(saved));
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<RoadmapItemDto>> getAllRoadmapItems(Long roadmapId) {

        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if (roadmap == null) {
            throw new RoadmapNotFoundException("roadmap is not found");
        }

        List<RoadmapItemDto> dtoList = roadmapItemRepository.findAllByRoadmap(roadmap)
                .stream()
                .map(roadmapItemMapper::toRoadMapItemDto)
                .toList();

        return ApiResponse.success("retrieve roadmap items", dtoList);
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<RoadmapItemDto> getRoadmapItem(UUID roadmapItemId) {

        RoadmapItem roadmapItem = find(roadmapItemId);

        return ApiResponse.success("retrieve roadmap item", roadmapItemMapper.toRoadMapItemDto(roadmapItem));
    }

    @Transactional
    @Override
    public ApiResponse<RoadmapItemDto> updateRoadmapItem(UUID roadmapItemId, UpdateItemRequest request) {

        RoadmapItem roadmapItem = find(roadmapItemId);

        workspaceAuthorizationService.requireStaffWrite(workspaceIdOf(roadmapItem));

        validate(request.getTitle(), request.getDescription());

        RoadMapStatus previous = roadmapItem.getStatus();

        roadmapItem.setTitle(request.getTitle().trim());
        roadmapItem.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        // PUT semantics: a null target date clears it (undated items are valid).
        roadmapItem.setTargetDate(request.getTargetDate());

        if (request.getStatus() != null) {
            roadmapItem.setStatus(request.getStatus());
        }

        RoadmapItem saved = roadmapItemRepository.saveAndFlush(roadmapItem);

        notifyIfStatusChanged(saved, previous);

        return ApiResponse.success("update roadmap item successfully", roadmapItemMapper.toRoadMapItemDto(saved));
    }

    @Transactional
    @Override
    public ApiResponse<RoadmapItemDto> editRoadmapItem(UUID roadmapItemId, RoadMapStatus status) {

        RoadmapItem roadmapItem = find(roadmapItemId);

        workspaceAuthorizationService.requireStaffWrite(workspaceIdOf(roadmapItem));

        if (status == null) {
            throw new IllegalArgumentException("A status is required");
        }

        RoadMapStatus previous = roadmapItem.getStatus();

        roadmapItem.setStatus(status);

        RoadmapItem saved = roadmapItemRepository.saveAndFlush(roadmapItem);

        notifyIfStatusChanged(saved, previous);

        return ApiResponse.success("update roadmap item status successfully", roadmapItemMapper.toRoadMapItemDto(saved));
    }

    @Transactional
    @Override
    public ApiResponse<Void> deleteRoadmapItem(UUID roadmapItemId) {

        RoadmapItem roadmapItem = find(roadmapItemId);

        workspaceAuthorizationService.requireStaff(workspaceIdOf(roadmapItem));

        // Unlink requests first so the foreign key never blocks the delete.
        for (FeedbackPost post : feedbackPostRepository.findByRoadMapItemId(roadmapItemId)) {
            post.setRoadMapItem(null);
            feedbackPostRepository.save(post);
        }

        Roadmap roadmap = roadmapItem.getRoadmap();

        if (roadmap != null) {
            roadmap.removeRoadmapItem(roadmapItem);
        }

        roadmapItemRepository.delete(roadmapItem);

        return ApiResponse.success("deleted item successfully", null);
    }

    private void notifyIfStatusChanged(RoadmapItem item, RoadMapStatus previous) {

        if (item.getStatus() == null || item.getStatus() == previous) {
            return;
        }

        String label = switch (item.getStatus()) {
            case CONSIDERING -> "being considered";
            case PLANNED -> "planned";
            case IN_PROGRESS -> "in progress";
            case TESTING -> "in testing";
            case COMPLETE -> "shipped";
            case CANCELLED -> "no longer planned";
        };

        for (FeedbackPost post : feedbackPostRepository.findByRoadMapItemId(item.getId())) {

            notificationService.queueEvent(
                    post,
                    "ROADMAP:" + item.getId() + ":" + item.getStatus().name(),
                    "Update on \"" + post.getTitle() + "\"",
                    "\"" + post.getTitle() + "\" is " + label
            );
        }
    }

    private RoadmapItem find(UUID roadmapItemId) {

        return roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("item is not found please try again later"));
    }

    private UUID workspaceIdOf(RoadmapItem item) {

        if (item.getRoadmap() == null || item.getRoadmap().getWorkspace() == null) {
            throw new RoadmapNotFoundException("item is not found please try again later");
        }

        return item.getRoadmap().getWorkspace().getId();
    }

    private void validate(String title, String description) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("A title is required");
        }

        if (title.trim().length() > MAX_TITLE) {
            throw new IllegalArgumentException("Title must be at most " + MAX_TITLE + " characters");
        }

        if (description != null && description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("Description must be at most " + MAX_DESCRIPTION + " characters");
        }
    }
}
