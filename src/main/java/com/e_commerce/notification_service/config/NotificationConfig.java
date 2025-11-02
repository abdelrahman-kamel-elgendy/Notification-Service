package com.e_commerce.notification_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfig {
    private Map<String, ProviderConfig> providers = new HashMap<>();
    private int maxRetryAttempts = 3;
    private long retryDelay = 1000;
    private boolean asyncEnabled = true;

    @Data
    public static class ProviderConfig {
        private String url;
        private String apiKey;
        private String secret;
        private boolean enabled = true;
        private int timeout = 30000;
        private String from;
        private String senderId;
    }
}