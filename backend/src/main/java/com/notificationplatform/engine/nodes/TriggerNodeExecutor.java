package com.notificationplatform.engine.nodes;

import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.service.trigger.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Trigger Node Executor that routes to specific trigger executors based on TriggerType.
 * 
 * This executor acts as a router/dispatcher for all trigger types:
 * - API_CALL -> ApiTriggerExecutor
 * - SCHEDULER -> SchedulerTriggerExecutor
 * - EVENT -> EventTriggerExecutor
 * 
 * See: @import(features/trigger-registry.md)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerNodeExecutor implements NodeExecutor {

    private final TriggerService triggerService;
    private final ApiTriggerExecutor apiTriggerExecutor;
    private final SchedulerTriggerExecutor schedulerTriggerExecutor;
    private final EventTriggerExecutor eventTriggerExecutor;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing trigger node: nodeId={}", nodeId);
        
        try {
            // Extract triggerConfigId from node data (new structure)
            // Support multiple locations: top level, nested config
            // New structure: nodeConfig.triggerConfigId or nodeConfig.config.triggerConfigId
            // Old structure: data.registryId (backward compatibility)
            String triggerConfigId = (String) nodeData.get("triggerConfigId");
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                // Check nested config structure (data.config.triggerConfigId)
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) nodeData.get("config");
                if (config != null) {
                    triggerConfigId = (String) config.get("triggerConfigId");
                    // Also check nested config.config.triggerConfigId
                    if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> nestedConfig = (Map<String, Object>) config.get("config");
                        if (nestedConfig != null) {
                            triggerConfigId = (String) nestedConfig.get("triggerConfigId");
                        }
                    }
                }
            }
            String registryId = (String) nodeData.get("registryId"); // Old structure
            
            // Use triggerConfigId if available, otherwise fallback to registryId (backward compatibility)
            String configId = triggerConfigId != null ? triggerConfigId : registryId;
            
            if (configId == null) {
                throw new IllegalArgumentException("triggerConfigId or registryId is required for trigger node");
            }
            
            // Load trigger config from database
            TriggerResponse triggerConfig;
            try {
                triggerConfig = triggerService.getTriggerConfigById(configId);
            } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                throw new IllegalArgumentException("Trigger config not found: " + configId);
            }
            
            // Get trigger type
            TriggerType triggerType = TriggerType.fromValue(triggerConfig.getTriggerType());
            if (triggerType == null) {
                throw new IllegalArgumentException("Invalid trigger type: " + triggerConfig.getTriggerType());
            }
            
            // Merge trigger config with instance-specific overrides
            Map<String, Object> mergedConfig = new HashMap<>();
            if (triggerConfig.getConfig() != null) {
                mergedConfig.putAll(triggerConfig.getConfig());
            }
            
            // Get instanceConfig from nodeData (new structure)
            @SuppressWarnings("unchecked")
            Map<String, Object> instanceConfig = (Map<String, Object>) nodeData.get("instanceConfig");
            if (instanceConfig != null && !instanceConfig.isEmpty()) {
                // Instance config overrides base config
                mergedConfig.putAll(instanceConfig);
            }
            
            // Create updated nodeData with merged config
            Map<String, Object> updatedNodeData = new HashMap<>(nodeData);
            updatedNodeData.put("config", mergedConfig);
            updatedNodeData.put("triggerType", triggerType.getValue());
            
            log.debug("Routing trigger node to executor: nodeId={}, triggerType={}, triggerConfigId={}", 
                     nodeId, triggerType, configId);
            
            // Route to appropriate executor based on trigger type
            return switch (triggerType) {
                case API_CALL -> apiTriggerExecutor.execute(nodeId, updatedNodeData, context);
                case SCHEDULER -> schedulerTriggerExecutor.execute(nodeId, updatedNodeData, context);
                case EVENT -> eventTriggerExecutor.execute(nodeId, updatedNodeData, context);
            };
            
        } catch (Exception e) {
            log.error("Error executing trigger node: nodeId={}", nodeId, e);
            NodeExecutionResult result = new NodeExecutionResult(false, null);
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TRIGGER;
    }
}

