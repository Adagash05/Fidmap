package com.amsal.fidmap.user;


import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.exception.UserAlreadyExistsException;
import com.amsal.fidmap.exception.UserNotFoundException;
import com.amsal.fidmap.jwt.JwtService;
import com.amsal.fidmap.payment.billing.PlanEntitlementService;
import com.amsal.fidmap.security.SecurityUtils;
import com.amsal.fidmap.workspace.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.amsal.fidmap.user.Role.MEMBER;


@Component
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PlanEntitlementService planEntitlementService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("user is not found"));

    }

    @Transactional(readOnly = true)
    public ApiResponse<UserDto> me() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findUserById(userId);

        UserDto dto = userMapper.toUserDto(user);
        dto.setWorkspaceId(user.getWorkspace().getId());

        return ApiResponse.success("current user", dto);
    }


    @Transactional
    public ApiResponse<UserDto> createNewUserInWorkspace(AddUserRequest request, UUID workspaceId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findUserById(currentUserId);
        if (currentUser == null) {
            throw new UserNotFoundException("user not found");
        }
        requireOwnerInWorkspace(currentUser, workspaceId);

        if (userRepository.existsByEmailAndWorkspaceId(request.getEmail(), workspaceId)) {
            throw new UserAlreadyExistsException("A user with this email already exists in this workspace");
        }


        //Plan Limit check
        long currentUsers = userRepository.countByWorkspaceId(workspaceId);
        planEntitlementService.checkTeamMemberLimit(workspaceId,currentUsers);


        User newUser = new User();
        newUser.setWorkspace(currentUser.getWorkspace());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // New users are members by default.
        newUser.setRole(MEMBER);
        User savedUser = userRepository.save(newUser);
        return ApiResponse.success("new user is created in the workspace successfully", userMapper.toUserDto(savedUser));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<UserDto>> getAllUsersInWorkspace(UUID workspaceId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findUserById(currentUserId);
        if (currentUser == null) {
            throw new UserNotFoundException("user not found");
        }
        requireOwnerInWorkspace(currentUser, workspaceId);
        List<UserDto> users = userRepository.findAllByWorkspace(currentUser.getWorkspace()).stream().map(userMapper::toUserDto).toList();
        return ApiResponse.success("retrieved all users in the workspace successfully", users);
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserDto> getUser(UUID userId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findUserById(currentUserId);
        if (currentUser == null) {
            throw new UserNotFoundException("user not found");
        }
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only the workspace owner can manage users");
        }
        User targetUser = userRepository.findUserById(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("user not found");
        }
        requireSameWorkspace(currentUser, targetUser);
        return ApiResponse.success("user retrieved successfully", userMapper.toUserDto(targetUser));
    }

    @Transactional
    public ApiResponse<UserDto> editUserInWorkspace(UpdateUserRequest request, UUID userId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findUserById(currentUserId);
        if (currentUser == null) {
            throw new UserNotFoundException("user not found");
        }
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only the workspace owner can manage users");
        }
        User targetUser = userRepository.findUserById(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("user not found");
        } /* * CRITICAL: * * Never move a user between workspaces merely because * the caller supplied a user UUID. */
        requireSameWorkspace(currentUser, targetUser); /* * Only check email uniqueness if the email is actually * being changed. */
        if (!targetUser.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmailAndWorkspaceId(request.getEmail(), currentUser.getWorkspace().getId())) {
            throw new UserAlreadyExistsException("A user with this email already exists in this workspace");
        }
        targetUser.setFullName(request.getFullName());
        targetUser.setEmail(request.getEmail()); /* * Role can be changed by the OWNER. */
        if (request.getRole() != null) {
            targetUser.setRole(request.getRole());
        } /* * IMPORTANT: * * Do NOT do this: * * targetUser.setWorkspace(currentUser.getWorkspace()); * * We already verified that both users belong to the * same workspace. */
        User savedUser = userRepository.save(targetUser);
        return ApiResponse.success("user updated successfully", userMapper.toUserDto(savedUser));
    }

    @Transactional
    public void deleteUserInWorkspace(UUID userId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findUserById(currentUserId);
        if (currentUser == null) {
            throw new UserNotFoundException("user not found");
        }
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only the workspace owner can manage users");
        }
        User targetUser = userRepository.findUserById(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("this user does not exist");
        }
        /* * CRITICAL: * *
        An OWNER must not be able to delete a user from *
        another workspace simply by knowing their UUID. */

        requireSameWorkspace(currentUser, targetUser);
        userRepository.delete(targetUser);
    }

    @Transactional
    public ApiResponse<UserDto> updateOwnProfile(UUID userId, UpdateOwnProfileRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        /* * Do not trust a userId supplied by the client. *
        The endpoint must only modify the authenticated user. */

        if (!currentUserId.equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        User user = userRepository.findUserById(currentUserId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }
        String newEmail = request.getEmail().trim(); /* * Prevent changing to another user's email inside * the same workspace. */
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmailAndWorkspaceId(newEmail, user.getWorkspace().getId())) {
            throw new UserAlreadyExistsException("A user with this email already exists in this workspace");
        }
        user.setFullName(request.getFullName().trim());
        user.setEmail(newEmail);
        User savedUser = userRepository.save(user);
        return ApiResponse.success("profile updated successfully", userMapper.toUserDto(savedUser));
    }

    /* * ============================================================ * Authorization helpers * ============================================================ */

    private void requireOwnerInWorkspace(User currentUser, UUID workspaceId) {
        if (currentUser.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only the workspace owner can manage users");
        }
        if (workspaceId == null || currentUser.getWorkspace() == null || !workspaceId.equals(currentUser.getWorkspace().getId())) {
            throw new AccessDeniedException("You do not have access to this workspace");
        }
    }

    private void requireSameWorkspace(User currentUser, User targetUser) {
        if (currentUser.getWorkspace() == null || targetUser.getWorkspace() == null || !currentUser.getWorkspace().getId().equals(targetUser.getWorkspace().getId())) {
            throw new AccessDeniedException("You do not have access to this user");
        }
    }





    @Transactional
    public ApiResponse<Void> changePassword(ChangePasswordRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findUserById(currentUserId);

        if (user == null) {
            throw new UserNotFoundException("user not found");
        }

        // Check that the current password is correct
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check that new password and confirmation match
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            //todo
            throw new IllegalArgumentException(
                    "New password and confirmation password do not match"
            );
        }

        // Prevent using the same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password must be different from your current password"
            );
        }

        // Hash the new password before saving
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        return ApiResponse.success(
                "Password changed successfully",
                null
        );
    }

    @Transactional
    public ApiResponse<UserDto> updateOwnProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new UserNotFoundException("user not found");

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        // deliberately do NOT allow role/workspace changes through this endpoint

        var saved = userRepository.save(user);
        return ApiResponse.success("profile updated", userMapper.toUserDto(saved));
    }

}
