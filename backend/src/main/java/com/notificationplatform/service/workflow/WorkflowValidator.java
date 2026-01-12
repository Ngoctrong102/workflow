package com.notificationplatform.service.workflow;

import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.trigger.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowValidator {

    private static final int MAX_NAME_LENGTH = 255;
    private static final List<String> VALID_STATUSES = List.of("draft", "active", "inactive", "paused", "archived");

    private final ActionRegistryService actionRegistryService;
    private final TriggerService triggerService;

    public void validateCreateRequest(com.notificationplatform.dto.request.CreateWorkflowRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name is required");
        }

        if (request.getName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Workflow name exceeds maximum length of " + MAX_NAME_LENGTH);
        }

        if (request.getDefinition() == null) {
            throw new IllegalArgumentException("Workflow definition is required");
        }

        if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus().toLowerCase())) {
            throw new IllegalArgumentException("Invalid workflow status: " + request.getStatus());
        }
    }

    public void validateUpdateRequest(Workflow existing, com.notificationplatform.dto.request.UpdateWorkflowRequest request) {
        if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus().toLowerCase())) {
            throw new IllegalArgumentException("Invalid workflow status: " + request.getStatus());
        }

        if (request.getName() != null && request.getName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Workflow name exceeds maximum length of " + MAX_NAME_LENGTH);
        }
    }

    public void validateWorkflow(Workflow workflow) {
        // Validate workflow structure
        if (workflow.getDefinition() == null) {
            throw new IllegalArgumentException("Workflow definition is required");
        }

        // Validate definition structure
        validateDefinitionStructure(workflow.getDefinition());

        // Validate status
        if (workflow.getStatus() == null || !VALID_STATUSES.contains(workflow.getStatus().getValue().toLowerCase())) {
            throw new IllegalArgumentException("Invalid workflow status: " + workflow.getStatus());
        }
    }

    /**
     * Validate workflow structure according to Sprint 05 requirements:
     * - Exactly one trigger node
     * - All nodes have valid types
     * - All edges connect valid nodes
     * - No circular dependencies
     * - Trigger node references valid trigger config
     * - Action nodes reference valid action registry entries
     */
    private void validateWorkflowStructure(Map<String, Object> definition) {
        List<Map<String, Object>> nodes = getNodes(definition);
        List<Map<String, Object>> edges = getEdges(definition);

        // 1. Verify workflow has exactly one trigger node
        validateExactlyOneTriggerNode(nodes);

        // 2. Verify all nodes have valid types
        validateNodeTypes(nodes);

        // 3. Verify all edges connect valid nodes
        validateEdgesConnectValidNodes(nodes, edges);

        // 4. Verify no circular dependencies
        validateNoCircularDependencies(nodes, edges);

        // 5. Verify trigger node references valid trigger config
        validateTriggerNodeReferences(nodes);

        // 6. Verify action nodes reference valid action registry entries
        validateActionNodeReferences(nodes);
    }

    @SuppressWarnings("unchecked")
    private void validateDefinitionStructure(Object definition) {
        if (!(definition instanceof Map)) {
            throw new IllegalArgumentException("Workflow definition must be a JSON object");
        }

        Map<String, Object> def = (Map<String, Object>) definition;

        // Validate nodes exist
        if (!def.containsKey("nodes")) {
            throw new IllegalArgumentException("Workflow definition must contain 'nodes' array");
        }

        Object nodesObj = def.get("nodes");
        if (!(nodesObj instanceof List)) {
            throw new IllegalArgumentException("Workflow 'nodes' must be an array");
        }

        List<Object> nodes = (List<Object>) nodesObj;

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Workflow must contain at least one node");
        }

        // Validate each node has required fields
        for (Object nodeObj : nodes) {
            if (!(nodeObj instanceof Map)) {
                throw new IllegalArgumentException("Each node must be a JSON object");
            }

            Map<String, Object> node = (Map<String, Object>) nodeObj;

            if (!node.containsKey("id")) {
                throw new IllegalArgumentException("Each node must have an 'id' field");
            }

            // Support both new structure (nodeType) and old structure (type) for backward compatibility
            String nodeTypeStr = (String) node.get("nodeType"); // New structure
            if (nodeTypeStr == null) {
                nodeTypeStr = (String) node.get("type"); // Old structure (backward compatibility)
            }
            
            if (nodeTypeStr == null) {
                throw new IllegalArgumentException("Each node must have a 'nodeType' or 'type' field");
            }

            // Validate node type is a valid enum value
            try {
                // Convert string to enum name format (uppercase, replace "-" with "_")
                String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
                NodeType.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid node type: " + nodeTypeStr + 
                    ". Valid types are: " + String.join(", ", 
                        java.util.Arrays.stream(NodeType.values())
                            .map(Enum::name)
                            .toArray(String[]::new)));
            }
        }

        // Validate edges if present
        if (def.containsKey("edges")) {
            Object edgesObj = def.get("edges");
            if (!(edgesObj instanceof List)) {
                throw new IllegalArgumentException("Workflow 'edges' must be an array");
            }

            List<Object> edges = (List<Object>) edgesObj;

            for (Object edgeObj : edges) {
                if (!(edgeObj instanceof Map)) {
                    throw new IllegalArgumentException("Each edge must be a JSON object");
                }

                Map<String, Object> edge = (Map<String, Object>) edgeObj;

                if (!edge.containsKey("source") || !edge.containsKey("target")) {
                    throw new IllegalArgumentException("Each edge must have 'source' and 'target' fields");
                }
            }
        }

        // Validate workflow structure (Sprint 05 requirements)
        validateWorkflowStructure(def);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getNodes(Map<String, Object> definition) {
        Object nodesObj = definition.get("nodes");
        if (!(nodesObj instanceof List)) {
            throw new IllegalArgumentException("Workflow 'nodes' must be an array");
        }
        List<Object> nodesList = (List<Object>) nodesObj;
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Object nodeObj : nodesList) {
            if (!(nodeObj instanceof Map)) {
                throw new IllegalArgumentException("Each node must be a JSON object");
            }
            nodes.add((Map<String, Object>) nodeObj);
        }
        return nodes;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getEdges(Map<String, Object> definition) {
        if (!definition.containsKey("edges")) {
            return new ArrayList<>();
        }
        Object edgesObj = definition.get("edges");
        if (!(edgesObj instanceof List)) {
            throw new IllegalArgumentException("Workflow 'edges' must be an array");
        }
        List<Object> edgesList = (List<Object>) edgesObj;
        List<Map<String, Object>> edges = new ArrayList<>();
        for (Object edgeObj : edgesList) {
            if (!(edgeObj instanceof Map)) {
                throw new IllegalArgumentException("Each edge must be a JSON object");
            }
            edges.add((Map<String, Object>) edgeObj);
        }
        return edges;
    }

    /**
     * Verify workflow has exactly one trigger node.
     */
    private void validateExactlyOneTriggerNode(List<Map<String, Object>> nodes) {
        long triggerCount = nodes.stream()
                .filter(node -> {
                    // Support both new structure (nodeType) and old structure (type)
                    String nodeTypeStr = (String) node.get("nodeType");
                    if (nodeTypeStr == null) {
                        nodeTypeStr = (String) node.get("type");
                    }
                    if (nodeTypeStr == null) {
                        return false;
                    }
                    // All trigger subtypes use TRIGGER node type
                    String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
                    try {
                        return NodeType.valueOf(enumName) == NodeType.TRIGGER;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .count();

        if (triggerCount == 0) {
            throw new IllegalArgumentException("Workflow must contain exactly one trigger node");
        }
        if (triggerCount > 1) {
            throw new IllegalArgumentException("Workflow must contain exactly one trigger node, found: " + triggerCount);
        }
    }

    /**
     * Verify all nodes have valid types.
     */
    private void validateNodeTypes(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            // Support both new structure (nodeType) and old structure (type)
            String nodeTypeStr = (String) node.get("nodeType");
            if (nodeTypeStr == null) {
                nodeTypeStr = (String) node.get("type");
            }
            if (nodeTypeStr == null) {
                throw new IllegalArgumentException("Node must have a 'nodeType' or 'type' field");
            }

            // Convert to enum format and validate
            String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
            try {
                NodeType.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid node type: " + nodeTypeStr +
                        ". Valid types are: " + Arrays.toString(NodeType.values()));
            }
        }
    }

    /**
     * Verify all edges connect valid nodes.
     */
    private void validateEdgesConnectValidNodes(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
        Set<String> nodeIds = new HashSet<>();
        for (Map<String, Object> node : nodes) {
            String nodeId = (String) node.get("id");
            if (nodeId == null) {
                throw new IllegalArgumentException("Node must have an 'id' field");
            }
            nodeIds.add(nodeId);
        }

        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");

            if (source == null || target == null) {
                throw new IllegalArgumentException("Edge must have 'source' and 'target' fields");
            }

            if (!nodeIds.contains(source)) {
                throw new IllegalArgumentException("Edge source node not found: " + source);
            }

            if (!nodeIds.contains(target)) {
                throw new IllegalArgumentException("Edge target node not found: " + target);
            }
        }
    }

    /**
     * Verify no circular dependencies using DFS.
     */
    private void validateNoCircularDependencies(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
        Map<String, List<String>> graph = new HashMap<>();
        Set<String> nodeIds = new HashSet<>();

        // Build graph
        for (Map<String, Object> node : nodes) {
            String nodeId = (String) node.get("id");
            nodeIds.add(nodeId);
            graph.put(nodeId, new ArrayList<>());
        }

        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            graph.get(source).add(target);
        }

        // Check for cycles using DFS
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String nodeId : nodeIds) {
            if (!visited.contains(nodeId)) {
                if (hasCycle(nodeId, graph, visited, recursionStack)) {
                    throw new IllegalArgumentException("Workflow contains circular dependencies");
                }
            }
        }
    }

    private boolean hasCycle(String nodeId, Map<String, List<String>> graph,
                            Set<String> visited, Set<String> recursionStack) {
        visited.add(nodeId);
        recursionStack.add(nodeId);

        List<String> neighbors = graph.get(nodeId);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    if (hasCycle(neighbor, graph, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(neighbor)) {
                    return true;
                }
            }
        }

        recursionStack.remove(nodeId);
        return false;
    }

    /**
     * Verify trigger node references valid trigger config.
     * According to documentation, trigger nodes should have:
     * - nodeType: "trigger" (or type: "trigger" for backward compatibility)
     * - nodeConfig.triggerConfigId: Reference to trigger config in database (or data.triggerConfigId for old structure)
     */
    @SuppressWarnings("unchecked")
    private void validateTriggerNodeReferences(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            // Support both new structure (nodeType) and old structure (type)
            String nodeTypeStr = (String) node.get("nodeType");
            if (nodeTypeStr == null) {
                nodeTypeStr = (String) node.get("type");
            }
            if (nodeTypeStr == null) {
                continue;
            }

            // Check if this is a trigger node
            String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
            boolean isTrigger = false;
            try {
                isTrigger = NodeType.valueOf(enumName) == NodeType.TRIGGER;
            } catch (IllegalArgumentException e) {
                // Invalid node type, skip
                continue;
            }

            if (isTrigger) {
                // Support both new structure (nodeConfig) and old structure (data)
                Map<String, Object> nodeConfig = null;
                if (node.containsKey("nodeConfig")) {
                    nodeConfig = (Map<String, Object>) node.get("nodeConfig");
                } else if (node.containsKey("data")) {
                    nodeConfig = (Map<String, Object>) node.get("data");
                }

                if (nodeConfig == null) {
                    throw new IllegalArgumentException("Trigger node must have 'nodeConfig' or 'data' field");
                }

                // Check triggerConfigId (new structure) or registryId (old structure for backward compatibility)
                // Support multiple locations: top level, nested config, and root level
                String triggerConfigId = (String) nodeConfig.get("triggerConfigId");
                if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                    // Check nested config structure (data.config.config.triggerConfigId)
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedConfig = (Map<String, Object>) nodeConfig.get("config");
                    if (nestedConfig != null) {
                        triggerConfigId = (String) nestedConfig.get("triggerConfigId");
                    }
                }
                if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                    // Backward compatibility: check registryId at top level
                    triggerConfigId = (String) nodeConfig.get("registryId");
                }
                if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                    // Check nested config for registryId
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedConfig = (Map<String, Object>) nodeConfig.get("config");
                    if (nestedConfig != null) {
                        triggerConfigId = (String) nestedConfig.get("registryId");
                    }
                }
                if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                    // Also check at root level for old structure
                    triggerConfigId = (String) node.get("registryId");
                    if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                        throw new IllegalArgumentException("Trigger node must have 'triggerConfigId' in nodeConfig (or 'registryId' for backward compatibility)");
                    }
                }

                // Verify trigger config exists in database (not hardcoded registry)
                try {
                    triggerService.getTriggerConfigById(triggerConfigId);
                } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                    throw new IllegalArgumentException("Trigger config not found: " + triggerConfigId);
                }
            }
        }
    }

    /**
     * Verify action nodes reference valid action registry entries.
     * According to documentation, action nodes should have:
     * - nodeType: "action" (or type: "action" for backward compatibility)
     * - nodeConfig.registryId: Reference to action definition in registry
     */
    @SuppressWarnings("unchecked")
    private void validateActionNodeReferences(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            // Support both new structure (nodeType) and old structure (type)
            String nodeTypeStr = (String) node.get("nodeType");
            if (nodeTypeStr == null) {
                nodeTypeStr = (String) node.get("type");
            }
            if (nodeTypeStr == null) {
                continue;
            }

            // Check if this is an action node
            String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
            boolean isAction = false;
            try {
                isAction = NodeType.valueOf(enumName) == NodeType.ACTION;
            } catch (IllegalArgumentException e) {
                // Invalid node type, skip
                continue;
            }

            if (isAction) {
                // Support both new structure (nodeConfig) and old structure (data)
                Map<String, Object> nodeConfig = null;
                if (node.containsKey("nodeConfig")) {
                    nodeConfig = (Map<String, Object>) node.get("nodeConfig");
                } else if (node.containsKey("data")) {
                    nodeConfig = (Map<String, Object>) node.get("data");
                }

                if (nodeConfig == null) {
                    throw new IllegalArgumentException("Action node must have 'nodeConfig' or 'data' field");
                }

                // Check registryId in nodeConfig (support multiple locations: top level, nested config)
                String registryId = (String) nodeConfig.get("registryId");
                if (registryId == null || registryId.isEmpty()) {
                    // Check nested config structure (data.config.registryId)
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) nodeConfig.get("config");
                    if (config != null) {
                        registryId = (String) config.get("registryId");
                        // Also check nested config.config.registryId
                        if (registryId == null || registryId.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> nestedConfig = (Map<String, Object>) config.get("config");
                            if (nestedConfig != null) {
                                registryId = (String) nestedConfig.get("registryId");
                            }
                        }
                    }
                }
                if (registryId == null || registryId.isEmpty()) {
                    // Backward compatibility: check at root level
                    registryId = (String) node.get("registryId");
                    if (registryId == null || registryId.isEmpty()) {
                        throw new IllegalArgumentException("Action node must have 'registryId' in nodeConfig");
                    }
                }

                // Verify action exists in registry
                try {
                    actionRegistryService.getActionById(registryId);
                } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                    throw new IllegalArgumentException("Action registry ID not found: " + registryId);
                }
            }
        }
    }
}

