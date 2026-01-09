package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor for Loop node.
 * Iterates over array and processes each item in sequence.
 * 
 * See: @import(features/node-types.md#loop)
 */
@Slf4j
@Component
public class LoopNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing loop node: nodeId={}", nodeId);
        
        String arrayField = (String) nodeData.getOrDefault("arrayField", "items");
        String itemVariable = (String) nodeData.getOrDefault("itemVariable", "item");
        String indexVariable = (String) nodeData.getOrDefault("indexVariable", "index");
        
        // Get array from context
        Object arrayObj = getFieldValue(arrayField, context);
        
        if (!(arrayObj instanceof List)) {
            log.warn("Array field is not a list: arrayField={}, nodeId={}", arrayField, nodeId);
            Map<String, Object> output = new HashMap<>();
            output.put("itemsProcessed", 0);
            output.put("results", new ArrayList<>());
            return new NodeExecutionResult(true, output);
        }
        
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) arrayObj;
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Process each item
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            
            // Set item variable in context
            context.setVariable(itemVariable, item);
            context.setVariable(indexVariable, i);
            
            // Store result
            Map<String, Object> result = new HashMap<>();
            result.put("index", i);
            result.put("item", item);
            results.add(result);
        }
        
        // Clear loop variables
        context.getVariables().remove(itemVariable);
        context.getVariables().remove(indexVariable);
        
        Map<String, Object> output = new HashMap<>();
        output.put("itemsProcessed", items.size());
        output.put("results", results);
        
        return new NodeExecutionResult(true, output);
    }

    /**
     * Get field value from context.
     */
    @SuppressWarnings("unchecked")
    private Object getFieldValue(String field, ExecutionContext context) {
        if (field == null) {
            return null;
        }
        
        String[] parts = field.split("\\.");
        
        // Check if field starts with _nodeOutputs
        if (parts.length > 1 && "_nodeOutputs".equals(parts[0])) {
            Map<String, Object> nodeOutputs = context.getNodeOutputs();
            if (parts.length >= 2) {
                Object nodeOutput = nodeOutputs.get(parts[1]);
                if (nodeOutput instanceof Map) {
                    Map<String, Object> output = (Map<String, Object>) nodeOutput;
                    Object current = output;
                    for (int i = 2; i < parts.length; i++) {
                        if (current instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) current;
                            current = map.get(parts[i]);
                        } else {
                            return null;
                        }
                    }
                    return current;
                }
            }
            return null;
        }
        
        // Try variables
        Object variableValue = context.getVariable(field);
        if (variableValue != null) {
            return variableValue;
        }
        
        // Try node outputs
        Map<String, Object> nodeOutputs = context.getNodeOutputs();
        Object current = null;
        for (Object nodeOutput : nodeOutputs.values()) {
            if (nodeOutput instanceof Map) {
                Map<String, Object> output = (Map<String, Object>) nodeOutput;
                current = output;
                break;
            }
        }
        
        if (current == null) {
            return null;
        }
        
        for (String part : parts) {
            if (current instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LOGIC;
    }
}

