package com.e_commerce.notification_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    public NotificationResponse sendEmail(NotificationRequest request) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(request.getRecipient());
        helper.setSubject(request.getSubject());
        helper.setText(request.getMessage(), true);

        mailSender.send(message);

        Map<String, Object> details = new HashMap<>();
        details.put("provider", "smtp");
        details.put("subject", request.getSubject());
        details.put("recipient", request.getRecipient());
        details.put("timestamp", LocalDateTime.now().toString());

        return NotificationResponse.builder()
                .success(true)
                .message("Email sent successfully")
                .providerMessageId(generateMessageId())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

    }

    private String generateMessageId() {
        return "email-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }
}