package com.notificationplatform.dto.request;

import com.notificationplatform.entity.enums.ActionType;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for updating an existing action definition in the registry.
 */
public class UpdateActionRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private ActionType type;

    private String description;

    private Map<String, Object> configTemplate;

    private Map<String, Object> metadata; // Icon, color, version, etc.

    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    private Boolean enabled;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConfigTemplate() {
        return configTemplate;
    }

    public void setConfigTemplate(Map<String, Object> configTemplate) {
        this.configTemplate = configTemplate;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

