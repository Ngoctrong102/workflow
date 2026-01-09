package com.notificationplatform.service.execution;

import com.notificationplatform.dto.request.CancelExecutionRequest;
import com.notificationplatform.dto.request.RetryExecutionRequest;
import com.notificationplatform.dto.request.VisualizeStepRequest;
import com.notificationplatform.dto.response.ExecutionDetailResponse;
import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.dto.response.ExecutionVisualizationResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.VisualizeStepResponse;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class ExecutionServiceImpl implements ExecutionService {

    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutor workflowExecutor;

    public ExecutionServiceImpl(ExecutionRepository executionRepository,
                               NodeExecutionRepository nodeExecutionRepository,
                               WorkflowRepository workflowRepository,
                               WorkflowExecutor workflowExecutor) {
        this.executionRepository = executionRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.workflowRepository = workflowRepository;
        this.workflowExecutor = workflowExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionStatusResponse getExecutionStatus(String executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        // Get node executions
        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);

        ExecutionStatusResponse response = new ExecutionStatusResponse();
        response.setExecutionId(execution.getId());
        response.setWorkflowId(execution.getWorkflow().getId());
        response.setWorkflowName(execution.getWorkflow().getName());
        response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        response.setDuration(execution.getDuration());
        response.setError(execution.getError());
        // Get trigger data from execution context or trigger config
        Map<String, Object> triggerDataMap = null;
        if (execution.getContext() != null) {
            Map<String, Object> context = execution.getContext();
            if (context.containsKey("triggerData")) {
                triggerDataMap = (Map<String, Object>) context.get("triggerData");
            } else {
                triggerDataMap = context;
            }
        }
        if (triggerDataMap == null && execution.getTrigger() != null && execution.getTrigger().getConfig() != null) {
            triggerDataMap = (Map<String, Object>) execution.getTrigger().getConfig();
        }
        response.setTriggerData(triggerDataMap);

        // Map node executions
        List<ExecutionStatusResponse.NodeExecutionStatus> nodeStatuses = nodeExecutions.stream()
                .map(ne -> {
                    ExecutionStatusResponse.NodeExecutionStatus status = new ExecutionStatusResponse.NodeExecutionStatus();
                    status.setNodeId(ne.getNodeId());
                    status.setNodeType(extractNodeType(ne));
                    status.setStatus(ne.getStatus() != null ? ne.getStatus().getValue() : null);
                    status.setStartedAt(ne.getStartedAt());
                    status.setCompletedAt(ne.getCompletedAt());
                    status.setDuration(ne.getDuration());
                    status.setError(ne.getError());
                    status.setOutput(ne.getOutputData() != null ? 
                            (Map<String, Object>) ne.getOutputData() : null);
                    return status;
                })
                .collect(Collectors.toList());

        response.setNodeExecutions(nodeStatuses);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionDetailResponse getExecutionDetail(String executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        // Get node executions
        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);

        ExecutionDetailResponse response = new ExecutionDetailResponse();
        response.setExecutionId(execution.getId());
        response.setWorkflowId(execution.getWorkflow().getId());
        response.setWorkflowName(execution.getWorkflow().getName());
        response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        response.setDuration(execution.getDuration());
        response.setError(execution.getError());
        // Get trigger data from execution context or trigger config
        Map<String, Object> triggerDataMap = null;
        if (execution.getContext() != null) {
            Map<String, Object> context = execution.getContext();
            if (context.containsKey("triggerData")) {
                triggerDataMap = (Map<String, Object>) context.get("triggerData");
            } else {
                triggerDataMap = context;
            }
        }
        if (triggerDataMap == null && execution.getTrigger() != null && execution.getTrigger().getConfig() != null) {
            triggerDataMap = (Map<String, Object>) execution.getTrigger().getConfig();
        }
        response.setTriggerData(triggerDataMap);
        response.setExecutionContext(execution.getContext());

        if (execution.getTrigger() != null) {
            response.setTriggerId(execution.getTrigger().getId());
            response.setTriggerType(execution.getTrigger().getTriggerType() != null ? execution.getTrigger().getTriggerType().getValue() : null);
        }

        // Map node executions with details
        List<ExecutionDetailResponse.NodeExecutionDetail> nodeDetails = nodeExecutions.stream()
                .map(ne -> {
                    ExecutionDetailResponse.NodeExecutionDetail detail = new ExecutionDetailResponse.NodeExecutionDetail();
                    detail.setNodeId(ne.getNodeId());
                    detail.setNodeName(extractNodeName(ne));
                    detail.setNodeType(extractNodeType(ne));
                    detail.setStatus(ne.getStatus() != null ? ne.getStatus().getValue() : null);
                    detail.setStartedAt(ne.getStartedAt());
                    detail.setCompletedAt(ne.getCompletedAt());
                    detail.setDuration(ne.getDuration());
                    detail.setError(ne.getError());
                    detail.setInputData(ne.getInputData() != null ?
                            (Map<String, Object>) ne.getInputData() : null);
                    detail.setOutputData(ne.getOutputData() != null ?
                            (Map<String, Object>) ne.getOutputData() : null);
                    // Generate logs from node execution
                    detail.setLogs(generateNodeLogs(ne));
                    return detail;
                })
                .collect(Collectors.toList());

        response.setNodeExecutions(nodeDetails);

        // Generate execution logs
        response.setLogs(generateExecutionLogs(execution, nodeExecutions));

        // Calculate performance metrics
        response.setPerformanceMetrics(calculatePerformanceMetrics(execution, nodeExecutions));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExecutionStatusResponse> listExecutions(String workflowId, String status, 
                                                                 LocalDateTime startDate, LocalDateTime endDate,
                                                                 String search,
                                                                 int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Execution> executions;

        if (workflowId != null && !workflowId.isEmpty()) {
            if (startDate != null && endDate != null) {
                executions = executionRepository.findByWorkflowIdAndStartedAtBetween(workflowId, startDate, endDate);
            } else {
                executions = executionRepository.findByWorkflowId(workflowId);
            }
        } else if (startDate != null && endDate != null) {
            executions = executionRepository.findByStartedAtBetween(startDate, endDate);
        } else {
            executions = executionRepository.findAll();
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            executions = executions.stream()
                    .filter(e -> status.equals(e.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            executions = executions.stream()
                    .filter(e -> {
                        // Search in workflow name
                        if (e.getWorkflow() != null && e.getWorkflow().getName() != null &&
                            e.getWorkflow().getName().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        // Search in error message
                        if (e.getError() != null && e.getError().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        // Search in execution ID
                        if (e.getId().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        long total = executions.size();

        // Apply pagination
        int fromIndex = Math.min(offset, executions.size());
        int toIndex = Math.min(offset + limit, executions.size());
        List<Execution> pagedExecutions = executions.subList(fromIndex, toIndex);

        List<ExecutionStatusResponse> responses = pagedExecutions.stream()
                .map(e -> {
                    ExecutionStatusResponse resp = new ExecutionStatusResponse();
                    resp.setExecutionId(e.getId());
                    resp.setWorkflowId(e.getWorkflow().getId());
                    resp.setWorkflowName(e.getWorkflow().getName());
                    resp.setStatus(e.getStatus() != null ? e.getStatus().getValue() : null);
                    resp.setStartedAt(e.getStartedAt());
                    resp.setCompletedAt(e.getCompletedAt());
                    resp.setDuration(e.getDuration());
                    resp.setError(e.getError());
                    // Get trigger data from execution context or trigger config
                    Map<String, Object> triggerDataMap = null;
                    if (e.getContext() != null) {
                        Map<String, Object> context = (Map<String, Object>) e.getContext();
                        if (context.containsKey("triggerData")) {
                            triggerDataMap = (Map<String, Object>) context.get("triggerData");
                        } else {
                            triggerDataMap = context;
                        }
                    }
                    if (triggerDataMap == null && e.getTrigger() != null && e.getTrigger().getConfig() != null) {
                        triggerDataMap = (Map<String, Object>) e.getTrigger().getConfig();
                    }
                    resp.setTriggerData(triggerDataMap);
                    return resp;
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionDetailResponse.ExecutionLog> getExecutionLogs(String executionId, String nodeId, String level) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        List<NodeExecution> nodeExecutions;
        if (nodeId != null && !nodeId.isEmpty()) {
            nodeExecutions = nodeExecutionRepository.findByExecutionIdAndNodeId(executionId, nodeId);
        } else {
            nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);
        }

        List<ExecutionDetailResponse.ExecutionLog> logs = generateExecutionLogs(execution, nodeExecutions);

        // Filter by level if provided
        if (level != null && !level.isEmpty()) {
            logs = logs.stream()
                    .filter(log -> level.equalsIgnoreCase(log.getLevel()))
                    .collect(Collectors.toList());
        }

        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExecutionContext(String executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        Map<String, Object> context = new HashMap<>();
        if (execution.getContext() != null) {
            context.putAll(execution.getContext());
        }

        // Add execution metadata
        context.put("executionId", execution.getId());
        context.put("workflowId", execution.getWorkflow().getId());
        context.put("status", execution.getStatus());
        context.put("startedAt", execution.getStartedAt());
        context.put("completedAt", execution.getCompletedAt());

        return context;
    }

    @Override
    public void cancelExecution(String executionId, CancelExecutionRequest request) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        if (execution.getStatus() != ExecutionStatus.RUNNING) {
            throw new IllegalStateException("Execution can only be cancelled when running. Current status: " + execution.getStatus());
        }

        execution.setStatus(ExecutionStatus.CANCELLED);
        execution.setCompletedAt(LocalDateTime.now());
        if (request.getReason() != null) {
            execution.setError("Cancelled: " + request.getReason());
        } else {
            execution.setError("Cancelled by user");
        }

        // Calculate duration
        if (execution.getStartedAt() != null) {
            long duration = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
            execution.setDuration((int) duration);
        }

        executionRepository.save(execution);

        log.info("Cancelled execution: executionId={}, reason={}", executionId, request.getReason());
    }

    @Override
    public ExecutionStatusResponse retryExecution(String executionId, RetryExecutionRequest request) {
        Execution originalExecution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        if (!"failed".equals(originalExecution.getStatus()) && !"cancelled".equals(originalExecution.getStatus())) {
            throw new IllegalStateException("Execution can only be retried when failed or cancelled. Current status: " + originalExecution.getStatus());
        }

        Workflow workflow = originalExecution.getWorkflow();
        if (workflow == null) {
            throw new IllegalStateException("Original execution has no associated workflow");
        }

        // Get trigger data from original execution
        Map<String, Object> triggerData = originalExecution.getContext() != null ?
                (Map<String, Object>) originalExecution.getContext() : new HashMap<>();

        // If retrying from failed node, we would need to find the failed node and start from there
        // For MVP, we'll retry from the beginning
        String triggerId = originalExecution.getTrigger() != null ? originalExecution.getTrigger().getId() : null;

        // Execute workflow
        Execution newExecution = workflowExecutor.execute(workflow, triggerData, triggerId);

        log.info("Retried execution: originalExecutionId={}, newExecutionId={}", executionId, newExecution.getId());

        return getExecutionStatus(newExecution.getId());
    }

    private String extractNodeType(NodeExecution nodeExecution) {
        // Try to extract node type from input data or node ID
        if (nodeExecution.getInputData() != null && nodeExecution.getInputData() instanceof Map) {
            Map<String, Object> inputData = (Map<String, Object>) nodeExecution.getInputData();
            if (inputData.containsKey("type")) {
                return inputData.get("type").toString();
            }
        }
        return "unknown";
    }

    private String extractNodeName(NodeExecution nodeExecution) {
        // Try to extract node name from input data
        if (nodeExecution.getInputData() != null && nodeExecution.getInputData() instanceof Map) {
            Map<String, Object> inputData = (Map<String, Object>) nodeExecution.getInputData();
            if (inputData.containsKey("name")) {
                return inputData.get("name").toString();
            }
            if (inputData.containsKey("label")) {
                return inputData.get("label").toString();
            }
        }
        return nodeExecution.getNodeId();
    }

    private List<ExecutionDetailResponse.ExecutionLog> generateExecutionLogs(Execution execution, List<NodeExecution> nodeExecutions) {
        List<ExecutionDetailResponse.ExecutionLog> logs = new ArrayList<>();

        // Add execution start log
        ExecutionDetailResponse.ExecutionLog startLog = new ExecutionDetailResponse.ExecutionLog();
        startLog.setTimestamp(execution.getStartedAt());
        startLog.setLevel("INFO");
        startLog.setMessage("Execution started");
        logs.add(startLog);

        // Add node execution logs
        for (NodeExecution nodeExecution : nodeExecutions) {
            ExecutionDetailResponse.ExecutionLog nodeStartLog = new ExecutionDetailResponse.ExecutionLog();
            nodeStartLog.setTimestamp(nodeExecution.getStartedAt());
            nodeStartLog.setLevel("INFO");
            nodeStartLog.setMessage("Node execution started: " + nodeExecution.getNodeId());
            nodeStartLog.setNodeId(nodeExecution.getNodeId());
            logs.add(nodeStartLog);

            if (nodeExecution.getCompletedAt() != null) {
                ExecutionDetailResponse.ExecutionLog nodeCompleteLog = new ExecutionDetailResponse.ExecutionLog();
                nodeCompleteLog.setTimestamp(nodeExecution.getCompletedAt());
                nodeCompleteLog.setLevel("INFO");
                nodeCompleteLog.setMessage("Node execution completed: " + nodeExecution.getNodeId());
                nodeCompleteLog.setNodeId(nodeExecution.getNodeId());
                logs.add(nodeCompleteLog);
            }

            if (nodeExecution.getError() != null) {
                ExecutionDetailResponse.ExecutionLog errorLog = new ExecutionDetailResponse.ExecutionLog();
                errorLog.setTimestamp(nodeExecution.getCompletedAt() != null ? nodeExecution.getCompletedAt() : nodeExecution.getStartedAt());
                errorLog.setLevel("ERROR");
                errorLog.setMessage("Node execution failed: " + nodeExecution.getError());
                errorLog.setNodeId(nodeExecution.getNodeId());
                logs.add(errorLog);
            }
        }

        // Add execution completion log
        if (execution.getCompletedAt() != null) {
            ExecutionDetailResponse.ExecutionLog completeLog = new ExecutionDetailResponse.ExecutionLog();
            completeLog.setTimestamp(execution.getCompletedAt());
            completeLog.setLevel("INFO");
            completeLog.setMessage("Execution " + execution.getStatus());
            logs.add(completeLog);
        }

        if (execution.getError() != null) {
            ExecutionDetailResponse.ExecutionLog errorLog = new ExecutionDetailResponse.ExecutionLog();
            errorLog.setTimestamp(execution.getCompletedAt() != null ? execution.getCompletedAt() : execution.getStartedAt());
            errorLog.setLevel("ERROR");
            errorLog.setMessage("Execution error: " + execution.getError());
            logs.add(errorLog);
        }

        // Sort by timestamp
        logs.sort(Comparator.comparing(ExecutionDetailResponse.ExecutionLog::getTimestamp));

        return logs;
    }

    private List<ExecutionDetailResponse.ExecutionLog> generateNodeLogs(NodeExecution nodeExecution) {
        List<ExecutionDetailResponse.ExecutionLog> logs = new ArrayList<>();

        ExecutionDetailResponse.ExecutionLog startLog = new ExecutionDetailResponse.ExecutionLog();
        startLog.setTimestamp(nodeExecution.getStartedAt());
        startLog.setLevel("INFO");
        startLog.setMessage("Node execution started");
        startLog.setNodeId(nodeExecution.getNodeId());
        logs.add(startLog);

        if (nodeExecution.getCompletedAt() != null) {
            ExecutionDetailResponse.ExecutionLog completeLog = new ExecutionDetailResponse.ExecutionLog();
            completeLog.setTimestamp(nodeExecution.getCompletedAt());
            completeLog.setLevel("INFO");
            completeLog.setMessage("Node execution completed");
            completeLog.setNodeId(nodeExecution.getNodeId());
            logs.add(completeLog);
        }

        if (nodeExecution.getError() != null) {
            ExecutionDetailResponse.ExecutionLog errorLog = new ExecutionDetailResponse.ExecutionLog();
            errorLog.setTimestamp(nodeExecution.getCompletedAt() != null ? nodeExecution.getCompletedAt() : nodeExecution.getStartedAt());
            errorLog.setLevel("ERROR");
            errorLog.setMessage("Node execution error: " + nodeExecution.getError());
            errorLog.setNodeId(nodeExecution.getNodeId());
            logs.add(errorLog);
        }

        return logs;
    }

    private Map<String, Object> calculatePerformanceMetrics(Execution execution, List<NodeExecution> nodeExecutions) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalNodes", nodeExecutions.size());
        metrics.put("nodesExecuted", execution.getNodesExecuted());
        metrics.put("notificationsSent", execution.getNotificationsSent());
        metrics.put("totalDuration", execution.getDuration());

        if (!nodeExecutions.isEmpty()) {
            // Calculate average node execution time
            double avgNodeDuration = nodeExecutions.stream()
                    .filter(ne -> ne.getDuration() != null)
                    .mapToInt(NodeExecution::getDuration)
                    .average()
                    .orElse(0.0);
            metrics.put("averageNodeDuration", avgNodeDuration);

            // Find slowest node
            Optional<NodeExecution> slowestNode = nodeExecutions.stream()
                    .filter(ne -> ne.getDuration() != null)
                    .max(Comparator.comparing(NodeExecution::getDuration));
            if (slowestNode.isPresent()) {
                Map<String, Object> slowest = new HashMap<>();
                slowest.put("nodeId", slowestNode.get().getNodeId());
                slowest.put("duration", slowestNode.get().getDuration());
                metrics.put("slowestNode", slowest);
            }

            // Find fastest node
            Optional<NodeExecution> fastestNode = nodeExecutions.stream()
                    .filter(ne -> ne.getDuration() != null)
                    .min(Comparator.comparing(NodeExecution::getDuration));
            if (fastestNode.isPresent()) {
                Map<String, Object> fastest = new HashMap<>();
                fastest.put("nodeId", fastestNode.get().getNodeId());
                fastest.put("duration", fastestNode.get().getDuration());
                metrics.put("fastestNode", fastest);
            }
        }

        return metrics;
    }

    // Visualization methods
    private final Map<String, Integer> visualizationStepCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public ExecutionVisualizationResponse getExecutionVisualization(String executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        Workflow workflow = execution.getWorkflow();
        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);

        ExecutionVisualizationResponse response = new ExecutionVisualizationResponse();

        // Execution info
        ExecutionVisualizationResponse.ExecutionInfo execInfo = new ExecutionVisualizationResponse.ExecutionInfo();
        execInfo.setId(execution.getId());
        execInfo.setWorkflowId(execution.getWorkflow().getId());
        execInfo.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        execInfo.setStartedAt(execution.getStartedAt());
        execInfo.setCompletedAt(execution.getCompletedAt());
        response.setExecution(execInfo);

        // Workflow info
        ExecutionVisualizationResponse.WorkflowInfo workflowInfo = new ExecutionVisualizationResponse.WorkflowInfo();
        workflowInfo.setId(workflow.getId());
        workflowInfo.setName(workflow.getName());
        workflowInfo.setDefinition(workflow.getDefinition());
        response.setWorkflow(workflowInfo);

        // Trigger info
        if (execution.getTrigger() != null) {
            ExecutionVisualizationResponse.TriggerInfo triggerInfo = new ExecutionVisualizationResponse.TriggerInfo();
            triggerInfo.setType(execution.getTrigger().getTriggerType() != null ? 
                execution.getTrigger().getTriggerType().getValue() : null);
            triggerInfo.setData(execution.getTriggerData());
            response.setTrigger(triggerInfo);
        }

        // Current step and total steps
        response.setCurrentStep(0);
        response.setTotalSteps(nodeExecutions.size());

        // Node info
        List<ExecutionVisualizationResponse.NodeInfo> nodeInfos = new ArrayList<>();
        Map<String, NodeExecution> nodeExecutionMap = nodeExecutions.stream()
                .collect(Collectors.toMap(NodeExecution::getNodeId, ne -> ne));

        if (workflow.getDefinition() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

            if (nodes != null) {
                for (Map<String, Object> node : nodes) {
                    ExecutionVisualizationResponse.NodeInfo nodeInfo = new ExecutionVisualizationResponse.NodeInfo();
                    nodeInfo.setId((String) node.get("id"));
                    nodeInfo.setType((String) node.get("type"));
                    
                    NodeExecution nodeExec = nodeExecutionMap.get(nodeInfo.getId());
                    if (nodeExec != null) {
                        nodeInfo.setStatus(nodeExec.getStatus() != null ? nodeExec.getStatus().getValue() : null);
                        Map<String, Object> nodeData = new HashMap<>();
                        nodeData.put("input", nodeExec.getInputData());
                        nodeData.put("output", nodeExec.getOutputData());
                        nodeInfo.setData(nodeData);
                    } else {
                        nodeInfo.setStatus("pending");
                        nodeInfo.setData(new HashMap<>());
                    }
                    
                    nodeInfos.add(nodeInfo);
                }
            }
        }
        response.setNodes(nodeInfos);

        // Context
        response.setContext(execution.getContext() != null ? execution.getContext() : new HashMap<>());

        // Initialize step cache
        visualizationStepCache.put(executionId, 0);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public VisualizeStepResponse executeVisualizationStep(String executionId, VisualizeStepRequest request) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);
        int currentStep = visualizationStepCache.getOrDefault(executionId, 0);
        int totalSteps = nodeExecutions.size();

        // Determine direction
        boolean forward = "forward".equalsIgnoreCase(request.getDirection()) || 
                         request.getDirection() == null || request.getDirection().isEmpty();

        int nextStep;
        if (forward) {
            nextStep = Math.min(currentStep + 1, totalSteps);
        } else {
            nextStep = Math.max(currentStep - 1, 0);
        }

        visualizationStepCache.put(executionId, nextStep);

        VisualizeStepResponse response = new VisualizeStepResponse();
        response.setStepNumber(nextStep);
        response.setHasNext(nextStep < totalSteps);
        response.setHasPrevious(nextStep > 0);

        if (nextStep > 0 && nextStep <= nodeExecutions.size()) {
            NodeExecution nodeExec = nodeExecutions.get(nextStep - 1);
            response.setNodeId(nodeExec.getNodeId());
            response.setNodeType(nodeExec.getNodeType());
            response.setStatus(nodeExec.getStatus() != null ? nodeExec.getStatus().getValue() : null);

            // Execution info
            ExecutionVisualizationResponse.ExecutionInfo execInfo = new ExecutionVisualizationResponse.ExecutionInfo();
            execInfo.setId(execution.getId());
            execInfo.setWorkflowId(execution.getWorkflow().getId());
            execInfo.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
            execInfo.setStartedAt(execution.getStartedAt());
            execInfo.setCompletedAt(execution.getCompletedAt());
            response.setExecution(execInfo);

            // Context at this step
            Map<String, Object> context = new HashMap<>();
            if (execution.getContext() != null) {
                context.putAll(execution.getContext());
            }
            // Add node outputs up to this step
            for (int i = 0; i < nextStep; i++) {
                NodeExecution ne = nodeExecutions.get(i);
                if (ne.getOutputData() != null) {
                    context.put("node_" + ne.getNodeId() + "_output", ne.getOutputData());
                }
            }
            response.setContext(context);

            // Next node
            if (nextStep < nodeExecutions.size()) {
                response.setNextNode(nodeExecutions.get(nextStep).getNodeId());
            }
        } else {
            response.setNodeId(null);
            response.setNodeType(null);
            response.setStatus(null);
            response.setExecution(null);
            response.setContext(execution.getContext() != null ? execution.getContext() : new HashMap<>());
            response.setNextNode(null);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionVisualizationResponse getExecutionStateAtStep(String executionId, Integer stepNumber) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);
        int totalSteps = nodeExecutions.size();
        int step = Math.max(0, Math.min(stepNumber, totalSteps));

        visualizationStepCache.put(executionId, step);

        // Build response similar to getExecutionVisualization but with current step set
        ExecutionVisualizationResponse response = getExecutionVisualization(executionId);
        response.setCurrentStep(step);

        return response;
    }

    @Override
    public void resetVisualization(String executionId) {
        visualizationStepCache.put(executionId, 0);
        log.debug("Reset visualization for execution: executionId={}", executionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getVisualizationContext(String executionId) {
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        int currentStep = visualizationStepCache.getOrDefault(executionId, 0);
        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(executionId);

        Map<String, Object> context = new HashMap<>();
        if (execution.getContext() != null) {
            context.putAll(execution.getContext());
        }

        // Add node outputs up to current step
        for (int i = 0; i < currentStep && i < nodeExecutions.size(); i++) {
            NodeExecution ne = nodeExecutions.get(i);
            if (ne.getOutputData() != null) {
                context.put("node_" + ne.getNodeId() + "_output", ne.getOutputData());
            }
        }

        return context;
    }
}

