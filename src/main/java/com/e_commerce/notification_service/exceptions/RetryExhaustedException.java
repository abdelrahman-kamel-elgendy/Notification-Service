package com.e_commerce.notification_service.exceptions;

import java.util.Map;

public class RetryExhaustedException extends NotificationException {
    public RetryExhaustedException(String message) {
        super(message, "RETRY_EXHAUSTED");
    }

    public RetryExhaustedException(String message, int maxAttempts) {
        super(message, "RETRY_EXHAUSTED", Map.of("maxAttempts", maxAttempts));
    }
}