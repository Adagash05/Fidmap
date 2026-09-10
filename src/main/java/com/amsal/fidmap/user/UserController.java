package com.amsal.fidmap.user;

import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me() {


        return ResponseEntity.status(OK).body(userService.me());
    }

    @PostMapping("/{workspace-id}/new-user")
    public ResponseEntity<ApiResponse<UserDto>> createNewUserInWorkspace(
            @RequestBody AddUserRequest request,
            @PathVariable("workspace-id") UUID workspaceId) {

        return ResponseEntity
                .status(CREATED)
                .body(userService.createNewUserInWorkspace(request, workspaceId));
    }

    @GetMapping("/{workspace-id}/get-all-users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsersInWorkspace(@PathVariable("workspace-id") UUID workspaceId) {

        return ResponseEntity
                .status(OK)
                .body(userService.getAllUsersInWorkspace(workspaceId));
    }

    //todo redundant
    @GetMapping("/{user-id}/user")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable("user-id") UUID userId) {

        return ResponseEntity
                .status(OK)
                .body(userService.getUser(userId));
    }

    @PutMapping("/{user-id}/user")
    public ResponseEntity<ApiResponse<UserDto>> editUserInWorkspace(
            @RequestBody UpdateUserRequest request,
            @PathVariable("user-id") UUID userId) {

        return ResponseEntity
                .status(OK)
                .body(userService.editUserInWorkspace(request, userId));

    }

    @DeleteMapping("/{user-id}/user")
    public ResponseEntity<ApiResponse<Void>> deleteUserInWorkspace(@PathVariable("user-id") UUID userId) {

        userService.deleteUserInWorkspace(userId);

        return ResponseEntity.ok().build();
    }

    // NEW
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity
                .status(OK)
                .body(userService.changePassword(request));
    }


    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateMyProfile(@Valid @RequestBody UpdateOwnProfileRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateOwnProfile(currentUserId, request));
    }

}
