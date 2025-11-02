package com.e_commerce.notification_service.services;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    private boolean twilioEnabled = false;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(twilioAccountSid) && StringUtils.hasText(twilioAuthToken)) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            twilioEnabled = true;
            log.info("Twilio SMS service initialized successfully");
        } else {
            log.warn("Twilio credentials not configured. SMS service will run in simulation mode.");
        }
    }

    public NotificationResponse sendSms(NotificationRequest request) {
        try {
            String recipient = formatPhoneNumber(request.getRecipient());
            String messageBody = request.getMessage();

            Map<String, Object> details = new HashMap<>();

            if (twilioEnabled) {
                // Real Twilio implementation
                Message message = Message.creator(
                        new PhoneNumber(recipient),
                        new PhoneNumber(twilioPhoneNumber),
                        messageBody).create();

                details.put("provider", "twilio");
                details.put("messageSid", message.getSid());
                details.put("status", message.getStatus().toString());
                details.put("price", message.getPrice());
                details.put("recipient", recipient);

                log.info("SMS sent via Twilio. SID: {}, Status: {}", message.getSid(), message.getStatus());

                return NotificationResponse.builder()
                        .success(true)
                        .message("SMS sent successfully via Twilio")
                        .providerMessageId(message.getSid())
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
            } else {
                // Simulation mode for development
                log.info("SIMULATION: SMS to {}: {}", recipient, messageBody);
                Thread.sleep(100); // Simulate API call

                details.put("provider", "simulated");
                details.put("recipient", recipient);
                details.put("messageLength", messageBody.length());

                return NotificationResponse.builder()
                        .success(true)
                        .message("SMS sent successfully (simulation)")
                        .providerMessageId("sms-sim-" + System.currentTimeMillis())
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Ensure phone number is in E.164 format
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        } else if (phoneNumber.startsWith("00")) {
            return "+" + phoneNumber.substring(2);
        } else {
            // Assume it's a local number, add your default country code
            return "+2" + phoneNumber; // Egypt country code as example
        }
    }
}