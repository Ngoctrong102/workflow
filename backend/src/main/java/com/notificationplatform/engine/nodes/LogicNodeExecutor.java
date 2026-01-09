package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * Executor for logic nodes (condition, switch, etc.)
 * For MVP, basic condition evaluation
 */
@Slf4j
@Component
public class LogicNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing logic node: nodeId={}, subtype={}", nodeId, nodeData.get("subtype"));
        
        String subtype = (String) nodeData.getOrDefault("subtype", "condition");
        
        Map<String, Object> output = new HashMap<>();
        String nextNodeId = null;

        if ("condition".equals(subtype)) {
            // Condition evaluation with AND/OR logic
            boolean conditionResult = evaluateCondition(nodeData, context);
            output.put("conditionResult", conditionResult);
            
            // Determine next node based on condition
            Map<String, String> branches = (Map<String, String>) nodeData.get("branches");
            if (branches != null) {
                nextNodeId = conditionResult ? branches.get("true") : branches.get("false");
            }
        } else if ("switch".equals(subtype)) {
            // Switch node - multi-case branching
            nextNodeId = evaluateSwitch(nodeData, context);
            output.put("selectedCase", nextNodeId);
        } else if ("delay".equals(subtype)) {
            // Delay node - wait for specified time
            executeDelay(nodeData);
            output.put("delayCompleted", true);
        }

        NodeExecutionResult result = new NodeExecutionResult(true, output);
        result.setNextNodeId(nextNodeId);
        return result;
    }

    private boolean evaluateCondition(Map<String, Object> nodeData, ExecutionContext context) {
        // Condition evaluation with support for multiple conditions and AND/OR logic
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) nodeData.get("conditions");
        
        if (conditions == null || conditions.isEmpty()) {
            // Single condition (backward compatibility)
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

    private String evaluateSwitch(Map<String, Object> nodeData, ExecutionContext context) {
        // Switch node - evaluate cases and return matching case node ID
        String field = (String) nodeData.get("field");
        Object fieldValue = getFieldValue(field, context);

        List<Map<String, Object>> cases = (List<Map<String, Object>>) nodeData.get("cases");

        if (cases != null) {
            for (Map<String, Object> caseData : cases) {
                Object caseValue = caseData.get("value");
                if (Objects.equals(fieldValue, caseValue)) {
                    return (String) caseData.get("nodeId");
                }
            }
        }

        // Default case
        Map<String, String> branches = (Map<String, String>) nodeData.get("branches");
        return branches != null ? branches.get("default") : null;
    }

    private void executeDelay(Map<String, Object> nodeData) {
        // Delay node - wait for specified time
        Integer delayMs = (Integer) nodeData.get("delayMs");
        if (delayMs == null || delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
            log.info("Delay completed: {}ms", delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delay interrupted", e);
        }
    }

    private Object getFieldValue(String field, ExecutionContext context) {
        // Support nested field access (e.g., "user.name" or "_nodeOutputs.triggerNodeId.field")
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
        
        // Fallback: try to find in any trigger node output (for backward compatibility)
        // This is not ideal but helps with migration
        Map<String, Object> nodeOutputs = context.getNodeOutputs();
        Object current = null;
        for (Object nodeOutput : nodeOutputs.values()) {
            if (nodeOutput instanceof Map) {
                Map<String, Object> output = (Map<String, Object>) nodeOutput;
                current = output;
                break; // Use first trigger node output found
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
    public com.notificationplatform.entity.enums.NodeType getNodeType() {
        return com.notificationplatform.entity.enums.NodeType.LOGIC;
    }
}

