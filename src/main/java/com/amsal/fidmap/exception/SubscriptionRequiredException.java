package com.amsal.fidmap.exception;

/**
 * Thrown when a workspace tries to perform a protected (write/paid) operation
 * while it has no active entitlement: expired trial, expired/cancelled/past-due
 * subscription, or missing billing record. Mapped to HTTP 402.
 */
public class SubscriptionRequiredException extends RuntimeException {

    private final String reasonCode;

    public SubscriptionRequiredException(String reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
