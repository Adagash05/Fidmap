package com.amsal.fidmap.payment.paddle;

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
public class PaddleWebhookService {

    private final ObjectMapper objectMapper;
    private final PaddleWebhookEventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaddleConfig paddleConfig;

    @Transactional
    public void process(String rawBody) {

        try {

            System.out.println("WEBHOOK PROCESSING DATA...");
            JsonNode root = objectMapper.readTree(rawBody);

            String eventId = root.path("event_id").asText(null);

            String eventType = root.path("event_type").asText(null);

            System.out.println(
                    "Processing Paddle event: " + eventType +
                            " | eventId: " + eventId
            );

            JsonNode data = root.path("data");

            if (eventId == null || eventType == null || data.isMissingNode()) {

                throw new IllegalArgumentException("Invalid Paddle webhook payload");
            }

            /*
             * Idempotency.
             *
             * If Paddle sends the same event again,
             * do not process it twice.
             */
            if (eventRepository.existsByPaddleEventId(eventId)) {
                return;
            }

            /*
             * Process the actual business event first.
             *
             * The webhook event is recorded only after
             * successful processing.
             */
            switch (eventType) {

                case "subscription.created",
                     "subscription.updated",
                     "subscription.canceled" -> handleSubscription(data);

                case "transaction.completed" -> handleCompletedTransaction(data);

                default -> System.out.println("Ignoring Paddle event: " + eventType);
            }

            /*
             * Mark event as processed only after
             * successful handling.
             */
            eventRepository.save(
                    PaddleWebhookEvent.builder()
                            .paddleEventId(eventId)
                            .eventType(eventType)
                            .processedAt(Instant.now())
                            .build()
            );

        } catch (Exception e) {

            throw new IllegalStateException("Failed to process Paddle webhook", e);
        }
    }
    private void handleSubscription(JsonNode data) {

        String paddleSubscriptionId =
                data.path("id").asText(null);

        if (paddleSubscriptionId == null || paddleSubscriptionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing Paddle subscription ID"
            );
        }

        String customerId =
                data.path("customer_id").asText(null);

