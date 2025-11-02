package com.e_commerce.notification_service.services;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.e_commerce.notification_service.config.RetryConfig;
import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.exceptions.ChannelNotSupportedException;
import com.e_commerce.notification_service.exceptions.NotificationNotFoundException;
import com.e_commerce.notification_service.exceptions.NotificationSendException;
import com.e_commerce.notification_service.exceptions.RetryExhaustedException;
import com.e_commerce.notification_service.models.NotificationLog;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.models.enums.NotificationStatus;
import com.e_commerce.notification_service.repositories.NotificationLogRepository;
import com.e_commerce.notification_service.strategies.NotificationStrategy;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Transactional
public class NotificationService {

    @Autowired
    private List<NotificationStrategy> strategies;

    @Autowired
    private NotificationLogRepository logRepository;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private RetryConfig retryConfig;

    public NotificationResponse sendNotification(NotificationRequest request) {
        NotificationStrategy strategy = findStrategy(request.getChannel());
        NotificationLog logEntry = createLogEntry(request);

        try {
            NotificationResponse response = retryTemplate.execute(context -> {
                int attempt = context.getRetryCount() + 1;
                logEntry.setRetryCount(attempt);

                if (attempt > 1) {
                    logEntry.setStatus(NotificationStatus.RETRYING);
                    logRepository.save(logEntry);
                }

                NotificationResponse result = strategy.send(request);

                if (!result.isSuccess())
                    throw new NotificationSendException("Notification send failed: " + result.getMessage());

                return result;
            });

            updateLogEntry(logEntry, response, null);
            return response;

        } catch (Exception e) {
            updateLogEntry(logEntry, null, e.getMessage());

            if (e instanceof NotificationSendException)
                throw (NotificationSendException) e;
            else
                throw new RetryExhaustedException(
                        "Notification failed after maximum retry attempts: " + e.getMessage(),
                        retryConfig.getMaxAttempts());
        }

    }

    @Async("notificationTaskExecutor")
    public CompletableFuture<NotificationResponse> sendNotificationAsync(NotificationRequest request) {
        return CompletableFuture.completedFuture(sendNotification(request));
    }

    @Transactional(readOnly = true)
    public Page<NotificationLog> getNotificationHistory(String recipient, ChannelType channel,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        Specification<NotificationLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (recipient != null && !recipient.trim().isEmpty())
                predicates.add(cb.equal(root.get("recipient"), recipient));

            if (channel != null)
                predicates.add(cb.equal(root.get("channel"), channel));

            if (startDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));

            if (endDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return logRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public NotificationLog getNotificationById(Long id) {
        return logRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    public boolean resendNotification(Long notificationId) {
        NotificationLog original = getNotificationById(notificationId);

        NotificationRequest request = new NotificationRequest();
        request.setChannel(original.getChannel());
        request.setRecipient(original.getRecipient());
        request.setSubject(original.getSubject());
        request.setMessage(original.getMessage());
        request.setPriority(original.getPriority());
        request.setMetadata(original.getMetadata());

        try {
            NotificationResponse response = sendNotification(request);
            return response.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationLog> getFailedNotifications() {
        int maxAttempts = retryConfig.getMaxAttempts();
        return logRepository.findByStatusAndRetryCountLessThan(
                NotificationStatus.FAILED,
                maxAttempts);
    }

    public void retryFailedNotifications() {
        List<NotificationLog> failedNotifications = getFailedNotifications();
        log.info("Retrying {} failed notifications", failedNotifications.size());

        for (NotificationLog failed : failedNotifications) {
            try {
                boolean success = resendNotification(failed.getId());
                if (success)
                    log.info("Successfully retried notification ID: {}", failed.getId());
                else
                    log.warn("Failed to retry notification ID: {}", failed.getId());

            } catch (Exception e) {
                log.error("Error retrying notification {}: {}", failed.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void scheduledRetry() {
        log.info("Running scheduled retry for failed notifications");
        retryFailedNotifications();
    }

    private NotificationStrategy findStrategy(ChannelType channel) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(channel))
                .findFirst()
                .orElseThrow(() -> new ChannelNotSupportedException(channel.name()));
    }

    private NotificationLog createLogEntry(NotificationRequest request) {
        NotificationLog logEntry = NotificationLog.builder()
                .channel(request.getChannel())
                .type(request.getType()) // Add this line
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .priority(request.getPriority())
                .retryCount(0)
                .metadata(request.getMetadata())
                .build();

        return logRepository.save(logEntry);
    }

    private void updateLogEntry(NotificationLog logEntry, NotificationResponse response, String error) {
        if (response != null && response.isSuccess()) {
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setProviderMessageId(response.getProviderMessageId());
            logEntry.setSentAt(LocalDateTime.now());
            if (response.getDetails() != null && response.getDetails().containsKey("provider"))
                logEntry.setProvider(response.getDetails().get("provider").toString());

        } else {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(
                    error != null ? error : response != null ? response.getMessage() : "Unknown error");
        }

        logRepository.save(logEntry);
    }
}