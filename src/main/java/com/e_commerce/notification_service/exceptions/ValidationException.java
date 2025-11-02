package com.e_commerce.notification_service.exceptions;

import java.util.Map;

public class ValidationException extends NotificationException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR", validationErrors);
    }
}