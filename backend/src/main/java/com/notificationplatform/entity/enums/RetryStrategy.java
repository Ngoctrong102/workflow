package com.notificationplatform.entity.enums;

/**
 * Enum representing retry strategy values.
 */
public enum RetryStrategy {
    IMMEDIATE("immediate"),
    EXPONENTIAL_BACKOFF("exponential_backoff"),
    FIXED_DELAY("fixed_delay"),
    CUSTOM("custom");

    private final String value;

    RetryStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get RetryStrategy from string value.
     * @param value The string value
     * @return RetryStrategy enum or null if not found
     */
    public static RetryStrategy fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RetryStrategy strategy : RetryStrategy.values()) {
            if (strategy.value.equalsIgnoreCase(value)) {
                return strategy;
            }
        }
        return null;
    }
}

