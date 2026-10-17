package com.amsal.fidmap.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around Resend so every transactional email goes through one
 * place and callers receive the PROVIDER message id (proof the provider
 * accepted the message) rather than assuming delivery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    /**
     * Resend's shared test sender. It can ONLY deliver to the email address of the
     * Resend account owner; every other recipient is rejected by the provider.
     * Production must set RESEND_FROM to an address on a verified domain.
     */
    public static final String TEST_SENDER = "FIDMAP <onboarding@resend.dev>";

    private final Resend resend;

    /** Empty when RESEND_FROM is unset OR set to an empty string (Spring only applies a default when the key is absent). */
    @Value("${resend.from:}")
    private String configuredFrom;

    private String from = TEST_SENDER;

    @PostConstruct
    void resolveSender() {

        from = resolveFrom(configuredFrom);

        if (TEST_SENDER.equals(from)) {
            log.warn("RESEND_FROM is not set: emails use Resend's test sender, which only delivers to the Resend "
                    + "account owner. Team invitations, password resets and request updates to anyone else will be "
                    + "rejected by the provider. Set RESEND_FROM to an address on a verified domain.");
        }
    }

    static String resolveFrom(String configured) {
        return configured == null || configured.isBlank() ? TEST_SENDER : configured.trim();
    }

    /** The sender every transactional email must use (shared with password reset). */
    public String sender() {
        return from;
    }

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
