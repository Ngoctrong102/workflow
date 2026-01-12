package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateActionRequest;
import com.notificationplatform.dto.request.UpdateActionRequest;
import com.notificationplatform.dto.response.ActionDefinition;
import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import com.notificationplatform.exception.ValidationException;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.validator.ActionConfigTemplateValidator;
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
 * Controller for managing action definitions.
 * Actions are defined in the registry and can be reused across multiple workflows.
 * 
 * This controller provides both:
 * - Management APIs (CRUD operations) at /actions
 * - Registry APIs (backward compatibility) at /actions/registry
 * 
 * See: @import(api/endpoints.md#action-registry)
 */
@RestController
@RequestMapping("/actions")
@Tag(name = "Actions", description = "Action definition management and registry APIs - Create, manage, and browse action definitions")
public class ActionController {

    private final ActionRegistryService actionRegistryService;
    private final ActionConfigTemplateValidator configTemplateValidator;

    public ActionController(
            ActionRegistryService actionRegistryService,
            ActionConfigTemplateValidator configTemplateValidator) {
        this.actionRegistryService = actionRegistryService;
        this.configTemplateValidator = configTemplateValidator;
    }

    // ============================================
    // Management APIs
    // ============================================

    /**
     * Get all actions from registry.
     * GET /actions
     */
    @GetMapping
    @Operation(summary = "Get all actions", description = "Get all action definitions from registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getAllActions() {
        List<Action> actions = actionRegistryService.getAllActions();
        
        List<ActionDefinition> actionDefinitions = actions.stream()
                .map(this::mapToActionDefinition)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("actions", actionDefinitions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get action by ID.
     * GET /actions/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get action by ID", description = "Get action definition by ID from registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action found",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "404", description = "Action not found")
    })
    public ResponseEntity<ActionDefinition> getActionById(
            @Parameter(description = "Action ID", required = true) @PathVariable String id) {
        Action action = actionRegistryService.getActionById(id);
        ActionDefinition actionDefinition = mapToActionDefinition(action);
        return ResponseEntity.ok(actionDefinition);
    }

    /**
     * Get actions by type.
     * GET /actions/type/{type}
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get actions by type", description = "Get action definitions filtered by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid action type")
    })
    public ResponseEntity<Map<String, Object>> getActionsByType(
            @Parameter(description = "Action type (api-call, publish-event, function, custom-action)", required = true)
            @PathVariable String type) {
        ActionType actionType = ActionType.fromValue(type);
        
        if (actionType == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Action> actions = actionRegistryService.getActionsByType(actionType);
        
        List<ActionDefinition> actionDefinitions = actions.stream()
                .map(this::mapToActionDefinition)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("actions", actionDefinitions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get custom actions.
     * GET /actions/custom
     */
    @GetMapping("/custom")
    @Operation(summary = "Get custom actions", description = "Get all custom action definitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Custom actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getCustomActions() {
        List<Action> actions = actionRegistryService.getCustomActions();
        
        List<ActionDefinition> actionDefinitions = actions.stream()
                .map(this::mapToActionDefinition)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("actions", actionDefinitions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new action definition in registry.
     * POST /actions
     */
    @PostMapping
    @Operation(summary = "Create action", description = "Create a new action definition in registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Action created successfully",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<ActionDefinition> createAction(@Valid @RequestBody CreateActionRequest request) {
        // Validate config template structure based on action type
        if (request.getType() != null && request.getConfigTemplate() != null) {
            ActionConfigTemplateValidator.ValidationResult validationResult = 
                    configTemplateValidator.validate(request.getType(), request.getConfigTemplate());
            if (!validationResult.isValid()) {
                throw new ValidationException(validationResult.getErrorMessage());
            }
        }
        
        Action action = mapToAction(request);
        Action saved = actionRegistryService.registerAction(action);
        ActionDefinition response = mapToActionDefinition(saved);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing action definition in registry.
     * PUT /actions/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update action", description = "Update an existing action definition in registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action updated successfully",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "404", description = "Action not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<ActionDefinition> updateAction(
            @Parameter(description = "Action ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateActionRequest request) {
        // Validate config template structure if provided (partial update)
        // Need to get existing action to determine type if type is not provided
        ActionType actionType = request.getType();
        if (actionType == null && request.getConfigTemplate() != null) {
            // Get existing action to determine type
            Action existing = actionRegistryService.getActionById(id);
            actionType = existing.getType();
        }
        
        if (actionType != null && request.getConfigTemplate() != null) {
            ActionConfigTemplateValidator.ValidationResult validationResult = 
                    configTemplateValidator.validate(actionType, request.getConfigTemplate());
            if (!validationResult.isValid()) {
                throw new ValidationException(validationResult.getErrorMessage());
            }
        }
        
        Action updatedAction = mapToAction(id, request);
        Action saved = actionRegistryService.updateAction(id, updatedAction);
        ActionDefinition response = mapToActionDefinition(saved);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an action definition from registry.
     * DELETE /actions/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete action", description = "Delete an action definition from registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Action deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Action not found")
    })
    public ResponseEntity<Void> deleteAction(
            @Parameter(description = "Action ID", required = true) @PathVariable String id) {
        actionRegistryService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Registry APIs (backward compatibility)
    // These endpoints provide backward compatibility with ActionRegistryController
    // ============================================

    /**
     * Get all actions from registry (Registry format).
     * GET /actions/registry
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @GetMapping("/registry")
    @Operation(summary = "Get all actions (Registry)", description = "Get all action definitions from registry (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getAllActionsRegistry() {
        return getAllActions();
    }

    /**
     * Get action by ID (Registry format).
     * GET /actions/registry/{id}
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @GetMapping("/registry/{id}")
    @Operation(summary = "Get action by ID (Registry)", description = "Get action definition by ID from registry (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action found",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "404", description = "Action not found")
    })
    public ResponseEntity<ActionDefinition> getActionByIdRegistry(
            @Parameter(description = "Action ID", required = true) @PathVariable String id) {
        return getActionById(id);
    }

    /**
     * Get actions by type (Registry format).
     * GET /actions/registry/type/{type}
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @GetMapping("/registry/type/{type}")
    @Operation(summary = "Get actions by type (Registry)", description = "Get action definitions filtered by type (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid action type")
    })
    public ResponseEntity<Map<String, Object>> getActionsByTypeRegistry(
            @Parameter(description = "Action type (api-call, publish-event, function, custom-action)", required = true)
            @PathVariable String type) {
        return getActionsByType(type);
    }

    /**
     * Get custom actions (Registry format).
     * GET /actions/registry/custom
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @GetMapping("/registry/custom")
    @Operation(summary = "Get custom actions (Registry)", description = "Get all custom action definitions (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Custom actions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getCustomActionsRegistry() {
        return getCustomActions();
    }

    /**
     * Create a new action definition in registry (Registry format).
     * POST /actions/registry
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @PostMapping("/registry")
    @Operation(summary = "Create action (Registry)", description = "Create a new action definition in registry (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Action created successfully",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<ActionDefinition> createActionRegistry(@Valid @RequestBody CreateActionRequest request) {
        return createAction(request);
    }

    /**
     * Update an existing action definition in registry (Registry format).
     * PUT /actions/registry/{id}
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @PutMapping("/registry/{id}")
    @Operation(summary = "Update action (Registry)", description = "Update an existing action definition in registry (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action updated successfully",
                    content = @Content(schema = @Schema(implementation = ActionDefinition.class))),
            @ApiResponse(responseCode = "404", description = "Action not found"),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<ActionDefinition> updateActionRegistry(
            @Parameter(description = "Action ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateActionRequest request) {
        return updateAction(id, request);
    }

    /**
     * Delete an action definition from registry (Registry format).
     * DELETE /actions/registry/{id}
     * 
     * This endpoint provides backward compatibility with ActionRegistryController.
     */
    @DeleteMapping("/registry/{id}")
    @Operation(summary = "Delete action (Registry)", description = "Delete an action definition from registry (backward compatibility)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Action deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Action not found")
    })
    public ResponseEntity<Void> deleteActionRegistry(
            @Parameter(description = "Action ID", required = true) @PathVariable String id) {
        return deleteAction(id);
    }

    // ============================================
    // Helper methods
    // ============================================

    private Action mapToAction(CreateActionRequest request) {
        Action action = new Action();
        action.setId(request.getId());
        action.setName(request.getName());
        action.setType(request.getType());
        action.setDescription(request.getDescription());
        action.setConfigTemplate(request.getConfigTemplate());
        action.setMetadata(request.getMetadata());
        action.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        action.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        return action;
    }

    private Action mapToAction(String id, UpdateActionRequest request) {
        Action action = new Action();
        action.setId(id);
        if (request.getName() != null) {
            action.setName(request.getName());
        }
        if (request.getType() != null) {
            action.setType(request.getType());
        }
        if (request.getDescription() != null) {
            action.setDescription(request.getDescription());
        }
        if (request.getConfigTemplate() != null) {
            action.setConfigTemplate(request.getConfigTemplate());
        }
        if (request.getMetadata() != null) {
            action.setMetadata(request.getMetadata());
        }
        if (request.getVersion() != null) {
            action.setVersion(request.getVersion());
        }
        if (request.getEnabled() != null) {
            action.setEnabled(request.getEnabled());
        }
        return action;
    }

    private ActionDefinition mapToActionDefinition(Action action) {
        return ActionDefinition.builder()
                .id(action.getId())
                .name(action.getName())
                .type(action.getType())
                .description(action.getDescription())
                .configTemplate(action.getConfigTemplate())
                .metadata(action.getMetadata())
                .version(action.getVersion())
                .enabled(action.getEnabled())
                .createdAt(action.getCreatedAt())
                .updatedAt(action.getUpdatedAt())
                .build();
    }
}

