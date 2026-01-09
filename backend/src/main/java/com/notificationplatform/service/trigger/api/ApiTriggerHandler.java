package com.notificationplatform.service.trigger.api;

import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for API trigger requests.
 * Validates requests, extracts trigger data, and starts workflow execution.
 * 
 * See: @import(features/triggers.md#1-api-call-trigger)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTriggerHandler {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutor workflowExecutor;
    private final TriggerEndpointRegistry endpointRegistry;

    /**
     * Handle HTTP request for API trigger.
     * 
     * @param endpointPath Endpoint path
     * @param httpMethod HTTP method
     * @param requestBody Request body
     * @param apiKey API key from header
     * @param queryParams Query parameters
     * @return Trigger activation response
     */
    @Transactional
    public TriggerActivationResponse handleRequest(String endpointPath, String httpMethod,
                                                   Map<String, Object> requestBody, String apiKey,
                                                   Map<String, String> queryParams) {
        log.info("Handling API trigger request: endpointPath={}, method={}", endpointPath, httpMethod);
        
        // Get trigger by endpoint path
        Trigger trigger = endpointRegistry.getTriggerByEndpoint(endpointPath);
        if (trigger == null) {
            log.warn("No trigger found for endpoint: endpointPath={}", endpointPath);
            throw new RuntimeException("No trigger found for endpoint: " + endpointPath);
        }
        
        // Check if trigger is active
        if (trigger.getStatus() != TriggerStatus.ACTIVE) {
            log.warn("Trigger is not active: triggerId={}, status={}", trigger.getId(), trigger.getStatus());
            throw new RuntimeException("Trigger is not active: " + trigger.getId());
        }
        
        // Validate authentication if configured
        validateAuthentication(trigger, apiKey);
        
        // Validate request against trigger config
        validateRequest(trigger, httpMethod, requestBody, queryParams);
        
        // Extract trigger data from request
        Map<String, Object> triggerData = extractTriggerData(requestBody, queryParams);
        
        // Get workflow
        Workflow workflow = trigger.getWorkflow();
        if (workflow == null) {
            throw new RuntimeException("Workflow not found for trigger: " + trigger.getId());
        }
        
        // Check if workflow is active
        if (workflow.getStatus() != com.notificationplatform.entity.enums.WorkflowStatus.ACTIVE) {
            log.warn("Workflow is not active: workflowId={}, status={}", workflow.getId(), workflow.getStatus());
            throw new RuntimeException("Workflow is not active: " + workflow.getId());
        }
        
        // Execute workflow
        Execution execution = workflowExecutor.execute(workflow, triggerData, trigger.getId());
        
        // Build response
        TriggerActivationResponse response = new TriggerActivationResponse();
        response.setWorkflowId(workflow.getId());
        response.setExecutionId(execution.getId());
        response.setStatus("triggered");
        response.setMessage("Workflow execution started");
        
        log.info("Workflow triggered via API: workflowId={}, executionId={}, triggerId={}", 
                 workflow.getId(), execution.getId(), trigger.getId());
        
        return response;
    }

    /**
     * Validate authentication if configured.
     */
    @SuppressWarnings("unchecked")
    private void validateAuthentication(Trigger trigger, String apiKey) {
        Map<String, Object> config = trigger.getConfig();
        if (config == null) {
            return;
        }
        
        Map<String, Object> auth = (Map<String, Object>) config.get("authentication");
        if (auth == null) {
            return; // No authentication required
        }
        
        String authType = (String) auth.get("type");
        if ("none".equals(authType) || authType == null) {
            return; // No authentication required
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Authentication required but API key not provided");
        }
        
        if ("api-key".equals(authType)) {
            String expectedApiKey = (String) auth.get("apiKey");
            if (expectedApiKey != null && !expectedApiKey.equals(apiKey)) {
                throw new RuntimeException("Invalid API key");
            }
        } else if ("bearer-token".equals(authType)) {
            String expectedToken = (String) auth.get("token");
            if (expectedToken != null && !expectedToken.equals(apiKey)) {
                throw new RuntimeException("Invalid bearer token");
            }
        }
    }

    /**
     * Validate request against trigger configuration.
     */
    @SuppressWarnings("unchecked")
    private void validateRequest(Trigger trigger, String httpMethod, 
                                Map<String, Object> requestBody, Map<String, String> queryParams) {
        Map<String, Object> config = trigger.getConfig();
        if (config == null) {
            return;
        }
        
        // Validate HTTP method
        String expectedMethod = (String) config.get("httpMethod");
        if (expectedMethod != null && !expectedMethod.equalsIgnoreCase(httpMethod)) {
            throw new RuntimeException("Invalid HTTP method. Expected: " + expectedMethod + ", got: " + httpMethod);
        }
        
        // Validate request schema if configured
        Map<String, Object> requestSchema = (Map<String, Object>) config.get("requestSchema");
        if (requestSchema != null) {
            // Basic validation - in production, use JSON schema validator
            // For MVP, we'll do basic checks
            if (requestBody == null && "POST".equalsIgnoreCase(httpMethod)) {
                throw new RuntimeException("Request body is required for POST requests");
            }
        }
    }

    /**
     * Extract trigger data from request.
     */
    private Map<String, Object> extractTriggerData(Map<String, Object> requestBody, Map<String, String> queryParams) {
        Map<String, Object> triggerData = new HashMap<>();
        
        // Add request body data
        if (requestBody != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
            if (data != null) {
                triggerData.putAll(data);
            } else {
                // If no "data" key, use entire body
                triggerData.putAll(requestBody);
            }
        }
        
        // Add query parameters
        if (queryParams != null && !queryParams.isEmpty()) {
            triggerData.put("queryParams", queryParams);
        }
        
        return triggerData;
    }
}

