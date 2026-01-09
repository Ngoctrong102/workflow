package com.notificationplatform.service.workflow;

import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.registry.TriggerRegistryService;
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
    private final TriggerRegistryService triggerRegistryService;

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

            if (!node.containsKey("type")) {
                throw new IllegalArgumentException("Each node must have a 'type' field");
            }

            // Validate node type is a valid enum value
            Object typeObj = node.get("type");
            if (!(typeObj instanceof String)) {
                throw new IllegalArgumentException("Node 'type' must be a string");
            }

            String nodeTypeStr = (String) typeObj;
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
                    String type = (String) node.get("type");
                    return type != null && (type.equalsIgnoreCase("trigger") ||
                            type.equalsIgnoreCase("api-trigger") ||
                            type.equalsIgnoreCase("schedule-trigger") ||
                            type.equalsIgnoreCase("event-trigger") ||
                            type.equalsIgnoreCase("file-trigger"));
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
            String type = (String) node.get("type");
            if (type == null) {
                throw new IllegalArgumentException("Node must have a 'type' field");
            }

            // Convert to enum format and validate
            String enumName = type.toUpperCase().replace("-", "_");
            try {
                NodeType.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid node type: " + type +
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
     */
    private void validateTriggerNodeReferences(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            String type = (String) node.get("type");
            if (type == null) {
                continue;
            }

            boolean isTrigger = type.equalsIgnoreCase("trigger") ||
                    type.equalsIgnoreCase("api-trigger") ||
                    type.equalsIgnoreCase("schedule-trigger") ||
                    type.equalsIgnoreCase("event-trigger") ||
                    type.equalsIgnoreCase("file-trigger");

            if (isTrigger) {
                String registryId = (String) node.get("registryId");
                if (registryId == null || registryId.isEmpty()) {
                    throw new IllegalArgumentException("Trigger node must have a 'registryId' field");
                }

                // Verify trigger exists in registry
                var triggerDef = triggerRegistryService.getTriggerById(registryId);
                if (triggerDef.isEmpty()) {
                    throw new IllegalArgumentException("Trigger registry ID not found: " + registryId);
                }
            }
        }
    }

    /**
     * Verify action nodes reference valid action registry entries.
     */
    private void validateActionNodeReferences(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            String type = (String) node.get("type");
            if (type == null) {
                continue;
            }

            boolean isAction = type.equalsIgnoreCase("action");

            if (isAction) {
                String registryId = (String) node.get("registryId");
                if (registryId == null || registryId.isEmpty()) {
                    throw new IllegalArgumentException("Action node must have a 'registryId' field");
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

