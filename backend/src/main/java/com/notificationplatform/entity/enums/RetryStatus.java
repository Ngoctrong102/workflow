package com.notificationplatform.entity.enums;

/**
 * Enum representing retry status values.
 */
public enum RetryStatus {
    PENDING("pending"),
    SCHEDULED("scheduled"),
    RETRYING("retrying"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String value;

    RetryStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get RetryStatus from string value.
     * @param value The string value
     * @return RetryStatus enum or null if not found
     */
    public static RetryStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RetryStatus status : RetryStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if retry is in progress.
     */
    public boolean isInProgress() {
        return this == PENDING || this == SCHEDULED || this == RETRYING;
    }

    /**
     * Check if retry is finished.
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}

