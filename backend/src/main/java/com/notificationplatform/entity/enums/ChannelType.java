package com.notificationplatform.entity.enums;

/**
 * Enum representing channel type values.
 */
public enum ChannelType {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push"),
    SLACK("slack"),
    DISCORD("discord"),
    WEBHOOK("webhook");

    private final String value;

    ChannelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get ChannelType from string value.
     * @param value The string value
     * @return ChannelType enum or null if not found
     */
    public static ChannelType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ChannelType type : ChannelType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}

