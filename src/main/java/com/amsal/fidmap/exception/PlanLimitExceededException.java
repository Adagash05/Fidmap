package com.amsal.fidmap.exception;

public class PlanLimitExceededException extends RuntimeException {
    public PlanLimitExceededException(String message) {
        super(message);
    }
}
