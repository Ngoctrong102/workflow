package com.notificationplatform.entity.enums;

/**
 * Enum representing trigger status values.
 */
public enum TriggerStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    ERROR("error");

    private final String value;

    TriggerStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get TriggerStatus from string value.
     * @param value The string value
     * @return TriggerStatus enum or null if not found
     */
    public static TriggerStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TriggerStatus status : TriggerStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if trigger is active.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}

