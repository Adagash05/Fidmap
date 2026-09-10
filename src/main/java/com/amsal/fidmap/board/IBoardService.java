package com.amsal.fidmap.board;

import com.amsal.fidmap.apiResponse.ApiResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface IBoardService {


     ApiResponse<BoardDto> createBoard(BoardDto boardDto, UUID workspaceId);


     ApiResponse<List<BoardDto>> getAllBoardsByWorkspace(UUID workspaceId);

    public ApiResponse<List<BoardDto>> getPublicBoardsByWorkspace(UUID workspaceId);

    ApiResponse<BoardDto> getBoard(UUID boarddId);


     ApiResponse<BoardDto> editBoard(UUID boardId, UpdateBoard request);

//     ApiResponse<BoardDto> editBoardName(UUID boardId, String boardName);

     ApiResponse<BoardDto> editBoardIsPublic(UUID boardId, boolean isPublic);


     ApiResponse<Void> deleteBoard(UUID boardId);

}
