package com.notificationplatform.service.trigger.handler;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;

/**
 * Strategy interface for handling different trigger types.
 * Each trigger type (API, Schedule, File, Event) has its own handler implementation.
 * Spring will auto-inject all implementations into a Map for easy lookup.
 */
public interface TriggerHandler {

    /**
     * Get the trigger type this handler supports.
     */
    TriggerType getSupportedType();

    /**
     * Handle trigger activation (e.g., register schedule, start consumer).
     */
    void onActivate(Trigger trigger);

    /**
     * Handle trigger deactivation (e.g., cancel schedule, stop consumer).
     */
    void onDeactivate(Trigger trigger);

    /**
     * Handle trigger update (e.g., update schedule, reconfigure consumer).
     */
    void onUpdate(Trigger trigger);

    /**
     * Handle trigger deletion (cleanup resources).
     */
    void onDelete(Trigger trigger);
}

