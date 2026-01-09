package com.notificationplatform.entity.enums;

/**
 * Enum representing action type values.
 */
public enum ActionType {
    API_CALL("api-call"),
    PUBLISH_EVENT("publish-event"),
    FUNCTION("function"),
    CUSTOM_ACTION("custom-action");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get ActionType from string value.
     * @param value The string value
     * @return ActionType enum or null if not found
     */
    public static ActionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ActionType type : ActionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}

