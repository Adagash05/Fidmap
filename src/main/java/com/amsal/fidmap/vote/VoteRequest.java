package com.amsal.fidmap.vote;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequest {
    private String endUserName;
    private String endUserEmail;
}