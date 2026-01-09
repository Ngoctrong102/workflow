package com.notificationplatform.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.response.WorkflowExportResponse;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.WorkflowRepository;
import com.notificationplatform.service.workflow.WorkflowService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class WorkflowExportServiceImpl implements WorkflowExportService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;

    public WorkflowExportServiceImpl(WorkflowRepository workflowRepository,
                                    WorkflowService workflowService,
                                    ObjectMapper objectMapper) {
        this.workflowRepository = workflowRepository;
        this.workflowService = workflowService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowExportResponse exportWorkflow(String workflowId) {
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        WorkflowExportResponse response = new WorkflowExportResponse();
        response.setExportedAt(LocalDateTime.now());

        WorkflowExportResponse.WorkflowExport export = toExport(workflow);
        response.setWorkflows(List.of(export));

        log.info("Exported workflow: workflowId={}, name={}", workflowId, workflow.getName());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowExportResponse exportWorkflows(List<String> workflowIds) {
        List<Workflow> workflows = workflowIds.stream()
                .map(id -> workflowRepository.findByIdAndNotDeleted(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + id)))
                .collect(Collectors.toList());

        WorkflowExportResponse response = new WorkflowExportResponse();
        response.setExportedAt(LocalDateTime.now());

        List<WorkflowExportResponse.WorkflowExport> exports = workflows.stream()
                .map(this::toExport)
                .collect(Collectors.toList());

        response.setWorkflows(exports);

        log.info("Exported {} workflows", workflows.size());

        return response;
    }

    @Override
    public ImportResult importWorkflow(String jsonContent, ImportOptions options) {
        try {
            WorkflowExportResponse exportResponse = objectMapper.readValue(jsonContent, WorkflowExportResponse.class);

            if (exportResponse.getWorkflows() == null || exportResponse.getWorkflows().isEmpty()) {
                ImportResult result = new ImportResult();
                result.setSuccess(false);
                result.setMessage("No workflows found in export");
                return result;
            }

            // Import first workflow
            return importWorkflowData(exportResponse.getWorkflows().get(0), options);

        } catch (Exception e) {
            log.error("Error importing workflow", e);
            ImportResult result = new ImportResult();
            result.setSuccess(false);
            result.setMessage("Error parsing JSON: " + e.getMessage());
            return result;
        }
    }

    @Override
    public List<ImportResult> importWorkflows(String jsonContent, ImportOptions options) {
        List<ImportResult> results = new ArrayList<>();

        try {
            WorkflowExportResponse exportResponse = objectMapper.readValue(jsonContent, WorkflowExportResponse.class);

            if (exportResponse.getWorkflows() == null || exportResponse.getWorkflows().isEmpty()) {
                ImportResult result = new ImportResult();
                result.setSuccess(false);
                result.setMessage("No workflows found in export");
                results.add(result);
                return results;
            }

            for (WorkflowExportResponse.WorkflowExport export : exportResponse.getWorkflows()) {
                results.add(importWorkflowData(export, options));
            }

        } catch (Exception e) {
            log.error("Error importing workflows", e);
            ImportResult result = new ImportResult();
            result.setSuccess(false);
            result.setMessage("Error parsing JSON: " + e.getMessage());
            results.add(result);
        }

        return results;
    }

    private ImportResult importWorkflowData(WorkflowExportResponse.WorkflowExport export, ImportOptions options) {
        ImportResult result = new ImportResult();
        result.setWorkflowName(export.getName());

        try {
            // Validate if not skipping
            if (!options.isSkipValidation()) {
                validateWorkflowExport(export);
            }

            // Check for existing workflow with same name
            Optional<Workflow> existing = workflowRepository.findAllActive().stream()
                    .filter(w -> export.getName().equals(w.getName()))
                    .findFirst();

            if (existing.isPresent()) {
                // Handle conflict
                String resolution = options.getConflictResolution();
                if ("skip".equals(resolution)) {
                    result.setSuccess(false);
                    result.setAction("skipped");
                    result.setMessage("Workflow with name '" + export.getName() + "' already exists");
                    return result;
                } else if ("overwrite".equals(resolution) || options.isOverwriteExisting()) {
                    // Update existing workflow
                    Workflow workflow = existing.get();
                    workflow.setDescription(export.getDescription());
                    workflow.setDefinition(export.getDefinition());
                    workflow.setTags(export.getTags());
                    workflow.setVersion(workflow.getVersion() + 1);
                    workflow = workflowRepository.save(workflow);

                    result.setSuccess(true);
                    result.setWorkflowId(workflow.getId());
                    result.setAction("updated");
                    result.setMessage("Workflow updated successfully");

                    log.info("Imported workflow (updated): workflowId={}, name={}", workflow.getId(), workflow.getName());
                    return result;
                } else if ("rename".equals(resolution)) {
                    // Create with new name
                    export.setName(export.getName() + " (Imported)");
                }
            }

            // Create new workflow
            com.notificationplatform.dto.request.CreateWorkflowRequest createRequest =
                    new com.notificationplatform.dto.request.CreateWorkflowRequest();
            createRequest.setName(export.getName());
            createRequest.setDescription(export.getDescription());
            createRequest.setDefinition(export.getDefinition());
            createRequest.setTags(export.getTags());
            createRequest.setStatus("draft"); // Import as draft

            com.notificationplatform.dto.response.WorkflowResponse workflowResponse =
                    workflowService.createWorkflow(createRequest);

            result.setSuccess(true);
            result.setWorkflowId(workflowResponse.getId());
            result.setAction("created");
            result.setMessage("Workflow imported successfully");

            log.info("Imported workflow (created): workflowId={}, name={}", workflowResponse.getId(), export.getName());

        } catch (Exception e) {
            log.error("Error importing workflow: {}", export.getName(), e);
            result.setSuccess(false);
            result.setMessage("Error importing workflow: " + e.getMessage());
        }

        return result;
    }

    private void validateWorkflowExport(WorkflowExportResponse.WorkflowExport export) {
        if (export.getName() == null || export.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name is required");
        }

        if (export.getDefinition() == null) {
            throw new IllegalArgumentException("Workflow definition is required");
        }

        // Validate definition structure
        Map<String, Object> definition = (Map<String, Object>) export.getDefinition();
        if (!definition.containsKey("nodes")) {
            throw new IllegalArgumentException("Workflow definition must contain 'nodes'");
        }
    }

    private WorkflowExportResponse.WorkflowExport toExport(Workflow workflow) {
        WorkflowExportResponse.WorkflowExport export = new WorkflowExportResponse.WorkflowExport();
        export.setId(workflow.getId());
        export.setName(workflow.getName());
        export.setDescription(workflow.getDescription());
        export.setDefinition(workflow.getDefinition() != null ?
                (Map<String, Object>) workflow.getDefinition() : null);
        export.setStatus(workflow.getStatus() != null ? workflow.getStatus().getValue() : null);
        export.setVersion(workflow.getVersion());
        export.setTags(workflow.getTags());
        export.setCreatedAt(workflow.getCreatedAt());
        export.setUpdatedAt(workflow.getUpdatedAt());
        return export;
    }
}

