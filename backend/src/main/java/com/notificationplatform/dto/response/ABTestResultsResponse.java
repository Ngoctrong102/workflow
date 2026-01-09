package com.notificationplatform.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ABTestResultsResponse {

    private String abTestId;
    private String abTestName;
    private String successMetric;
    private List<VariantResult> variantResults;
    private StatisticalAnalysis statisticalAnalysis;
    private String winnerVariantId;
    private String recommendation;

    // Getters and Setters
    public String getAbTestId() {
        return abTestId;
    }

    public void setAbTestId(String abTestId) {
        this.abTestId = abTestId;
    }

    public String getAbTestName() {
        return abTestName;
    }

    public void setAbTestName(String abTestName) {
        this.abTestName = abTestName;
    }

    public String getSuccessMetric() {
        return successMetric;
    }

    public void setSuccessMetric(String successMetric) {
        this.successMetric = successMetric;
    }

    public List<VariantResult> getVariantResults() {
        return variantResults;
    }

    public void setVariantResults(List<VariantResult> variantResults) {
        this.variantResults = variantResults;
    }

    public StatisticalAnalysis getStatisticalAnalysis() {
        return statisticalAnalysis;
    }

    public void setStatisticalAnalysis(StatisticalAnalysis statisticalAnalysis) {
        this.statisticalAnalysis = statisticalAnalysis;
    }

    public String getWinnerVariantId() {
        return winnerVariantId;
    }

    public void setWinnerVariantId(String winnerVariantId) {
        this.winnerVariantId = winnerVariantId;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public static class VariantResult {
        private String variantId;
        private String variantName;
        private String variantLabel;
        private Long totalAssignments;
        private Long totalOpens;
        private Long totalClicks;
        private Long totalConversions;
        private BigDecimal openRate;
        private BigDecimal clickRate;
        private BigDecimal conversionRate;
        private BigDecimal engagementRate;
        private BigDecimal averageEngagementScore;

        // Getters and Setters
        public String getVariantId() {
            return variantId;
        }

        public void setVariantId(String variantId) {
            this.variantId = variantId;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public String getVariantLabel() {
            return variantLabel;
        }

        public void setVariantLabel(String variantLabel) {
            this.variantLabel = variantLabel;
        }

        public Long getTotalAssignments() {
            return totalAssignments;
        }

        public void setTotalAssignments(Long totalAssignments) {
            this.totalAssignments = totalAssignments;
        }

        public Long getTotalOpens() {
            return totalOpens;
        }

        public void setTotalOpens(Long totalOpens) {
            this.totalOpens = totalOpens;
        }

        public Long getTotalClicks() {
            return totalClicks;
        }

        public void setTotalClicks(Long totalClicks) {
            this.totalClicks = totalClicks;
        }

        public Long getTotalConversions() {
            return totalConversions;
        }

        public void setTotalConversions(Long totalConversions) {
            this.totalConversions = totalConversions;
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

        public BigDecimal getAverageEngagementScore() {
            return averageEngagementScore;
        }

        public void setAverageEngagementScore(BigDecimal averageEngagementScore) {
            this.averageEngagementScore = averageEngagementScore;
        }
    }

    public static class StatisticalAnalysis {
        private BigDecimal pValue;
        private BigDecimal confidenceInterval;
        private Boolean isStatisticallySignificant;
        private String significanceLevel;
        private Map<String, Object> additionalMetrics;

        // Getters and Setters
        public BigDecimal getPValue() {
            return pValue;
        }

        public void setPValue(BigDecimal pValue) {
            this.pValue = pValue;
        }

        public BigDecimal getConfidenceInterval() {
            return confidenceInterval;
        }

        public void setConfidenceInterval(BigDecimal confidenceInterval) {
            this.confidenceInterval = confidenceInterval;
        }

        public Boolean getIsStatisticallySignificant() {
            return isStatisticallySignificant;
        }

        public void setIsStatisticallySignificant(Boolean isStatisticallySignificant) {
            this.isStatisticallySignificant = isStatisticallySignificant;
        }

        public String getSignificanceLevel() {
            return significanceLevel;
        }

        public void setSignificanceLevel(String significanceLevel) {
            this.significanceLevel = significanceLevel;
        }

        public Map<String, Object> getAdditionalMetrics() {
            return additionalMetrics;
        }

        public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
            this.additionalMetrics = additionalMetrics;
        }
    }
}

