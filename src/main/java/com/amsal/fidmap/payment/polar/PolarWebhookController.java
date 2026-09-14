package com.amsal.fidmap.payment.polar;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/polar")
@RequiredArgsConstructor
public class PolarWebhookController {

    private final PolarConfig polarConfig;
    private final PolarWebhookVerifier verifier;
    private final PolarWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(
                    value = "webhook-id",
                    required = false
            )
            String webhookId,

            @RequestHeader(
                    value = "webhook-timestamp",
                    required = false
            )
            String webhookTimestamp,

            @RequestHeader(
                    value = "webhook-signature",
                    required = false
            )
            String webhookSignature,

            @RequestBody
            String rawBody
    ) {

        if (webhookId == null || webhookId.isBlank()
                || webhookTimestamp == null
                || webhookTimestamp.isBlank()
                || webhookSignature == null
                || webhookSignature.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        boolean valid =
                verifier.verify(
                        rawBody,
                        webhookId,
                        webhookTimestamp,
                        webhookSignature,
                        polarConfig.getWebhookSecret()
                );

        if (!valid) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        webhookService.process(
                webhookId,
                rawBody
        );

        return ResponseEntity.ok().build();
    }
}