package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ab_test_aggregates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ab_test_id", "variant_id", "date"})
})
public class ABTestAggregate {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "ab_test_id", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String abTestId;

    @Column(name = "variant_id", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String variantId;

    @Column(name = "date", nullable = false)
    @NotNull
    private LocalDate date;

    @Column(name = "total_assignments")
    private Integer totalAssignments = 0;

    @Column(name = "total_opens")
    private Integer totalOpens = 0;

    @Column(name = "total_clicks")
    private Integer totalClicks = 0;

    @Column(name = "total_conversions")
    private Integer totalConversions = 0;

    @Column(name = "total_engagement_score", precision = 15, scale = 2)
    private BigDecimal totalEngagementScore = BigDecimal.ZERO;

    @Column(name = "open_rate", precision = 10, scale = 4)
    private BigDecimal openRate = BigDecimal.ZERO;

    @Column(name = "click_rate", precision = 10, scale = 4)
    private BigDecimal clickRate = BigDecimal.ZERO;

    @Column(name = "conversion_rate", precision = 10, scale = 4)
    private BigDecimal conversionRate = BigDecimal.ZERO;

    @Column(name = "engagement_rate", precision = 10, scale = 4)
    private BigDecimal engagementRate = BigDecimal.ZERO;

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

    public String getAbTestId() {
        return abTestId;
    }

    public void setAbTestId(String abTestId) {
        this.abTestId = abTestId;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Integer totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Integer getTotalOpens() {
        return totalOpens;
    }

    public void setTotalOpens(Integer totalOpens) {
        this.totalOpens = totalOpens;
    }

    public Integer getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(Integer totalClicks) {
        this.totalClicks = totalClicks;
    }

    public Integer getTotalConversions() {
        return totalConversions;
    }

    public void setTotalConversions(Integer totalConversions) {
        this.totalConversions = totalConversions;
    }

    public BigDecimal getTotalEngagementScore() {
        return totalEngagementScore;
    }

    public void setTotalEngagementScore(BigDecimal totalEngagementScore) {
        this.totalEngagementScore = totalEngagementScore;
    }

    public BigDecimal getOpenRate() {
        return openRate;
    }

    public void setOpenRate(BigDecimal openRate) {
        this.openRate = openRate;
    }

    public BigDecimal getClickRate() {
        return clickRate;
    }

    public void setClickRate(BigDecimal clickRate) {
        this.clickRate = clickRate;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public BigDecimal getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(BigDecimal engagementRate) {
        this.engagementRate = engagementRate;
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
}

