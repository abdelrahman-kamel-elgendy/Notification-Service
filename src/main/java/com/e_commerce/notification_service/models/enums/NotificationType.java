package com.e_commerce.notification_service.models.enums;

public enum NotificationType {
    TRANSACTIONAL, // Order confirmations, password resets, etc.
    MARKETING, // Promotions, newsletters
    ALERT, // Security alerts, system notifications
    VERIFICATION, // Email verification, OTP codes
    REMINDER, // Appointment reminders, cart abandonment
    SUPPORT, // Customer support messages
    SYSTEM, // System-generated notifications
    BROADCAST // Broadcast messages to multiple users
}
