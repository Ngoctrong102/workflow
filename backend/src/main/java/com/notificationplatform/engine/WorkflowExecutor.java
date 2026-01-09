package com.notificationplatform.engine;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

/**
 * Core workflow execution engine
 */
@Slf4j
@Component
public class WorkflowExecutor {

    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final NodeExecutorRegistry nodeExecutorRegistry;

    public WorkflowExecutor(ExecutionRepository executionRepository,
                           NodeExecutionRepository nodeExecutionRepository,
                           NodeExecutorRegistry nodeExecutorRegistry) {
        this.executionRepository = executionRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.nodeExecutorRegistry = nodeExecutorRegistry;
    }

    /**
     * Execute a workflow
     *
     * @param workflow Workflow to execute
     * @param triggerData Data from trigger
     * @param triggerId Trigger ID that started execution
     * @return Execution entity
     */
    public Execution execute(Workflow workflow, Map<String, Object> triggerData, String triggerId) {
        log.info("Starting workflow execution: workflowId={}, triggerId={}", workflow.getId(), triggerId);

        // Create execution record
        Execution execution = new Execution();
        execution.setId(UUID.randomUUID().toString());
        execution.setWorkflow(workflow);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        execution.setNodesExecuted(0);
        execution.setNotificationsSent(0);
        execution.setContext(triggerData);
        execution = executionRepository.save(execution);

        // Create execution context
        ExecutionContext context = new ExecutionContext(execution.getId(), workflow.getId());

        try {
            // Get workflow definition
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            
            // Map trigger data to trigger nodeId
            // For now, we'll find the first trigger node and map data to it
            // In the future, trigger services should specify which trigger nodeId to use
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
            String triggerNodeId = findTriggerNodeId(nodes);
            if (triggerNodeId != null) {
                context.setTriggerDataForNode(triggerNodeId, triggerData != null ? triggerData : new HashMap<>());
            } else {
                log.warn("No trigger node found in workflow definition, trigger data will not be available");
            }
            
            // Get edges
            
            List<Map<String, Object>> edges = definition.containsKey("edges") ? 
                (List<Map<String, Object>>) definition.get("edges") : new ArrayList<>();

            // Build node graph
            Map<String, Map<String, Object>> nodeMap = buildNodeMap(nodes);
            Map<String, List<String>> adjacencyList = buildAdjacencyList(edges);

            // Find start nodes (trigger nodes or nodes with no incoming edges)
            List<String> startNodeIds = findStartNodes(nodes, edges);

            // Execute workflow
            int nodesExecuted = 0;
            Set<String> executedNodes = new HashSet<>();
            
            for (String startNodeId : startNodeIds) {
                nodesExecuted += executeNodeAndDependencies(startNodeId, nodeMap, adjacencyList, 
                                                           context, execution, executedNodes);
            }

            // Update execution status
            // Don't mark as completed if any node is waiting
            if (context.isWaiting()) {
                log.info("Workflow execution paused (waiting for events): executionId={}, waitingNodeId={}, nodesExecuted={}", 
                           execution.getId(), context.getWaitingNodeId(), nodesExecuted);
                // Status already set to "waiting" in executeNodeAndDependencies
            } else {
                execution.setStatus(ExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setNodesExecuted(nodesExecuted);
                
                long duration = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
                execution.setDuration((int) duration);
                
                execution = executionRepository.save(execution);

                log.info("Workflow execution completed: executionId={}, nodesExecuted={}", 
                           execution.getId(), nodesExecuted);
            }

        } catch (Exception e) {
            log.error("Workflow execution failed: executionId={}", execution.getId(), e);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setError(e.getMessage());
            
            long duration = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
            execution.setDuration((int) duration);
            
            execution = executionRepository.save(execution);
        }

        return execution;
    }

    private int executeNodeAndDependencies(String nodeId, Map<String, Map<String, Object>> nodeMap,
                                          Map<String, List<String>> adjacencyList,
                                          ExecutionContext context, Execution execution,
                                          Set<String> executedNodes) {
        if (executedNodes.contains(nodeId)) {
            return 0; // Already executed
        }

        Map<String, Object> node = nodeMap.get(nodeId);
        if (node == null) {
            log.warn("Node not found: {}", nodeId);
            return 0;
        }

        int count = 0;

        try {
            // Execute node - validate and convert node type
            String nodeTypeStr = (String) node.get("type");
            if (nodeTypeStr == null) {
                log.warn("Node type is missing for node: {}", nodeId);
                createNodeExecution(execution, nodeId, "failed", null, null, 
                                  "Node type is missing");
                return 0;
            }
            
            NodeType nodeType = null;
            try {
                // Convert string to enum name format (uppercase, replace "-" with "_")
                String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
                nodeType = NodeType.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid node type: {} for node: {}", nodeTypeStr, nodeId);
                createNodeExecution(execution, nodeId, "failed", null, null, 
                                  "Invalid node type: " + nodeTypeStr);
                return 0;
            }
            
            // Get executor using Strategy Pattern
            NodeExecutor executor = nodeExecutorRegistry.getExecutor(nodeType);
            
            if (executor == null) {
                log.warn("No executor found for node type: {}", nodeType);
                // Create failed node execution record
                createNodeExecution(execution, nodeId, "failed", null, null, 
                                  "No executor found for node type: " + nodeType);
                return 0;
            }

            // Create node execution record
            NodeExecution nodeExecution = createNodeExecution(execution, nodeId, "running", 
                                                            context.getDataForNode(nodeId), null, null);

            long startTime = System.currentTimeMillis();
            
            // Execute node
            Map<String, Object> nodeData = node.containsKey("data") ? 
                (Map<String, Object>) node.get("data") : new HashMap<>();
            
            NodeExecutionResult result = executor.execute(nodeId, nodeData, context);

            long duration = System.currentTimeMillis() - startTime;

            // Update node execution
            if (result.isSuccess()) {
                // Check if node is waiting for events
                if (result.isWaiting()) {
                    // Node is waiting for events, mark as waiting
                    nodeExecution.setStatus("waiting");
                    nodeExecution.setOutputData(result.getOutput());
                    context.setNodeOutput(nodeId, result.getOutput());
                    
                    // Store wait state reference in context if available
                    Map<String, Object> output = result.getOutput() != null ? 
                        (Map<String, Object>) result.getOutput() : new HashMap<>();
                    String waitStateId = (String) output.get("waitStateId");
                    if (waitStateId != null) {
                        context.setWaitState(waitStateId, nodeId);
                    }
                    
                    nodeExecution.setDuration((int) duration);
                    nodeExecutionRepository.save(nodeExecution);
                    
                    // Don't continue execution - will be resumed when events are received
                    log.info("Node is waiting for events: nodeId={}, executionId={}, waitStateId={}", 
                               nodeId, execution.getId(), waitStateId);
                    execution.setStatus(ExecutionStatus.WAITING);
                    executionRepository.save(execution);
                    return 1;
                } else {
                    // Node completed normally
                    nodeExecution.setStatus("completed");
                    nodeExecution.setOutputData(result.getOutput());
                    context.setNodeOutput(nodeId, result.getOutput());
                }
            } else {
                nodeExecution.setStatus("failed");
                nodeExecution.setError(result.getError());
            }
            nodeExecution.setCompletedAt(LocalDateTime.now());
            nodeExecution.setDuration((int) duration);
            nodeExecutionRepository.save(nodeExecution);

            executedNodes.add(nodeId);
            count = 1;

            // Execute dependent nodes
            List<String> nextNodes = adjacencyList.getOrDefault(nodeId, new ArrayList<>());
            if (result.getNextNodeId() != null) {
                // Conditional node - follow specific branch
                nextNodes = Arrays.asList(result.getNextNodeId());
            }

            for (String nextNodeId : nextNodes) {
                if (result.isSuccess()) {
                    count += executeNodeAndDependencies(nextNodeId, nodeMap, adjacencyList, 
                                                       context, execution, executedNodes);
                }
            }

        } catch (Exception e) {
            log.error("Error executing node: nodeId={}", nodeId, e);
            createNodeExecution(execution, nodeId, "failed", null, null, e.getMessage());
        }

        return count;
    }

    /**
     * Resume workflow execution from a specific node after waiting for events
     * This method is idempotent and handles concurrent resume attempts gracefully
     *
     * @param executionId Execution ID
     * @param nodeId Node ID to resume from
     * @param aggregatedData Aggregated data from events
     */
    @org.springframework.transaction.annotation.Transactional
    public void resumeExecution(String executionId, String nodeId, Map<String, Object> aggregatedData) {
        log.info("Resuming workflow execution: executionId={}, nodeId={}, aggregatedDataKeys={}", 
                   executionId, nodeId, aggregatedData != null ? aggregatedData.keySet() : "null");

        // Validate execution exists
        Optional<Execution> executionOpt = executionRepository.findById(executionId);
        if (executionOpt.isEmpty()) {
            String errorMsg = String.format("Execution not found: executionId=%s", executionId);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        Execution execution = executionOpt.get();
        
        // Idempotency check: if execution is not waiting, it might have been resumed by another instance
        if (execution.getStatus() != ExecutionStatus.WAITING) {
            log.warn("Execution is not in waiting status (may have been resumed by another instance): " +
                       "executionId={}, status={}, nodeId={}", executionId, execution.getStatus(), nodeId);
            // Don't throw exception - this is expected in multi-instance deployments
            // Another instance may have already resumed this execution
            return;
        }

        // Validate workflow exists
        Workflow workflow = execution.getWorkflow();
        if (workflow == null) {
            String errorMsg = String.format("Workflow not found for execution: executionId=%s", executionId);
            log.error(errorMsg);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(errorMsg);
            executionRepository.save(execution);
            return;
        }
        
        // Validate workflow definition
        Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
        
        if (definition == null) {
            String errorMsg = String.format("Workflow definition is null: executionId=%s, workflowId=%s", 
                                          executionId, workflow.getId());
            log.error(errorMsg);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(errorMsg);
            executionRepository.save(execution);
            return;
        }
        
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
        
        if (nodes == null) {
            String errorMsg = String.format("Workflow nodes are null: executionId=%s, workflowId=%s", 
                                          executionId, workflow.getId());
            log.error(errorMsg);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(errorMsg);
            executionRepository.save(execution);
            return;
        }
        
        List<Map<String, Object>> edges = definition.containsKey("edges") ? 
            (List<Map<String, Object>>) definition.get("edges") : new ArrayList<>();

        // Build node graph
        Map<String, Map<String, Object>> nodeMap = buildNodeMap(nodes);
        Map<String, List<String>> adjacencyList = buildAdjacencyList(edges);
        
        // Validate waiting node exists in workflow definition
        if (!nodeMap.containsKey(nodeId)) {
            String errorMsg = String.format("Waiting node not found in workflow definition: executionId=%s, nodeId=%s, workflowId=%s", 
                                          executionId, nodeId, workflow.getId());
            log.error(errorMsg);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(errorMsg);
            executionRepository.save(execution);
            return;
        }

        // Create execution context with aggregated data
        ExecutionContext context = new ExecutionContext(executionId, workflow.getId());
        
        // Map trigger data to trigger nodeId (if available)
        // Note: For resume, we may not know which trigger node, so we'll try to find it
        Map<String, Object> workflowDefinition = (Map<String, Object>) workflow.getDefinition();
        if (workflowDefinition != null && workflowDefinition.containsKey("nodes")) {
            List<Map<String, Object>> workflowNodes = (List<Map<String, Object>>) workflowDefinition.get("nodes");
            String triggerNodeId = findTriggerNodeId(workflowNodes);
            if (triggerNodeId != null) {
                Map<String, Object> savedContext = execution.getContext() != null ? execution.getContext() : new HashMap<>();
                context.setTriggerDataForNode(triggerNodeId, savedContext);
            }
        }
        
        // Add aggregated event data to context
        if (aggregatedData != null) {
            aggregatedData.forEach(context::setVariable);
        }

        // Update execution status
        execution.setStatus(ExecutionStatus.RUNNING);
        execution = executionRepository.save(execution);

        try {
            // Find the node execution record and update it
            List<com.notificationplatform.entity.NodeExecution> nodeExecutions = 
                nodeExecutionRepository.findByExecutionId(executionId);
            
            boolean nodeExecutionUpdated = false;
            for (com.notificationplatform.entity.NodeExecution nodeExecution : nodeExecutions) {
                if (nodeId.equals(nodeExecution.getNodeId()) && 
                    ("waiting".equals(nodeExecution.getStatus()) || "waiting_for_events".equals(nodeExecution.getStatus()))) {
                    nodeExecution.setStatus("completed");
                    nodeExecution.setOutputData(aggregatedData);
                    nodeExecution.setCompletedAt(LocalDateTime.now());
                    
                    // Calculate duration from start to completion
                    long nodeDuration = java.time.Duration.between(
                        nodeExecution.getStartedAt(), 
                        LocalDateTime.now()
                    ).toMillis();
                    nodeExecution.setDuration((int) nodeDuration);
                    
                    nodeExecutionRepository.save(nodeExecution);
                    context.setNodeOutput(nodeId, aggregatedData);
                    nodeExecutionUpdated = true;
                    log.info("Node execution updated from waiting to completed: executionId={}, nodeId={}", 
                               executionId, nodeId);
                    break;
                }
            }
            
            if (!nodeExecutionUpdated) {
                log.warn("Node execution not found or not in waiting status (may have been updated by another instance): " +
                           "executionId={}, nodeId={}", executionId, nodeId);
                // Continue anyway - this is expected in multi-instance deployments for idempotency
                // Another instance may have already updated the node execution
            }

            // Get executed nodes from existing node executions and restore context
            Set<String> executedNodes = new HashSet<>();
            for (com.notificationplatform.entity.NodeExecution nodeExecution : nodeExecutions) {
                if ("completed".equals(nodeExecution.getStatus()) || "failed".equals(nodeExecution.getStatus())) {
                    executedNodes.add(nodeExecution.getNodeId());
                    // Restore node outputs to context
                    if (nodeExecution.getOutputData() != null) {
                        context.setNodeOutput(nodeExecution.getNodeId(), nodeExecution.getOutputData());
                    }
                }
            }

            // Continue execution from next nodes (not from the waiting node itself)
            int nodesExecuted = execution.getNodesExecuted() != null ? execution.getNodesExecuted() : 0;
            
            // Get next nodes from adjacency list
            List<String> nextNodes = adjacencyList.getOrDefault(nodeId, new ArrayList<>());
            for (String nextNodeId : nextNodes) {
                nodesExecuted += executeNodeAndDependencies(nextNodeId, nodeMap, adjacencyList, 
                                                           context, execution, executedNodes);
                
                // Check if execution is waiting again (nested wait states)
                if (execution.getStatus() == ExecutionStatus.WAITING) {
                    log.info("Workflow execution paused again (nested wait): executionId={}, nodesExecuted={}", 
                               executionId, nodesExecuted);
                    execution.setNodesExecuted(nodesExecuted);
                    execution = executionRepository.save(execution);
                    return;
                }
            }

            // Update execution status - check if still waiting
            if (!context.isWaiting()) {
                execution.setStatus(ExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                execution.setNodesExecuted(nodesExecuted);
                
                long duration = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
                execution.setDuration((int) duration);
                
                execution = executionRepository.save(execution);

                log.info("Workflow execution resumed and completed: executionId={}, nodesExecuted={}", 
                           executionId, nodesExecuted);
            } else {
                // Still waiting (nested wait states)
                log.info("Workflow execution still waiting after resume: executionId={}, waitingNodeId={}, nodesExecuted={}", 
                           executionId, context.getWaitingNodeId(), nodesExecuted);
                execution.setNodesExecuted(nodesExecuted);
                execution = executionRepository.save(execution);
            }

        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            // Another instance may have already resumed this execution
            log.warn("Optimistic locking failure during resume (another instance may have resumed): " +
                       "executionId={}, nodeId={}", executionId, nodeId, e);
            // Don't update execution status - another instance handled it
            // This is expected in multi-instance deployments
        } catch (Exception e) {
            String errorMsg = String.format("Error resuming workflow execution: executionId=%s, nodeId=%s, error=%s", 
                                          executionId, nodeId, e.getMessage());
            log.error(errorMsg, e);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setError(errorMsg);
            execution = executionRepository.save(execution);
        }
    }

    private NodeExecution createNodeExecution(Execution execution, String nodeId, String status,
                                             Map<String, Object> inputData, Map<String, Object> outputData, String error) {
        NodeExecution nodeExecution = new NodeExecution();
        nodeExecution.setId(UUID.randomUUID().toString());
        nodeExecution.setExecution(execution);
        nodeExecution.setNodeId(nodeId);
        nodeExecution.setStatus(status);
        nodeExecution.setStartedAt(LocalDateTime.now());
        nodeExecution.setInputData(inputData);
        nodeExecution.setOutputData(outputData);
        nodeExecution.setError(error);
        return nodeExecutionRepository.save(nodeExecution);
    }

    private Map<String, Map<String, Object>> buildNodeMap(List<Map<String, Object>> nodes) {
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();
        for (Map<String, Object> node : nodes) {
            String nodeId = (String) node.get("id");
            nodeMap.put(nodeId, node);
        }
        return nodeMap;
    }

    private Map<String, List<String>> buildAdjacencyList(List<Map<String, Object>> edges) {
        Map<String, List<String>> adjacencyList = new HashMap<>();
        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
        }
        return adjacencyList;
    }

    private List<String> findStartNodes(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
        Set<String> hasIncoming = new HashSet<>();
        for (Map<String, Object> edge : edges) {
            String target = (String) edge.get("target");
            hasIncoming.add(target);
        }

        List<String> startNodes = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            String nodeId = (String) node.get("id");
            String nodeTypeStr = (String) node.get("type");
            NodeType nodeType = null;
            if (nodeTypeStr != null) {
                try {
                    String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
                    nodeType = NodeType.valueOf(enumName);
                } catch (IllegalArgumentException e) {
                    // Invalid node type, skip
                }
            }
            
            // Start from trigger nodes or nodes with no incoming edges
            if (isTriggerNodeType(nodeType) || !hasIncoming.contains(nodeId)) {
                startNodes.add(nodeId);
            }
        }

        return startNodes.isEmpty() && !nodes.isEmpty() ? 
               Arrays.asList((String) nodes.get(0).get("id")) : startNodes;
    }

    /**
     * Find the first trigger node ID in the workflow
     * Used to map trigger data to trigger node
     */
    private String findTriggerNodeId(List<Map<String, Object>> nodes) {
        if (nodes == null) {
            return null;
        }
        
        for (Map<String, Object> node : nodes) {
            String nodeTypeStr = (String) node.get("type");
            NodeType nodeType = null;
            if (nodeTypeStr != null) {
                try {
                    String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
                    nodeType = NodeType.valueOf(enumName);
                } catch (IllegalArgumentException e) {
                    // Invalid node type, skip
                }
            }
            if (isTriggerNodeType(nodeType)) {
                return (String) node.get("id");
            }
        }
        
        return null;
    }

    /**
     * Check if a node type is a trigger node type
     */
    private boolean isTriggerNodeType(NodeType nodeType) {
        if (nodeType == null) {
            return false;
        }
        return nodeType == NodeType.TRIGGER ||
               nodeType == NodeType.API_TRIGGER ||
               nodeType == NodeType.SCHEDULE_TRIGGER ||
               nodeType == NodeType.FILE_TRIGGER ||
               nodeType == NodeType.EVENT_TRIGGER;
    }
}

