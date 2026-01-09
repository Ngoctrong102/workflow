package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CancelExecutionRequest;
import com.notificationplatform.dto.request.RetryExecutionRequest;
import com.notificationplatform.dto.response.ExecutionDetailResponse;
import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.execution.ExecutionService;
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

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
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
    public ResponseEntity<com.notificationplatform.dto.response.ExecutionVisualizationResponse> getExecutionVisualization(
            @PathVariable String id) {
        com.notificationplatform.dto.response.ExecutionVisualizationResponse response = 
            executionService.getExecutionVisualization(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/visualize/step")
    public ResponseEntity<com.notificationplatform.dto.response.VisualizeStepResponse> executeVisualizationStep(
            @PathVariable String id,
            @Valid @RequestBody com.notificationplatform.dto.request.VisualizeStepRequest request) {
        com.notificationplatform.dto.response.VisualizeStepResponse response = 
            executionService.executeVisualizationStep(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/visualize/step/{stepNumber}")
    public ResponseEntity<com.notificationplatform.dto.response.ExecutionVisualizationResponse> getExecutionStateAtStep(
            @PathVariable String id,
            @PathVariable Integer stepNumber) {
        com.notificationplatform.dto.response.ExecutionVisualizationResponse response = 
            executionService.getExecutionStateAtStep(id, stepNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/visualize/reset")
    public ResponseEntity<Void> resetVisualization(@PathVariable String id) {
        executionService.resetVisualization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/visualize/context")
    public ResponseEntity<Map<String, Object>> getVisualizationContext(@PathVariable String id) {
        Map<String, Object> context = executionService.getVisualizationContext(id);
        return ResponseEntity.ok(context);
    }
}

