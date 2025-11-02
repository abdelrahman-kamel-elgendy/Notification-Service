package com.e_commerce.notification_service.controllers;

import com.e_commerce.notification_service.services.InAppNotificationService;
import com.e_commerce.notification_service.services.WebSocketService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketNotificationController {

    @Autowired
    private InAppNotificationService notificationService;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * Handle subscription to user's notification queue
     */
    @SubscribeMapping("/queue/notifications")
    public Map<String, Object> handleSubscription(Principal principal) {
        String userId = principal.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "SUBSCRIBED");
        response.put("userId", userId);
        response.put("message", "Successfully subscribed to notifications");
        response.put("timestamp", System.currentTimeMillis());

        log.info("User {} subscribed to notifications", userId);

        return response;
    }

    /**
     * Handle mark as read request from client
     */
    @MessageMapping("/notifications.markRead")
    @SendToUser("/queue/notifications")
    public Map<String, Object> markAsRead(MarkAsReadRequest request, Principal principal) {
        String userId = principal.getName();

        try {
            // Here you would implement the mark as read logic
            // notificationService.markAsRead(request.getNotificationId(), userId);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "MARKED_READ");
            response.put("notificationId", request.getNotificationId());
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());

            log.debug("Notification {} marked as read by user {}", request.getNotificationId(), userId);

            return response;
        } catch (Exception e) {
            log.error("Failed to mark notification as read: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("type", "ERROR");
            response.put("message", "Failed to mark notification as read");
            response.put("timestamp", System.currentTimeMillis());

            return response;
        }
    }

    /**
     * Handle request for unread count
     */
    @MessageMapping("/notifications.getUnreadCount")
    @SendToUser("/queue/notifications")
    public Map<String, Object> getUnreadCount(Principal principal) {
        String userId = principal.getName();

        try {
            // Here you would get the actual unread count from repository
            // long unreadCount = notificationService.getUnreadCount(userId);
            long unreadCount = 5; // Example

            Map<String, Object> response = new HashMap<>();
            response.put("type", "UNREAD_COUNT");
            response.put("userId", userId);
            response.put("unreadCount", unreadCount);
            response.put("timestamp", System.currentTimeMillis());

            return response;
        } catch (Exception e) {
            log.error("Failed to get unread count for user {}: {}", userId, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("type", "ERROR");
            response.put("message", "Failed to get unread count");
            response.put("timestamp", System.currentTimeMillis());

            return response;
        }
    }

    // DTO for mark as read request
    public static class MarkAsReadRequest {
        private Long notificationId;

        // Getters and setters
        public Long getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(Long notificationId) {
            this.notificationId = notificationId;
        }
    }
}