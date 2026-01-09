package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for Function Action node.
 * Evaluates expressions for data transformations.
 * 
 * See: @import(features/node-types.md#function-action)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionNodeExecutor implements NodeExecutor {

    private final TemplateRenderer templateRenderer;
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
            
            try {
                actionRegistryService.getActionById(registryId);
            } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Parse expression
            String expression = (String) nodeData.get("expression");
            if (expression == null || expression.isEmpty()) {
                throw new IllegalArgumentException("Expression is required for function action");
            }
            
            // Get variables from context
            Map<String, Object> variables = context.getDataForNode(nodeId);
            
            // Evaluate expression using template renderer
            // For MVP, we use simple template rendering
            // In the future, this could use a more sophisticated expression evaluator
            String result = templateRenderer.render(expression, variables);
            
            // Parse result if it's a number or boolean
            Object parsedResult = parseResult(result);
            
            // Get output field name
            String outputField = (String) nodeData.getOrDefault("outputField", "result");
            
            // Build output
            Map<String, Object> output = new HashMap<>();
            output.put(outputField, parsedResult);
            output.put("expression", expression);
            output.put("result", parsedResult);
            
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
     * Parse result string to appropriate type.
     */
    private Object parseResult(String result) {
        if (result == null) {
            return null;
        }
        
        // Try to parse as number
        try {
            if (result.contains(".")) {
                return Double.parseDouble(result);
            } else {
                return Long.parseLong(result);
            }
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(result) || "false".equalsIgnoreCase(result)) {
            return Boolean.parseBoolean(result);
        }
        
        // Return as string
        return result;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION;
    }
}

