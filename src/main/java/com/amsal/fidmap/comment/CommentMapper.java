package com.amsal.fidmap.comment;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {

        CommentDto dto = new CommentDto();

        dto.setId(comment.getId());
        dto.setMessage(comment.getMessage());
        dto.setVotes(comment.getVotes());
        dto.setEndUserName(comment.getEndUser().getName());
        dto.setCreatedAt(LocalDate.now());

        if (comment.getEndUser() != null) {
            dto.setEndUserName(comment.getEndUser().getName());
        }

        return dto;
    }
}

