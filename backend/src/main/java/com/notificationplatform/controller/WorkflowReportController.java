package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateWorkflowReportRequest;
import com.notificationplatform.dto.request.UpdateWorkflowReportRequest;
import com.notificationplatform.dto.request.ValidateQueryRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.QueryValidationResponse;
import com.notificationplatform.dto.response.WorkflowReportHistoryResponse;
import com.notificationplatform.dto.response.WorkflowReportPreviewResponse;
import com.notificationplatform.dto.response.WorkflowReportResponse;
import com.notificationplatform.service.workflowreport.WorkflowReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows/{workflowId}/report")
@Tag(name = "Workflow Reports", description = "Workflow report APIs - Configure and generate automated reports")
public class WorkflowReportController {

    private final WorkflowReportService workflowReportService;

    public WorkflowReportController(WorkflowReportService workflowReportService) {
        this.workflowReportService = workflowReportService;
    }

    @PostMapping
    @Operation(summary = "Create workflow report configuration", description = "Create a new report configuration for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report configuration created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowReportResponse> createWorkflowReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Valid @RequestBody CreateWorkflowReportRequest request) {
        WorkflowReportResponse response = workflowReportService.createWorkflowReport(workflowId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get workflow report configuration", description = "Get the report configuration for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report configuration retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<WorkflowReportResponse> getWorkflowReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId) {
        WorkflowReportResponse response = workflowReportService.getWorkflowReport(workflowId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update workflow report configuration", description = "Update the report configuration for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report configuration updated successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<WorkflowReportResponse> updateWorkflowReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Valid @RequestBody UpdateWorkflowReportRequest request) {
        WorkflowReportResponse response = workflowReportService.updateWorkflowReport(workflowId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Delete workflow report configuration", description = "Delete the report configuration for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Report configuration deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<Void> deleteWorkflowReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId) {
        workflowReportService.deleteWorkflowReport(workflowId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate analyst query", description = "Validate the SQL query syntax and safety")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Query validation result")
    })
    public ResponseEntity<QueryValidationResponse> validateQuery(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Valid @RequestBody ValidateQueryRequest request) {
        QueryValidationResponse response = workflowReportService.validateQuery(workflowId, request.getAnalystQuery());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate workflow report manually", description = "Manually trigger report generation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<WorkflowReportHistoryResponse> generateReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId) {
        WorkflowReportHistoryResponse response = workflowReportService.generateReport(workflowId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview workflow report", description = "Preview query results without generating a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report preview retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<WorkflowReportPreviewResponse> previewReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId) {
        WorkflowReportPreviewResponse response = workflowReportService.previewReport(workflowId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get workflow report history", description = "Get paginated list of generated reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report history retrieved successfully")
    })
    public ResponseEntity<PagedResponse<WorkflowReportHistoryResponse>> getReportHistory(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<WorkflowReportHistoryResponse> response = 
                workflowReportService.getReportHistory(workflowId, limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{reportId}/download")
    @Operation(summary = "Download workflow report", description = "Download a generated report file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report file downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<Resource> downloadReport(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Report history ID", required = true) @PathVariable String reportId) {
        Resource resource = workflowReportService.downloadReport(workflowId, reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PatchMapping("/status")
    @Operation(summary = "Update workflow report status", description = "Update the status of a report configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Report configuration not found")
    })
    public ResponseEntity<WorkflowReportResponse> updateReportStatus(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "New status (active, inactive, paused)", required = true) 
            @RequestParam String status) {
        WorkflowReportResponse response = workflowReportService.updateReportStatus(workflowId, status);
        return ResponseEntity.ok(response);
    }
}

