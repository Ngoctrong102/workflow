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
 * Executor for Merge node.
 * Combines multiple branches into single flow.
 * 
 * See: @import(features/node-types.md#merge)
 */
@Slf4j
@Component
public class MergeNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing merge node: nodeId={}", nodeId);
        
        String mergeStrategy = (String) nodeData.getOrDefault("mergeStrategy", "all");
        
        // Get data from all input branches (stored in nodeOutputs)
        Map<String, Object> nodeOutputs = context.getNodeOutputs();
        
        Map<String, Object> output = mergeData(nodeOutputs, mergeStrategy);
        
        return new NodeExecutionResult(true, output);
    }

    /**
     * Merge data from multiple branches based on strategy.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeData(Map<String, Object> nodeOutputs, String mergeStrategy) {
        Map<String, Object> merged = new HashMap<>();
        
        switch (mergeStrategy.toLowerCase()) {
            case "all":
                // Include all data from all branches
                for (Map.Entry<String, Object> entry : nodeOutputs.entrySet()) {
                    merged.put(entry.getKey(), entry.getValue());
                }
                break;
                
            case "first":
                // Use data from first branch
                if (!nodeOutputs.isEmpty()) {
                    Map.Entry<String, Object> firstEntry = nodeOutputs.entrySet().iterator().next();
                    if (firstEntry.getValue() instanceof Map) {
                        merged.putAll((Map<String, Object>) firstEntry.getValue());
                    } else {
                        merged.put(firstEntry.getKey(), firstEntry.getValue());
                    }
                }
                break;
                
            case "last":
                // Use data from last branch
                if (!nodeOutputs.isEmpty()) {
                    List<Map.Entry<String, Object>> entries = new ArrayList<>(nodeOutputs.entrySet());
                    Map.Entry<String, Object> lastEntry = entries.get(entries.size() - 1);
                    if (lastEntry.getValue() instanceof Map) {
                        merged.putAll((Map<String, Object>) lastEntry.getValue());
                    } else {
                        merged.put(lastEntry.getKey(), lastEntry.getValue());
                    }
                }
                break;
                
            case "custom":
                // Custom merge logic - for now, same as "all"
                for (Map.Entry<String, Object> entry : nodeOutputs.entrySet()) {
                    merged.put(entry.getKey(), entry.getValue());
                }
                break;
                
            default:
                // Default to "all"
                for (Map.Entry<String, Object> entry : nodeOutputs.entrySet()) {
                    merged.put(entry.getKey(), entry.getValue());
                }
        }
        
        return merged;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LOGIC;
    }
}

