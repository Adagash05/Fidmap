package com.amsal.fidmap.board;

import org.springframework.stereotype.Service;

@Service
public class BoardMapper {
    public Board toBoard(BoardDto boardDto) {

        Board board = new Board();
        board.setName(boardDto.getName());
        board.setDescription(boardDto.getDescription());
        board.setIsPublic(boardDto.getIsPublic() != null ? boardDto.getIsPublic() : true); //By Default every board is public unless it is set to be private

        return board;

    }

    public BoardDto toBoardDto(Board board) {

        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setIsPublic(board.getIsPublic());
        dto.setDescription(board.getDescription());

        return dto;
    }
}
