package com.e_commerce.notification_service.strategies;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.enums.ChannelType;

import jakarta.mail.MessagingException;

public interface NotificationStrategy {
    ChannelType getChannelType();

    NotificationResponse send(NotificationRequest request) throws MessagingException;

    boolean supports(ChannelType channelType);
}