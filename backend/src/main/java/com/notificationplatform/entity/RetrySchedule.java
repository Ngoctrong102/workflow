package com.notificationplatform.entity;

import com.notificationplatform.entity.enums.RetryStatus;
import com.notificationplatform.entity.enums.RetryStrategy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RetrySchedule entity - Stores retry tasks for failed node executions and executions.
 * Supports long-term retries (days, weeks) and is designed for multi-instance deployment.
 */
@Entity
@Table(name = "retry_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"execution"})
@EqualsAndHashCode(exclude = {"execution"})
public class RetrySchedule {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "retry_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String retryType; // 'node_execution', 'execution'

    @Column(name = "target_id", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String targetId; // node_execution_id or execution_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    @NotNull
    private Execution execution;

    @Column(name = "node_id", length = 255)
    @Size(max = 255)
    private String nodeId; // Node ID (for node_execution retry)

    @Column(name = "retry_strategy", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RetryStrategy retryStrategy;

    @Column(name = "max_attempts", nullable = false)
    @NotNull
    private Integer maxAttempts = 3;

    @Column(name = "current_attempt", nullable = false)
    @NotNull
    private Integer currentAttempt = 0;

    @Column(name = "initial_delay_seconds")
    private Integer initialDelaySeconds = 0;

    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    @Column(name = "multiplier", precision = 5, scale = 2)
    private BigDecimal multiplier = BigDecimal.valueOf(2.0);

    @Column(name = "max_delay_seconds")
    private Integer maxDelaySeconds;

    @Column(name = "custom_schedule", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> customSchedule; // Custom schedule configuration

    @Column(name = "scheduled_at", nullable = false)
    @NotNull
    private LocalDateTime scheduledAt; // When to retry next

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // When to stop retrying (optional)

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RetryStatus status = RetryStatus.PENDING;

    @Column(name = "locked_by", length = 255)
    @Size(max = 255)
    private String lockedBy; // Instance ID that locked this retry

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "retry_context", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> retryContext; // Context data for retry

    @Column(name = "error_history", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> errorHistory; // History of errors from retry attempts

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @NotNull
    private Integer version = 1; // Optimistic locking

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

