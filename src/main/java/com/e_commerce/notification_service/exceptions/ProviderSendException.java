package com.e_commerce.notification_service.exceptions;

public class ProviderSendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProviderSendException(String message) {
        super(message);
    }

    public ProviderSendException(String message, Throwable cause) {
        super(message, cause);
    }
}


