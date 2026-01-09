package com.notificationplatform.dto.response;

import com.notificationplatform.entity.enums.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Trigger Definition in Trigger Registry.
 * Trigger definitions are hardcoded in code and provide templates for trigger configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDefinition {

    /**
     * Unique trigger definition ID
     */
    private String id;

    /**
     * Trigger name
     */
    private String name;

    /**
     * Trigger type (api-call, scheduler, event)
     */
    private TriggerType type;

    /**
     * Trigger description
     */
    private String description;

    /**
     * Configuration template for trigger instances
     */
    private Map<String, Object> configTemplate;

    /**
     * Metadata (icon, color, version, etc.)
     */
    private Map<String, Object> metadata;
}

