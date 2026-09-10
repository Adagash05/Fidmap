package com.amsal.fidmap.payment.paddle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaddleWebhookEventRepository extends JpaRepository<PaddleWebhookEvent, UUID> {

    boolean existsByPaddleEventId(String paddleEventId);
}