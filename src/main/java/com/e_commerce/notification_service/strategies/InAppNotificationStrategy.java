package com.e_commerce.notification_service.strategies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.enums.ChannelType;

import java.time.LocalDateTime;

@Component
@Slf4j
public class InAppNotificationStrategy implements NotificationStrategy {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.IN_APP;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            log.info("Sending In-App notification to user: {}", request.getRecipient());

            // Simulate in-app notification storage
            Thread.sleep(50);

            NotificationResponse response = NotificationResponse.builder()
                    .success(true)
                    .message("In-app notification sent successfully")
                    .providerMessageId("inapp-" + System.currentTimeMillis())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.getDetails().put("provider", "in-app");
            response.getDetails().put("userId", request.getRecipient());

            log.info("In-app notification sent successfully to user: {}", request.getRecipient());
            return response;

        } catch (Exception e) {
            log.error("Failed to send in-app notification to user {}: {}", request.getRecipient(), e.getMessage(), e);
            return NotificationResponse.builder()
                    .success(false)
                    .message("Failed to send in-app notification: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.IN_APP.equals(channelType);
    }
}