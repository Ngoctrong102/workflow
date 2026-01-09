package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "execution_wait_states", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"execution_id", "node_id"}))
public class ExecutionWaitState {

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
    private String nodeId;

    @Column(name = "correlation_id", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String correlationId;

    @Column(name = "wait_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String waitType; // 'api_response', 'kafka_event', 'both'

    @Column(name = "api_response_data", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> apiResponseData;

    @Column(name = "kafka_event_data", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> kafkaEventData;

    @Column(name = "received_events", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> receivedEvents; // Array of received event types

    @Column(name = "status", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String status = "waiting"; // waiting, completed, timeout, failed

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @Column(name = "resumed_by", length = 255)
    @Size(max = 255)
    private String resumedBy; // Instance ID that resumed

    @Version
    @Column(name = "version", nullable = false)
    @NotNull
    private Integer version = 1; // Optimistic locking version

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "waiting";
        }
        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getWaitType() {
        return waitType;
    }

    public void setWaitType(String waitType) {
        this.waitType = waitType;
    }

    public Map<String, Object> getApiResponseData() {
        return apiResponseData;
    }

    public void setApiResponseData(Map<String, Object> apiResponseData) {
        this.apiResponseData = apiResponseData;
    }

    public Map<String, Object> getKafkaEventData() {
        return kafkaEventData;
    }

    public void setKafkaEventData(Map<String, Object> kafkaEventData) {
        this.kafkaEventData = kafkaEventData;
    }

    public List<String> getReceivedEvents() {
        return receivedEvents;
    }

    public void setReceivedEvents(List<String> receivedEvents) {
        this.receivedEvents = receivedEvents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getResumedAt() {
        return resumedAt;
    }

    public void setResumedAt(LocalDateTime resumedAt) {
        this.resumedAt = resumedAt;
    }

    public String getResumedBy() {
        return resumedBy;
    }

    public void setResumedBy(String resumedBy) {
        this.resumedBy = resumedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}

