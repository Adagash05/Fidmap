package com.amsal.fidmap.payment.paddle;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaddleWebhookVerifier {

    /*
     * Paddle's recommended/default timestamp tolerance
     * is 5 seconds.
     */
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 5;

    public boolean verify(
            String rawBody,
            String signatureHeader,
            String secret
    ) {

        if (rawBody == null || signatureHeader == null || signatureHeader.isBlank()
                || secret == null || secret.isBlank()) {

            return false;
        }

        String timestamp = null;
        List<String> providedSignatures = new ArrayList<>();

        /*
         * Paddle-Signature format:
         *
         * ts=1671552777;h1=abcdef...
         *
         * There may be more than one h1 during
         * secret rotation.
         */
        String[] parts = signatureHeader.split(";");

        for (String part : parts) {

            String[] keyValue = part.split("=", 2);

            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if ("ts".equals(key)) {

                timestamp = value;

            } else if ("h1".equals(key)) {

                providedSignatures.add(value);
            }
        }

        if (timestamp == null || timestamp.isBlank() || providedSignatures.isEmpty()) {

            return false;
        }

        long timestampSeconds;

        try {

            timestampSeconds = Long.parseLong(timestamp);

        } catch (NumberFormatException e) {

            return false;
        }

        /*
         * Protect against replay attacks.
         */
        long now = Instant.now().getEpochSecond();

        if (Math.abs(now - timestampSeconds) > TIMESTAMP_TOLERANCE_SECONDS) {

            return false;
        }

        /*
         * Paddle signs:
         *
         * timestamp + ":" + rawBody
         *
         * The raw request body must not be modified,
         * reformatted, or re-serialized.
         */
        String signedPayload = timestamp + ":" + rawBody;

        String expectedSignature = hmacSha256(signedPayload, secret);

        /*
         * Accept the webhook if any h1 signature
         * matches the computed signature.
         */
        for (String providedSignature : providedSignatures) {

            if (MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8))) {

                return true;
            }
        }

        return false;
    }

    private String hmacSha256(String payload, String secret) {

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(key);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder(hash.length * 2);

            for (byte b : hash) {

                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (Exception e) {

            throw new IllegalStateException("Unable to calculate webhook signature", e);
        }
    }
}
