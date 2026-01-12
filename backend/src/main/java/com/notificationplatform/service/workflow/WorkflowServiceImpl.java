package com.notificationplatform.service.workflow;

import com.notificationplatform.dto.mapper.WorkflowMapper;
import com.notificationplatform.dto.request.CreateWorkflowRequest;
import com.notificationplatform.dto.request.ExecuteWorkflowRequest;
import com.notificationplatform.dto.request.UpdateWorkflowRequest;
import com.notificationplatform.dto.response.ExecutionResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowResponse;
import com.notificationplatform.dto.response.WorkflowTriggerResponse;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.WorkflowRepository;
import com.notificationplatform.service.trigger.handler.TriggerHandlerRegistry;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowValidator workflowValidator;
    private final WorkflowExecutor workflowExecutor;
    private final TriggerRepository triggerRepository;
    private final TriggerHandlerRegistry triggerHandlerRegistry;

    public WorkflowServiceImpl(WorkflowRepository workflowRepository,
                              WorkflowMapper workflowMapper,
                              WorkflowValidator workflowValidator,
                              WorkflowExecutor workflowExecutor,
                              TriggerRepository triggerRepository,
                              TriggerHandlerRegistry triggerHandlerRegistry) {
        this.workflowRepository = workflowRepository;
        this.workflowMapper = workflowMapper;
        this.workflowValidator = workflowValidator;
        this.workflowExecutor = workflowExecutor;
        this.triggerRepository = triggerRepository;
        this.triggerHandlerRegistry = triggerHandlerRegistry;
    }

    @Override
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        // Validate request
        workflowValidator.validateCreateRequest(request);

        // Create entity
        Workflow workflow = workflowMapper.toEntity(request);

        // Validate workflow structure
        workflowValidator.validateWorkflow(workflow);

        // Save
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "workflows", key = "#id")
    public WorkflowResponse getWorkflowById(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        return workflowMapper.toResponse(workflow);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkflowResponse> listWorkflows(String status, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Workflow> workflows;
        long total;

        // Build query based on filters
        if (status != null && !status.isEmpty()) {
            workflows = workflowRepository.findByStatus(status);
            total = workflowRepository.countByStatus(status);
        } else {
            workflows = workflowRepository.findAllActive();
            total = workflowRepository.count();
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            workflows = workflows.stream()
                    .filter(w -> w.getName().toLowerCase().contains(searchLower) ||
                               (w.getDescription() != null && w.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
            total = workflows.size();
        }

        // Apply pagination
        int fromIndex = Math.min(offset, workflows.size());
        int toIndex = Math.min(offset + limit, workflows.size());
        List<Workflow> pagedWorkflows = workflows.subList(fromIndex, toIndex);

        List<WorkflowResponse> responses = workflowMapper.toResponseList(pagedWorkflows);
        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse updateWorkflow(String id, UpdateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Validate update request
        workflowValidator.validateUpdateRequest(workflow, request);

        // Update entity
        workflowMapper.updateEntity(workflow, request);

        // Validate updated workflow structure if definition changed
        if (request.getDefinition() != null) {
            workflowValidator.validateWorkflow(workflow);
        }

        // Save
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public void deleteWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Soft delete
        workflow.setDeletedAt(LocalDateTime.now());
        workflowRepository.save(workflow);
    }

    @Override
    public void validateWorkflow(Workflow workflow) {
        workflowValidator.validateWorkflow(workflow);
    }

    @Override
    public ExecutionResponse executeWorkflow(String workflowId, ExecuteWorkflowRequest request) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        if (!"active".equals(workflow.getStatus())) {
            throw new IllegalStateException("Workflow is not active: " + workflow.getStatus());
        }

        // Execute workflow
        Map<String, Object> triggerData = request.getData() != null ? request.getData() : new java.util.HashMap<>();
        Execution execution = workflowExecutor.execute(workflow, triggerData, "manual");

        // Map to response
        ExecutionResponse response = new ExecutionResponse();
        response.setExecutionId(execution.getId());
        response.setWorkflowId(workflowId);
        response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        response.setDuration(execution.getDuration());
        response.setError(execution.getError());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> getWorkflowVersions(String id) {
        // Verify workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        // Since there's no version history table, return current workflow as the only version
        // In the future, this could query a workflow_versions table if version history is implemented
        return List.of(workflowMapper.toResponse(workflow));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowByVersion(String id, Integer version) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));
        
        // Verify version matches current version
        // In the future, this could query a workflow_versions table if version history is implemented
        if (!workflow.getVersion().equals(version)) {
            throw new ResourceNotFoundException(
                    String.format("Workflow version %d not found for workflow id: %s. Current version is %d", 
                            version, id, workflow.getVersion()));
        }
        
        return workflowMapper.toResponse(workflow);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse activateWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Validate status transition
        if (workflow.getStatus() == com.notificationplatform.entity.enums.WorkflowStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot activate archived workflow");
        }

        workflow.setStatus(com.notificationplatform.entity.enums.WorkflowStatus.ACTIVE);
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // Update trigger instances runtime state to ACTIVE
        updateTriggerInstancesRuntimeState(workflow, "ACTIVE", null);
        
        // Activate triggers (register consumers for event triggers)
        activateWorkflowTriggers(workflow);
        
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse deactivateWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        workflow.setStatus(com.notificationplatform.entity.enums.WorkflowStatus.INACTIVE);
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // Update trigger instances runtime state to STOPPED
        updateTriggerInstancesRuntimeState(workflow, "STOPPED", null);
        
        // Deactivate triggers (unregister consumers for event triggers)
        deactivateWorkflowTriggers(workflow);
        
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse pauseWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Validate status transition
        if (workflow.getStatus() != com.notificationplatform.entity.enums.WorkflowStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause active workflow. Current status: " + workflow.getStatus());
        }

        workflow.setStatus(com.notificationplatform.entity.enums.WorkflowStatus.PAUSED);
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // Update trigger instances runtime state to PAUSED
        updateTriggerInstancesRuntimeState(workflow, "PAUSED", null);
        
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse resumeWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Validate status transition
        if (workflow.getStatus() != com.notificationplatform.entity.enums.WorkflowStatus.PAUSED) {
            throw new IllegalStateException("Can only resume paused workflow. Current status: " + workflow.getStatus());
        }

        workflow.setStatus(com.notificationplatform.entity.enums.WorkflowStatus.ACTIVE);
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // Update trigger instances runtime state to ACTIVE
        updateTriggerInstancesRuntimeState(workflow, "ACTIVE", null);
        
        Workflow saved = workflowRepository.save(workflow);
        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse rollbackWorkflow(String id, Integer version) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        // Verify version exists (currently only current version is available)
        if (!workflow.getVersion().equals(version)) {
            throw new ResourceNotFoundException(
                    String.format("Workflow version %d not found for workflow id: %s. Current version is %d",
                            version, id, workflow.getVersion()));
        }

        // Since we don't have version history table, rollback is a no-op
        // In the future, this would restore the workflow definition from the version history
        log.warn("Rollback requested for workflow {} to version {}, but version history is not implemented", id, version);

        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        return workflowMapper.toResponse(saved);
    }

    /**
     * Update runtime state for all trigger instances in workflow definition.
     * 
     * @param workflow Workflow entity
     * @param runtimeState New runtime state (ACTIVE, PAUSED, STOPPED, ERROR)
     * @param errorMessage Optional error message if state is ERROR
     */
    private void updateTriggerInstancesRuntimeState(Workflow workflow, String runtimeState, String errorMessage) {
        Map<String, Object> definition = workflow.getDefinition();
        if (definition == null || !definition.containsKey("nodes")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

        boolean updated = false;
        for (Map<String, Object> node : nodes) {
            Object typeObj = node.get("type");
            if (!(typeObj instanceof String)) {
                continue;
            }
            String typeStr = ((String) typeObj).toUpperCase();
            if (!NodeType.TRIGGER.name().equals(typeStr)) {
                continue;
            }

            // Get or create node data
            @SuppressWarnings("unchecked")
            Map<String, Object> nodeData = node.containsKey("data") ?
                    (Map<String, Object>) node.get("data") : new HashMap<>();
            
            // Update runtime state
            nodeData.put("runtimeState", runtimeState);
            if (errorMessage != null) {
                nodeData.put("errorMessage", errorMessage);
            } else {
                nodeData.remove("errorMessage");
            }
            
            node.put("data", nodeData);
            updated = true;
        }

        if (updated) {
            workflow.setDefinition(definition);
            workflow.setUpdatedAt(LocalDateTime.now());
            workflowRepository.save(workflow);
        }
    }

    /**
     * Activate all triggers in a workflow (register consumers for event triggers)
     */
    private void activateWorkflowTriggers(Workflow workflow) {
        Map<String, Object> definition = workflow.getDefinition();
        if (definition == null || !definition.containsKey("nodes")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

        for (Map<String, Object> node : nodes) {
            Object typeObj = node.get("type");
            if (!(typeObj instanceof String)) {
                continue;
            }
            String typeStr = ((String) typeObj).toUpperCase();
            if (!NodeType.TRIGGER.name().equals(typeStr)) {
                continue;
            }

            // Get triggerConfigId from node data
            @SuppressWarnings("unchecked")
            Map<String, Object> nodeData = node.containsKey("data") ?
                    (Map<String, Object>) node.get("data") : new HashMap<>();
            
            // Get triggerConfigId (support multiple locations)
            String triggerConfigId = (String) nodeData.get("triggerConfigId");
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) nodeData.get("config");
                if (config != null) {
                    triggerConfigId = (String) config.get("triggerConfigId");
                }
            }
            
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                log.warn("Trigger node has no triggerConfigId: workflowId={}, nodeId={}", 
                         workflow.getId(), node.get("id"));
                continue;
            }

            // Load trigger config from database
            Trigger triggerConfig = triggerRepository.findByIdAndNotDeleted(triggerConfigId).orElse(null);
            if (triggerConfig == null) {
                log.warn("Trigger config not found: triggerConfigId={}, workflowId={}", 
                         triggerConfigId, workflow.getId());
                continue;
            }

            // Activate trigger (set status to ACTIVE and call handler)
            try {
                triggerConfig.setStatus(TriggerStatus.ACTIVE);
                triggerRepository.save(triggerConfig);
                
                // Call trigger handler to register consumer
                triggerHandlerRegistry.handleActivate(triggerConfig);
                log.info("Activated trigger: triggerConfigId={}, workflowId={}", 
                         triggerConfigId, workflow.getId());
            } catch (Exception e) {
                log.error("Failed to activate trigger: triggerConfigId={}, workflowId={}", 
                          triggerConfigId, workflow.getId(), e);
            }
        }
    }

    /**
     * Deactivate all triggers in a workflow (unregister consumers for event triggers)
     */
    private void deactivateWorkflowTriggers(Workflow workflow) {
        Map<String, Object> definition = workflow.getDefinition();
        if (definition == null || !definition.containsKey("nodes")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

        for (Map<String, Object> node : nodes) {
            Object typeObj = node.get("type");
            if (!(typeObj instanceof String)) {
                continue;
            }
            String typeStr = ((String) typeObj).toUpperCase();
            if (!NodeType.TRIGGER.name().equals(typeStr)) {
                continue;
            }

            // Get triggerConfigId from node data
            @SuppressWarnings("unchecked")
            Map<String, Object> nodeData = node.containsKey("data") ?
                    (Map<String, Object>) node.get("data") : new HashMap<>();
            
            // Get triggerConfigId (support multiple locations)
            String triggerConfigId = (String) nodeData.get("triggerConfigId");
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) nodeData.get("config");
                if (config != null) {
                    triggerConfigId = (String) config.get("triggerConfigId");
                }
            }
            
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                continue;
            }

            // Load trigger config from database
            Trigger triggerConfig = triggerRepository.findByIdAndNotDeleted(triggerConfigId).orElse(null);
            if (triggerConfig == null) {
                continue;
            }

            // Deactivate trigger (call handler to unregister consumer)
            try {
                triggerHandlerRegistry.handleDeactivate(triggerConfig);
                log.info("Deactivated trigger: triggerConfigId={}, workflowId={}", 
                         triggerConfigId, workflow.getId());
            } catch (Exception e) {
                log.error("Failed to deactivate trigger: triggerConfigId={}, workflowId={}", 
                          triggerConfigId, workflow.getId(), e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowTriggerResponse> getWorkflowTriggers(String workflowId) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        Map<String, Object> definition = workflow.getDefinition();
        if (definition == null || !definition.containsKey("nodes")) {
            return new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

        // Extract trigger nodes
        List<Map<String, Object>> triggerNodes = nodes.stream()
                .filter(node -> {
                    Object typeObj = node.get("type");
                    if (!(typeObj instanceof String)) {
                        return false;
                    }
                    String typeStr = ((String) typeObj).toUpperCase();
                    return NodeType.TRIGGER.name().equals(typeStr);
                })
                .collect(Collectors.toList());

        List<WorkflowTriggerResponse> responses = new ArrayList<>();

        for (Map<String, Object> triggerNode : triggerNodes) {
            String nodeId = (String) triggerNode.get("id");
            if (nodeId == null) {
                log.warn("Trigger node has no ID: workflowId={}", workflowId);
                continue;
            }

            // Get node data
            @SuppressWarnings("unchecked")
            Map<String, Object> nodeData = triggerNode.containsKey("data") ?
                    (Map<String, Object>) triggerNode.get("data") : new HashMap<>();

            // Get triggerConfigId (required)
            // Support multiple locations: top level, nested config
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
            if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                log.warn("Trigger node has no triggerConfigId: workflowId={}, nodeId={}", workflowId, nodeId);
                continue;
            }

            // Load trigger config from database
            Trigger triggerConfig = triggerRepository.findByIdAndNotDeleted(triggerConfigId)
                    .orElse(null);
            if (triggerConfig == null) {
                log.warn("Trigger config not found: triggerConfigId={}, workflowId={}, nodeId={}", 
                         triggerConfigId, workflowId, nodeId);
                // Create response with ERROR state
                WorkflowTriggerResponse response = new WorkflowTriggerResponse();
                response.setNodeId(nodeId);
                response.setTriggerConfigId(triggerConfigId);
                response.setRuntimeState("ERROR");
                response.setErrorMessage("Trigger config not found: " + triggerConfigId);
                responses.add(response);
                continue;
            }

            // Get instance-specific overrides
            @SuppressWarnings("unchecked")
            Map<String, Object> instanceConfig = nodeData.containsKey("instanceConfig") ?
                    (Map<String, Object>) nodeData.get("instanceConfig") : new HashMap<>();

            // Get runtime state from node data (default to INITIALIZED)
            String runtimeState = (String) nodeData.getOrDefault("runtimeState", "INITIALIZED");
            String errorMessage = (String) nodeData.get("errorMessage");

            // Merge trigger config with instance-specific overrides
            Map<String, Object> mergedConfig = new HashMap<>();
            if (triggerConfig.getConfig() != null) {
                mergedConfig.putAll(triggerConfig.getConfig());
            }
            // Instance config overrides base config
            mergedConfig.putAll(instanceConfig);

            // Build response
            WorkflowTriggerResponse response = new WorkflowTriggerResponse();
            response.setNodeId(nodeId);
            response.setTriggerConfigId(triggerConfigId);
            response.setTriggerType(triggerConfig.getTriggerType());
            response.setTriggerConfig(mergedConfig);
            response.setInstanceConfig(instanceConfig);
            response.setRuntimeState(runtimeState);
            response.setErrorMessage(errorMessage);

            responses.add(response);
        }

        return responses;
    }
}

