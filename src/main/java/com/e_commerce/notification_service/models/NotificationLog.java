package com.e_commerce.notification_service.models;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_logs")
public class NotificationLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String type; // EMAIL, SMS, PUSH, IN_APP

	@Column(nullable = false, length = 255)
	private String recipient;

	@Column(nullable = false, length = 255)
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false, length = 20)
	private String status; // SENT, FAILED, PENDING

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	@Column(length = 50)
	private String templateName;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column
	private Instant updatedAt;

	@Column
	private Instant sentAt;

	@Column
	private Integer retryCount;

	@Column(length = 1000)
	private String metadata; // JSON string for additional data

	@Column(length = 100)
	private String idempotencyKey;

	@Column(length = 30)
	private String channel; // EMAIL, SMS, PUSH, IN_APP

	@Column(length = 50)
	private String provider; // e.g., SMTP, Twilio, FCM

	@Column
	private Integer attempts;
}


