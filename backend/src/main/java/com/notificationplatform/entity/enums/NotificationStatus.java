package com.notificationplatform.entity.enums;

/**
 * Enum representing notification status values.
 */
public enum NotificationStatus {
    QUEUED("queued"),
    SENDING("sending"),
    SENT("sent"),
    FAILED("failed");

    private final String value;

    NotificationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get NotificationStatus from string value.
     * @param value The string value
     * @return NotificationStatus enum or null if not found
     */
    public static NotificationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (NotificationStatus status : NotificationStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if notification is in progress.
     */
    public boolean isInProgress() {
        return this == QUEUED || this == SENDING;
    }

    /**
     * Check if notification is completed (successfully or not).
     */
    public boolean isCompleted() {
        return this == SENT || this == FAILED;
    }

    /**
     * Check if notification was sent successfully.
     */
    public boolean isSuccess() {
        return this == SENT;
    }
}

