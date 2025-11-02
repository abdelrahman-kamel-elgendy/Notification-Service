package com.e_commerce.notification_service.exceptions;

public class ChannelNotSupportedException extends NotificationException {
    public ChannelNotSupportedException(String channel) {
        super("Notification channel not supported: " + channel, "CHANNEL_NOT_SUPPORTED", channel);
    }
}