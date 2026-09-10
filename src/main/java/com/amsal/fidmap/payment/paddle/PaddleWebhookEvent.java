package com.amsal.fidmap.payment.paddle;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "paddle_webhook_events",
        indexes = {
                @Index(
                        name = "uk_paddle_event_id",
                        columnList = "paddle_event_id",
                        unique = true
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaddleWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "paddle_event_id",
            nullable = false
    )
    private String paddleEventId;

    @Column(
            name = "event_type",
            nullable = false
    )
    private String eventType;

    @Column(
            name = "processed_at",
            nullable = false
    )
    private Instant processedAt;
}
