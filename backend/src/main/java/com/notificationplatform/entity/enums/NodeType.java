package com.notificationplatform.entity.enums;

/**
 * Enum representing node types in workflow definitions.
 */
public enum NodeType {
    TRIGGER,
    API_TRIGGER,
    SCHEDULE_TRIGGER,
    FILE_TRIGGER,
    EVENT_TRIGGER,
    ACTION,
    LOGIC,
    DATA,
    WAIT_EVENTS;
}

