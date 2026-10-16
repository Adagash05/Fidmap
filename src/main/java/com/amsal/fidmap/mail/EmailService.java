package com.amsal.fidmap.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around Resend so every transactional email goes through one
 * place and callers receive the PROVIDER message id (proof the provider
 * accepted the message) rather than assuming delivery.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Resend resend;

    @Value("${resend.from:FIDMAP <onboarding@resend.dev>}")
    private String from;

    /**
     * @return provider message id (never null on success)
     * @throws EmailDeliveryException when the provider rejects or is unreachable
     */
    public String send(String to, String subject, String html) {

        try {

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);

            if (response == null || response.getId() == null || response.getId().isBlank()) {
                throw new EmailDeliveryException("Email provider returned no message id", null);
            }

            return response.getId();

        } catch (ResendException e) {
            throw new EmailDeliveryException("Email provider error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e instanceof EmailDeliveryException ed) {
                throw ed;
            }
            throw new EmailDeliveryException("Email send failed: " + e.getMessage(), e);
        }
    }

    public static String escape(String value) {

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
