package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateTriggerConfigRequest;
import com.notificationplatform.dto.request.UpdateTriggerConfigRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.service.trigger.TriggerService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing trigger configs.
 * Trigger configs are independent and can be shared across multiple workflows.
 * 
 * This controller provides both:
 * - Management APIs (CRUD operations) at /triggers
 * - Registry APIs (read-only catalog) at /triggers/registry
 * 
 * Note: This controller must have higher priority than ApiTriggerController
 * to ensure /triggers endpoints are matched before /trigger/** wildcard.
 */
@RestController
@RequestMapping("/triggers")
@Tag(name = "Trigger Configs", description = "Trigger config management and registry APIs - Create, manage, and browse trigger configurations")
@org.springframework.core.annotation.Order(1) // Higher priority than ApiTriggerController
public class TriggerController {

    private final TriggerService triggerService;

    public TriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @PostMapping
    @Operation(summary = "Create trigger config", description = "Create a new trigger configuration that can be reused across multiple workflows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trigger config created successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TriggerResponse> createTriggerConfig(@Valid @RequestBody CreateTriggerConfigRequest request) {
        TriggerResponse response = triggerService.createTriggerConfig(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trigger config", description = "Get trigger config by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger config found",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trigger config not found")
    })
    public ResponseEntity<TriggerResponse> getTriggerConfig(
            @Parameter(description = "Trigger config ID", required = true) @PathVariable String id) {
        TriggerResponse response = triggerService.getTriggerConfigById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List trigger configs", description = "Get paginated list of trigger configs with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger configs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<TriggerResponse>> listTriggerConfigs(
            @Parameter(description = "Filter by trigger type (api-call, scheduler, event)") 
            @RequestParam(required = false) String triggerType,
            @Parameter(description = "Filter by status (active, inactive)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search query (searches in name and config)") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 20, max: 100)") 
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") 
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<TriggerResponse> responses = triggerService.listTriggerConfigs(triggerType, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trigger config", description = "Update trigger configuration. Changes apply to all workflows using this config.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger config updated successfully",
                    content = @Content(schema = @Schema(implementation = TriggerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trigger config not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TriggerResponse> updateTriggerConfig(
            @Parameter(description = "Trigger config ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateTriggerConfigRequest request) {
        TriggerResponse response = triggerService.updateTriggerConfig(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trigger config", description = "Delete a trigger config (soft delete). Does not affect workflows using it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trigger config deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trigger config not found")
    })
    public ResponseEntity<Void> deleteTriggerConfig(
            @Parameter(description = "Trigger config ID", required = true) @PathVariable String id) {
        triggerService.deleteTriggerConfig(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Registry APIs (backward compatibility)
    // These endpoints provide read-only access with Map response format
    // for frontend registry/catalog use cases
    // ============================================

    /**
     * Get all trigger configs from database (Registry format).
     * GET /triggers/registry
     * 
     * This endpoint provides backward compatibility with TriggerRegistryController.
     * Returns data in Map format with "triggers" key for registry/catalog use cases.
     */
    @GetMapping("/registry")
    @Operation(summary = "Get all trigger configs (Registry)", description = "Get all trigger configs from database in registry format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger configs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getAllTriggersRegistry(
            @Parameter(description = "Filter by status (active, inactive)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search query (searches in name and config)") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 100, max: 1000)") 
            @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Pagination offset (default: 0)") 
            @RequestParam(defaultValue = "0") int offset) {
        
        // Get trigger configs from database
        PagedResponse<TriggerResponse> pagedResponse = triggerService.listTriggerConfigs(null, status, search, limit, offset);
        
        // Format response to match expected structure
        List<Map<String, Object>> triggerResponses = pagedResponse.getData().stream()
                .map(this::formatTriggerResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("triggers", triggerResponses);
        response.put("total", pagedResponse.getTotal());
        response.put("limit", pagedResponse.getLimit());
        response.put("offset", pagedResponse.getOffset());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get trigger config by ID from database (Registry format).
     * GET /triggers/registry/{id}
     * 
     * This endpoint provides backward compatibility with TriggerRegistryController.
     */
    @GetMapping("/registry/{id}")
    @Operation(summary = "Get trigger config by ID (Registry)", description = "Get trigger config by ID from database in registry format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger config found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Trigger config not found")
    })
    public ResponseEntity<Map<String, Object>> getTriggerByIdRegistry(
            @Parameter(description = "Trigger config ID", required = true) @PathVariable String id) {
        
        try {
            TriggerResponse triggerResponse = triggerService.getTriggerConfigById(id);
            Map<String, Object> response = formatTriggerResponse(triggerResponse);
            return ResponseEntity.ok(response);
        } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get trigger configs by type from database (Registry format).
     * GET /triggers/registry/type/{type}
     * 
     * This endpoint provides backward compatibility with TriggerRegistryController.
     */
    @GetMapping("/registry/type/{type}")
    @Operation(summary = "Get trigger configs by type (Registry)", description = "Get trigger configs filtered by type from database in registry format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger configs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid trigger type")
    })
    public ResponseEntity<Map<String, Object>> getTriggersByTypeRegistry(
            @Parameter(description = "Trigger type (api-call, scheduler, event)", required = true) 
            @PathVariable String type,
            @Parameter(description = "Filter by status (active, inactive)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search query (searches in name and config)") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 100, max: 1000)") 
            @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "Pagination offset (default: 0)") 
            @RequestParam(defaultValue = "0") int offset) {
        
        // Validate trigger type
        TriggerType triggerType = TriggerType.fromValue(type);
        if (triggerType == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get trigger configs from database filtered by type
        PagedResponse<TriggerResponse> pagedResponse = triggerService.listTriggerConfigs(type, status, search, limit, offset);
        
        // Format response to match expected structure
        List<Map<String, Object>> triggerResponses = pagedResponse.getData().stream()
                .map(this::formatTriggerResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("triggers", triggerResponses);
        response.put("total", pagedResponse.getTotal());
        response.put("limit", pagedResponse.getLimit());
        response.put("offset", pagedResponse.getOffset());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Format TriggerResponse to match expected API response structure.
     * Used by registry endpoints to provide Map format instead of DTO format.
     */
    private Map<String, Object> formatTriggerResponse(TriggerResponse triggerResponse) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("id", triggerResponse.getId());
        triggerMap.put("name", triggerResponse.getName());
        triggerMap.put("triggerType", triggerResponse.getTriggerType());
        triggerMap.put("status", triggerResponse.getStatus());
        triggerMap.put("config", triggerResponse.getConfig());
        
        // Add error message if present
        if (triggerResponse.getErrorMessage() != null && !triggerResponse.getErrorMessage().isEmpty()) {
            triggerMap.put("errorMessage", triggerResponse.getErrorMessage());
        }
        
        // Add timestamps
        if (triggerResponse.getCreatedAt() != null) {
            triggerMap.put("createdAt", triggerResponse.getCreatedAt());
        }
        if (triggerResponse.getUpdatedAt() != null) {
            triggerMap.put("updatedAt", triggerResponse.getUpdatedAt());
        }
        
        return triggerMap;
    }
}
