package com.amsal.fidmap.notification;

public enum NotificationStatus {
    /** Recorded, not yet handed to the email provider. */
    PENDING,
    /** The email provider accepted the message and returned a message id. */
    SENT,
    /** Last attempt failed; will be retried until attempts are exhausted. */
    FAILED,
    /** Retries exhausted or recipient no longer eligible. */
    GAVE_UP
}
