package com.notificationplatform.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    /**
     * Get the string value for JSON serialization.
     * @return The string value
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Create ActionType from string value for JSON deserialization.
     * @param value The string value
     * @return ActionType enum
     * @throws IllegalArgumentException if value is not recognized
     */
    @JsonCreator
    public static ActionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ActionType type : ActionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ActionType value: " + value);
    }
}

