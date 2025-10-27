package com.e_commerce.notification_service.dto;

import java.util.Map;

import com.e_commerce.notification_service.models.ChannelType;
import com.e_commerce.notification_service.models.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DispatchRequest {

	@NotNull
	private ChannelType channel;

	@NotNull
	private NotificationType type;

	@NotBlank
	private String to;

	private String templateName;
	private Map<String, Object> variables;

	// idempotency key to dedupe requests
	private String idempotencyKey;

	// free-form metadata for auditing
	private Map<String, String> metadata;
}


