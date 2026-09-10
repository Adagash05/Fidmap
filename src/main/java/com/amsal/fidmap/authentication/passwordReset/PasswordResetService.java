package com.amsal.fidmap.authentication.passwordReset;

import com.amsal.fidmap.token.TokenRepository;
import com.amsal.fidmap.user.User;
import com.amsal.fidmap.user.UserRepository;
import com.resend.core.exception.ResendException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired reset token"
                                ));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User account not found"
                        )
                );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate existing authentication tokens
        tokenRepository.deleteAllByUserId(user.getId());

        // Remove all reset tokens for this user
        passwordResetTokenRepository.deleteAllByUserId(user.getId());
    }

    @Transactional
    public void forgotPassword(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        Optional<User> userOptional =
                userRepository.findByEmail(normalizedEmail);

        // Do not reveal whether the account exists.
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // Invalidate any previous reset token.
        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .userId(user.getId())
                        .tokenHash(tokenHash)
                        .expiresAt(
                                Instant.now().plus(30, ChronoUnit.MINUTES)
                        )
                        .used(false)
                        .build();

        passwordResetTokenRepository.save(resetToken);

        try {
            passwordResetEmailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    rawToken
            );
        } catch (ResendException e) {
            throw new IllegalStateException(
                    "Failed to send password reset email",
                    e
            );
        }
    }

    private String generateToken() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable",
                    e
            );
        }
    }
}
