package com.e_commerce.notification_service.exceptions;

public class ProviderConfigurationException extends NotificationException {
    public ProviderConfigurationException(String provider) {
        super("Provider configuration error: " + provider, "PROVIDER_CONFIG_ERROR", provider);
    }

    public ProviderConfigurationException(String provider, String message) {
        super("Provider configuration error for " + provider + ": " + message,
                "PROVIDER_CONFIG_ERROR", provider);
    }
}