        String status =
                data.path("status").asText(null);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing Paddle subscription status"
            );
        }

        String priceId =
                extractSubscriptionPriceId(data);

        BillingPlan plan =
                determinePlan(priceId);

        /*
         * First identify the exact Paddle subscription.
         */
        Subscription subscription =
                subscriptionRepository
                        .findByPaddleSubscriptionId(paddleSubscriptionId)
                        .orElse(null);

        /*
         * If this Paddle subscription does not exist locally yet,
         * use FIDMAP workspace_id from Paddle custom_data.
         *
         * This is normally the subscription.created event.
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

        /*
         * IMPORTANT:
         * paddle_customer_id is NOT a unique identifier.
         * Multiple local records may have the same Paddle customer ID.
         */
        subscription.setPaddleCustomerId(customerId);

        subscription.setPaddleSubscriptionId(
                paddleSubscriptionId
        );

        subscription.setPaddlePriceId(priceId);

        subscription.setPaddleTransactionId(null);

        subscription.setPlan(plan);

        subscription.setStatus(
                mapStatus(status)
        );

        subscription.setTrialStartsAt(null);
        subscription.setTrialEndsAt(null);

        JsonNode billingPeriod =
                data.path("current_billing_period");

        if (billingPeriod.isObject()) {

            String startsAt =
                    billingPeriod.path("starts_at").asText(null);

            String endsAt =
                    billingPeriod.path("ends_at").asText(null);

            subscription.setCurrentPeriodStart(
                    startsAt != null
                            ? Instant.parse(startsAt)
                            : null
            );

            subscription.setCurrentPeriodEnd(
                    endsAt != null
                            ? Instant.parse(endsAt)
                            : null
            );

        } else {

            subscription.setCurrentPeriodStart(null);
            subscription.setCurrentPeriodEnd(null);
        }

        JsonNode scheduledChange =
                data.path("scheduled_change");

        subscription.setCancelAtPeriodEnd(
                scheduledChange.isObject()
                        && "cancel".equals(
                        scheduledChange.path("action").asText()
                )
        );

        subscriptionRepository.save(subscription);
    }

    private void handleCompletedTransaction(JsonNode data) {

        String transactionId = data.path("id").asText(null);

        if (transactionId == null) {

            throw new IllegalArgumentException("Missing Paddle transaction ID");
        }

        UUID workspaceId = extractWorkspaceId(data);

        String customerId = data.path("customer_id").asText(null);

        String priceId = extractTransactionPriceId(data);

        BillingPlan plan = determinePlan(priceId);

        /*
         * transaction.completed is particularly important
         * for Lifetime because Lifetime is a one-time
         * purchase and does not create a subscription.
         */
        if (plan != BillingPlan.LIFETIME) {

            /*
             * Recurring subscription transactions do not
             * need to create a separate Subscription record.
             *
             * subscription.created / updated is responsible
             * for recurring subscription state.
             */
            return;
        }

        Subscription subscription = subscriptionRepository.findByWorkspaceId(workspaceId)
                .orElseGet(Subscription::new);

        subscription.setWorkspaceId(workspaceId);

        subscription.setPaddleTransactionId(transactionId);

        subscription.setPaddleCustomerId(customerId);

        subscription.setPaddlePriceId(priceId);

        subscription.setPaddleSubscriptionId(null);

        subscription.setPlan(BillingPlan.LIFETIME);

        subscription.setStatus(SubscriptionStatus.ACTIVE);

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

        JsonNode customData = data.path("custom_data");

        String workspaceIdString = customData.path("workspace_id").asText(null);

        if (workspaceIdString == null || workspaceIdString.isBlank()) {

            throw new IllegalArgumentException("Missing workspace_id in Paddle custom_data");
        }

        try {

            return UUID.fromString(workspaceIdString);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Invalid workspace_id in Paddle custom_data", e);
        }
    }

    private String extractSubscriptionPriceId(JsonNode data) {

        JsonNode items = data.path("items");

        if (!items.isArray() || items.isEmpty()) {

            throw new IllegalArgumentException("Missing Paddle subscription items");
        }

        String priceId = items.get(0)
                .path("price")
                .path("id")
                .asText(null);

        if (priceId == null || priceId.isBlank()) {

            throw new IllegalArgumentException("Missing Paddle subscription price ID");
        }

        return priceId;
    }

    private String extractTransactionPriceId(JsonNode data) {

        JsonNode items = data.path("items");

        if (!items.isArray() || items.isEmpty()) {

            throw new IllegalArgumentException("Missing Paddle transaction items");
        }

        String priceId = items.get(0)
                .path("price")
                .path("id")
                .asText(null);

        if (priceId == null || priceId.isBlank()) {

            throw new IllegalArgumentException("Missing Paddle transaction price ID");
        }

        return priceId;
    }

    private SubscriptionStatus mapStatus(String status) {

        return switch (status) {

            case "active" -> SubscriptionStatus.ACTIVE;

            case "trialing" -> SubscriptionStatus.TRIALING;

            case "past_due" -> SubscriptionStatus.PAST_DUE;

            case "paused" -> SubscriptionStatus.PAUSED;

            case "canceled" -> SubscriptionStatus.CANCELED;

            default -> throw new IllegalArgumentException("Unknown Paddle subscription status: " + status);
        };
    }

    private BillingPlan determinePlan(String priceId
    ) {

        if (priceId == null || priceId.isBlank()) {

            throw new IllegalArgumentException("Paddle price ID is required");
        }

        if (priceId.equals(paddleConfig.getPrices().getStartupMonthly())) {

            return BillingPlan.STARTUP_MONTHLY;
        }

        if (priceId.equals(paddleConfig.getPrices().getStartupYearly())) {

            return BillingPlan.STARTUP_YEARLY;
        }

        if (priceId.equals(paddleConfig.getPrices().getBusinessMonthly())) {

            return BillingPlan.BUSINESS_MONTHLY;
        }

        if (priceId.equals(paddleConfig.getPrices().getBusinessYearly())) {

            return BillingPlan.BUSINESS_YEARLY;
        }

        if (priceId.equals(paddleConfig.getPrices().getLifetime()
        )) {

            return BillingPlan.LIFETIME;
        }

        throw new IllegalArgumentException("Unknown Paddle price ID: " + priceId);
    }
}