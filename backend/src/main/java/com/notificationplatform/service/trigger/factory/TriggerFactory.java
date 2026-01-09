package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;

/**
 * Factory interface for creating Trigger entities.
 * Each trigger type has its own factory implementation.
 */
public interface TriggerFactory {

    /**
     * Get the trigger type this factory supports.
     */
    TriggerType getSupportedType();

    /**
     * Create a Trigger entity from CreateApiTriggerRequest.
     */
    default Trigger createFromApiRequest(CreateApiTriggerRequest request) {
        throw new UnsupportedOperationException("API trigger creation not supported by " + getClass().getSimpleName());
    }

    /**
     * Create a Trigger entity from CreateScheduleTriggerRequest.
     */
    default Trigger createFromScheduleRequest(CreateScheduleTriggerRequest request) {
        throw new UnsupportedOperationException("Schedule trigger creation not supported by " + getClass().getSimpleName());
    }

    /**
     * Create a Trigger entity from CreateFileTriggerRequest.
     */
    default Trigger createFromFileRequest(CreateFileTriggerRequest request) {
        throw new UnsupportedOperationException("File trigger creation not supported by " + getClass().getSimpleName());
    }

    /**
     * Create a Trigger entity from CreateEventTriggerRequest.
     */
    default Trigger createFromEventRequest(CreateEventTriggerRequest request) {
        throw new UnsupportedOperationException("Event trigger creation not supported by " + getClass().getSimpleName());
    }
}

