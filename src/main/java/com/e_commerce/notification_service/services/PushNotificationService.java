package com.e_commerce.notification_service.services;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.dto.response.NotificationResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PushNotificationService {

    @Value("${firebase.config.path:}")
    private String firebaseConfigPath;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void init() {
        try {
            if (StringUtils.hasText(firebaseConfigPath)) {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream());

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }

                firebaseInitialized = true;
                log.info("Firebase Cloud Messaging initialized successfully");
            } else {
                log.warn("Firebase config path not provided. Push notifications will run in simulation mode.");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }

    public NotificationResponse sendPushNotification(NotificationRequest request) {
        try {
            String deviceToken = request.getRecipient();
            String title = request.getSubject() != null ? request.getSubject() : "Notification";
            String body = request.getMessage();

            Map<String, Object> details = new HashMap<>();
            details.put("token", deviceToken);
            details.put("title", title);
            details.put("body", body);

            if (firebaseInitialized) {
                // Convert Map<String, Object> to Map<String, String> for FCM data
                Map<String, String> fcmData = convertToFcmData(request.getData());

                // Real FCM implementation
                Message message = Message.builder()
                        .setToken(deviceToken)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putAllData(fcmData) // Now this accepts Map<String, String>
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build())
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setAlert(ApsAlert.builder()
                                                .setTitle(title)
                                                .setBody(body)
                                                .build())
                                        .setSound("default")
                                        .build())
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);

                details.put("provider", "fcm");
                details.put("messageId", response);
                details.put("platform", "android/ios");
                details.put("dataFields", fcmData.keySet());

                log.info("Push notification sent via FCM. Message ID: {}", response);

                return NotificationResponse.builder()
                        .success(true)
                        .message("Push notification sent successfully")
                        .providerMessageId(response)
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
            } else {
                // Simulation mode
                log.info("SIMULATION: Push notification to token {}: {} - {}", deviceToken, title, body);
                Thread.sleep(100);

                details.put("provider", "simulated");
                details.put("platform", "android/ios");
                details.put("data", request.getData());

                return NotificationResponse.builder()
                        .success(true)
                        .message("Push notification sent successfully (simulation)")
                        .providerMessageId("push-sim-" + System.currentTimeMillis())
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to send push notification to {}: {}", request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
        }
    }

    /**
     * Convert Map<String, Object> to Map<String, String> for FCM data payload
     * FCM only supports string values in data payload
     */
    private Map<String, String> convertToFcmData(Map<String, Object> originalData) {
        if (originalData == null || originalData.isEmpty()) {
            return new HashMap<>();
        }

        return originalData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Object value = entry.getValue();
                            if (value == null) {
                                return "null";
                            }
                            // Convert to string representation
                            return value.toString();
                        }));
    }

    /**
     * Alternative method for sending push with custom data handling
     */
    public NotificationResponse sendPushWithCustomData(NotificationRequest request) {
        try {
            String deviceToken = request.getRecipient();
            String title = request.getSubject() != null ? request.getSubject() : "Notification";
            String body = request.getMessage();

            if (firebaseInitialized) {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(deviceToken)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());

                // Add data fields one by one with proper string conversion
                if (request.getData() != null && !request.getData().isEmpty()) {
                    for (Map.Entry<String, Object> entry : request.getData().entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        String stringValue = value != null ? value.toString() : "null";
                        messageBuilder.putData(key, stringValue);
                    }
                }

                Message message = messageBuilder.build();
                String response = FirebaseMessaging.getInstance().send(message);

                Map<String, Object> details = new HashMap<>();
                details.put("provider", "fcm");
                details.put("messageId", response);
                details.put("token", deviceToken);
                details.put("dataFields", request.getData() != null ? request.getData().keySet() : "none");

                return NotificationResponse.builder()
                        .success(true)
                        .message("Push notification with custom data sent successfully")
                        .providerMessageId(response)
                        .timestamp(LocalDateTime.now())
                        .details(details)
                        .build();
            } else {
                // Simulation mode
                return sendPushNotification(request); // Use the main method for simulation
            }

        } catch (Exception e) {
            log.error("Failed to send push notification with custom data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
        }
    }
}