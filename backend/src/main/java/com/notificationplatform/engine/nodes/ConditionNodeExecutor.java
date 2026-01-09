package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Executor for Condition node.
 * Evaluates conditions with AND/OR logic and returns true/false branch.
 * 
 * See: @import(features/node-types.md#condition)
 */
@Slf4j
@Component
public class ConditionNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing condition node: nodeId={}", nodeId);
        
        Map<String, Object> output = new HashMap<>();
        String nextNodeId = null;

        // Evaluate condition
        boolean conditionResult = evaluateCondition(nodeData, context);
        output.put("conditionResult", conditionResult);
        
        // Determine next node based on condition
        @SuppressWarnings("unchecked")
        Map<String, String> branches = (Map<String, String>) nodeData.get("branches");
        if (branches != null) {
            nextNodeId = conditionResult ? branches.get("true") : branches.get("false");
        }

        NodeExecutionResult result = new NodeExecutionResult(true, output);
        result.setNextNodeId(nextNodeId);
        return result;
    }

    /**
     * Evaluate condition with support for multiple conditions and AND/OR logic.
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Map<String, Object> nodeData, ExecutionContext context) {
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) nodeData.get("conditions");
        
        if (conditions == null || conditions.isEmpty()) {
            // Single condition (backward compatibility)
            @SuppressWarnings("unchecked")
            Map<String, Object> condition = (Map<String, Object>) nodeData.get("condition");
            if (condition == null) {
                return true;
            }
            return evaluateSingleCondition(condition, context);
        }

        // Multiple conditions with AND/OR logic
        String logic = (String) nodeData.getOrDefault("logic", "AND");
        boolean result = "AND".equals(logic);

        for (Map<String, Object> condition : conditions) {
            boolean conditionResult = evaluateSingleCondition(condition, context);
            
            if ("AND".equals(logic)) {
                result = result && conditionResult;
                if (!result) break; // Short-circuit for AND
            } else { // OR
                result = result || conditionResult;
                if (result) break; // Short-circuit for OR
            }
        }

        return result;
    }

    /**
     * Evaluate single condition.
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateSingleCondition(Map<String, Object> condition, ExecutionContext context) {
        String field = (String) condition.get("field");
        String operator = (String) condition.getOrDefault("operator", "equals");
        Object value = condition.get("value");

        // Get field value from context
        Object fieldValue = getFieldValue(field, context);

        // Evaluate condition
        switch (operator) {
            case "equals":
                return Objects.equals(fieldValue, value);
            case "not_equals":
                return !Objects.equals(fieldValue, value);
            case "contains":
                return fieldValue != null && fieldValue.toString().contains(value != null ? value.toString() : "");
            case "greater_than":
                return compareNumbers(fieldValue, value) > 0;
            case "less_than":
                return compareNumbers(fieldValue, value) < 0;
            case "greater_than_or_equal":
                return compareNumbers(fieldValue, value) >= 0;
            case "less_than_or_equal":
                return compareNumbers(fieldValue, value) <= 0;
            default:
                return true;
        }
    }

    private int compareNumbers(Object fieldValue, Object value) {
        if (fieldValue == null || value == null) {
            return 0;
        }
        try {
            double fieldNum = Double.parseDouble(fieldValue.toString());
            double valueNum = Double.parseDouble(value.toString());
            return Double.compare(fieldNum, valueNum);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get field value from context.
     * Supports nested field access (e.g., "user.name" or "_nodeOutputs.triggerNodeId.field").
     */
    private Object getFieldValue(String field, ExecutionContext context) {
        if (field == null) {
            return null;
        }
        
        String[] parts = field.split("\\.");
        
        // Check if field starts with _nodeOutputs
        if (parts.length > 1 && "_nodeOutputs".equals(parts[0])) {
            // Access via _nodeOutputs.{nodeId}.{fieldPath}
            Map<String, Object> nodeOutputs = context.getNodeOutputs();
            if (parts.length >= 2) {
                Object nodeOutput = nodeOutputs.get(parts[1]);
                if (nodeOutput instanceof Map) {
                    Map<String, Object> output = (Map<String, Object>) nodeOutput;
                    // Navigate through remaining parts
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
        
        // Try to find in variables
        Object variableValue = context.getVariable(field);
        if (variableValue != null) {
            return variableValue;
        }
        
        // Try to find in node outputs (nested access)
        Map<String, Object> nodeOutputs = context.getNodeOutputs();
        Object current = null;
        for (Object nodeOutput : nodeOutputs.values()) {
            if (nodeOutput instanceof Map) {
                Map<String, Object> output = (Map<String, Object>) nodeOutput;
                current = output;
                break; // Use first node output found
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

