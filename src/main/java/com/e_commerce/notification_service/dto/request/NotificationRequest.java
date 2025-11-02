package com.e_commerce.notification_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.models.enums.NotificationPriority;
import com.e_commerce.notification_service.models.enums.NotificationType;
import com.e_commerce.notification_service.validation.ValidRecipientForChannel;

import lombok.Builder;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidRecipientForChannel
public class NotificationRequest {

    @NotNull(message = "Channel type is required")
    private ChannelType channel;

    @NotNull(message = "Notification type is required")
    @Builder.Default
    private NotificationType type = NotificationType.TRANSACTIONAL;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;

    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String message;

    @Valid
    private Map<@NotBlank(message = "Data key cannot be blank") String, @NotNull(message = "Data value cannot be null") Object> data;

    @NotNull(message = "Priority is required")
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Template ID can only contain letters, numbers, hyphens, and underscores")
    @Size(max = 100, message = "Template ID cannot exceed 100 characters")
    private String templateId;

    @Valid
    private Map<@NotBlank(message = "Template variable key cannot be blank") String, @NotNull(message = "Template variable value cannot be null") Object> templateVariables;

    @NotNull(message = "Async flag is required")
    @Builder.Default
    private Boolean async = true;

    @Valid
    private Map<@NotBlank(message = "Metadata key cannot be blank") @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", message = "Metadata key must be alphanumeric with underscores") String, @NotBlank(message = "Metadata value cannot be blank") String> metadata;

    // Helper method to initialize default values
    public Map<String, Object> getData() {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        return this.data;
    }

    public Map<String, Object> getTemplateVariables() {
        if (this.templateVariables == null) {
            this.templateVariables = new HashMap<>();
        }
        return this.templateVariables;
    }

    public Map<String, String> getMetadata() {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        return this.metadata;
    }

    public Boolean getAsync() {
        return this.async != null ? this.async : true;
    }

    public NotificationType getType() {
        return this.type != null ? this.type : NotificationType.TRANSACTIONAL;
    }
}