package com.e_commerce.notification_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.e_commerce.notification_service.models.InAppNotification;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
	List<InAppNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
}


