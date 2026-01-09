package com.notificationplatform.service.visualization;

import com.notificationplatform.dto.request.VisualizeStepRequest;
import com.notificationplatform.dto.response.ExecutionVisualizationResponse;
import com.notificationplatform.dto.response.VisualizeStepResponse;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of execution visualization service.
 * Handles step-by-step execution replay and context reconstruction.
 * 
 * See: @import(features/execution-visualization.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionVisualizationServiceImpl implements ExecutionVisualizationService {

    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    
    // Cache for tracking current step per execution
    private final Map<String, Integer> visualizationStepCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public ExecutionVisualizationResponse loadExecutionForVisualization(String executionId) {
        log.debug("Loading execution for visualization: executionId={}", executionId);
        
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        Workflow workflow = execution.getWorkflow();
        List<NodeExecution> nodeExecutions = nodeExecutionRepository
                .findByExecutionIdOrderByStartedAtAsc(executionId);

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
        int currentStep = visualizationStepCache.getOrDefault(executionId, 0);
        response.setCurrentStep(currentStep);
        response.setTotalSteps(nodeExecutions.size());

        // Node info - reconstruct from workflow definition and node executions
        List<ExecutionVisualizationResponse.NodeInfo> nodeInfos = buildNodeInfos(workflow, nodeExecutions, currentStep);
        response.setNodes(nodeInfos);

        // Context - reconstruct from execution context and node outputs
        Map<String, Object> context = reconstructContext(execution, nodeExecutions, currentStep);
        response.setContext(context);

        // Initialize step cache if not already set
        if (!visualizationStepCache.containsKey(executionId)) {
            visualizationStepCache.put(executionId, 0);
        }

        log.debug("Loaded execution for visualization: executionId={}, totalSteps={}, currentStep={}", 
                executionId, nodeExecutions.size(), currentStep);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionVisualizationResponse getExecutionStateAtStep(String executionId, Integer stepNumber) {
        log.debug("Getting execution state at step: executionId={}, stepNumber={}", executionId, stepNumber);
        
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        List<NodeExecution> nodeExecutions = nodeExecutionRepository
                .findByExecutionIdOrderByStartedAtAsc(executionId);
        int totalSteps = nodeExecutions.size();
        int step = Math.max(0, Math.min(stepNumber, totalSteps));

        // Update step cache
        visualizationStepCache.put(executionId, step);

        // Build response with current step
        ExecutionVisualizationResponse response = loadExecutionForVisualization(executionId);
        response.setCurrentStep(step);

        // Update node infos to reflect current step
        Workflow workflow = execution.getWorkflow();
        List<ExecutionVisualizationResponse.NodeInfo> nodeInfos = buildNodeInfos(workflow, nodeExecutions, step);
        response.setNodes(nodeInfos);

        // Reconstruct context at this step
        Map<String, Object> context = reconstructContext(execution, nodeExecutions, step);
        response.setContext(context);

        log.debug("Got execution state at step: executionId={}, step={}", executionId, step);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public VisualizeStepResponse executeNextStep(String executionId, VisualizeStepRequest request) {
        log.debug("Executing next step: executionId={}, direction={}", executionId, request.getDirection());
        
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        List<NodeExecution> nodeExecutions = nodeExecutionRepository
                .findByExecutionIdOrderByStartedAtAsc(executionId);
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

        // Update step cache
        visualizationStepCache.put(executionId, nextStep);

        VisualizeStepResponse response = new VisualizeStepResponse();
        response.setStepNumber(nextStep);
        response.setHasNext(nextStep < totalSteps);
        response.setHasPrevious(nextStep > 0);

        // Execution info
        ExecutionVisualizationResponse.ExecutionInfo execInfo = new ExecutionVisualizationResponse.ExecutionInfo();
        execInfo.setId(execution.getId());
        execInfo.setWorkflowId(execution.getWorkflow().getId());
        execInfo.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        execInfo.setStartedAt(execution.getStartedAt());
        execInfo.setCompletedAt(execution.getCompletedAt());
        response.setExecution(execInfo);

        // Node info at this step
        if (nextStep > 0 && nextStep <= nodeExecutions.size()) {
            NodeExecution nodeExec = nodeExecutions.get(nextStep - 1);
            response.setNodeId(nodeExec.getNodeId());
            response.setNodeType(nodeExec.getNodeType());
            response.setStatus(nodeExec.getStatus() != null ? nodeExec.getStatus().getValue() : null);

            // Context at this step
            Map<String, Object> context = reconstructContext(execution, nodeExecutions, nextStep);
            response.setContext(context);

            // Next node
            if (nextStep < nodeExecutions.size()) {
                response.setNextNode(nodeExecutions.get(nextStep).getNodeId());
            }
        } else {
            response.setNodeId(null);
            response.setNodeType(null);
            response.setStatus(null);
            response.setContext(reconstructContext(execution, nodeExecutions, nextStep));
            response.setNextNode(null);
        }

        log.debug("Executed step: executionId={}, step={}, hasNext={}, hasPrevious={}", 
                executionId, nextStep, response.getHasNext(), response.getHasPrevious());

        return response;
    }

    @Override
    public void resetVisualization(String executionId) {
        log.debug("Resetting visualization: executionId={}", executionId);
        visualizationStepCache.put(executionId, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentContext(String executionId) {
        log.debug("Getting current context: executionId={}", executionId);
        
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        int currentStep = visualizationStepCache.getOrDefault(executionId, 0);
        List<NodeExecution> nodeExecutions = nodeExecutionRepository
                .findByExecutionIdOrderByStartedAtAsc(executionId);

        return reconstructContext(execution, nodeExecutions, currentStep);
    }

    /**
     * Build node infos from workflow definition and node executions.
     */
    @SuppressWarnings("unchecked")
    private List<ExecutionVisualizationResponse.NodeInfo> buildNodeInfos(
            Workflow workflow, List<NodeExecution> nodeExecutions, int currentStep) {
        
        List<ExecutionVisualizationResponse.NodeInfo> nodeInfos = new ArrayList<>();
        Map<String, NodeExecution> nodeExecutionMap = nodeExecutions.stream()
                .collect(Collectors.toMap(NodeExecution::getNodeId, ne -> ne));

        if (workflow.getDefinition() != null) {
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

            if (nodes != null) {
                for (Map<String, Object> node : nodes) {
                    ExecutionVisualizationResponse.NodeInfo nodeInfo = new ExecutionVisualizationResponse.NodeInfo();
                    nodeInfo.setId((String) node.get("id"));
                    nodeInfo.setType((String) node.get("type"));
                    
                    NodeExecution nodeExec = nodeExecutionMap.get(nodeInfo.getId());
                    if (nodeExec != null) {
                        nodeInfo.setStatus(nodeExec.getStatus() != null ? nodeExec.getStatus().getValue() : null);
                        
                        // Build node data with input and output
                        Map<String, Object> nodeData = new HashMap<>();
                        nodeData.put("input", nodeExec.getInputData());
                        nodeData.put("output", nodeExec.getOutputData());
                        nodeData.put("started_at", nodeExec.getStartedAt());
                        nodeData.put("completed_at", nodeExec.getCompletedAt());
                        nodeData.put("duration", nodeExec.getDuration());
                        if (nodeExec.getError() != null) {
                            nodeData.put("error", nodeExec.getError());
                        }
                        nodeInfo.setData(nodeData);
                    } else {
                        // Node not executed yet
                        nodeInfo.setStatus("pending");
                        nodeInfo.setData(new HashMap<>());
                    }
                    
                    nodeInfos.add(nodeInfo);
                }
            }
        }

        return nodeInfos;
    }

    /**
     * Reconstruct execution context at a specific step.
     * Reconstructs node outputs and variables from execution context and node executions.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> reconstructContext(Execution execution, 
                                                   List<NodeExecution> nodeExecutions, 
                                                   int step) {
        Map<String, Object> context = new HashMap<>();
        
        // Start with base execution context
        if (execution.getContext() != null) {
            context.putAll(execution.getContext());
        }

        // Reconstruct node outputs up to current step
        Map<String, Object> nodeOutputs = new HashMap<>();
        if (context.containsKey("nodeOutputs")) {
            nodeOutputs.putAll((Map<String, Object>) context.get("nodeOutputs"));
        }

        // Add node outputs from node executions up to current step
        for (int i = 0; i < step && i < nodeExecutions.size(); i++) {
            NodeExecution ne = nodeExecutions.get(i);
            if (ne.getOutputData() != null) {
                nodeOutputs.put(ne.getNodeId(), ne.getOutputData());
            }
        }
        context.put("nodeOutputs", nodeOutputs);

        // Reconstruct variables from node outputs if needed
        // Variables are typically stored in execution context, but we can also derive them
        // from node outputs if they're stored there

        return context;
    }
}

