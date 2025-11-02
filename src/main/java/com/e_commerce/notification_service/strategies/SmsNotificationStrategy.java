package com.e_commerce.notification_service.strategies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.services.SmsService;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsNotificationStrategy implements NotificationStrategy {

    @Autowired
    private SmsService smsService;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            log.info("Sending SMS to: {}", request.getRecipient());

            NotificationResponse response = smsService.sendSms(request);

            log.info("SMS sent successfully to: {}", request.getRecipient());
            return response;

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.getRecipient(), e.getMessage(), e);
            return NotificationResponse.builder()
                    .success(false)
                    .message("Failed to send SMS: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.SMS.equals(channelType);
    }
}