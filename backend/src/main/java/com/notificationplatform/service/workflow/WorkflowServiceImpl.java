package com.notificationplatform.service.workflow;

import com.notificationplatform.dto.mapper.WorkflowMapper;
import com.notificationplatform.dto.request.CreateWorkflowRequest;
import com.notificationplatform.dto.request.ExecuteWorkflowRequest;
import com.notificationplatform.dto.request.UpdateWorkflowRequest;
import com.notificationplatform.dto.response.ExecutionResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowResponse;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final WorkflowScheduleSyncService scheduleSyncService;
    private final WorkflowEventTriggerSyncService eventTriggerSyncService;
    private final WorkflowTriggerSyncService triggerSyncService;

    public WorkflowServiceImpl(WorkflowRepository workflowRepository,
                              WorkflowMapper workflowMapper,
                              WorkflowValidator workflowValidator,
                              WorkflowExecutor workflowExecutor,
                              WorkflowScheduleSyncService scheduleSyncService,
                              WorkflowEventTriggerSyncService eventTriggerSyncService,
                              WorkflowTriggerSyncService triggerSyncService) {
        this.workflowRepository = workflowRepository;
        this.workflowMapper = workflowMapper;
        this.workflowValidator = workflowValidator;
        this.workflowExecutor = workflowExecutor;
        this.scheduleSyncService = scheduleSyncService;
        this.eventTriggerSyncService = eventTriggerSyncService;
        this.triggerSyncService = triggerSyncService;
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
        
        // Sync all triggers based on workflow status (in separate transaction)
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow creation if sync fails
        }
        
        // Sync schedule triggers from definition (in separate transaction)
        try {
            scheduleSyncService.syncScheduleTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync schedule triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow creation if sync fails
        }
        
        // Sync event triggers from definition (in separate transaction)
        try {
            eventTriggerSyncService.syncEventTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync event triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow creation if sync fails
        }
        
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
        
        // Sync all triggers based on workflow status (in separate transaction)
        // This ensures all triggers (including API triggers) are deactivated when workflow is paused/inactive
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow update if sync fails
        }
        
        // Sync schedule triggers from definition (in separate transaction)
        try {
            scheduleSyncService.syncScheduleTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync schedule triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow update if sync fails
        }
        
        // Sync event triggers from definition (in separate transaction)
        try {
            eventTriggerSyncService.syncEventTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync event triggers for workflow: workflowId={}", 
                        saved.getId(), e);
            // Don't fail workflow update if sync fails
        }
        
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
        Workflow saved = workflowRepository.save(workflow);

        // Sync triggers
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", saved.getId(), e);
        }

        return workflowMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "workflows", key = "#id")
    public WorkflowResponse deactivateWorkflow(String id) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id));

        workflow.setStatus(com.notificationplatform.entity.enums.WorkflowStatus.INACTIVE);
        workflow.setUpdatedAt(LocalDateTime.now());
        Workflow saved = workflowRepository.save(workflow);

        // Sync triggers to deactivate them
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", saved.getId(), e);
        }

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
        Workflow saved = workflowRepository.save(workflow);

        // Sync triggers to pause them
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", saved.getId(), e);
        }

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
        Workflow saved = workflowRepository.save(workflow);

        // Sync triggers to resume them
        try {
            triggerSyncService.syncAllTriggers(saved);
        } catch (Exception e) {
            log.error("Failed to sync triggers for workflow: workflowId={}", saved.getId(), e);
        }

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
}

