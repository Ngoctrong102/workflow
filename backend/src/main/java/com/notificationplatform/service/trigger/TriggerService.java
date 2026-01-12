package com.notificationplatform.service.trigger;

import com.notificationplatform.dto.request.CreateTriggerConfigRequest;
import com.notificationplatform.dto.request.UpdateTriggerConfigRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.dto.response.TriggerResponse;

import java.util.Map;

/**
 * Service for managing trigger configs.
 * Trigger configs are independent and can be shared across multiple workflows.
 */
public interface TriggerService {

    /**
     * Create a new trigger config.
     * Trigger configs are independent and do not belong to any workflow.
     */
    TriggerResponse createTriggerConfig(CreateTriggerConfigRequest request);

    /**
     * Get trigger config by ID.
     */
    TriggerResponse getTriggerConfigById(String id);

    /**
     * List trigger configs with pagination and filters.
     */
    PagedResponse<TriggerResponse> listTriggerConfigs(String triggerType, String status, String search, int limit, int offset);

    /**
     * Update trigger config.
     * When trigger config is updated, changes apply to all workflows using it.
     */
    TriggerResponse updateTriggerConfig(String id, UpdateTriggerConfigRequest request);

    /**
     * Delete trigger config (soft delete).
     * Deleting trigger config does not affect workflows.
     */
    void deleteTriggerConfig(String id);

    /**
     * Activate API trigger (for triggering workflow execution).
     * This method is used when an HTTP request is received at the trigger endpoint.
     * 
     * Note: This method needs to find which workflow(s) are using this trigger config.
     * This will be refactored in a later sprint to support multiple workflows per trigger.
     */
    TriggerActivationResponse activateApiTrigger(String path, String method, Map<String, Object> requestData, String apiKey);

    /**
     * Get trigger config by endpoint path and method.
     * Used for API trigger endpoint lookup.
     */
    TriggerResponse getTriggerByPath(String path, String method);
}

