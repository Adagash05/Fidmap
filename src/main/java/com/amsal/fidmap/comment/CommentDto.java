package com.amsal.fidmap.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CommentDto {

    private UUID id;

    private String message;

    private Long votes;

    private String endUserName;

    private LocalDate createdAt;
}

