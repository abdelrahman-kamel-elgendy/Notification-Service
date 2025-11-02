package com.e_commerce.notification_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.StringUtils;

import com.e_commerce.notification_service.dto.request.NotificationRequest;
import com.e_commerce.notification_service.models.enums.ChannelType;

import java.util.regex.Pattern;

public class RecipientChannelValidator implements ConstraintValidator<ValidRecipientForChannel, NotificationRequest> {

    // Phone number pattern (E.164 format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    // URL pattern for webhooks
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            Pattern.CASE_INSENSITIVE);

    // Slack channel/user pattern
    private static final Pattern SLACK_PATTERN = Pattern.compile("^[#@][a-zA-Z0-9._-]+$");

    // Discord ID pattern (snowflake format)
    private static final Pattern DISCORD_PATTERN = Pattern.compile("^[0-9]{17,20}$");

    // Firebase push token pattern (approximate)
    private static final Pattern PUSH_TOKEN_PATTERN = Pattern.compile("^[a-zA-Z0-9:_-]{100,500}$");

    // WhatsApp pattern (same as phone but can also use group IDs)
    private static final Pattern WHATSAPP_PATTERN = Pattern.compile("^(\\+[1-9]\\d{1,14}|[a-zA-Z0-9._-]+@g\\.us)$");

    @Override
    public boolean isValid(NotificationRequest request, ConstraintValidatorContext context) {
        if (request.getChannel() == null || !StringUtils.hasText(request.getRecipient())) {
            return true; // Let @NotNull and @NotBlank handle these cases
        }

        String recipient = request.getRecipient().trim();
        boolean isValid = validateRecipientForChannel(recipient, request.getChannel());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    getErrorMessage(recipient, request.getChannel())).addPropertyNode("recipient")
                    .addConstraintViolation();
        }

        return isValid;
    }

    private boolean validateRecipientForChannel(String recipient, ChannelType channel) {
        switch (channel) {
            case EMAIL:
                return isValidEmail(recipient);
            case WHATSAPP:
                return isValidWhatsappRecipient(recipient);
            case PUSH:
                return isValidPushToken(recipient);
            case IN_APP:
                return isValidInAppRecipient(recipient);
            default:
                return true;
        }
    }

    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    private boolean isValidPhoneNumber(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidWhatsappRecipient(String recipient) {
        return PHONE_PATTERN.matcher(recipient).matches() ||
                recipient.matches("^[a-zA-Z0-9._-]+@g\\.us$"); // WhatsApp group ID
    }

    private boolean isValidPushToken(String token) {
        // Firebase FCM tokens are typically long alphanumeric strings
        return PUSH_TOKEN_PATTERN.matcher(token).matches();
    }

    private boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    private boolean isValidSlackRecipient(String recipient) {
        return SLACK_PATTERN.matcher(recipient).matches() ||
                isValidEmail(recipient); // Can also be email for direct messages
    }

    private boolean isValidDiscordRecipient(String recipient) {
        return DISCORD_PATTERN.matcher(recipient).matches();
    }

    private boolean isValidTeamsRecipient(String recipient) {
        // Teams can be email for users or webhook URL for channels
        return isValidEmail(recipient) || isValidUrl(recipient);
    }

    private boolean isValidInAppRecipient(String recipient) {
        // In-app notifications typically use user IDs or usernames
        return recipient.length() >= 1 && recipient.length() <= 100 &&
                recipient.matches("^[a-zA-Z0-9._-]+$");
    }

    private String getErrorMessage(String recipient, ChannelType channel) {
        switch (channel) {
            case EMAIL:
                return String.format("'%s' is not a valid email address", recipient);
            case WHATSAPP:
                return String.format("'%s' is not a valid WhatsApp recipient (phone number or group ID)", recipient);
            case PUSH:
                return String.format("'%s' is not a valid push notification token", recipient);
            case IN_APP:
                return String.format("'%s' is not a valid user identifier", recipient);
            default:
                return String.format("'%s' is not valid for channel '%s'", recipient, channel);
        }
    }
}
