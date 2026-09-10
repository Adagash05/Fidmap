package com.amsal.fidmap.workspace;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.payment.billing.BillingService;
import com.amsal.fidmap.roadmap.Roadmap;
import com.amsal.fidmap.roadmap.RoadmapRepository;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService implements IWorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final RoadmapRepository roadmapRepository;
    private final WorkspaceSlugGenerator workspaceSlugGenerator;
    private final BillingService billingService;


    @Override
    public ApiResponse<WorkspaceDto> createWorkspace(WorkspaceDto workspaceDto, User user) {

        Workspace workspace = workspaceMapper.toWorkspace(workspaceDto);

        String slug = generateUniqueSlug(workspace.getName());
        workspace.setSlug(slug);
        workspace.addUserToWorkspace(user);

        Workspace saveWorkspace = workspaceRepository.save(workspace);
        billingService.startTrial(saveWorkspace.getId());




        //Adding user to workspace membership
//        workspaceMemberShipService.addUserToWorkspaceMembership(user.getId(), saveWorkspace.getId());

        //create a Roadmap for the new Workspace
        Roadmap roadmap = new Roadmap();
        roadmap.setWorkspace(workspace);
        roadmapRepository.save(roadmap);

        WorkspaceDto dto = workspaceMapper.toWorkspaceDto(saveWorkspace);

        return ApiResponse.success("workspace found", dto);


    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<WorkspaceDto> getWorkspace(UUID workspaceId) {

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
        if (workspace == null) {
            throw new WorkspaceNotFoundException("workspace not found");
        }

        WorkspaceDto dto = workspaceMapper.toWorkspaceDto(workspace);

        return ApiResponse.success("workspace found", dto);
    }

    @Transactional(readOnly = true)
    public ApiResponse<WorkspaceDto> getWorkspaceBySlug(String slug) {

        Workspace workspace = workspaceRepository
                .findBySlug(slug)
                .orElseThrow(() ->
                        new WorkspaceNotFoundException(
                                "Workspace not found"
                        )
                );

        WorkspaceDto dto = workspaceMapper.toWorkspaceDto(workspace);

        return ApiResponse.success(
                "workspace found successfully",
                dto
        );
    }



    private String generateUniqueSlug(String workspaceName) {

        String baseSlug = workspaceSlugGenerator.generate(workspaceName);

        String slug = baseSlug;

        int counter = 2;

        while (workspaceRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
