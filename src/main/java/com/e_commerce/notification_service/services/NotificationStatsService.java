package com.e_commerce.notification_service.services;

import com.e_commerce.notification_service.models.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class NotificationStatsService {

    private final Map<String, UserNotificationStats> userStats = new ConcurrentHashMap<>();

    public void incrementNotificationCount(String userId, NotificationType type) {
        UserNotificationStats stats = userStats.computeIfAbsent(userId, k -> new UserNotificationStats());
        stats.incrementCount(type);

        log.debug("Updated notification stats for user {}: {}", userId, stats.getTotalCount());
    }

    public long getNotificationCount(String userId) {
        UserNotificationStats stats = userStats.get(userId);
        return stats != null ? stats.getTotalCount() : 0;
    }

    public Map<NotificationType, Long> getNotificationCountByType(String userId) {
        UserNotificationStats stats = userStats.get(userId);
        return stats != null ? stats.getCountByType() : Map.of();
    }

    // Inner class for user notification statistics
    private static class UserNotificationStats {
        private final Map<NotificationType, AtomicLong> counts = new ConcurrentHashMap<>();

        public void incrementCount(NotificationType type) {
            counts.computeIfAbsent(type, k -> new AtomicLong(0)).incrementAndGet();
        }

        public long getTotalCount() {
            return counts.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
        }

        public Map<NotificationType, Long> getCountByType() {
            Map<NotificationType, Long> result = new ConcurrentHashMap<>();
            counts.forEach((type, atomicCount) -> result.put(type, atomicCount.get()));
            return result;
        }
    }
}
