package com.notificationplatform.engine;

import com.notificationplatform.entity.enums.NodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for node executors using Strategy Pattern.
 * Automatically collects all NodeExecutor implementations via Spring's List injection
 * and creates a lookup map for O(1) access by node type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NodeExecutorRegistry {

    private final List<NodeExecutor> executors;
    private final Map<NodeType, NodeExecutor> executorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NodeExecutor executor : executors) {
            NodeType nodeType = executor.getNodeType();
            if (executorMap.containsKey(nodeType)) {
                log.warn("Multiple executors found for node type: {}. Using: {}", 
                        nodeType, executor.getClass().getSimpleName());
            }
            executorMap.put(nodeType, executor);
            log.debug("Registered node executor: {} for node type: {}", 
                    executor.getClass().getSimpleName(), nodeType);
        }
        
        // All trigger subtypes (api-call, scheduler, event) use TRIGGER node type
        // Subtype is stored in node.data.config.subtype or node.data.config.registryId
        // No need to register separate executors for trigger subtypes
        
        log.info("Initialized NodeExecutorRegistry with {} executors", executorMap.size());
    }

    /**
     * Get executor for a specific node type.
     * @param nodeType Node type enum
     * @return NodeExecutor for the node type, or null if not found
     */
    public NodeExecutor getExecutor(NodeType nodeType) {
        NodeExecutor executor = executorMap.get(nodeType);
        if (executor == null) {
            log.warn("No executor found for node type: {}", nodeType);
        }
        return executor;
    }

    /**
     * Get executor for a specific node type string (e.g., "trigger", "action").
     * @param nodeTypeStr Node type string value
     * @return NodeExecutor for the node type, or null if not found
     */
    public NodeExecutor getExecutor(String nodeTypeStr) {
        NodeType nodeType = null;
        try {
            // Convert string to enum name format (uppercase, replace "-" with "_")
            String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
            nodeType = NodeType.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid node type string: {}", nodeTypeStr);
            return null;
        }
        return getExecutor(nodeType);
    }
}

