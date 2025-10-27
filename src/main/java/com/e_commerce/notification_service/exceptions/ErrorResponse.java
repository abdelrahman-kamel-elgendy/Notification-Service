package com.e_commerce.notification_service.exceptions;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final String error;
    private final String message;
    private final String path;
}


