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

        if (user == null) {
            throw new UserNotFoundException("user not found");
        }

        UserDto dto = userMapper.toUserDto(user);


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

        String newEmail = normalizeEmail(request.getEmail());

        // Login resolves users by email globally, so email must be globally unique.
        if (userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        //Plan Limit check
        long currentUsers = userRepository.countByWorkspaceId(workspaceId);
        planEntitlementService.checkTeamMemberLimit(workspaceId,currentUsers);


        User newUser = new User();
        newUser.setWorkspace(currentUser.getWorkspace());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(newEmail);
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
        requireManagerInWorkspace(currentUser, workspaceId);
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
        String editedEmail = normalizeEmail(request.getEmail());

        if (!targetUser.getEmail().equalsIgnoreCase(editedEmail) && userRepository.existsByEmailIgnoreCase(editedEmail)) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }
        targetUser.setFullName(request.getFullName());
        targetUser.setEmail(editedEmail);

        if (request.getRole() != null && request.getRole() != targetUser.getRole()) {

            // Workspace roles only: never SUPER_ADMIN / PARTNER (privilege escalation).
            if (request.getRole() != Role.OWNER && request.getRole() != Role.ADMIN && request.getRole() != MEMBER) {
                throw new AccessDeniedException("That role cannot be assigned");
            }

            // A workspace must always keep at least one owner.
            if (targetUser.getRole() == Role.OWNER
                    && userRepository.countByWorkspaceIdAndRole(currentUser.getWorkspace().getId(), Role.OWNER) <= 1) {
                throw new IllegalStateException(
                        "A workspace must keep at least one owner. Promote another owner first."
                );
            }

            targetUser.setRole(request.getRole());
        }
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

        if (targetUser.getRole() == Role.OWNER
                && userRepository.countByWorkspaceIdAndRole(currentUser.getWorkspace().getId(), Role.OWNER) <= 1) {
            throw new IllegalStateException(
                    "The last owner of a workspace cannot be removed. Transfer ownership first."
            );
        }

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
        newEmail = normalizeEmail(newEmail);
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new UserAlreadyExistsException("A user with this email already exists");
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

    private void requireManagerInWorkspace(User currentUser, UUID workspaceId) {
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only a workspace owner or admin can view the team");
        }
        if (workspaceId == null || currentUser.getWorkspace() == null || !workspaceId.equals(currentUser.getWorkspace().getId())) {
            throw new AccessDeniedException("You do not have access to this workspace");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || !email.trim().matches("^[^\\s@]{1,64}@[^\\s@]{1,190}\\.[^\\s@]{2,}$")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        return email.trim().toLowerCase();
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

}
