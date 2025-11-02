package com.e_commerce.notification_service.strategies;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.services.EmailService;

import jakarta.mail.MessagingException;

@Component
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    @Autowired
    private EmailService emailService;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) throws MessagingException {
        log.info("Sending email to: {}", request.getRecipient());
        NotificationResponse response = emailService.sendEmail(request);
        log.info("Email sent successfully to: {}", request.getRecipient());
        return response;
    }

    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.EMAIL.equals(channelType);
    }
}