package com.notificationplatform.entity.enums;

/**
 * Enum representing trigger type values.
 */
public enum TriggerType {
    API_CALL("api-call"),
    SCHEDULER("scheduler"),
    EVENT("event");

    private final String value;

    TriggerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get TriggerType from string value.
     * @param value The string value
     * @return TriggerType enum or null if not found
     */
    public static TriggerType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TriggerType type : TriggerType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}

