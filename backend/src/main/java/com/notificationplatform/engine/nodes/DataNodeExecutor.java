package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * Executor for data transformation nodes (map, filter, transform)
 * For MVP, basic data transformation
 */
@Slf4j
@Component
public class DataNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing data node: nodeId={}, subtype={}", nodeId, nodeData.get("subtype"));
        
        String subtype = (String) nodeData.getOrDefault("subtype", "map");
        
        // Get input data from previous node or context
        Map<String, Object> inputData = getInputData(nodeId, context);
        
        Map<String, Object> output = new HashMap<>();
        
        try {
            if ("map".equals(subtype)) {
                // Map node - transform data structure
                output = executeMap(nodeData, inputData, context);
            } else if ("filter".equals(subtype)) {
                // Filter node - filter array based on condition
                output = executeFilter(nodeData, inputData, context);
            } else if ("transform".equals(subtype)) {
                // Transform node - custom data transformation
                output = executeTransform(nodeData, inputData, context);
            } else {
                // Default: pass through
                output.putAll(inputData);
            }
            
            NodeExecutionResult result = new NodeExecutionResult(true, output);
            return result;
            
        } catch (Exception e) {
            log.error("Error executing data node: nodeId={}", nodeId, e);
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }

    private Map<String, Object> executeMap(Map<String, Object> nodeData, Map<String, Object> inputData, ExecutionContext context) {
        // Map node - transform data structure based on mapping rules
        Map<String, String> mapping = (Map<String, String>) nodeData.get("mapping");
        
        Map<String, Object> output = new HashMap<>();
        
        if (mapping != null && !mapping.isEmpty()) {
            // Apply mapping rules
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String targetField = entry.getKey();
                String sourceField = entry.getValue();
                
                Object value = getNestedValue(inputData, sourceField);
                setNestedValue(output, targetField, value);
            }
        } else {
            // No mapping defined, pass through
            output.putAll(inputData);
        }
        
        return output;
    }

    private Map<String, Object> executeFilter(Map<String, Object> nodeData, Map<String, Object> inputData, ExecutionContext context) {
        // Filter node - filter array based on condition
        Map<String, Object> condition = (Map<String, Object>) nodeData.get("condition");
        
        String arrayField = (String) nodeData.get("arrayField");
        
        List<Map<String, Object>> array = arrayField != null ? 
            (List<Map<String, Object>>) getNestedValue(inputData, arrayField) :
            (List<Map<String, Object>>) inputData.get("items");
        
        if (array == null) {
            return inputData; // No array to filter
        }
        
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : array) {
            if (evaluateFilterCondition(condition, item)) {
                filtered.add(item);
            }
        }
        
        Map<String, Object> output = new HashMap<>(inputData);
        if (arrayField != null) {
            setNestedValue(output, arrayField, filtered);
        } else {
            output.put("items", filtered);
        }
        
        return output;
    }

    private Map<String, Object> executeTransform(Map<String, Object> nodeData, Map<String, Object> inputData, ExecutionContext context) {
        // Transform node - custom data transformation
        // For MVP, support field renaming and value transformation
        Map<String, Object> transform = (Map<String, Object>) nodeData.get("transform");
        
        Map<String, Object> output = new HashMap<>();
        
        if (transform != null) {
            // Apply transformations
            for (Map.Entry<String, Object> entry : transform.entrySet()) {
                String targetField = entry.getKey();
                Object transformRule = entry.getValue();
                
                if (transformRule instanceof String) {
                    // Field reference or expression
                    String rule = (String) transformRule;
                    if (rule.startsWith("{{") && rule.endsWith("}}")) {
                        // Variable reference
                        String varName = rule.substring(2, rule.length() - 2);
                        Object value = getNestedValue(inputData, varName);
                        output.put(targetField, value);
                    } else {
                        // Direct field reference
                        Object value = getNestedValue(inputData, rule);
                        output.put(targetField, value);
                    }
                } else {
                    // Direct value
                    output.put(targetField, transformRule);
                }
            }
        } else {
            // No transform defined, pass through
            output.putAll(inputData);
        }
        
        return output;
    }

    private boolean evaluateFilterCondition(Map<String, Object> condition, Map<String, Object> item) {
        if (condition == null) {
            return true; // No condition, include all
        }
        
        String field = (String) condition.get("field");
        String operator = (String) condition.getOrDefault("operator", "equals");
        Object value = condition.get("value");
        
        Object fieldValue = item.get(field);
        
        switch (operator) {
            case "equals":
                return Objects.equals(fieldValue, value);
            case "not_equals":
                return !Objects.equals(fieldValue, value);
            case "contains":
                return fieldValue != null && fieldValue.toString().contains(value != null ? value.toString() : "");
            default:
                return true;
        }
    }

    private Object getNestedValue(Map<String, Object> data, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = data;
        
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

    private void setNestedValue(Map<String, Object> data, String path, Object value) {
        if (path == null || path.isEmpty()) {
            return;
        }
        
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<String, Object>());
        }
        
        current.put(parts[parts.length - 1], value);
    }

    private Map<String, Object> getInputData(String nodeId, ExecutionContext context) {
        // Try to get from previous node output, otherwise use context
        Object nodeOutput = context.getNodeOutput(nodeId);
        if (nodeOutput instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) nodeOutput;
            return map;
        }
        
        // Fallback to context data
        return context.getDataForNode(nodeId);
    }

    @Override
    public com.notificationplatform.entity.enums.NodeType getNodeType() {
        return com.notificationplatform.entity.enums.NodeType.DATA;
    }
}

