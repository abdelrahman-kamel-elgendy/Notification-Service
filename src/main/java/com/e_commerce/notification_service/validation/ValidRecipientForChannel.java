package com.e_commerce.notification_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RecipientChannelValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRecipientForChannel {
    String message() default "Recipient format is invalid for the selected channel";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}