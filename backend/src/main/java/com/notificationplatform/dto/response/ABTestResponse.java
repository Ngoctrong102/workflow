package com.notificationplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ABTestResponse {

    private String id;
    private String name;
    private String description;
    private String workflowId;
    private String status;
    private String successMetric;
    private String trafficSplitType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minSampleSize;
    private BigDecimal confidenceLevel;
    private String winnerVariantId;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ABTestVariantResponse> variants;

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
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

    public List<ABTestVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ABTestVariantResponse> variants) {
        this.variants = variants;
    }
}

