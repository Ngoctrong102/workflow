package com.notificationplatform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChannelTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChannelType {
    String message() default "Invalid channel type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

