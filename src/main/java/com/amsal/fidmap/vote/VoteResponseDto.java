package com.amsal.fidmap.vote;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Data
public class VoteResponseDto {
    private UUID id;

    private long voteCount;

    private boolean votedCurrentEndUser;

}
