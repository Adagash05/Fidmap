package com.amsal.fidmap.authentication.passwordReset;


import com.resend.core.exception.ResendException;

public interface PasswordResetEmailService {

    void sendPasswordResetEmail(
            String email,
            String fullName,
            String rawToken
    ) throws ResendException;
}