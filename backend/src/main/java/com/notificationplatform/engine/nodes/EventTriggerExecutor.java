package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for Event Trigger nodes (Kafka, RabbitMQ, etc.).
 * 
 * Event triggers receive messages from message queues (Kafka, RabbitMQ).
 * They process event messages and pass them to workflow context.
 * 
 * This executor is called by TriggerNodeExecutor for EVENT trigger type.
 */
@Slf4j
@Component
public class EventTriggerExecutor implements TriggerExecutor {

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.debug("Executing event trigger node: nodeId={}", nodeId);
        
        // Get trigger data for this specific trigger node
        Map<String, Object> triggerData = context.getTriggerDataForNode(nodeId);
        
        // Validate trigger data (event triggers should have message data)
        if (triggerData == null || triggerData.isEmpty()) {
            log.warn("Event trigger node has no trigger data: nodeId={}", nodeId);
            triggerData = new HashMap<>();
        }
        
        // Process trigger data - event triggers may need to extract/parse message data
        // For now, just pass through the trigger data
        Map<String, Object> output = new HashMap<>(triggerData);
        
        // Add event metadata
        output.put("_triggerType", "event");
        output.put("_triggeredAt", System.currentTimeMillis());
        
        // Store trigger data as node output so other nodes can access it via _nodeOutputs.{nodeId}
        context.setNodeOutput(nodeId, output);
        
        return new NodeExecutionResult(true, output);
    }
}

