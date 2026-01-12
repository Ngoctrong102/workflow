package com.notificationplatform.service.workflow;

import com.notificationplatform.engine.ExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Builder for MVEL execution context from ExecutionContext.
 * Builds context structure for MVEL expression evaluation.
 * 
 * Context structure:
 * {
 *   "nodeId": { "field": value },  // Previous node outputs
 *   "_trigger": { "field": value }, // Trigger data (from first trigger node or aggregated)
 *   "_vars": { "varName": value },  // Workflow variables
 *   "_now": () -> timestamp,        // Built-in function
 *   "_uuid": () -> uuid             // Built-in function
 * }
 * 
 * See: @import(features/mvel-expression-system.md)
 */
@Slf4j
public class ExecutionContextBuilder {
    
    /**
     * Build MVEL execution context from workflow execution context.
     * 
     * @param executionContext Workflow execution context
     * @return Map suitable for MVEL evaluation
     */
    public static Map<String, Object> buildContext(ExecutionContext executionContext) {
        Map<String, Object> context = new HashMap<>();
        
        // Add previous node outputs (keyed by nodeId)
        // Example: { "fetchUser": { "userId": "123", "name": "John" } }
        if (executionContext.getNodeOutputs() != null) {
            for (Map.Entry<String, Object> entry : executionContext.getNodeOutputs().entrySet()) {
                String nodeId = entry.getKey();
                Object nodeOutput = entry.getValue();
                context.put(nodeId, nodeOutput);
            }
        }
        
        // Add trigger data (prefixed with _trigger)
        // Get trigger data from first trigger node in triggerDataMap, or from nodeOutputs
        Map<String, Object> triggerData = getTriggerData(executionContext);
        if (triggerData != null && !triggerData.isEmpty()) {
            context.put("_trigger", triggerData);
        }
        
        // Add workflow variables (prefixed with _vars)
        // Example: { "_vars": { "apiKey": "secret", "baseUrl": "https://api.com" } }
        if (executionContext.getVariables() != null && !executionContext.getVariables().isEmpty()) {
            context.put("_vars", executionContext.getVariables());
        }
        
        // Add built-in functions
        // Note: MVEL will call these as functions: @{_now()}, @{_uuid()}
        context.put("_now", (Supplier<Long>) () -> System.currentTimeMillis());
        context.put("_uuid", (Supplier<String>) () -> UUID.randomUUID().toString());
        
        return context;
    }
    
    /**
     * Build output context for output mapping evaluation.
     * Includes previous context plus _response containing raw action result.
     * 
     * @param executionContext Workflow execution context
     * @param rawResponse Raw response from action execution
     * @return Map suitable for MVEL output mapping evaluation
     */
    public static Map<String, Object> buildOutputContext(
            ExecutionContext executionContext, 
            Map<String, Object> rawResponse) {
        Map<String, Object> context = buildContext(executionContext);
        
        // Add raw action response (prefixed with _response)
        // Example: { "_response": { "statusCode": 200, "body": {...} } }
        if (rawResponse != null) {
            context.put("_response", rawResponse);
        }
        
        return context;
    }
    
    /**
     * Get trigger data from execution context.
     * Priority: nodeOutputs (if trigger node already executed) > triggerDataMap
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getTriggerData(ExecutionContext executionContext) {
        // First, try to get trigger data from nodeOutputs (if trigger node already executed)
        // Look for common trigger node patterns
        Map<String, Object> nodeOutputs = executionContext.getNodeOutputs();
        if (nodeOutputs != null) {
            for (Map.Entry<String, Object> entry : nodeOutputs.entrySet()) {
                String nodeId = entry.getKey();
                // Check if this looks like a trigger node output
                Object output = entry.getValue();
                if (output instanceof Map) {
                    Map<String, Object> outputMap = (Map<String, Object>) output;
                    // If output contains common trigger fields, use it
                    if (outputMap.containsKey("eventType") || 
                        outputMap.containsKey("triggerType") ||
                        outputMap.containsKey("userId") ||
                        outputMap.containsKey("data")) {
                        return outputMap;
                    }
                }
            }
        }
        
        // Fallback: get from triggerDataMap (first entry)
        Map<String, Map<String, Object>> triggerDataMap = executionContext.getTriggerDataMap();
        if (triggerDataMap != null && !triggerDataMap.isEmpty()) {
            return triggerDataMap.values().iterator().next();
        }
        
        return new HashMap<>();
    }
}

