package com.amsal.fidmap.payment.polar;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_events",
        indexes = {
                @Index(
                        name = "uk_webhook_event_id",
                        columnList = "provider_event_id",
                        unique = true
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolarWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Polar webhook event ID.
     *
     * Used for idempotency so the same Polar event
     * cannot be processed twice.
     */
    @Column(
            name = "provider_event_id",
            nullable = false,
            unique = true
    )
    private String providerEventId;

    /**
     * Polar event type.
     */
    @Column(
            name = "event_type",
            nullable = false
    )
    private String eventType;

    /**
     * Time at which FIDMAP successfully processed
     * the webhook.
     */
    @Column(
            name = "processed_at",
            nullable = false
    )
    private Instant processedAt;
}
