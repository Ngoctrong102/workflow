package com.notificationplatform.controller;

import com.notificationplatform.dto.response.ChannelAnalyticsResponse;
import com.notificationplatform.dto.response.DeliveryAnalyticsResponse;
import com.notificationplatform.dto.response.ErrorAnalyticsResponse;
import com.notificationplatform.dto.response.WorkflowAnalyticsResponse;
import com.notificationplatform.service.analytics.AnalyticsService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Analytics APIs - Get workflow, delivery, and channel analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/workflows/{workflowId}")
    @Operation(summary = "Get workflow analytics", description = "Get analytics for a specific workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow analytics retrieved",
                    content = @Content(schema = @Schema(implementation = WorkflowAnalyticsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<WorkflowAnalyticsResponse> getWorkflowAnalytics(
            @Parameter(description = "Workflow ID", required = true) @PathVariable String workflowId,
            @Parameter(description = "Start date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        WorkflowAnalyticsResponse response = analyticsService.getWorkflowAnalytics(workflowId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workflows")
    @Operation(summary = "Get all workflows analytics", description = "Get analytics for all workflows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflows analytics retrieved",
                    content = @Content(schema = @Schema(implementation = WorkflowAnalyticsResponse.class)))
    })
    public ResponseEntity<List<WorkflowAnalyticsResponse>> getAllWorkflowsAnalytics(
            @Parameter(description = "Start date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WorkflowAnalyticsResponse> responses = analyticsService.getAllWorkflowsAnalytics(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Get delivery analytics", description = "Get delivery analytics across all channels or filtered by channel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery analytics retrieved",
                    content = @Content(schema = @Schema(implementation = DeliveryAnalyticsResponse.class)))
    })
    public ResponseEntity<DeliveryAnalyticsResponse> getDeliveryAnalytics(
            @Parameter(description = "Start date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by channel type") @RequestParam(required = false) String channel) {
        DeliveryAnalyticsResponse response = analyticsService.getDeliveryAnalytics(startDate, endDate, channel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/channels")
    @Operation(summary = "Get channel analytics", description = "Get analytics for all channels")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Channel analytics retrieved",
                    content = @Content(schema = @Schema(implementation = ChannelAnalyticsResponse.class)))
    })
    public ResponseEntity<List<ChannelAnalyticsResponse>> getChannelAnalytics(
            @Parameter(description = "Start date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ChannelAnalyticsResponse> responses = analyticsService.getAllChannelsAnalytics(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/errors")
    @Operation(summary = "Get error analytics", description = "Get error analytics with breakdown by type, workflow, channel, and node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Error analytics retrieved",
                    content = @Content(schema = @Schema(implementation = ErrorAnalyticsResponse.class)))
    })
    public ResponseEntity<ErrorAnalyticsResponse> getErrorAnalytics(
            @Parameter(description = "Start date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by workflow ID") 
            @RequestParam(required = false) String workflowId,
            @Parameter(description = "Filter by error type") 
            @RequestParam(required = false) String errorType) {
        ErrorAnalyticsResponse response = analyticsService.getErrorAnalytics(startDate, endDate, workflowId, errorType);
        return ResponseEntity.ok(response);
    }
}

