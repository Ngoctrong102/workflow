package com.notificationplatform.validation;

import com.notificationplatform.service.trigger.schedule.CronValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CronExpressionValidator implements ConstraintValidator<ValidCronExpression, String> {

    private final CronValidator cronValidator;

    @Autowired
    public CronExpressionValidator(CronValidator cronValidator) {
        this.cronValidator = cronValidator;
    }

    @Override
    public void initialize(ValidCronExpression constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotNull handle null validation
        }

        if (!cronValidator.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid cron expression: " + value)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

