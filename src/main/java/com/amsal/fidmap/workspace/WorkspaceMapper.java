package com.amsal.fidmap.workspace;

import org.springframework.stereotype.Service;

@Service
public class WorkspaceMapper {

    public Workspace toWorkspace(WorkspaceDto dto){

        Workspace workspace = new Workspace();
        workspace.setName(dto.getName());

        return workspace;
    }

    public WorkspaceDto toWorkspaceDto(Workspace workspace) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setSlug(workspace.getSlug());
        return dto;
    }
}
