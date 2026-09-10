package com.amsal.fidmap.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;


@Getter
@AllArgsConstructor
public class BoardVoteStatusDto {

    private UUID feedbackPostId;
    private Long voteCount;
    private boolean votedByCurrentEndUser;
}