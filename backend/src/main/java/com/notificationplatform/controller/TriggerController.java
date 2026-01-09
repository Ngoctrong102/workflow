package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.dto.request.UpdateTriggerRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.service.trigger.TriggerService;
import com.notificationplatform.service.trigger.api.TriggerInstanceService;
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


@RestController
@RequestMapping("/triggers")
@Tag(name = "Triggers", description = "Trigger management APIs - Create and manage workflow triggers")
public class TriggerController {

    private final TriggerService triggerService;
    private final TriggerInstanceService triggerInstanceService;

    public TriggerController(TriggerService triggerService, TriggerInstanceService triggerInstanceService) {
        this.triggerService = triggerService;
        this.triggerInstanceService = triggerInstanceService;
    }

    @PostMapping("/api")
    @Operation(summary = "Create API trigger", description = "Create an API trigger for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API trigger created successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "409", description = "Trigger path already exists"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TriggerResponse> createApiTrigger(@Valid @RequestBody CreateApiTriggerRequest request) {
        TriggerResponse response = triggerService.createApiTrigger(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/schedule")
    @Operation(summary = "Create schedule trigger", description = "Create a schedule trigger for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule trigger created successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "422", description = "Invalid cron expression or validation error")
    })
    public ResponseEntity<TriggerResponse> createScheduleTrigger(@Valid @RequestBody CreateScheduleTriggerRequest request) {
        TriggerResponse response = triggerService.createScheduleTrigger(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/file")
    @Operation(summary = "Create file trigger", description = "Create a file trigger for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File trigger created successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TriggerResponse> createFileTrigger(@Valid @RequestBody CreateFileTriggerRequest request) {
        TriggerResponse response = triggerService.createFileTrigger(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/event")
    @Operation(summary = "Create event trigger", description = "Create an event (Kafka) trigger for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event trigger created successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "422", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Failed to connect to message queue")
    })
    public ResponseEntity<TriggerResponse> createEventTrigger(@Valid @RequestBody CreateEventTriggerRequest request) {
        TriggerResponse response = triggerService.createEventTrigger(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trigger", description = "Get trigger by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger found",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trigger not found")
    })
    public ResponseEntity<TriggerResponse> getTrigger(
            @Parameter(description = "Trigger ID", required = true) @PathVariable String id) {
        TriggerResponse response = triggerService.getTriggerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List triggers", description = "Get paginated list of triggers with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Triggers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<TriggerResponse>> listTriggers(
            @Parameter(description = "Filter by workflow ID") @RequestParam(required = false) String workflowId,
            @Parameter(description = "Filter by trigger type") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<TriggerResponse> responses = triggerService.listTriggersPaged(workflowId, type, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trigger", description = "Update trigger configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger updated successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trigger not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TriggerResponse> updateTrigger(
            @Parameter(description = "Trigger ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateTriggerRequest request) {
        TriggerResponse response = triggerService.updateTrigger(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trigger", description = "Delete a trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trigger deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trigger not found")
    })
    public ResponseEntity<Void> deleteTrigger(
            @Parameter(description = "Trigger ID", required = true) @PathVariable String id) {
        triggerService.deleteTrigger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<TriggerResponse> activateTrigger(@PathVariable String id) {
        TriggerResponse response = triggerService.activateTrigger(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<TriggerResponse> deactivateTrigger(@PathVariable String id) {
        TriggerResponse response = triggerService.deactivateTrigger(id);
        return ResponseEntity.ok(response);
    }

    // Trigger instance lifecycle endpoints
    @PostMapping("/{id}/initialize")
    public ResponseEntity<Void> initializeTrigger(@PathVariable String id) {
        triggerInstanceService.initializeTrigger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startTrigger(@PathVariable String id) {
        triggerInstanceService.startTrigger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pauseTrigger(@PathVariable String id) {
        triggerInstanceService.pauseTrigger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeTrigger(@PathVariable String id) {
        triggerInstanceService.resumeTrigger(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopTrigger(@PathVariable String id) {
        triggerInstanceService.stopTrigger(id);
        return ResponseEntity.noContent().build();
    }
}

