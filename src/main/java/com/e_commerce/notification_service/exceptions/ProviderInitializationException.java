package com.e_commerce.notification_service.exceptions;

public class ProviderInitializationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProviderInitializationException(String message) {
        super(message);
    }

    public ProviderInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}


