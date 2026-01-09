package com.notificationplatform.entity;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.entity.enums.ExecutionStatus;
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
@Table(name = "executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"workflow", "trigger", "nodeExecutions", "notifications"})
@EqualsAndHashCode(exclude = {"workflow", "trigger", "nodeExecutions", "notifications"})
public class Execution {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @NotNull
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id")
    private Trigger trigger;

    @Column(name = "trigger_node_id", length = 255)
    @Size(max = 255)
    private String triggerNodeId; // Node ID of trigger node

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    @Column(name = "started_at", nullable = false)
    @NotNull
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration")
    private Integer duration; // Duration in milliseconds

    @Column(name = "nodes_executed")
    private Integer nodesExecuted = ApplicationConstants.Defaults.NODES_EXECUTED_INITIAL;

    @Column(name = "notifications_sent")
    private Integer notificationsSent = ApplicationConstants.Defaults.NOTIFICATIONS_SENT_INITIAL;

    @Column(name = "context", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private Map<String, Object> context; // Full execution context (stored when paused/completed)

    @Column(name = "trigger_data", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> triggerData; // Data from trigger

    @Column(name = "workflow_metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> workflowMetadata; // Workflow metadata at execution time

    @Column(name = "execution_metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> executionMetadata; // Execution-specific metadata

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "error_details", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> errorDetails; // Detailed error information

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeExecution> nodeExecutions = new ArrayList<>();

    @OneToMany(mappedBy = "execution")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetrySchedule> retrySchedules = new ArrayList<>();

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExecutionWaitState> executionWaitStates = new ArrayList<>();

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

