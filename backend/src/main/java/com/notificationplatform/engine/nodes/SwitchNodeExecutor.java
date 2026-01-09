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
 * Executor for Switch node.
 * Evaluates switch field and matches against cases, returns appropriate branch or default.
 * 
 * See: @import(features/node-types.md#switch)
 */
@Slf4j
@Component
public class SwitchNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing switch node: nodeId={}", nodeId);
        
        Map<String, Object> output = new HashMap<>();
        String nextNodeId = evaluateSwitch(nodeData, context);
        output.put("selectedCase", nextNodeId);

        NodeExecutionResult result = new NodeExecutionResult(true, output);
        result.setNextNodeId(nextNodeId);
        return result;
    }

    /**
     * Evaluate switch cases and return matching case node ID.
     */
    @SuppressWarnings("unchecked")
    private String evaluateSwitch(Map<String, Object> nodeData, ExecutionContext context) {
        String field = (String) nodeData.get("field");
        if (field == null) {
            field = (String) nodeData.get("switchField"); // Support both field names
        }
        
        Object fieldValue = getFieldValue(field, context);

        List<Map<String, Object>> cases = (List<Map<String, Object>>) nodeData.get("cases");

        if (cases != null) {
            for (Map<String, Object> caseData : cases) {
                Object caseValue = caseData.get("value");
                if (Objects.equals(fieldValue, caseValue)) {
                    String branchNodeId = (String) caseData.get("nodeId");
                    if (branchNodeId == null) {
                        branchNodeId = (String) caseData.get("branch"); // Support both field names
                    }
                    return branchNodeId;
                }
            }
        }

        // Default case
        Map<String, String> branches = (Map<String, String>) nodeData.get("branches");
        if (branches != null) {
            return branches.get("default");
        }
        
        return null;
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

