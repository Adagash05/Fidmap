package com.amsal.fidmap.payment.paddle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/paddle")
@RequiredArgsConstructor
public class PaddleWebhookController {

    private final PaddleConfig paddleConfig;
    private final PaddleWebhookVerifier verifier;
    private final PaddleWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(
                    value = "Paddle-Signature",
                    required = false
            )
            String signature,
            @RequestBody
            String rawBody
    ) {

        if (signature == null || signature.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        boolean valid = verifier.verify(
                rawBody,
                signature,
                paddleConfig.getWebhookSecret()
        );

        if (!valid) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        webhookService.process(rawBody);

        return ResponseEntity.ok().build();
    }
}