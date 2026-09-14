package com.amsal.fidmap.payment.polar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class PolarWebhookVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SECRET_PREFIX = "whsec_";

    /**
     * Verifies Polar webhook requests using the
     * Standard Webhooks signature format.
     *
     * Headers:
     *
     * webhook-id
     * webhook-timestamp
     * webhook-signature
     *
     * Signed payload:
     *
     * webhook-id + "." + webhook-timestamp + "." + rawBody
     *
     * Supports both:
     *
     * 1. Standard Webhooks key derivation:
     *    whsec_<base64> -> strip prefix -> Base64 decode
     *
     * 2. Polar's current signing behavior:
     *    use the complete webhook secret as UTF-8 bytes
     */
    public boolean verify(
            String rawBody,
            String webhookId,
            String webhookTimestamp,
            String webhookSignature,
            String secret
    ) {

        if (rawBody == null
                || webhookId == null
                || webhookId.isBlank()
                || webhookTimestamp == null
                || webhookTimestamp.isBlank()
                || webhookSignature == null
                || webhookSignature.isBlank()
                || secret == null
                || secret.isBlank()) {

            return false;
        }

        try {

            long timestamp =
                    Long.parseLong(webhookTimestamp);

            long now =
                    System.currentTimeMillis() / 1000;

            /*
             * Reject replayed or excessively old/future
             * webhook requests.
             */
            if (Math.abs(now - timestamp) > 300) {
                return false;
            }

            String signedPayload =
                    webhookId
                            + "."
                            + webhookTimestamp
                            + "."
                            + rawBody;

            byte[] payloadBytes =
                    signedPayload.getBytes(
                            StandardCharsets.UTF_8
                    );

            /*
             * Try the Standard Webhooks key derivation.
             */
            byte[] standardKey =
                    resolveStandardWebhooksKey(secret);

            if (matchesSignature(
                    standardKey,
                    payloadBytes,
                    webhookSignature
            )) {

                return true;
            }

            /*
             * Also try Polar's literal-secret key derivation.
             *
             * This is required for Polar webhook deliveries
             * that are signed using the literal secret bytes.
             */
            byte[] polarKey =
                    secret.getBytes(
                            StandardCharsets.UTF_8
                    );

            return matchesSignature(
                    polarKey,
                    payloadBytes,
                    webhookSignature
            );

        } catch (Exception e) {

            return false;
        }
    }

    private byte[] resolveStandardWebhooksKey(
            String secret
    ) {

        String encodedSecret = secret;

        if (encodedSecret.startsWith(SECRET_PREFIX)) {

            encodedSecret =
                    encodedSecret.substring(
                            SECRET_PREFIX.length()
                    );
        }

        /*
         * Standard Webhooks secrets may be unpadded
         * Base64, so add padding when necessary.
         */
        int remainder =
                encodedSecret.length() % 4;

        if (remainder != 0) {

            encodedSecret +=
                    "=".repeat(4 - remainder);
        }

        return Base64.getDecoder().decode(
                encodedSecret
        );
    }

    private boolean matchesSignature(
            byte[] key,
            byte[] payload,
            String webhookSignature
    ) throws Exception {

        Mac mac =
                Mac.getInstance(HMAC_SHA256);

        SecretKeySpec secretKey =
                new SecretKeySpec(
                        key,
                        HMAC_SHA256
                );

        mac.init(secretKey);

        byte[] expectedDigest =
                mac.doFinal(payload);

        /*
         * Polar may provide multiple signatures during
         * secret rotation:
         *
         * v1,<base64> v1,<base64>
         */
        String[] signatures =
                webhookSignature.split("\\s+");

        for (String signature : signatures) {

            String[] parts =
                    signature.split(",", 2);

            if (parts.length != 2) {
                continue;
            }

            if (!"v1".equals(parts[0])) {
                continue;
            }

            byte[] providedSignature;

            try {

                providedSignature =
                        Base64.getDecoder().decode(parts[1]);

            } catch (IllegalArgumentException e) {

                continue;
            }

            if (MessageDigest.isEqual(
                    expectedDigest,
                    providedSignature
            )) {

                return true;
            }
        }

        return false;
    }
}