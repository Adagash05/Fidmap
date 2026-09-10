package com.amsal.fidmap.endUser;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.EndUserException;
import com.amsal.fidmap.exception.WorkspaceNotFoundException;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.workspace.Workspace;
import com.amsal.fidmap.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndUserService implements IEndUserService{

    private final EndUserRepository endUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PlanEntitlementService planEntitlementService;


//todo
    public EndUser processGoogleUser(OidcUser oidcUser) {

        String googleSubject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        Optional<EndUser> existingUser = endUserRepository.findByGoogleSubject(googleSubject);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        EndUser endUser = EndUser.builder()
                .googleSubject(googleSubject)
                .email(email)
                .name(name)
                .profilePicture(picture)
                .emailVerified(true)
                .build();

        return endUserRepository.save(endUser);
    }


    @Override
    public ApiResponse<EndUserDto> createEndUser(UUID workspaceId, EndUserDto dto) {

        Workspace workspace = workspaceRepository.findWorkspaceById(workspaceId);
        if (workspace == null) {
            throw new WorkspaceNotFoundException("cannot create user in a workspace that those not exists,please try again later");
        }

        long currentEndUsers = endUserRepository.countByWorkspaceId(workspace.getId());
        planEntitlementService.checkEndUserLimit(workspaceId,currentEndUsers);

        boolean existingUSer = endUserRepository.existsByEmail(dto.getEmail());
        if (existingUSer) {
            throw new EndUserException("this user already exist in this workspace");
        }

        EndUser newEndUSer = new EndUser();
        newEndUSer.setName(dto.getName());
        newEndUSer.setEmail(dto.getEmail());
        newEndUSer.setWorkspaces(List.of(workspace));


        var savedUser = endUserRepository.save(newEndUSer);

        dto.setId(savedUser.getId());

        return ApiResponse.success("new user is created successfully", dto);
    }
}
