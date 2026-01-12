package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for API Call Trigger nodes.
 * 
 * API triggers receive HTTP request data and pass it to workflow context.
 * This executor validates and processes API trigger data.
 * 
 * This executor is called by TriggerNodeExecutor for API_CALL trigger type.
 */
@Slf4j
@Component
public class ApiTriggerExecutor implements TriggerExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.debug("Executing API trigger node: nodeId={}", nodeId);
        
        // Get trigger data for this specific trigger node
        Map<String, Object> triggerData = context.getTriggerDataForNode(nodeId);
        
        // Validate trigger data (API triggers should have request data)
        if (triggerData == null || triggerData.isEmpty()) {
            log.warn("API trigger node has no trigger data: nodeId={}", nodeId);
            triggerData = new HashMap<>();
        }
        
        // Process trigger data - API triggers may need to extract/validate request data
        // For now, just pass through the trigger data
        Map<String, Object> output = new HashMap<>(triggerData);
        
        // Store trigger data as node output so other nodes can access it via _nodeOutputs.{nodeId}
        context.setNodeOutput(nodeId, output);
        
        return new NodeExecutionResult(true, output);
    }
}

