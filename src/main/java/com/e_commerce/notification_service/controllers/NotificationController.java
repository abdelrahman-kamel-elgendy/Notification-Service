package com.e_commerce.notification_service.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.e_commerce.notification_service.models.NotificationLog;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<NotificationLog>> getNotificationHistory(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) ChannelType channel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam Pageable pageable) {

        Page<NotificationLog> history = notificationService.getNotificationHistory(
                recipient, channel, startDate, endDate, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationLog> getNotification(@PathVariable Long id) {
        NotificationLog notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<NotificationResponse> resendNotification(@PathVariable Long id) {
        boolean success = notificationService.resendNotification(id);

        NotificationResponse response = NotificationResponse.builder()
                .success(success)
                .message(success ? "Notification resent successfully" : "Failed to resend notification")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<NotificationResponse> sendBatchNotifications(
            @Valid @RequestBody List<NotificationRequest> requests) {

        log.info("Processing batch of {} notifications", requests.size());

        int successCount = 0;
        int failureCount = 0;

        for (NotificationRequest request : requests) {
            try {
                // Force async for batch processing
                request.setAsync(true);
                notificationService.sendNotificationAsync(request);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to queue notification for {}: {}",
                        request.getRecipient(), e.getMessage());
                failureCount++;
            }
        }

        NotificationResponse response = NotificationResponse.builder()
                .success(failureCount == 0)
                .message(String.format("Batch processing completed. Success: %d, Failed: %d",
                        successCount, failureCount))
                .timestamp(LocalDateTime.now())
                .build();

        response.getDetails().put("successCount", successCount);
        response.getDetails().put("failureCount", failureCount);
        response.getDetails().put("totalCount", requests.size());

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<NotificationLog>> getFailedNotifications() {
        List<NotificationLog> failed = notificationService.getFailedNotifications();
        return ResponseEntity.ok(failed);
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<NotificationResponse> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok(NotificationResponse.builder()
                .success(true)
                .message("Retry process initiated for failed notifications")
                .timestamp(LocalDateTime.now())
                .build());
    }
}