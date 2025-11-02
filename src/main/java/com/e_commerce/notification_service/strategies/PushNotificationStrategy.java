package com.e_commerce.notification_service.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.services.PushNotificationService;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushNotificationStrategy implements NotificationStrategy {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            log.info("Sending Push notification to: {}", request.getRecipient());

            // Use the corrected push notification service
            NotificationResponse response = pushNotificationService.sendPushNotification(request);

            log.info("Push notification sent successfully to: {}", request.getRecipient());
            return response;

        } catch (Exception e) {
            log.error("Failed to send push notification to {}: {}", request.getRecipient(), e.getMessage(), e);
            return NotificationResponse.builder()
                    .success(false)
                    .message("Failed to send push notification: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.PUSH.equals(channelType);
    }
}
