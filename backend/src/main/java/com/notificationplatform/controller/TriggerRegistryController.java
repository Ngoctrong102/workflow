package com.notificationplatform.controller;

import com.notificationplatform.dto.response.TriggerDefinition;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.service.registry.TriggerRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for Trigger Registry API.
 * Provides access to available trigger definitions.
 * 
 * See: @import(api/endpoints.md#trigger-registry)
 */
@RestController
@RequestMapping("/triggers/registry")
public class TriggerRegistryController {

    private final TriggerRegistryService triggerRegistryService;

    public TriggerRegistryController(TriggerRegistryService triggerRegistryService) {
        this.triggerRegistryService = triggerRegistryService;
    }

    /**
     * Get all trigger definitions from registry.
     * GET /triggers/registry
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTriggers() {
        List<TriggerDefinition> triggers = triggerRegistryService.getAllTriggers();
        
        // Convert TriggerType enum to string value for API response
        List<Map<String, Object>> triggerResponses = triggers.stream()
                .map(trigger -> {
                    Map<String, Object> triggerMap = new HashMap<>();
                    triggerMap.put("id", trigger.getId());
                    triggerMap.put("name", trigger.getName());
                    triggerMap.put("type", trigger.getType() != null ? trigger.getType().getValue() : null);
                    triggerMap.put("description", trigger.getDescription());
                    triggerMap.put("configTemplate", trigger.getConfigTemplate());
                    triggerMap.put("metadata", trigger.getMetadata());
                    // Add version from metadata if available
                    if (trigger.getMetadata() != null && trigger.getMetadata().containsKey("version")) {
                        triggerMap.put("version", trigger.getMetadata().get("version"));
                    }
                    // Add enabled flag (all registry triggers are enabled)
                    triggerMap.put("enabled", true);
                    return triggerMap;
                })
                .collect(java.util.stream.Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("triggers", triggerResponses);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get trigger definition by ID.
     * GET /triggers/registry/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTriggerById(@PathVariable String id) {
        Optional<TriggerDefinition> triggerOpt = triggerRegistryService.getTriggerById(id);
        
        if (triggerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TriggerDefinition trigger = triggerOpt.get();
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("id", trigger.getId());
        triggerMap.put("name", trigger.getName());
        triggerMap.put("type", trigger.getType() != null ? trigger.getType().getValue() : null);
        triggerMap.put("description", trigger.getDescription());
        triggerMap.put("configTemplate", trigger.getConfigTemplate());
        triggerMap.put("metadata", trigger.getMetadata());
        if (trigger.getMetadata() != null && trigger.getMetadata().containsKey("version")) {
            triggerMap.put("version", trigger.getMetadata().get("version"));
        }
        triggerMap.put("enabled", true);
        
        return ResponseEntity.ok(triggerMap);
    }

    /**
     * Get trigger definitions by type.
     * GET /triggers/registry/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getTriggersByType(@PathVariable String type) {
        TriggerType triggerType = TriggerType.fromValue(type);
        
        if (triggerType == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<TriggerDefinition> triggers = triggerRegistryService.getTriggerByType(triggerType);
        
        // Convert TriggerType enum to string value for API response
        List<Map<String, Object>> triggerResponses = triggers.stream()
                .map(trigger -> {
                    Map<String, Object> triggerMap = new HashMap<>();
                    triggerMap.put("id", trigger.getId());
                    triggerMap.put("name", trigger.getName());
                    triggerMap.put("type", trigger.getType() != null ? trigger.getType().getValue() : null);
                    triggerMap.put("description", trigger.getDescription());
                    triggerMap.put("configTemplate", trigger.getConfigTemplate());
                    triggerMap.put("metadata", trigger.getMetadata());
                    if (trigger.getMetadata() != null && trigger.getMetadata().containsKey("version")) {
                        triggerMap.put("version", trigger.getMetadata().get("version"));
                    }
                    triggerMap.put("enabled", true);
                    return triggerMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("triggers", triggerResponses);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get configuration template for a trigger definition.
     * GET /triggers/registry/{id}/config-template
     */
    @GetMapping("/{id}/config-template")
    public ResponseEntity<Map<String, Object>> getConfigTemplate(@PathVariable String id) {
        Map<String, Object> template = triggerRegistryService.getConfigTemplate(id);
        
        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(template);
    }
}

