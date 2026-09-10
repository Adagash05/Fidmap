package com.amsal.fidmap.board;

import com.amsal.fidmap.apiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/{workspace-id}") //change this and be getting it from the JWT token
    public ResponseEntity<ApiResponse<BoardDto>> createBoard(
            @RequestBody BoardDto dto,
            @PathVariable("workspace-id") UUID workspaceId
    ) {

        return ResponseEntity
                .status(CREATED)
                .body(boardService.createBoard(dto, workspaceId));

    }

    @GetMapping("/get-all/{workspace-id}")
    public ResponseEntity<ApiResponse<List<BoardDto>>> getAllBoardByWorkspace(
            @PathVariable("workspace-id") UUID workspaceId
    ) {
        return ResponseEntity
                .status(OK)
                .body(boardService.getAllBoardsByWorkspace(workspaceId));
    }

    @GetMapping("/public/workspace/{workspace-id}")
    public ResponseEntity<ApiResponse<List<BoardDto>>> getPublicBoardsByWorkspace(
            @PathVariable("workspace-id") UUID workspaceId
    ) {

        return ResponseEntity
                .status(OK)
                .body(boardService.getPublicBoardsByWorkspace(workspaceId));
    }

    @GetMapping("/{board-id}")
    public ResponseEntity<ApiResponse<BoardDto>> getBoard(@PathVariable("board-id") UUID boardId) {
        return ResponseEntity
                .status(OK)
                .body(boardService.getBoard(boardId));

    }

    @PutMapping("/{board-id}")
    public ResponseEntity<ApiResponse<BoardDto>> editBoard(
            @PathVariable("board-id") UUID boardId,
            @RequestBody UpdateBoard request
    ) {

        return ResponseEntity
                .status(ACCEPTED)
                .body(boardService.editBoard(boardId, request));
    }

    @PatchMapping("/board-is-public/{board-id}")
    public ResponseEntity<ApiResponse<BoardDto>> editBoardIsPublic(
            @PathVariable("board-id") UUID boardId,
            @RequestBody boolean isPublic
    ) {

        return ResponseEntity
                .status(ACCEPTED)
                .body(boardService.editBoardIsPublic(boardId, isPublic));
    }

    @DeleteMapping("/{board-id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable("board-id") UUID boardId
    ) {

        boardService.deleteBoard(boardId);

        return ResponseEntity.ok().build();
    }
}
