package com.notificationplatform.entity;

import com.notificationplatform.entity.enums.NodeExecutionStatus;
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
 * NodeExecution entity - Stores individual node execution records with comprehensive data.
 */
@Entity
@Table(name = "node_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"execution"})
@EqualsAndHashCode(exclude = {"execution"})
public class NodeExecution {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    @NotNull
    private Execution execution;

    @Column(name = "node_id", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String nodeId; // Node identifier from workflow definition

    @Column(name = "node_label", length = 255)
    @Size(max = 255)
    private String nodeLabel; // Human-readable node label

    @Column(name = "node_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String nodeType; // trigger, logic, action

    @Column(name = "node_sub_type", length = 50)
    @Size(max = 50)
    private String nodeSubType; // api-call, condition, delay, etc.

    @Column(name = "registry_id", length = 255)
    @Size(max = 255)
    private String registryId; // Registry ID for trigger/action nodes

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private NodeExecutionStatus status;

    @Column(name = "started_at", nullable = false)
    @NotNull
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration")
    private Integer duration; // Duration in milliseconds

    @Column(name = "input_data", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private Map<String, Object> inputData; // Input data to the node

    @Column(name = "output_data", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> outputData; // Output data from the node

    @Column(name = "node_config", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> nodeConfig; // Node configuration at execution time

    @Column(name = "execution_context", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> executionContext; // Execution context available to node

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "error_details", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> errorDetails; // Detailed error information

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "retry_details", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> retryDetails; // Retry attempts information (Resilience4j)

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
