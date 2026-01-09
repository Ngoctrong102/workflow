package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CancelExecutionRequest;
import com.notificationplatform.dto.request.RetryExecutionRequest;
import com.notificationplatform.dto.request.VisualizeStepRequest;
import com.notificationplatform.dto.response.ExecutionDetailResponse;
import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.dto.response.ExecutionVisualizationResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.VisualizeStepResponse;
import com.notificationplatform.service.execution.ExecutionService;
import com.notificationplatform.service.visualization.ExecutionVisualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/executions")
@Tag(name = "Executions", description = "Workflow execution management APIs - Track and manage workflow executions")
public class ExecutionController {

    private final ExecutionService executionService;
    private final ExecutionVisualizationService visualizationService;

    public ExecutionController(ExecutionService executionService,
                               ExecutionVisualizationService visualizationService) {
        this.executionService = executionService;
        this.visualizationService = visualizationService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get execution status", description = "Get execution status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution found",
                    content = @Content(schema = @Schema(implementation = ExecutionStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<ExecutionStatusResponse> getExecutionStatus(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id) {
        ExecutionStatusResponse response = executionService.getExecutionStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Get execution detail", description = "Get detailed execution information including node executions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution detail retrieved",
                    content = @Content(schema = @Schema(implementation = ExecutionDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<ExecutionDetailResponse> getExecutionDetail(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id) {
        ExecutionDetailResponse response = executionService.getExecutionDetail(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List executions", description = "Get paginated list of executions with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Executions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<ExecutionStatusResponse>> listExecutions(
            @Parameter(description = "Filter by workflow ID") @RequestParam(required = false) String workflowId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Start date filter (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date filter (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ExecutionStatusResponse> responses = executionService.listExecutions(
                workflowId, status, startDate, endDate, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<ExecutionDetailResponse.ExecutionLog>> getExecutionLogs(
            @PathVariable String id,
            @RequestParam(required = false) String nodeId,
            @RequestParam(required = false) String level) {
        List<ExecutionDetailResponse.ExecutionLog> logs = executionService.getExecutionLogs(id, nodeId, level);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}/context")
    public ResponseEntity<Map<String, Object>> getExecutionContext(@PathVariable String id) {
        Map<String, Object> context = executionService.getExecutionContext(id);
        return ResponseEntity.ok(context);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelExecution(
            @PathVariable String id,
            @Valid @RequestBody CancelExecutionRequest request) {
        executionService.cancelExecution(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ExecutionStatusResponse> retryExecution(
            @PathVariable String id,
            @Valid @RequestBody RetryExecutionRequest request) {
        ExecutionStatusResponse response = executionService.retryExecution(id, request);
        return ResponseEntity.ok(response);
    }

    // Visualization endpoints
    @GetMapping("/{id}/visualize")
    @Operation(summary = "Get execution for visualization", 
               description = "Load execution data for step-by-step visualization and debugging")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution visualization data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExecutionVisualizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<ExecutionVisualizationResponse> getExecutionVisualization(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id) {
        ExecutionVisualizationResponse response = visualizationService.loadExecutionForVisualization(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/visualize/step")
    @Operation(summary = "Execute next step", 
               description = "Execute next step in visualization (forward or backward)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step executed successfully",
                    content = @Content(schema = @Schema(implementation = VisualizeStepResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<VisualizeStepResponse> executeVisualizationStep(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id,
            @Valid @RequestBody VisualizeStepRequest request) {
        VisualizeStepResponse response = visualizationService.executeNextStep(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/visualize/step/{stepNumber}")
    @Operation(summary = "Get execution state at step", 
               description = "Get execution state at a specific step number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution state retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExecutionVisualizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<ExecutionVisualizationResponse> getExecutionStateAtStep(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id,
            @Parameter(description = "Step number", required = true) @PathVariable Integer stepNumber) {
        ExecutionVisualizationResponse response = visualizationService.getExecutionStateAtStep(id, stepNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/visualize/reset")
    @Operation(summary = "Reset visualization", 
               description = "Reset visualization to initial state (step 0)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Visualization reset successfully"),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<Void> resetVisualization(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id) {
        visualizationService.resetVisualization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/visualize/context")
    @Operation(summary = "Get current context", 
               description = "Get current execution context at the current visualization step")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Context retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Execution not found")
    })
    public ResponseEntity<Map<String, Object>> getVisualizationContext(
            @Parameter(description = "Execution ID", required = true) @PathVariable String id) {
        Map<String, Object> context = visualizationService.getCurrentContext(id);
        return ResponseEntity.ok(context);
    }
}

