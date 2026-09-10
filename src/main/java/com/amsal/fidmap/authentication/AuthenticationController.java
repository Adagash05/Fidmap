package com.amsal.fidmap.authentication;


import com.amsal.fidmap.apiResponse.ApiResponse;
import com.amsal.fidmap.authentication.dto.ForgotPasswordRequest;
import com.amsal.fidmap.authentication.dto.ResetPasswordRequest;
import com.amsal.fidmap.authentication.passwordReset.PasswordResetService;
import com.amsal.fidmap.user.CreateFirstUserRequest;
import com.amsal.fidmap.user.UserDto;
import com.amsal.fidmap.user.UserService;
import com.resend.core.exception.ResendException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;


    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<UserDto>> createFirstUserAndWorkspace(
            @RequestBody CreateFirstUserRequest request) {

        return ResponseEntity
                .status(CREATED)
                .body(authenticationService.createFirstUserAndWorkspace(request.getFirstUser(), request.getDto()));

    }

    //todo
    //might delete this later
    @PostMapping("/personal-register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        //admin
        return ResponseEntity
                .status(OK)
                .body(authenticationService.registerSuperAdmin(request));
    }

    @PostMapping(value = "/log-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody
            AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));

    }

    @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public void refreshToken(

            HttpServletRequest request,

            HttpServletResponse response
    ) throws IOException {

        authenticationService.refreshToken(request, response);


    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) throws ResendException {

        passwordResetService.forgotPassword(request.email());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists for this email, " +
                                "a password reset link has been sent.",
                        null
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully.",
                        null
                )
        );
    }
}
