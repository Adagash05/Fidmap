package com.amsal.fidmap.payment.polar;

import com.amsal.fidmap.payment.billing.BillingPlan;
import com.amsal.fidmap.payment.subscription.Subscription;
import com.amsal.fidmap.payment.subscription.SubscriptionRepository;
import com.amsal.fidmap.payment.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolarWebhookService {

    private final ObjectMapper objectMapper;
    private final PolarWebhookEventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PolarConfig polarConfig;

    @Transactional
    public void process(
            String webhookId,
            String rawBody
    ) {

        System.out.println("WEBHOOK PROCESSING..." + rawBody);

        try {
            if (webhookId == null || webhookId.isBlank()) {
                throw new IllegalArgumentException(
                        "Missing Polar webhook ID"
                );
            }

            if (rawBody == null || rawBody.isBlank()) {
                throw new IllegalArgumentException(
                        "Missing Polar webhook body"
                );
            }

            /*
             * Idempotency.
             *
             * Polar's webhook ID comes from the
             * webhook-id HTTP header.
             */
            if (eventRepository.existsByProviderEventId(webhookId)) {
                return;
            }

            JsonNode root = objectMapper.readTree(rawBody);

            String eventType =
                    root.path("type").asText(null);

            JsonNode data =
                    root.path("data");

            if (eventType == null
                    || eventType.isBlank()
                    || data.isMissingNode()
                    || !data.isObject()) {

                throw new IllegalArgumentException(
                        "Invalid Polar webhook payload"
                );
            }

            switch (eventType) {

                case "subscription.created",
                     "subscription.updated",
                     "subscription.active",
                     "subscription.uncanceled",
                     "subscription.canceled",
                     "subscription.past_due",
                     "subscription.revoked" ->
                        handleSubscription(
                                eventType,
                                data
                        );

                case "order.paid" ->
                        handlePaidOrder(data);

                default ->
                        System.out.println(
                                "Ignoring Polar event: "
                                        + eventType
                        );
            }

            /*
             * Only mark the event as processed after
             * successful business processing.
             */
            eventRepository.save(
                    PolarWebhookEvent.builder()
                            .providerEventId(webhookId)
                            .eventType(eventType)
                            .processedAt(Instant.now())
                            .build()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to process Polar webhook",
                    e
            );
        }
    }

    private void handleSubscription(
            String eventType,
            JsonNode data
    ) {

        String providerSubscriptionId =
                data.path("id").asText(null);

        if (providerSubscriptionId == null
                || providerSubscriptionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Polar subscription ID"
            );
        }

        String customerId =
                data.path("customer_id").asText(null);

        String productId =
                data.path("product_id").asText(null);

        if (productId == null || productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Polar product ID"
            );
        }

        BillingPlan plan =
                determinePlan(productId);

        /*
         * First try to find the existing subscription
         * using Polar's subscription ID.
         */
        Subscription subscription =
                subscriptionRepository
                        .findByProviderSubscriptionId(
                                providerSubscriptionId
                        )
                        .orElse(null);

        /*
         * If this is a new subscription, identify the
         * FIDMAP workspace from checkout metadata.
         */
        if (subscription == null) {

            UUID workspaceId =
                    extractWorkspaceId(data);

            subscription =
                    subscriptionRepository
                            .findByWorkspaceId(workspaceId)
                            .orElseGet(Subscription::new);

            subscription.setWorkspaceId(workspaceId);
        }

        subscription.setProviderCustomerId(customerId);

        subscription.setProviderSubscriptionId(
                providerSubscriptionId
        );

        subscription.setProviderProductId(productId);

        /*
         * A subscription webhook represents the
         * subscription itself, not a transaction.
         */
        subscription.setProviderTransactionId(null);

        subscription.setPlan(plan);

        /*
         * Polar's cancellation events need special handling.
         *
         * subscription.canceled can mean:
         * - cancel at period end
         * - immediate cancellation
         *
         * Polar documents that for end-of-period cancellation,
         * the subscription remains active until the period ends.
         */
        switch (eventType) {

            case "subscription.canceled" -> {

                boolean cancelAtPeriodEnd =
                        data.path("cancel_at_period_end")
                                .asBoolean(false);

                subscription.setCancelAtPeriodEnd(
                        cancelAtPeriodEnd
                );

                if (cancelAtPeriodEnd) {
                    subscription.setStatus(
                            SubscriptionStatus.ACTIVE
                    );
                } else {
                    subscription.setStatus(
                            SubscriptionStatus.CANCELED
                    );
                }
            }

            case "subscription.revoked" -> {

                subscription.setStatus(
                        SubscriptionStatus.CANCELED
                );

                subscription.setCancelAtPeriodEnd(
                        false
                );
            }

            case "subscription.uncanceled" -> {

                subscription.setStatus(
                        SubscriptionStatus.ACTIVE
                );

                subscription.setCancelAtPeriodEnd(
                        false
                );
            }

            case "subscription.active" -> {

                subscription.setStatus(
                        SubscriptionStatus.ACTIVE
                );

                subscription.setCancelAtPeriodEnd(
                        data.path("cancel_at_period_end")
                                .asBoolean(false)
                );
            }

            case "subscription.past_due" -> {

                subscription.setStatus(
                        SubscriptionStatus.PAST_DUE
                );

                subscription.setCancelAtPeriodEnd(
                        data.path("cancel_at_period_end")
                                .asBoolean(false)
                );
            }

            default -> {

                String status =
                        data.path("status").asText(null);

                subscription.setStatus(
                        mapStatus(status)
                );

                subscription.setCancelAtPeriodEnd(
                        data.path("cancel_at_period_end")
                                .asBoolean(false)
                );
            }
        }

        /*
         * This is now a real Polar subscription, so the
         * old local FIDMAP trial is no longer applicable.
         *
         * If Polar itself ever provides a trial, its dates
         * are stored below.
         */
        subscription.setTrialStartsAt(
                parseInstant(
                        data.path("trial_start").asText(null)
                )
        );

        subscription.setTrialEndsAt(
                parseInstant(
                        data.path("trial_end").asText(null)
                )
        );

        subscription.setCurrentPeriodStart(
                parseInstant(
                        data.path("current_period_start")
                                .asText(null)
                )
        );

        subscription.setCurrentPeriodEnd(
                parseInstant(
                        data.path("current_period_end")
                                .asText(null)
                )
        );

        subscriptionRepository.save(subscription);
    }

    private void handlePaidOrder(JsonNode data) {

        String orderId =
                data.path("id").asText(null);

        if (orderId == null || orderId.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Polar order ID"
            );
        }

        /*
         * Recurring subscriptions also generate paid orders.
         *
         * Their subscription state is handled by the
         * subscription webhooks, so we do not create/update
         * the Subscription entity here.
         */
        String subscriptionId =
                data.path("subscription_id").asText(null);

        if (subscriptionId != null
                && !subscriptionId.isBlank()) {
            return;
        }

        /*
         * No subscription_id means this can be a one-time
         * purchase, which is how FIDMAP Lifetime works.
         */
        String productId =
                data.path("product_id").asText(null);

        if (productId == null || productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Polar order product ID"
            );
        }

        BillingPlan plan =
                determinePlan(productId);

        /*
         * Currently FIDMAP only uses order.paid for
         * the Lifetime one-time product.
         */
        if (plan != BillingPlan.LIFETIME) {
            return;
        }

        UUID workspaceId =
                extractWorkspaceId(data);

        String customerId =
                data.path("customer_id").asText(null);

        Subscription subscription =
                subscriptionRepository
                        .findByWorkspaceId(workspaceId)
                        .orElseGet(Subscription::new);

        subscription.setWorkspaceId(workspaceId);

        subscription.setProviderCustomerId(customerId);

        subscription.setProviderTransactionId(orderId);

        subscription.setProviderProductId(productId);

        subscription.setProviderSubscriptionId(null);

        subscription.setPlan(BillingPlan.LIFETIME);

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        /*
         * Lifetime has no recurring billing period.
         */
        subscription.setCurrentPeriodStart(null);
        subscription.setCurrentPeriodEnd(null);

        subscription.setTrialStartsAt(null);
        subscription.setTrialEndsAt(null);

        subscription.setCancelAtPeriodEnd(false);

        subscriptionRepository.save(subscription);
    }

    private UUID extractWorkspaceId(JsonNode data) {

        JsonNode metadata =
                data.path("metadata");

        String workspaceIdString =
                metadata
                        .path("workspace_id")
                        .asText(null);

        if (workspaceIdString == null
                || workspaceIdString.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing workspace_id in Polar metadata"
            );
        }

        try {

            return UUID.fromString(workspaceIdString);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid workspace_id in Polar metadata",
                    e
            );
        }
    }

    private BillingPlan determinePlan(
            String productId
    ) {

        if (productId == null || productId.isBlank()) {

            throw new IllegalArgumentException(
                    "Polar product ID is required"
            );
        }

        if (productId.equals(
                polarConfig
                        .getProducts()
                        .getStartupMonthly()
        )) {

            return BillingPlan.STARTUP_MONTHLY;
        }

        if (productId.equals(
                polarConfig
                        .getProducts()
                        .getStartupYearly()
        )) {

            return BillingPlan.STARTUP_YEARLY;
        }

        if (productId.equals(
                polarConfig
                        .getProducts()
                        .getBusinessMonthly()
        )) {

            return BillingPlan.BUSINESS_MONTHLY;
        }

        if (productId.equals(
                polarConfig
                        .getProducts()
                        .getBusinessYearly()
        )) {

            return BillingPlan.BUSINESS_YEARLY;
        }

        if (productId.equals(
                polarConfig
                        .getProducts()
                        .getLifetime()
        )) {

            return BillingPlan.LIFETIME;
        }

        throw new IllegalArgumentException(
                "Unknown Polar product ID: " + productId
        );
    }

    private SubscriptionStatus mapStatus(
            String status
    ) {

        if (status == null || status.isBlank()) {

            throw new IllegalArgumentException(
                    "Missing Polar subscription status"
            );
        }

        return switch (status) {

            case "active" ->
                    SubscriptionStatus.ACTIVE;

            case "trialing" ->
                    SubscriptionStatus.TRIALING;

            case "past_due" ->
                    SubscriptionStatus.PAST_DUE;

            /*
             * FIDMAP does not currently have an INCOMPLETE
             * or UNPAID local status.
             *
             * Both states must not grant normal access.
             */
            case "incomplete" ->
                    SubscriptionStatus.PAST_DUE;

            case "unpaid" ->
                    SubscriptionStatus.CANCELED;

            case "canceled" ->
                    SubscriptionStatus.CANCELED;

            default ->
                    throw new IllegalArgumentException(
                            "Unknown Polar subscription status: "
                                    + status
                    );
        };
    }

    private Instant parseInstant(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return Instant.parse(value);

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid Polar timestamp: " + value,
                    e
            );
        }
    }
}