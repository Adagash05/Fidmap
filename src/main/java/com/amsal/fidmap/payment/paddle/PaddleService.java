package com.amsal.fidmap.payment.paddle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PaddleService {

    private final PaddleConfig paddleConfig;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl(paddleConfig.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paddleConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    public String changeSubscriptionPlan(
            String paddleSubscriptionId,
            String newPriceId
    ) {
        if (paddleSubscriptionId == null || paddleSubscriptionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Paddle subscription ID is required"
            );
        }

        if (newPriceId == null || newPriceId.isBlank()) {
            throw new IllegalArgumentException(
                    "New Paddle price ID is required"
            );
        }

        try {

            return client()
                    .patch()
                    .uri(
                            "/subscriptions/{id}",
                            paddleSubscriptionId
                    )
                    .body("""
                {
                  "items": [
                    {
                      "price_id": "%s",
                      "quantity": 1
                    }
                  ],
                  "proration_billing_mode": "prorated_immediately",
                  "on_payment_failure": "prevent_change"
                }
                """.formatted(newPriceId))
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to change Paddle subscription plan",
                    e
            );
        }
    }

    /**
     * Schedules cancellation of a Paddle subscription
     * at the end of its current billing period.
     *
     * The Paddle webhook remains the source of truth
     * for the final subscription status.
     */
    public String cancelSubscription(String paddleSubscriptionId) {

        if (paddleSubscriptionId == null || paddleSubscriptionId.isBlank()) {

            throw new IllegalArgumentException("Paddle subscription ID is required");
        }

        try {

            return client()
                    .post()
                    .uri("/subscriptions/{id}/cancel", paddleSubscriptionId)
                    .body("""
                            {
                              "effective_from": "next_billing_period"
                            }
                            """)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {

            throw new IllegalStateException("Failed to cancel Paddle subscription: "
                            + paddleSubscriptionId, e);
        }
    }
}
