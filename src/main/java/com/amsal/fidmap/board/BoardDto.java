package com.amsal.fidmap.board;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BoardDto {

    private UUID id;

    private String name;

    private String description;

    private Boolean isPublic;

}
