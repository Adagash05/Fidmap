package com.amsal.fidmap.payment.polar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PolarWebhookEventRepository
        extends JpaRepository<PolarWebhookEvent, UUID> {

    boolean existsByProviderEventId(
            String providerEventId
    );
}