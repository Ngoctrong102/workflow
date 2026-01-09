package com.notificationplatform.entity.enums;

/**
 * Enum representing execution status values.
 */
public enum ExecutionStatus {
    RUNNING("running"),
    WAITING("waiting"),
    PAUSED("paused"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String value;

    ExecutionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get ExecutionStatus from string value.
     * @param value The string value
     * @return ExecutionStatus enum or null if not found
     */
    public static ExecutionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExecutionStatus status : ExecutionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if execution is in progress.
     */
    public boolean isInProgress() {
        return this == RUNNING || this == WAITING || this == PAUSED;
    }

    /**
     * Check if execution is finished (successfully or not).
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * Check if execution completed successfully.
     */
    public boolean isSuccess() {
        return this == COMPLETED;
    }
}

