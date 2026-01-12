package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for Scheduler Trigger nodes.
 * 
 * Scheduler triggers are executed on a schedule (cron-based).
 * They may have static data or dynamic data passed to workflow context.
 * 
 * This executor is called by TriggerNodeExecutor for SCHEDULER trigger type.
 */
@Slf4j
@Component
public class SchedulerTriggerExecutor implements TriggerExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.debug("Executing scheduler trigger node: nodeId={}", nodeId);
        
        // Get trigger data for this specific trigger node
        Map<String, Object> triggerData = context.getTriggerDataForNode(nodeId);
        
        // If no trigger data, try to get from node config (scheduler may have static data)
        if (triggerData == null || triggerData.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = nodeData.containsKey("config") ? 
                (Map<String, Object>) nodeData.get("config") : new HashMap<>();
            
            // Scheduler triggers may have static data in config
            Object dataObj = config.get("data");
            if (dataObj instanceof Map) {
                triggerData = new HashMap<>((Map<String, Object>) dataObj);
            } else {
                triggerData = new HashMap<>();
            }
        }
        
        // Process trigger data - scheduler triggers may need to add timestamp, etc.
        Map<String, Object> output = new HashMap<>(triggerData);
        
        // Add scheduler metadata
        output.put("_triggerType", "scheduler");
        output.put("_triggeredAt", System.currentTimeMillis());
        
        // Store trigger data as node output so other nodes can access it via _nodeOutputs.{nodeId}
        context.setNodeOutput(nodeId, output);
        
        return new NodeExecutionResult(true, output);
    }
}

