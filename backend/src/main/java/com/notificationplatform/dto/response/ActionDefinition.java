package com.notificationplatform.dto.response;

import com.notificationplatform.entity.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for Action Definition in Action Registry.
 * Used for API responses to match API specifications.
 * 
 * See: @import(api/endpoints.md#action-registry)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionDefinition {

    private String id;
    private String name;
    private ActionType type;
    private String description;
    private Map<String, Object> configTemplate;
    private Map<String, Object> metadata;
    private String version;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

