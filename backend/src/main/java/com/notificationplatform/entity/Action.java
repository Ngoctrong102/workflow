package com.notificationplatform.entity;

import com.notificationplatform.entity.enums.ActionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Action entity - Stores action definitions in the registry.
 * Actions must be defined here before they can be used in workflows.
 */
@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Action {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ActionType type;

    @Column(name = "action_type", length = 50)
    @Size(max = 50)
    private String actionType; // For custom actions (send-email, send-sms, etc.)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "config_template", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private Map<String, Object> configTemplate; // Configuration template

    @Column(name = "metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata; // Icon, color, version, etc.

    @Column(name = "version", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String version = "1.0.0";

    @Column(name = "enabled", nullable = false)
    @NotNull
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

