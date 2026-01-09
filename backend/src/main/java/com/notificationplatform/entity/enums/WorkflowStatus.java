package com.notificationplatform.entity.enums;

/**
 * Enum representing workflow status values.
 */
public enum WorkflowStatus {
    DRAFT("draft"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    PAUSED("paused"),
    ARCHIVED("archived");

    private final String value;

    WorkflowStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get WorkflowStatus from string value.
     * @param value The string value
     * @return WorkflowStatus enum or null if not found
     */
    public static WorkflowStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (WorkflowStatus status : WorkflowStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if workflow is in an active state (can execute).
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Check if workflow can be executed.
     */
    public boolean canExecute() {
        return this == ACTIVE;
    }
}

