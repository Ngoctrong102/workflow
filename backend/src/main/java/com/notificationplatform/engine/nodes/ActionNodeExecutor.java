package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.service.registry.ActionRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Main Action Node Executor that routes to specific action executors based on ActionType.
 * 
 * This executor acts as a router/dispatcher for all action types:
 * - API_CALL -> ApiCallNodeExecutor
 * - PUBLISH_EVENT -> PublishEventNodeExecutor
 * - FUNCTION -> FunctionNodeExecutor
 * - CUSTOM_ACTION -> CustomActionNodeExecutor
 * 
 * See: @import(features/action-registry.md)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActionNodeExecutor implements NodeExecutor {

    private final ActionRegistryService actionRegistryService;
    private final ApiCallNodeExecutor apiCallNodeExecutor;
    private final PublishEventNodeExecutor publishEventNodeExecutor;
    private final FunctionNodeExecutor functionNodeExecutor;
    private final CustomActionNodeExecutor customActionNodeExecutor;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing action node: nodeId={}", nodeId);
        
        try {
            // Get registry ID and load action from registry
            String registryId = (String) nodeData.get("registryId");
            if (registryId == null) {
                throw new IllegalArgumentException("Registry ID is required for action node");
            }
            
            Action action;
            try {
                action = actionRegistryService.getActionById(registryId);
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Route to appropriate executor based on action type
            ActionType actionType = action.getType();
            log.debug("Routing action node to executor: nodeId={}, actionType={}, registryId={}", 
                     nodeId, actionType, registryId);
            
            return switch (actionType) {
                case API_CALL -> apiCallNodeExecutor.execute(nodeId, nodeData, context);
                case PUBLISH_EVENT -> publishEventNodeExecutor.execute(nodeId, nodeData, context);
                case FUNCTION -> functionNodeExecutor.execute(nodeId, nodeData, context);
                case CUSTOM_ACTION -> customActionNodeExecutor.execute(nodeId, nodeData, context);
            };
            
        } catch (Exception e) {
            log.error("Error executing action node: nodeId={}", nodeId, e);
            NodeExecutionResult result = new NodeExecutionResult(false, null);
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION;
    }
}

