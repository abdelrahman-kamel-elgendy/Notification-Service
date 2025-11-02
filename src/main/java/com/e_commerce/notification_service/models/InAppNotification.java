package com.e_commerce.notification_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import com.e_commerce.notification_service.models.enums.NotificationPriority;
import com.e_commerce.notification_service.models.enums.NotificationType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "in_app_notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
        @Index(name = "idx_is_read", columnList = "isRead")
})
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @ElementCollection
    @CollectionTable(name = "in_app_notification_metadata", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
