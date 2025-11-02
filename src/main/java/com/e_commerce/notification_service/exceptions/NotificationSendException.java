package com.e_commerce.notification_service.exceptions;

public class NotificationSendException extends NotificationException {
    public NotificationSendException(String message) {
        super(message, "NOTIFICATION_SEND_FAILED");
    }

    public NotificationSendException(String message, Throwable cause) {
        super(message, cause, "NOTIFICATION_SEND_FAILED");
    }

    public NotificationSendException(String message, String provider, Throwable cause) {
        super(message, cause, "PROVIDER_SEND_FAILED");
    }
}
