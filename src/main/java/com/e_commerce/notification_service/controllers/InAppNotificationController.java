package com.e_commerce.notification_service.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.e_commerce.notification_service.models.InAppNotification;
import com.e_commerce.notification_service.services.InAppNotificationService;

@RestController
@RequestMapping("/api/notifications/in-app")
public class InAppNotificationController {

	@Autowired
	private InAppNotificationService service;

	@PostMapping("/create")
	public ResponseEntity<InAppNotification> create(@RequestParam Long userId, @RequestParam String title,
			@RequestParam String body) {
		return ResponseEntity.ok(service.create(userId, title, body));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<InAppNotification>> list(@PathVariable Long userId) {
		return ResponseEntity.ok(service.list(userId));
	}

	@PostMapping("/mark-read/{id}")
	public ResponseEntity<Void> markRead(@PathVariable Long id) {
		service.markRead(id);
		return ResponseEntity.noContent().build();
	}
}
