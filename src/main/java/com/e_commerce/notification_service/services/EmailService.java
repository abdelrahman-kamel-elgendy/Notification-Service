package com.e_commerce.notification_service.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import com.e_commerce.notification_service.dto.EmailRequest;
import com.e_commerce.notification_service.models.NotificationLog;
import com.e_commerce.notification_service.repositories.NotificationLogRepository;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.Year;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Value("${spring.mail.username}")
    private String EMAIL_USERNAME;

    @Value("${app.email.from}")
    private String EMAIL_FROM;

    @Value("${app.email.from-name}")
    private String EMAIL_FROM_NAME;

    /**
     * Send email synchronously
     */
    public void sendEmail(EmailRequest emailRequest) {

        NotificationLog logEntry = saveNotificationLog(emailRequest, "PENDING", null);

        try {
            MimeMessage message = createMimeMessage(emailRequest);
            mailSender.send(message);

            // Update log entry to success
            logEntry.setStatus("SENT");
            logEntry.setSentAt(Instant.now());
            notificationLogRepository.save(logEntry);

            // sent

        } catch (Exception e) {
            handleEmailError(logEntry, emailRequest.getTo(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send email asynchronously
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendEmailAsync(EmailRequest emailRequest) {
        return CompletableFuture.supplyAsync(() -> {

            NotificationLog logEntry = saveNotificationLog(emailRequest, "PENDING", null);

            try {
                MimeMessage message = createMimeMessage(emailRequest);
                mailSender.send(message);

                // Update log entry to success
                logEntry.setStatus("SENT");
                logEntry.setSentAt(Instant.now());
                notificationLogRepository.save(logEntry);

                return true;

            } catch (Exception e) {
                handleEmailError(logEntry, emailRequest.getTo(), e);
                return false;
            }
        });
    }

    /**
     * Send email with retry logic
     */
    public boolean sendEmailWithRetry(EmailRequest emailRequest, int maxRetries) {

        NotificationLog logEntry = saveNotificationLog(emailRequest, "PENDING", null);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                MimeMessage message = createMimeMessage(emailRequest);
                mailSender.send(message);

                logEntry.setStatus("SENT");
                logEntry.setSentAt(Instant.now());
                notificationLogRepository.save(logEntry);

                // sent
                return true;

            } catch (Exception e) {
                // attempt failed

                if (attempt == maxRetries) {
                    handleEmailError(logEntry, emailRequest.getTo(), e);
                    return false;
                }

                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    handleEmailError(logEntry, emailRequest.getTo(), ie);
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Create MimeMessage from EmailRequest
     */
    private MimeMessage createMimeMessage(EmailRequest emailRequest) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Set sender - handle encoding exception properly
        try {
            helper.setFrom(EMAIL_USERNAME, EMAIL_FROM);
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(EMAIL_USERNAME);
        }

        // Set recipient
        helper.setTo(emailRequest.getTo());

        // Set subject
        helper.setSubject(emailRequest.getSubject());

        // Process HTML template
        String htmlContent = processTemplate(emailRequest.getTemplateName(), emailRequest.getTemplateData());
        helper.setText(htmlContent, true);

        return message;
    }

    /**
     * Process Thymeleaf template
     */
    private String processTemplate(String templateName, Map<String, Object> templateData) {
        try {
            Context context = new Context();

            // Add common variables to all templates
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("appName", EMAIL_FROM_NAME);

            // Add template-specific data
            if (templateData != null) {
                templateData.forEach(context::setVariable);
            }

            return templateEngine.process(templateName, context);

        } catch (TemplateInputException e) {
            throw new RuntimeException("Email template not found: " + templateName, e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing email template: " + e.getMessage(), e);
        }
    }

    /**
     * Handle email sending errors
     */
    private void handleEmailError(NotificationLog logEntry, String recipient, Exception e) {
        String errorMessage = e.getMessage();

        if (e instanceof MessagingException) {
            errorMessage = "Email configuration error: " + e.getMessage();
        } else if (e instanceof MailException) {
            errorMessage = "Mail server error: " + e.getMessage();
        } else if (e instanceof UnsupportedEncodingException) {
            errorMessage = "Encoding error: " + e.getMessage();
        }

        logEntry.setStatus("FAILED");
        logEntry.setErrorMessage(errorMessage);
        notificationLogRepository.save(logEntry);

    }

    /**
     * Save notification log entry
     */
    private NotificationLog saveNotificationLog(EmailRequest emailRequest, String status, String errorMessage) {
        String contentPreview = "Template: " + emailRequest.getTemplateName();
        if (emailRequest.getTemplateData() != null) {
            contentPreview += " | Data: " + emailRequest.getTemplateData().toString();
        }

        NotificationLog logEntry = NotificationLog.builder()
                .type("EMAIL")
                .recipient(emailRequest.getTo())
                .subject(emailRequest.getSubject())
                .content(contentPreview)
                .status(status)
                .errorMessage(errorMessage)
                .createdAt(Instant.now())
                .sentAt("SENT".equals(status) ? Instant.now() : null)
                .build();

        return notificationLogRepository.save(logEntry);
    }

    /**
     * Validate email request
     */
    public boolean validateEmailRequest(EmailRequest emailRequest) {
        if (emailRequest.getTo() == null || emailRequest.getTo().trim().isEmpty()) {
            return false;
        }

        if (emailRequest.getSubject() == null || emailRequest.getSubject().trim().isEmpty()) {
            return false;
        }

        if (emailRequest.getTemplateName() == null || emailRequest.getTemplateName().trim().isEmpty()) {
            return false;
        }

        // Basic email validation
        if (!isValidEmail(emailRequest.getTo())) {
            return false;
        }

        return true;
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}