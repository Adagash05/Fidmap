package com.amsal.fidmap.board;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.BoardNotFoundException;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService implements IBoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BoardMapper boardMapper;
    private final PlanEntitlementService planEntitlementService;

    @Transactional
    @Override
    public ApiResponse<BoardDto> createBoard(BoardDto boardDto, UUID workspaceId) {

        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);
        if(user == null) {
            throw new UserNotFoundException("user not found,sorry you are not allowed to access this resource, please try again later, thank you");
        }

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
        if (workspace == null ) {
            throw new WorkspaceNotFoundException("Something went wrong,workspace is not found");
        }

        // Plan limit
        long currentBoards = boardRepository.countByWorkspaceId(workspaceId);
        planEntitlementService.checkBoardLimit(workspaceId, currentBoards);

        //Private board check
        if (Boolean.FALSE.equals(boardDto.getIsPublic())) {
            planEntitlementService.requirePrivateBoards(workspaceId);
        }

        Board board = boardMapper.toBoard(boardDto);
        workspace.addBoardToWorkspace(board);

        Board saveBoard = boardRepository.save(board);

        BoardDto dto = boardMapper.toBoardDto(saveBoard);

        return ApiResponse.success("board is created successfully",dto);
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<BoardDto>> getAllBoardsByWorkspace(UUID workspaceId) {

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
        if (workspace == null ) {
            throw new WorkspaceNotFoundException("Something went wrong,workspace is not found");
        }


        List<BoardDto> dtos = boardRepository.findAllBoardByWorkspace(workspace)
                .stream()
                .map(boardMapper::toBoardDto)
                .toList();



        return ApiResponse.success("get all boards by workspace",dtos);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BoardDto>> getPublicBoardsByWorkspace(UUID workspaceId) {

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);

        if (workspace == null) {
            throw new WorkspaceNotFoundException(
                    "Something went wrong, workspace is not found"
            );
        }

        List<BoardDto> dtos = boardRepository
                .findAllByWorkspaceIdAndIsPublicTrue(workspaceId)
                .stream()
                .map(boardMapper::toBoardDto)
                .toList();

        return ApiResponse.success(
                "get all public boards by workspace",
                dtos
        );
    }
    @Transactional(readOnly = true)
    @Override
    public ApiResponse<BoardDto> getBoard(UUID boardId) {

        Board board =  boardRepository.findBoardById(boardId);
        if (board == null) {
            throw new BoardNotFoundException("this board is not found,please try again later");
        }

        BoardDto dto = boardMapper.toBoardDto(board);

        return ApiResponse.success("board is found successfully", dto);
    }

    @Transactional
    @Override
    public ApiResponse<BoardDto> editBoard(UUID boardId,UpdateBoard request) {

        Board board =  boardRepository.findBoardById(boardId);
        if (board == null) {
            throw new BoardNotFoundException("this board is not found,please try again later");
        }

        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setIsPublic(request.getIsPublic());


        Board saveBoard = boardRepository.save(board);

        BoardDto dto = boardMapper.toBoardDto(saveBoard);

        return ApiResponse.success("board is updated successfully",dto);
    }

//    @Override
//    public ApiResponse<BoardDto> editBoardName(UUID boardId, String boardName) {
//        return null;
//    }

    @Transactional
    @Override
    public ApiResponse<BoardDto> editBoardIsPublic(UUID boardId, boolean isPublic) {

        Board board =  boardRepository.findBoardById(boardId);
        if (board == null) {
            throw new BoardNotFoundException("this board is not found,please try again later");
        }

        board.setIsPublic(isPublic);

        Board saveBoard = boardRepository.save(board);

        BoardDto dto = boardMapper.toBoardDto(saveBoard);

        return ApiResponse.success("board is updated successfully",dto);
    }

    @Transactional
    @Override
    public ApiResponse<Void> deleteBoard(UUID boardId) {

        Board board =  boardRepository.findBoardById(boardId);
        if (board == null) {
            throw new BoardNotFoundException("this board is not found,please try again later");
        }


        boardRepository.deleteBoardById(boardId);

        return ApiResponse.success("board is deleted successfully", null);


    }
}
