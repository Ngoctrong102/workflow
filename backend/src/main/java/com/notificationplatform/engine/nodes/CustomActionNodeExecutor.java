package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.workflow.ExecutionContextBuilder;
import com.notificationplatform.util.MvelEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor for Custom Action nodes.
 * Handles custom actions from Action Registry (send-email, send-sms, send-push, etc.).
 * 
 * This executor is called by ActionNodeExecutor for CUSTOM_ACTION action type.
 * 
 * See: @import(features/action-registry.md#custom-actions)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomActionNodeExecutor implements ActionExecutor {

    private final ActionRegistryService actionRegistryService;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing custom action node: nodeId={}", nodeId);
        
        try {
            // Get registry ID and load action from registry
            String registryId = (String) nodeData.get("registryId");
            if (registryId == null) {
                throw new IllegalArgumentException("Registry ID is required for custom action");
            }
            
            Action action;
            try {
                action = actionRegistryService.getActionById(registryId);
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Verify this is a custom action
            if (action.getType() != ActionType.CUSTOM_ACTION) {
                throw new IllegalArgumentException("Action is not a custom action: " + registryId);
            }
            
            // Get config values (new structure) or parse from nodeData (backward compatibility)
            Map<String, Object> configValues = getConfigValues(nodeData);
            
            // Build MVEL execution context
            Map<String, Object> mvelContext = ExecutionContextBuilder.buildContext(context);
            
            // Evaluate MVEL expressions in config values
            @SuppressWarnings("unchecked")
            Map<String, Object> resolvedConfig = (Map<String, Object>) 
                MvelEvaluator.evaluateObject(configValues, mvelContext);
            
            // Execute custom action based on registry ID
            Map<String, Object> output = executeCustomAction(action, resolvedConfig, context, nodeId);
            
            return new NodeExecutionResult(true, output);
            
        } catch (Exception e) {
            log.error("Error executing custom action: nodeId={}", nodeId, e);
            Map<String, Object> output = new HashMap<>();
            output.put("status", "failed");
            output.put("error", e.getMessage());
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }
    
    /**
     * Get config values from node data.
     * Supports both new structure (config.configValues) and old structure (direct fields).
     */
    private Map<String, Object> getConfigValues(Map<String, Object> nodeData) {
        // Try new structure first: nodeData.config.configValues
        Object configObj = nodeData.get("config");
        if (configObj instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) configObj;
            Object configValuesObj = config.get("configValues");
            if (configValuesObj instanceof Map) {
                return new HashMap<>((Map<String, Object>) configValuesObj);
            }
        }
        
        // Fallback to old structure: direct fields in nodeData
        Map<String, Object> configValues = new HashMap<>();
        // Copy all config-related fields
        for (Map.Entry<String, Object> entry : nodeData.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("registryId") && !key.equals("subtype") && !key.equals("type")) {
                configValues.put(key, entry.getValue());
            }
        }
        
        return configValues;
    }
    
    /**
     * Execute custom action based on action registry ID.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeCustomAction(
            Action action, 
            Map<String, Object> resolvedConfig, 
            ExecutionContext context,
            String nodeId) {
        
        String registryId = action.getId();
        Map<String, Object> output = new HashMap<>();
        
        // Handle different custom action types
        if (registryId.startsWith("send-email")) {
            return executeSendEmail(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-sms")) {
            return executeSendSms(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-push")) {
            return executeSendPush(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-in-app")) {
            return executeSendInApp(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-slack")) {
            return executeSendSlack(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-discord")) {
            return executeSendDiscord(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-teams")) {
            return executeSendTeams(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("send-webhook")) {
            return executeSendWebhook(resolvedConfig, context, nodeId);
        } else if (registryId.startsWith("wait-events")) {
            return executeWaitForEvents(resolvedConfig, context, nodeId);
        } else {
            log.warn("Unknown custom action registry ID: {}", registryId);
            output.put("status", "skipped");
            output.put("error", "Unknown custom action type: " + registryId);
            return output;
        }
    }
    
    /**
     * Execute send-email action.
     * Note: Email sending not yet implemented - requires integration with email service provider
     */
    private Map<String, Object> executeSendEmail(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "skipped");
        output.put("error", "Email sending not implemented - requires email service integration");
        output.put("channel", "email");
        return output;
    }
    
    /**
     * Execute send-sms action.
     * Note: SMS sending not yet implemented - requires integration with SMS service provider
     */
    private Map<String, Object> executeSendSms(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "skipped");
        output.put("error", "SMS sending not implemented - requires SMS service integration");
        output.put("channel", "sms");
        return output;
    }
    
    /**
     * Execute send-push action.
     * Note: Push notification sending not yet implemented - requires integration with push notification service
     */
    private Map<String, Object> executeSendPush(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "skipped");
        output.put("error", "Push notification sending not implemented - requires push service integration");
        output.put("channel", "push");
        return output;
    }
    
    /**
     * Execute send-in-app action.
     * Note: In-app notification sending not yet implemented - requires in-app notification service
     */
    private Map<String, Object> executeSendInApp(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "skipped");
        output.put("error", "In-app notification sending not implemented - requires in-app service integration");
        output.put("channel", "in-app");
        return output;
    }
    
    /**
     * Execute send-slack action.
     * Note: Slack integration not yet implemented
     */
    private Map<String, Object> executeSendSlack(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "not_implemented");
        output.put("error", "Slack action not yet implemented");
        return output;
    }
    
    /**
     * Execute send-discord action.
     * Note: Discord integration not yet implemented
     */
    private Map<String, Object> executeSendDiscord(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "not_implemented");
        output.put("error", "Discord action not yet implemented");
        return output;
    }
    
    /**
     * Execute send-teams action.
     * Note: Teams integration not yet implemented
     */
    private Map<String, Object> executeSendTeams(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "not_implemented");
        output.put("error", "Teams action not yet implemented");
        return output;
    }
    
    /**
     * Execute send-webhook action.
     * Note: Webhook sending not yet implemented - requires HTTP client integration
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeSendWebhook(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "POST");
        Map<String, String> headers = (Map<String, String>) config.get("headers");
        Object body = config.get("body");
        
        // Get variables from context for template rendering
        Map<String, Object> variables = context.getDataForNode(nodeId);
        
        // Render body if it's a string with variables
        if (body instanceof String && variables != null && !variables.isEmpty()) {
            body = renderSimpleTemplateHelper((String) body, variables);
        }
        
        Map<String, Object> output = new HashMap<>();
        output.put("status", "skipped");
        output.put("error", "Webhook sending not implemented - requires HTTP client integration");
        output.put("url", url);
        output.put("method", method);
        return output;
    }
    
    /**
     * Execute wait-events action.
     * Note: Wait-events logic not yet implemented - requires async event aggregation
     */
    private Map<String, Object> executeWaitForEvents(
            Map<String, Object> config, 
            ExecutionContext context, 
            String nodeId) {
        Map<String, Object> output = new HashMap<>();
        output.put("status", "not_implemented");
        output.put("error", "Wait for events action not yet implemented");
        return output;
    }
    
    /**
     * Simple template rendering helper - replaces @{variable} placeholders
     */
    private String renderSimpleTemplateHelper(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "@{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}

