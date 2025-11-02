package com.e_commerce.notification_service.exceptions;

public class NotificationNotFoundException extends NotificationException {
    public NotificationNotFoundException(Long id) {
        super("Notification not found with id: " + id, "NOTIFICATION_NOT_FOUND", id);
    }
}
