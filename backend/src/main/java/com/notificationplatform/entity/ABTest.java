package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ab_tests")
public class ABTest {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "workflow_id", length = 255)
    @Size(max = 255)
    private String workflowId;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private Workflow workflow;

    @Column(name = "status", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String status = "draft"; // draft, running, paused, completed, archived

    @Column(name = "success_metric", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String successMetric = "open_rate"; // open_rate, click_rate, conversion_rate, engagement_rate

    @Column(name = "traffic_split_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String trafficSplitType = "equal"; // equal, custom, weighted

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "min_sample_size")
    private Integer minSampleSize = 1000;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel = new BigDecimal("95.00");

    @Column(name = "winner_variant_id", length = 255)
    @Size(max = 255)
    private String winnerVariantId;

    @Column(name = "metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "abTest", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ABTestVariant> variants;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuccessMetric() {
        return successMetric;
    }

    public void setSuccessMetric(String successMetric) {
        this.successMetric = successMetric;
    }

    public String getTrafficSplitType() {
        return trafficSplitType;
    }

    public void setTrafficSplitType(String trafficSplitType) {
        this.trafficSplitType = trafficSplitType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getMinSampleSize() {
        return minSampleSize;
    }

    public void setMinSampleSize(Integer minSampleSize) {
        this.minSampleSize = minSampleSize;
    }

    public BigDecimal getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(BigDecimal confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getWinnerVariantId() {
        return winnerVariantId;
    }

    public void setWinnerVariantId(String winnerVariantId) {
        this.winnerVariantId = winnerVariantId;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<ABTestVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ABTestVariant> variants) {
        this.variants = variants;
    }
}

