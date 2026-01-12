package com.notificationplatform.entity;

import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "triggers", indexes = {
    @Index(name = "idx_triggers_name", columnList = "name"),
    @Index(name = "idx_triggers_trigger_type", columnList = "trigger_type"),
    @Index(name = "idx_triggers_status", columnList = "status"),
    @Index(name = "idx_triggers_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"executions"})
@EqualsAndHashCode(exclude = {"executions"})
public class Trigger {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name; // User-friendly name for the trigger config

    @Column(name = "trigger_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TriggerType triggerType;

    @Column(name = "config", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private Map<String, Object> config; // Trigger-specific configuration

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TriggerStatus status = TriggerStatus.ACTIVE;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "trigger")
    private List<Execution> executions = new ArrayList<>();

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

