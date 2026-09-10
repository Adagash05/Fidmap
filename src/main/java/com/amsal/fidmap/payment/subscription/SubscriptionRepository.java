package com.amsal.fidmap.payment.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByWorkspaceId(UUID workspaceId);

    Optional<Subscription> findByPaddleSubscriptionId(
            String paddleSubscriptionId
    );

    Optional<Subscription> findByPaddleTransactionId(
            String paddleTransactionId
    );

    Optional<Subscription> findByPaddleCustomerId(
            String paddleCustomerId
    );

    boolean existsByWorkspaceId(UUID workspaceId);
}