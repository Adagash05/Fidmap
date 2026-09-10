package com.amsal.fidmap.workspace;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WorkspaceDto {

    private UUID id;
    private String name;
    private String slug;

}
