package com.notificationplatform.exception;

public class WorkflowValidationException extends ValidationException {

    public WorkflowValidationException(String message) {
        super(message);
    }

    public WorkflowValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

