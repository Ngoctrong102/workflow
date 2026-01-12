package com.notificationplatform.dto.response;

import com.notificationplatform.entity.enums.TriggerType;
import lombok.Data;

import java.util.Map;

/**
 * Response DTO for workflow trigger instances.
 * Represents a trigger instance in a workflow with its config and runtime state.
 */
@Data
public class WorkflowTriggerResponse {

    /**
     * Node ID in workflow definition
     */
    private String nodeId;

    /**
     * Reference to trigger config (ID of trigger config in database)
     */
    private String triggerConfigId;

    /**
     * Trigger type (api-call, scheduler, event)
     */
    private TriggerType triggerType;

    /**
     * Full trigger config from database (merged with instance config)
     */
    private Map<String, Object> triggerConfig;

    /**
     * Instance-specific overrides (e.g., consumerGroup, endpointPath override)
     */
    private Map<String, Object> instanceConfig;

    /**
     * Runtime state: INITIALIZED, ACTIVE, PAUSED, STOPPED, ERROR
     */
    private String runtimeState;

    /**
     * Error message if runtime state is ERROR
     */
    private String errorMessage;
}

