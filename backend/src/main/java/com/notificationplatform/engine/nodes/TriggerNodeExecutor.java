package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for trigger nodes
 * Trigger nodes are entry points and pass trigger data to context
 */
@Component
public class TriggerNodeExecutor implements NodeExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        // Get trigger data for this specific trigger node
        // This ensures each trigger node has its own data, supporting multiple triggers
        Map<String, Object> triggerData = context.getTriggerDataForNode(nodeId);
        
        // Store trigger data as node output so other nodes can access it via _nodeOutputs.{nodeId}
        Map<String, Object> output = new HashMap<>(triggerData);
        
        // Also store in context nodeOutputs for consistency
        context.setNodeOutput(nodeId, output);
        
        NodeExecutionResult result = new NodeExecutionResult(true, output);
        return result;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TRIGGER;
    }
}

