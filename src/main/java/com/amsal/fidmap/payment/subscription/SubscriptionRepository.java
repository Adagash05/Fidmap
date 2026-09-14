package com.amsal.fidmap.payment.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByWorkspaceId(
            UUID workspaceId
    );

    Optional<Subscription> findByProviderSubscriptionId(
            String providerSubscriptionId
    );

    Optional<Subscription> findByProviderTransactionId(
            String providerTransactionId
    );

    Optional<Subscription> findByProviderCustomerId(
            String providerCustomerId
    );

    boolean existsByWorkspaceId(
            UUID workspaceId
    );
}