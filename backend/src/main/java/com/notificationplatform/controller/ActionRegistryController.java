package com.notificationplatform.controller;

import com.notificationplatform.dto.response.ActionDefinition;
import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import com.notificationplatform.service.registry.ActionRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Action Registry API.
 * Provides access to available action definitions.
 * 
 * See: @import(api/endpoints.md#action-registry)
 */
@RestController
@RequestMapping("/actions/registry")
public class ActionRegistryController {

    private final ActionRegistryService actionRegistryService;

    public ActionRegistryController(ActionRegistryService actionRegistryService) {
        this.actionRegistryService = actionRegistryService;
    }

    /**
     * Get all actions from registry.
     * GET /actions/registry
     */
    @GetMapping
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
     * GET /actions/registry/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActionDefinition> getActionById(@PathVariable String id) {
        Action action = actionRegistryService.getActionById(id);
        ActionDefinition actionDefinition = mapToActionDefinition(action);
        return ResponseEntity.ok(actionDefinition);
    }

    /**
     * Get actions by type.
     * GET /actions/registry/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getActionsByType(@PathVariable String type) {
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
     * GET /actions/registry/custom
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomActions() {
        List<Action> actions = actionRegistryService.getCustomActions();
        
        List<ActionDefinition> actionDefinitions = actions.stream()
                .map(this::mapToActionDefinition)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("actions", actionDefinitions);
        
        return ResponseEntity.ok(response);
    }

    private ActionDefinition mapToActionDefinition(Action action) {
        return ActionDefinition.builder()
                .id(action.getId())
                .name(action.getName())
                .type(action.getType())
                .actionType(action.getActionType())
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

