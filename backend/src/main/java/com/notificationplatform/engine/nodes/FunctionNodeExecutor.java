package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.entity.Action;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.workflow.ExecutionContextBuilder;
import com.notificationplatform.util.MvelEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for Function Action node.
 * Evaluates expressions for data transformations.
 * 
 * This executor is called by ActionNodeExecutor for FUNCTION action type.
 * 
 * See: @import(features/node-types.md#function-action)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionNodeExecutor implements ActionExecutor {

    private final ActionRegistryService actionRegistryService;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing function action node: nodeId={}", nodeId);
        
        try {
            // Get registry ID and load action from registry
            String registryId = (String) nodeData.get("registryId");
            if (registryId == null) {
                throw new IllegalArgumentException("Registry ID is required for function action");
            }
            
            Action action;
            try {
                action = actionRegistryService.getActionById(registryId);
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Get config values (new structure) or parse from nodeData (backward compatibility)
            Map<String, Object> configValues = getConfigValues(nodeData);
            
            // Build MVEL execution context
            Map<String, Object> mvelContext = ExecutionContextBuilder.buildContext(context);
            
            // Evaluate MVEL expressions in config values
            Map<String, Object> resolvedConfig = (Map<String, Object>) 
                MvelEvaluator.evaluateObject(configValues, mvelContext);
            
            // Get expression from resolved config
            Object expressionObj = resolvedConfig.get("expression");
            if (expressionObj == null || expressionObj.toString().isEmpty()) {
                throw new IllegalArgumentException("Expression is required for function action");
            }
            
            String expression = expressionObj.toString();
            
            // Evaluate expression using MVEL
            // Expression may contain MVEL syntax, so we need to extract and evaluate it
            Object result;
            if (expression.contains("@{")) {
                // Expression contains MVEL syntax, evaluate it
                result = MvelEvaluator.evaluateExpression(expression, mvelContext);
            } else {
                // Expression is a pure MVEL expression, evaluate directly
                try {
                    result = MVEL.eval(expression, mvelContext);
                } catch (Exception e) {
                    // If evaluation fails, treat as string literal
                    result = expression;
                }
            }
            
            // Get output field name
            Object outputFieldObj = resolvedConfig.get("outputField");
            String outputField = outputFieldObj != null ? outputFieldObj.toString() : "result";
            
            // Build raw response
            Map<String, Object> rawResponse = new HashMap<>();
            rawResponse.put("result", result);
            rawResponse.put("expression", expression);
            
            // Build output context for output mapping
            Map<String, Object> outputContext = ExecutionContextBuilder.buildOutputContext(context, rawResponse);
            
            // Apply output mapping (if available from action registry or node config)
            Map<String, Object> output = applyOutputMapping(action, nodeData, outputContext, rawResponse, outputField);
            
            return new NodeExecutionResult(true, output);
            
        } catch (Exception e) {
            log.error("Error executing function action: nodeId={}", nodeId, e);
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
    @SuppressWarnings("unchecked")
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
        if (nodeData.containsKey("expression")) {
            configValues.put("expression", nodeData.get("expression"));
        }
        if (nodeData.containsKey("outputField")) {
            configValues.put("outputField", nodeData.get("outputField"));
        } else {
            configValues.put("outputField", "result");
        }
        
        return configValues;
    }
    
    /**
     * Apply output mapping to raw response.
     * Uses output mapping from action registry or node config (if provided).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> applyOutputMapping(
            Action action, 
            Map<String, Object> nodeData, 
            Map<String, Object> outputContext,
            Map<String, Object> rawResponse,
            String defaultOutputField) {
        
        // Get output mapping from node config (if provided) or action registry
        Map<String, String> outputMapping = null;
        
        // Try node config first (custom override)
        Object configObj = nodeData.get("config");
        if (configObj instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) configObj;
            Object outputMappingObj = config.get("outputMapping");
            if (outputMappingObj instanceof Map) {
                outputMapping = (Map<String, String>) outputMappingObj;
            }
        }
        
        // Fallback to action registry output mapping
        if (outputMapping == null && action.getConfigTemplate() != null) {
            Object outputMappingObj = action.getConfigTemplate().get("outputMapping");
            if (outputMappingObj instanceof Map) {
                outputMapping = (Map<String, String>) outputMappingObj;
            }
        }
        
        // If no output mapping, return raw response with default output field
        if (outputMapping == null || outputMapping.isEmpty()) {
            Map<String, Object> output = new HashMap<>(rawResponse);
            output.put(defaultOutputField, rawResponse.get("result"));
            return output;
        }
        
        // Apply output mapping with MVEL evaluation
        Map<String, Object> mappedOutput = new HashMap<>();
        for (Map.Entry<String, String> entry : outputMapping.entrySet()) {
            String fieldName = entry.getKey();
            String mvelExpression = entry.getValue();
            
            try {
                Object value = MvelEvaluator.evaluateExpression(mvelExpression, outputContext);
                mappedOutput.put(fieldName, value);
            } catch (Exception e) {
                log.warn("Failed to evaluate output mapping for field '{}': {}", fieldName, e.getMessage());
                // Use raw response value if available
                mappedOutput.put(fieldName, rawResponse.get(fieldName));
            }
        }
        
        return mappedOutput;
    }

}

