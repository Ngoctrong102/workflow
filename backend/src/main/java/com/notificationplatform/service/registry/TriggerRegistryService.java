package com.notificationplatform.service.registry;

import com.notificationplatform.dto.response.TriggerDefinition;
import com.notificationplatform.entity.enums.TriggerType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Trigger Registry management.
 * Trigger definitions are hardcoded in code and provide templates for trigger configuration.
 */
public interface TriggerRegistryService {

    /**
     * Get all trigger definitions from registry.
     * @return List of all trigger definitions
     */
    List<TriggerDefinition> getAllTriggers();

    /**
     * Get trigger definition by ID.
     * @param id Trigger definition ID
     * @return Trigger definition if found
     */
    Optional<TriggerDefinition> getTriggerById(String id);

    /**
     * Get trigger definitions by type.
     * @param type Trigger type
     * @return List of trigger definitions matching the type
     */
    List<TriggerDefinition> getTriggerByType(TriggerType type);

    /**
     * Get configuration template for a trigger definition.
     * @param id Trigger definition ID
     * @return Configuration template
     */
    Map<String, Object> getConfigTemplate(String id);
}

