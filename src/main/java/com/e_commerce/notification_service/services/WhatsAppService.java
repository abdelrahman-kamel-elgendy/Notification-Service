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
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${spring.app.name}")
    private String twilioWhatsAppFrom;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(twilioAccountSid) && StringUtils.hasText(twilioAuthToken)) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            log.info("Twilio WhatsApp service initialized successfully");
        } else {
            log.warn("Twilio credentials not configured. WhatsApp service will run in simulation mode.");
        }
    }

    public NotificationResponse sendWhatsApp(NotificationRequest request) {
        try {
            String recipient = formatWhatsAppNumber(request.getRecipient());
            String messageBody = request.getMessage();

            Map<String, Object> details = new HashMap<>();
            details.put("recipient", recipient);
            details.put("message", messageBody);

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + recipient),
                    new PhoneNumber(twilioWhatsAppFrom),
                    messageBody).create();

            details.put("provider", "twilio-whatsapp");
            details.put("messageSid", message.getSid());
            details.put("status", message.getStatus().toString());
            details.put("price", message.getPrice());

            log.info("WhatsApp message sent via Twilio. SID: {}, Status: {}", message.getSid(),
                    message.getStatus());

            return NotificationResponse.builder()
                    .success(true)
                    .message("WhatsApp message sent successfully")
                    .providerMessageId(message.getSid())
                    .timestamp(LocalDateTime.now())
                    .details(details)
                    .build();

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    private String formatWhatsAppNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");

        // Ensure it starts with country code
        if (!cleaned.startsWith("+")) {
            cleaned = "+2" + cleaned;
        }

        return cleaned;
    }
}