package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.board.Board;
import com.amsal.fidmap.board.BoardRepository;
import com.amsal.fidmap.common.PageResponse;
import com.amsal.fidmap.common.Paging;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserResolver;
import com.amsal.fidmap.exception.BoardNotFoundException;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.exception.RoadmapNotFoundException;
import com.amsal.fidmap.notification.NotificationService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.roadmapItem.RoadmapItemRepository;
import com.amsal.fidmap.security.WorkspaceAuthorizationService;
import com.amsal.fidmap.workspace.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeedbackService implements IFeedbackService {

    static final int MAX_TITLE = 200;
    static final int MAX_DESCRIPTION = 5000;

    private final FeedbackPostRepository feedbackPostRepository;
    private final FeedbackMapper feedbackMapper;
    private final BoardRepository boardRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final PlanEntitlementService planEntitlementService;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final EndUserResolver endUserResolver;
    private final NotificationService notificationService;
    private final FeedbackDependentsCleaner dependentsCleaner;

    @Transactional
    @Override
    public ApiResponse<FeedbackDto> createFeedbackPost(AddFeedback feedback, UUID boardId) {

        Board board = requireBoard(boardId);

        requirePublicOrStaff(board);

        if (feedback == null) {
            throw new IllegalArgumentException("Feedback is required");
        }

        String title = feedback.getTitle() == null ? "" : feedback.getTitle().trim();
        String description = feedback.getDescription() == null ? "" : feedback.getDescription().trim();

        if (title.isEmpty()) {
            throw new IllegalArgumentException("A title is required");
        }
        if (title.length() > MAX_TITLE) {
            throw new IllegalArgumentException("Title must be at most " + MAX_TITLE + " characters");
        }
        if (description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("Description must be at most " + MAX_DESCRIPTION + " characters");
        }
        if (feedback.getEndUser() == null) {
            throw new IllegalArgumentException("Your name and email are required");
        }

        Workspace workspace = board.getWorkspace();

        // Entitlement gate (active trial/subscription) + plan limit.
        long currentFeedback = feedbackPostRepository.countByWorkspaceId(workspace.getId());
        planEntitlementService.checkFeedbackPostLimit(workspace.getId(), currentFeedback);

        EndUser endUser = endUserResolver.resolve(
                workspace,
                feedback.getEndUser().getName(),
                feedback.getEndUser().getEmail()
        );

        FeedbackPost post = feedbackMapper.toFeedbackPost(feedback);
        post.setTitle(title);
        post.setDescription(description);
        post.setBoard(board);
        post.setWorkspace(workspace);
        post.setCreatedAt(LocalDate.now());
        post.setEndUser(endUser);

        FeedbackPost saved = feedbackPostRepository.save(post);

        notificationService.follow(saved, endUser);

        return ApiResponse.success("feedback post is created successfully", feedbackMapper.toFeedbackDto(saved));
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<FeedbackDto>> getAllFeedbackByBoard(UUID boardId) {

        Board board = requireBoard(boardId);

        requirePublicOrStaff(board);

        List<FeedbackDto> dtos = feedbackPostRepository.findAllByBoard(board)
                .stream()
                .map(feedbackMapper::toFeedbackDto)
                .collect(Collectors.toList());

        return ApiResponse.success("retrieved all the feedbacks in this board", dtos);
    }

    /**
     * Paged, filterable list. Sort is limited to a safe whitelist and always
     * ends with the id so page boundaries are stable.
     */
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<FeedbackDto>> searchFeedbackByBoard(
            UUID boardId,
            Integer page,
            Integer size,
            String sort,
            FeedbackStatus status,
            String query
    ) {

        Board board = requireBoard(boardId);

        requirePublicOrStaff(board);

        Sort order = switch (sort == null ? "newest" : sort) {
            case "votes" -> Sort.by(Sort.Order.desc("voteCount"), Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
            case "oldest" -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
        };

        var result = feedbackPostRepository.search(
                boardId,
                status,
                Paging.likeContains(query),
                Paging.of(page, size, order)
        );

        return ApiResponse.success(
                "retrieved feedback page",
                PageResponse.of(result, feedbackMapper::toFeedbackDto)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<FeedbackDto> getFeedbackById(UUID boardId, UUID feedbackId) {

        Board board = requireBoard(boardId);

        requirePublicOrStaff(board);

        FeedbackPost post = requirePostInBoard(feedbackId, board);

        return ApiResponse.success("retrieve post successfully", feedbackMapper.toFeedbackDto(post));
    }

    @Transactional
    @Override
    public ApiResponse<FeedbackDto> assignFeedbackToRoadmapItem(UUID feedbackId, UUID roadmapItemId) {

        FeedbackPost post = requirePost(feedbackId);

        UUID workspaceId = post.getWorkspace().getId();

        workspaceAuthorizationService.requireStaffWrite(workspaceId);

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException("roadmap item is not found"));

        // The roadmap item must belong to the same workspace as the request.
        if (roadmapItem.getRoadmap() == null
                || roadmapItem.getRoadmap().getWorkspace() == null
                || !workspaceId.equals(roadmapItem.getRoadmap().getWorkspace().getId())) {
            throw new AccessDeniedException("You do not have access to this roadmap item");
        }

        post.setRoadMapItem(roadmapItem);

        FeedbackPost saved = feedbackPostRepository.save(post);

        return ApiResponse.success(
                "feedback post assigned to roadmap item successfully",
                feedbackMapper.toFeedbackDto(saved)
        );
    }

    @Transactional
    @Override
    public ApiResponse<FeedbackDto> editFeedback(UUID boardId, UUID feedbackId, UpdateFeedbackRequest request) {

        Board board = requireBoard(boardId);

        FeedbackPost post = requirePostInBoard(feedbackId, board);

        workspaceAuthorizationService.requireStaffWrite(post.getWorkspace().getId());

        String title = request.getTitle() == null ? "" : request.getTitle().trim();

        if (title.isEmpty() || title.length() > MAX_TITLE) {
            throw new IllegalArgumentException("Title is required and must be at most " + MAX_TITLE + " characters");
        }

        String description = request.getDescription() == null ? "" : request.getDescription().trim();

        if (description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("Description must be at most " + MAX_DESCRIPTION + " characters");
        }

        post.setTitle(title);
        post.setDescription(description);

        return ApiResponse.success(
                "post is updated successfully",
                feedbackMapper.toFeedbackDto(feedbackPostRepository.save(post))
        );
    }

    @Transactional
    @Override
    public ApiResponse<FeedbackDto> editFeedbackStatus(UUID boardId, UUID feedbackId, FeedbackStatus status) {

        Board board = requireBoard(boardId);

        FeedbackPost post = requirePostInBoard(feedbackId, board);

        workspaceAuthorizationService.requireStaffWrite(post.getWorkspace().getId());

        FeedbackStatus previous = post.getStatus();

        post.setStatus(status);

        FeedbackPost saved = feedbackPostRepository.save(post);

        if (status != null && status != previous && status != FeedbackStatus.OPEN) {

            notificationService.queueEvent(
                    saved,
                    "STATUS:" + status.name(),
                    "Update on \"" + saved.getTitle() + "\"",
                    "\"" + saved.getTitle() + "\" is now " + statusLabel(status)
            );
        }

        return ApiResponse.success("post is updated successfully", feedbackMapper.toFeedbackDto(saved));
    }

    @Transactional
    @Override
    public ApiResponse<Void> deleteFeedback(UUID boardId, UUID feedbackId) {

        Board board = requireBoard(boardId);

        FeedbackPost post = requirePostInBoard(feedbackId, board);

        // Deleting stays allowed for lapsed workspaces so customers can reduce usage.
        workspaceAuthorizationService.requireStaff(post.getWorkspace().getId());

        dependentsCleaner.deleteFor(post.getId());

        feedbackPostRepository.deleteById(feedbackId);

        return ApiResponse.success("post is deleted successfully", null);
    }

    static String statusLabel(FeedbackStatus status) {

        return switch (status) {
            case OPEN -> "open";
            case PLANNED -> "planned";
            case IN_PROGRESS -> "in progress";
            case COMPLETED -> "completed";
            case REJECTED -> "not planned";
        };
    }

    private Board requireBoard(UUID boardId) {

        Board board = boardRepository.findBoardById(boardId);

        if (board == null) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        return board;
    }

    private FeedbackPost requirePost(UUID feedbackId) {

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);

        if (post == null) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        return post;
    }

    /** The post must exist AND belong to the board named in the URL. */
    private FeedbackPost requirePostInBoard(UUID feedbackId, Board board) {

        FeedbackPost post = requirePost(feedbackId);

        if (post.getBoard() == null || !post.getBoard().getId().equals(board.getId())) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        return post;
    }

    private void requirePublicOrStaff(Board board) {

        if (Boolean.FALSE.equals(board.getIsPublic())
                && !workspaceAuthorizationService.isStaffOf(board.getWorkspace().getId())) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }
    }
}
