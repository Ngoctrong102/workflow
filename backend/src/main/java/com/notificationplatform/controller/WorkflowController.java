package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateWorkflowRequest;
import com.notificationplatform.dto.request.ExecuteWorkflowRequest;
import com.notificationplatform.dto.request.UpdateWorkflowRequest;
import com.notificationplatform.dto.response.ExecutionResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowResponse;
import com.notificationplatform.dto.response.WorkflowTriggerResponse;
import com.notificationplatform.service.workflow.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflows")
@Tag(name = "Workflows", description = "Workflow management APIs - Create, update, and manage workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    @Operation(summary = "Create workflow", description = "Create a new workflow with nodes and edges definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workflow created successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        WorkflowResponse response = workflowService.createWorkflow(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workflow", description = "Get workflow by ID with full definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow found",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        WorkflowResponse response = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List workflows", description = "Get paginated list of workflows with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflows retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<WorkflowResponse>> listWorkflows(
            @Parameter(description = "Filter by status (draft, active, inactive, paused, archived)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by name or description") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 20, max: 100)") 
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") 
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<WorkflowResponse> response = workflowService.listWorkflows(status, search, limit, offset);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workflow", description = "Update workflow definition (creates new version)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow updated successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateWorkflowRequest request) {
        WorkflowResponse response = workflowService.updateWorkflow(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workflow", description = "Soft delete a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Workflow deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<Void> deleteWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute workflow", description = "Manually trigger workflow execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow execution started",
                    content = @Content(schema = @Schema(implementation = ExecutionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "422", description = "Invalid workflow definition")
    })
    public ResponseEntity<ExecutionResponse> executeWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id,
            @RequestBody(required = false) ExecuteWorkflowRequest request) {
        if (request == null) {
            request = new ExecuteWorkflowRequest();
        }
        ExecutionResponse response = workflowService.executeWorkflow(id, request);
        return ResponseEntity.ok(response);
    }

    // Trigger endpoints for workflows
    @GetMapping("/{workflowId}/triggers")
    @Operation(summary = "Get workflow triggers", description = "Get all trigger instances for a workflow with their configs and runtime states")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Triggers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowTriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<List<WorkflowTriggerResponse>> getWorkflowTriggers(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId) {
        List<WorkflowTriggerResponse> responses = workflowService.getWorkflowTriggers(workflowId);
        return ResponseEntity.ok(responses);
    }

    // Workflow version endpoints
    @GetMapping("/{id}/versions")
    @Operation(summary = "Get workflow versions", description = "Get all versions of a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow versions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<List<WorkflowResponse>> getWorkflowVersions(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        List<WorkflowResponse> responses = workflowService.getWorkflowVersions(id);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(summary = "Get workflow by version", description = "Get a specific version of a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow version found",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow or version not found")
    })
    public ResponseEntity<WorkflowResponse> getWorkflowByVersion(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id,
            @Parameter(description = "Version number", required = true) @PathVariable Integer version) {
        WorkflowResponse response = workflowService.getWorkflowByVersion(id, version);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate workflow", description = "Activate a workflow to enable execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow activated successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowResponse> activateWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        WorkflowResponse response = workflowService.activateWorkflow(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate workflow", description = "Deactivate a workflow to disable execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow deactivated successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowResponse> deactivateWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        WorkflowResponse response = workflowService.deactivateWorkflow(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause workflow", description = "Pause a workflow temporarily")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow paused successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowResponse> pauseWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        WorkflowResponse response = workflowService.pauseWorkflow(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume workflow", description = "Resume a paused workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow resumed successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowResponse> resumeWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id) {
        WorkflowResponse response = workflowService.resumeWorkflow(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "Rollback workflow", description = "Rollback workflow to a previous version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow rolled back successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow or version not found"),
            @ApiResponse(responseCode = "400", description = "Version is required")
    })
    public ResponseEntity<WorkflowResponse> rollbackWorkflow(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        Integer version = request.get("version");
        if (version == null) {
            throw new IllegalArgumentException("Version is required");
        }
        WorkflowResponse response = workflowService.rollbackWorkflow(id, version);
        return ResponseEntity.ok(response);
    }
}

