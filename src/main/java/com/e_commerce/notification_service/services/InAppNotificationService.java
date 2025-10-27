package com.e_commerce.notification_service.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.e_commerce.notification_service.models.InAppNotification;
import com.e_commerce.notification_service.repositories.InAppNotificationRepository;

@Service
public class InAppNotificationService {

	@Autowired
	private InAppNotificationRepository repository;

	public InAppNotification create(Long userId, String title, String body) {
		InAppNotification notif = InAppNotification.builder()
				.userId(userId)
				.title(title)
				.body(body)
				.read(false)
				.build();
		return repository.save(notif);
	}

	public List<InAppNotification> list(Long userId) {
		return repository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	public void markRead(Long id) {
		repository.findById(id).ifPresent(n -> {
			n.setRead(true);
			repository.save(n);
		});
	}
}


