package com.e_commerce.notification_service.services;

import com.e_commerce.notification_service.models.InAppNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send notification to specific user
     */
    public void sendToUser(String userId, InAppNotification notification) {
        try {
            Map<String, Object> message = createWebSocketMessage(notification);

            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);

            log.debug("WebSocket message sent to user {}: {}", userId, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendToUsers(Iterable<String> userIds, InAppNotification notification) {
        try {
            Map<String, Object> message = createWebSocketMessage(notification);

            for (String userId : userIds) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
            }

            log.debug("WebSocket message sent to {} users: {}",
                    userIds instanceof Collection ? ((Collection<?>) userIds).size() : "multiple",
                    notification.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to multiple users: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast notification to all connected users
     */
    public void broadcastToAll(InAppNotification notification) {
        try {
            Map<String, Object> message = createWebSocketMessage(notification);

            messagingTemplate.convertAndSend("/topic/notifications", message);

            log.debug("WebSocket message broadcast to all users: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send user's unread count
     */
    public void sendUnreadCount(String userId, long unreadCount) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "UNREAD_COUNT");
            message.put("userId", userId);
            message.put("unreadCount", unreadCount);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);

            log.debug("Unread count sent to user {}: {}", userId, unreadCount);
        } catch (Exception e) {
            log.error("Failed to send unread count to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Create standardized WebSocket message
     */
    private Map<String, Object> createWebSocketMessage(InAppNotification notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_NOTIFICATION");
        message.put("notification", convertToDto(notification));
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * Convert entity to DTO for WebSocket transmission
     */
    private Map<String, Object> convertToDto(InAppNotification notification) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", notification.getId());
        dto.put("userId", notification.getUserId());
        dto.put("title", notification.getTitle());
        dto.put("message", notification.getMessage());
        dto.put("type", notification.getType().name());
        dto.put("priority", notification.getPriority().name());
        dto.put("metadata", notification.getMetadata());
        dto.put("isRead", notification.getIsRead());
        dto.put("createdAt", notification.getCreatedAt().toString());
        dto.put("readAt", notification.getReadAt() != null ? notification.getReadAt().toString() : null);
        return dto;
    }
}