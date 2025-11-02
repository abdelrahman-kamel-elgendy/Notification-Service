package com.e_commerce.notification_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.e_commerce.notification_service.models.NotificationLog;
import com.e_commerce.notification_service.models.enums.ChannelType;
import com.e_commerce.notification_service.models.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long>, JpaSpecificationExecutor<NotificationLog> {

    List<NotificationLog> findByRecipientAndChannelOrderByCreatedAtDesc(String recipient, ChannelType channel);

    List<NotificationLog> findByStatusAndRetryCountLessThan(NotificationStatus status, int retryCount);

    Page<NotificationLog> findByRecipient(String recipient, Pageable pageable);

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.createdAt BETWEEN :startDate AND :endDate")
    List<NotificationLog> findNotificationsBetweenDates(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.channel = :channel AND nl.status = :status")
    long countByChannelAndStatus(@Param("channel") ChannelType channel,
            @Param("status") NotificationStatus status);

    List<NotificationLog> findByRecipientAndCreatedAtAfter(String recipient, LocalDateTime date);
}
