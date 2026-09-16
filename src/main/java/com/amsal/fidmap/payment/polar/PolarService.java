package com.amsal.fidmap.payment.polar;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolarService {

    private final PolarConfig polarConfig;

    private RestClient client() {

        return RestClient.builder()
                .baseUrl(polarConfig.getApiUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + polarConfig.getAccessToken()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    /**
     * Creates a Polar hosted checkout session.
     *
     * The FIDMAP workspace ID is stored in Polar metadata.
     * Polar propagates this metadata to the resulting
     * subscription and/or order.
     *
     * embedOrigin is required for Polar Embedded Checkout.
     */
    public PolarCheckoutResult createCheckout(
            UUID workspaceId,
            String productId,
            String successUrl,
            String returnUrl,
            String embedOrigin
    ) {

        requireWorkspaceId(workspaceId);
        requireValue(productId, "Polar product ID");
        requireValue(successUrl, "Checkout success URL");
        requireValue(returnUrl, "Checkout return URL");
        requireValue(embedOrigin, "Checkout embed origin");

        Map<String, Object> body = Map.of(
                "product_id",
                productId,

                "metadata",
                Map.of(
                        "workspace_id",
                        workspaceId.toString()
                ),

                "success_url",
                successUrl,

                "return_url",
                returnUrl,

                "embed_origin",
                embedOrigin
        );

        try {

            PolarCheckoutResponse response =
                    client()
                            .post()
                            .uri("/checkouts/")
                            .body(body)
                            .retrieve()
                            .body(PolarCheckoutResponse.class);

            if (response == null) {

                throw new IllegalStateException(
                        "Polar returned an empty checkout response"
                );
            }

            if (response.id() == null) {

                throw new IllegalStateException(
                        "Polar checkout response does not contain an ID"
                );
            }

            if (response.url() == null
                    || response.url().isBlank()) {

                throw new IllegalStateException(
                        "Polar checkout response does not contain a checkout URL"
                );
            }

            return new PolarCheckoutResult(
                    response.id(),
                    response.url()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to create Polar checkout session",
                    e
            );
        }
    }

    /**
     * Schedules a recurring subscription for cancellation
     * at the end of the current billing period.
     */
    public void cancelSubscription(
            String providerSubscriptionId
    ) {

        requireValue(
                providerSubscriptionId,
                "Provider subscription ID"
        );

        Map<String, Object> body = Map.of(
                "cancel_at_period_end",
                true
        );

        try {

            client()
                    .patch()
                    .uri(
                            "/subscriptions/{id}",
                            providerSubscriptionId
                    )
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to schedule Polar subscription cancellation",
                    e
            );
        }
    }

    /**
     * Changes the product of an existing recurring subscription.
     *
     * Polar performs the billing/proration operation.
     *
     * The local FIDMAP subscription is updated from the
     * resulting Polar webhook rather than being changed
     * optimistically here.
     */
    public void changeSubscriptionPlan(
            String providerSubscriptionId,
            String newProductId
    ) {

        requireValue(
                providerSubscriptionId,
                "Provider subscription ID"
        );

        requireValue(
                newProductId,
                "New Polar product ID"
        );

        Map<String, Object> body = Map.of(
                "product_id",
                newProductId,

                "proration_behavior",
                "prorate"
        );

        try {

            client()
                    .patch()
                    .uri(
                            "/subscriptions/{id}",
                            providerSubscriptionId
                    )
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to change Polar subscription plan",
                    e
            );
        }
    }

    private void requireWorkspaceId(UUID workspaceId) {

        if (workspaceId == null) {

            throw new IllegalArgumentException(
                    "Workspace ID is required"
            );
        }
    }

    private void requireValue(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }
    }

    public record PolarCheckoutResult(
            UUID checkoutId,
            String checkoutUrl
    ) {
    }

    private record PolarCheckoutResponse(
            UUID id,
            String url
    ) {
    }
}
