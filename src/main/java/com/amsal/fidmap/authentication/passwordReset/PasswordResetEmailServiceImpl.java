package com.amsal.fidmap.authentication.passwordReset;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailServiceImpl
        implements PasswordResetEmailService {

    private final Resend resend;

    @Value("${fidmap.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(
            String email,
            String fullName,
            String rawToken
    ) throws ResendException {

        String resetUrl =
                frontendUrl + "/reset-password?token=" + rawToken;

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #111827;">
                    <div style="max-width: 560px; margin: 40px auto; padding: 24px;">
                        
                        <h2 style="margin-bottom: 24px;">
                            Reset your FIDMAP password
                        </h2>

                        <p>
                            Hi %s,
                        </p>

                        <p>
                            We received a request to reset the password
                            for your FIDMAP account.
                        </p>

                        <p style="margin: 32px 0;">
                            <a href="%s"
                               style="
                                   display: inline-block;
                                   padding: 12px 20px;
                                   background: #111827;
                                   color: white;
                                   text-decoration: none;
                                   border-radius: 6px;
                               ">
                                Reset password
                            </a>
                        </p>

                        <p>
                            This link will expire in 30 minutes and can
                            only be used once.
                        </p>

                        <p>
                            If you did not request a password reset,
                            you can safely ignore this email.
                        </p>

                        <p style="margin-top: 32px;">
                            — The FIDMAP team
                        </p>

                    </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(fullName),
                resetUrl
        );

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("FIDMAP <onboarding@resend.dev>")
                .to(email)
                .subject("Reset your FIDMAP password")
                .html(html)
                .build();

        resend.emails().send(params);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}