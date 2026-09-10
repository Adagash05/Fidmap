package com.amsal.fidmap.feedbackPost;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.board.Board;
import com.amsal.fidmap.board.BoardRepository;
import com.amsal.fidmap.endUser.EndUser;
import com.amsal.fidmap.endUser.EndUserRepository;
import com.amsal.fidmap.exception.BoardNotFoundException;
import com.amsal.fidmap.exception.FeedbackPostNotFoundException;
import com.amsal.fidmap.exception.RoadmapNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.roadmapItem.RoadmapItem;
import com.amsal.fidmap.roadmapItem.RoadmapItemRepository;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.vote.VoteRepository;
import com.amsal.fidmap.workspace.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeedbackService implements IFeedbackService {

    private final FeedbackPostRepository feedbackPostRepository;
    private final FeedbackMapper feedbackMapper;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final EndUserRepository endUserRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final PlanEntitlementService planEntitlementService;



    @Transactional
    @Override
    public ApiResponse<FeedbackDto> createFeedbackPost(AddFeedback feedback, UUID boardId) {

//        UUID userId = SecurityUtils.getCurrentUserId();
//        User user = userRepository.findUserById(userId);
//        if(user == null) {
//            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
//        }

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        Workspace workspace = board.getWorkspace();

        //Plan Limit
        long currentFeedback = feedbackPostRepository.countByWorkspaceId(workspace.getId());
        planEntitlementService.checkFeedbackPostLimit(workspace.getId(),currentFeedback);

        //feedback processing
        FeedbackPost post = feedbackMapper.toFeedbackPost(feedback);
        post.setBoard(board);
        post.setWorkspace(workspace);
        post.setCreatedAt(LocalDate.now());



        EndUser checkEndUser = endUserRepository.findEndUserByEmail(feedback.getEndUser().getEmail());
        if (checkEndUser == null ) {

            EndUser endUser = new EndUser();
            endUser.setWorkspaces(List.of(workspace));
            endUser.setName(feedback.getEndUser().getName());
            endUser.setEmail(feedback.getEndUser().getEmail());

            endUserRepository.save(endUser);

            post.setEndUser(endUser);


        } else {
            post.setEndUser(checkEndUser);

        }

        //todo
//        Vote vote = voteRepository.countAllVotesByEndUserAndFeedbackPost(post.getEndUser(),post);

        var saveFeedback = feedbackPostRepository.save(post);

        FeedbackDto dto = feedbackMapper.toFeedbackDto(saveFeedback);

        return ApiResponse.success("feedback post is created successfully",dto);




    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<FeedbackDto>> getAllFeedbackByBoard(UUID boardId) {

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        List<FeedbackDto> dtos = feedbackPostRepository.findAllByBoard(board)
                .stream()
                .map(feedbackMapper::toFeedbackDto)
                .collect(Collectors.toList());


        return ApiResponse.success("retrieved all the feedbacks in this board",dtos) ;
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<FeedbackDto> getFeedbackById(
            UUID boardId,
            UUID feedbackId) {

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        FeedbackDto dto = feedbackMapper.toFeedbackDto(post);

        return ApiResponse.success("retrieve post successfully", dto);
    }



    @Transactional
    @Override
    public ApiResponse<FeedbackDto> assignFeedbackToRoadmapItem(
            UUID feedbackId,
            UUID roadmapItemId
    ) {
        // todo check and understand the method

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);

        if (post == null) {
            throw new FeedbackPostNotFoundException(
                    "feedback post is not found"
            );
        }

        RoadmapItem roadmapItem = roadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new RoadmapNotFoundException(
                        "roadmap item is not found"
                ));

        post.setRoadMapItem(roadmapItem);

        FeedbackPost savedPost = feedbackPostRepository.save(post);

        FeedbackDto dto = feedbackMapper.toFeedbackDto(savedPost);

        return ApiResponse.success(
                "feedback post assigned to roadmap item successfully",
                dto
        );
    }




    @Transactional
    @Override
    public ApiResponse<FeedbackDto> editFeedback(
            UUID boardId,
            UUID feedbackId,
            UpdateFeedbackRequest request
    ) {

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());

        var savePost = feedbackPostRepository.save(post);
        FeedbackDto dto = feedbackMapper.toFeedbackDto(savePost);


        return ApiResponse.success("post is updated successfully", dto);
    }

    @Transactional
    @Override
    public ApiResponse<FeedbackDto> editFeedbackStatus(UUID boardId,UUID feedbackId, FeedbackStatus status) {

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        post.setStatus(status);

        var savePost = feedbackPostRepository.save(post);
        FeedbackDto dto = feedbackMapper.toFeedbackDto(savePost);


        return ApiResponse.success("post is updated successfully", dto);
    }

    @Transactional
    @Override
    public ApiResponse<Void> deleteFeedback(UUID boardId, UUID feedbackId) {

        Board board = boardRepository.findBoardById(boardId);
        if (board == null ) {
            throw new BoardNotFoundException("board does not exist exists,please try again later");
        }

        FeedbackPost post = feedbackPostRepository.findFeedbackPostById(feedbackId);
        if (post == null) {
            throw new FeedbackPostNotFoundException("sorry,the post you are looking for does not exists");
        }

        feedbackPostRepository.delete(post);

        return ApiResponse.success("post is deleted successfully", null);

    }
}