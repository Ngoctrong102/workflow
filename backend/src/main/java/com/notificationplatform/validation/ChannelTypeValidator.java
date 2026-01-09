package com.notificationplatform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class ChannelTypeValidator implements ConstraintValidator<ValidChannelType, String> {

    private static final List<String> VALID_CHANNEL_TYPES = Arrays.asList(
            "email", "sms", "push", "slack", "discord", "teams", "webhook"
    );

    @Override
    public void initialize(ValidChannelType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        return VALID_CHANNEL_TYPES.contains(value.toLowerCase());
    }
}

