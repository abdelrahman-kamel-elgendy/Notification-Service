package com.e_commerce.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private boolean success;
    private String message;
    private String notificationId;
    private String providerMessageId;
    private LocalDateTime timestamp;

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
}
