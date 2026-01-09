package com.notificationplatform.entity.enums;

/**
 * Enum representing node execution status values.
 */
public enum NodeExecutionStatus {
    RUNNING("running"),
    WAITING("waiting"),
    PAUSED("paused"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    NodeExecutionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get NodeExecutionStatus from string value.
     * @param value The string value
     * @return NodeExecutionStatus enum or null if not found
     */
    public static NodeExecutionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (NodeExecutionStatus status : NodeExecutionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if node execution is in progress.
     */
    public boolean isInProgress() {
        return this == RUNNING || this == WAITING || this == PAUSED;
    }

    /**
     * Check if node execution is finished.
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED;
    }
}

