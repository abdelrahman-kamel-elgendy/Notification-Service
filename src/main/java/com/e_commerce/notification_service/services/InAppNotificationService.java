package com.e_commerce.notification_service.services;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.InAppNotification;
import com.e_commerce.notification_service.repositories.InAppNotificationRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class InAppNotificationService {

    @Autowired
    private InAppNotificationRepository inAppNotificationRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private NotificationStatsService notificationStatsService;

    @Transactional
    public NotificationResponse sendInAppNotification(NotificationRequest request) {
        try {
            String userId = request.getRecipient();
            String title = request.getSubject() != null ? request.getSubject() : "Notification";
            String message = request.getMessage();

            // Create and save in-app notification
            InAppNotification notification = InAppNotification.builder()
                    .userId(userId)
                    .title(title)
                    .message(message)
                    .type(request.getType())
                    .priority(request.getPriority())
                    .metadata(request.getMetadata())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            InAppNotification savedNotification = inAppNotificationRepository.save(notification);

            // Send real-time notification via WebSocket (async)
            CompletableFuture.runAsync(() -> {
                try {
                    webSocketService.sendToUser(userId, savedNotification);

                    // Update unread count
                    long unreadCount = inAppNotificationRepository.countByUserIdAndIsReadFalse(userId);
                    webSocketService.sendUnreadCount(userId, unreadCount);

                    // Update stats
                    notificationStatsService.incrementNotificationCount(userId, savedNotification.getType());

                } catch (Exception e) {
                    log.warn("Failed to send real-time notification for user {}: {}", userId, e.getMessage());
                }
            });

            Map<String, Object> details = new HashMap<>();
            details.put("provider", "in-app");
            details.put("notificationId", savedNotification.getId());
            details.put("userId", userId);
            details.put("deliveredAt", LocalDateTime.now().toString());
            details.put("realTimeEnabled", true);

            log.info("In-app notification created for user {}: {}", userId, savedNotification.getId());

            return NotificationResponse.builder()
                    .success(true)
                    .message("In-app notification sent successfully")
                    .providerMessageId(savedNotification.getId().toString())
                    .timestamp(LocalDateTime.now())
                    .details(details)
                    .build();

        } catch (Exception e) {
            log.error("Failed to send in-app notification to user {}: {}", request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send in-app notification: " + e.getMessage(), e);
        }
    }

    /**
     * Send notification to multiple users
     */
    @Transactional
    public NotificationResponse sendBulkInAppNotification(NotificationRequest request, Iterable<String> userIds) {
        try {
            String title = request.getSubject() != null ? request.getSubject() : "Notification";
            String message = request.getMessage();
            int successCount = 0;
            int failureCount = 0;

            for (String userId : userIds) {
                try {
                    InAppNotification notification = InAppNotification.builder()
                            .userId(userId)
                            .title(title)
                            .message(message)
                            .type(request.getType())
                            .priority(request.getPriority())
                            .metadata(request.getMetadata())
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();

                    InAppNotification savedNotification = inAppNotificationRepository.save(notification);

                    // Send real-time notification
                    webSocketService.sendToUser(userId, savedNotification);

                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                    failureCount++;
                }
            }

            Map<String, Object> details = new HashMap<>();
            details.put("provider", "in-app");
            details.put("successCount", successCount);
            details.put("failureCount", failureCount);
            details.put("totalCount", successCount + failureCount);
            details.put("realTimeEnabled", true);

            return NotificationResponse.builder()
                    .success(failureCount == 0)
                    .message(String.format("Bulk notification sent. Success: %d, Failed: %d", successCount,
                            failureCount))
                    .timestamp(LocalDateTime.now())
                    .details(details)
                    .build();

        } catch (Exception e) {
            log.error("Failed to send bulk in-app notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send bulk in-app notification: " + e.getMessage(), e);
        }
    }
}