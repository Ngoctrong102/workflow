package com.notificationplatform.controller;

import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowDashboardDTO;
import com.notificationplatform.dto.response.WorkflowErrorAnalysisDTO;
import com.notificationplatform.dto.response.WorkflowExecutionTrendDTO;
import com.notificationplatform.dto.response.WorkflowNodePerformanceDTO;
import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.service.dashboard.WorkflowDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workflows/{workflowId}/dashboard")
@Tag(name = "Workflow Dashboard", description = "Workflow dashboard APIs - Monitor and analyze individual workflow performance")
public class WorkflowDashboardController {

    private final WorkflowDashboardService dashboardService;

    public WorkflowDashboardController(WorkflowDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get workflow dashboard overview", description = "Get comprehensive dashboard overview with metrics, trends, and KPIs for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowDashboardDTO.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowDashboardDTO> getDashboardOverview(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Start date (ISO 8601, optional, default: 30 days ago)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601, optional, default: now)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Timezone (optional, default: UTC)") 
            @RequestParam(required = false, defaultValue = "UTC") String timezone) {
        WorkflowDashboardDTO dashboard = dashboardService.getDashboardOverview(workflowId, startDate, endDate, timezone);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/trends")
    @Operation(summary = "Get execution trends", description = "Get execution trends over time with configurable granularity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution trends retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowExecutionTrendDTO.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<List<WorkflowExecutionTrendDTO>> getExecutionTrends(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Start date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Granularity (hourly, daily, weekly, monthly, optional)") 
            @RequestParam(required = false) String granularity) {
        List<WorkflowExecutionTrendDTO> trends = dashboardService.getExecutionTrends(
                workflowId, startDate, endDate, granularity);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/nodes")
    @Operation(summary = "Get node performance", description = "Get performance metrics for each node in the workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Node performance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkflowNodePerformanceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<List<WorkflowNodePerformanceDTO>> getNodePerformance(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Start date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<WorkflowNodePerformanceDTO> nodePerformance = dashboardService.getNodePerformance(
                workflowId, startDate, endDate);
        return ResponseEntity.ok(nodePerformance);
    }

    @GetMapping("/executions")
    @Operation(summary = "Get execution history", description = "Get paginated execution history with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<PagedResponse<ExecutionStatusResponse>> getExecutionHistory(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Filter by status (optional)") @RequestParam(required = false) String status,
            @Parameter(description = "Start date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Filter by trigger type (optional)") @RequestParam(required = false) String triggerType,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ExecutionStatusResponse> executions = dashboardService.getExecutionHistory(
                workflowId, status, startDate, endDate, triggerType, limit, offset);
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/errors")
    @Operation(summary = "Get error analysis", description = "Get error analysis with breakdown by type, node, and execution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Error analysis retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<PagedResponse<WorkflowErrorAnalysisDTO>> getErrorAnalysis(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Start date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601, optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Filter by error type (optional)") @RequestParam(required = false) String errorType,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<WorkflowErrorAnalysisDTO> errors = dashboardService.getErrorAnalysis(
                workflowId, startDate, endDate, errorType, limit, offset);
        return ResponseEntity.ok(errors);
    }
}

