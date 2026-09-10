package com.amsal.fidmap.vote;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VoteDto {
    private UUID feedbackPostId;
    private Long voteCount;
    private boolean votedByCurrentEndUser;
}