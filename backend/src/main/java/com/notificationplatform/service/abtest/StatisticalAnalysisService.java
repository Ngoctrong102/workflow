package com.notificationplatform.service.abtest;

import com.notificationplatform.entity.ABTest;
import com.notificationplatform.entity.ABTestVariant;
import com.notificationplatform.repository.ABTestResultRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for statistical analysis of A/B test results
 */
@Component
public class StatisticalAnalysisService {

    private final ABTestResultRepository resultRepository;

    public StatisticalAnalysisService(ABTestResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    /**
     * Calculate p-value using chi-square test (simplified)
     * For MVP, using simplified statistical analysis
     */
    public BigDecimal calculatePValue(String abTestId, String variant1Id, String variant2Id, String metric) {
        // Simplified p-value calculation
        // In production, would use proper statistical libraries
        
        long variant1Total = resultRepository.countByAbTestIdAndVariantId(abTestId, variant1Id);
        long variant2Total = resultRepository.countByAbTestIdAndVariantId(abTestId, variant2Id);

        if (variant1Total == 0 || variant2Total == 0) {
            return BigDecimal.ONE; // No significant difference if no data
        }

        long variant1Success = getSuccessCount(abTestId, variant1Id, metric);
        long variant2Success = getSuccessCount(abTestId, variant2Id, metric);

        double rate1 = (double) variant1Success / variant1Total;
        double rate2 = (double) variant2Success / variant2Total;

        // Simplified p-value calculation (Z-test approximation)
        double pooledRate = (double) (variant1Success + variant2Success) / (variant1Total + variant2Total);
        double se = Math.sqrt(pooledRate * (1 - pooledRate) * (1.0 / variant1Total + 1.0 / variant2Total));
        
        if (se == 0) {
            return BigDecimal.ONE;
        }

        double z = Math.abs(rate1 - rate2) / se;
        
        // Simplified p-value from z-score (two-tailed test)
        // Using approximation: p ≈ 2 * (1 - Φ(|z|))
        // For MVP, using simplified calculation
        double pValue = 2 * (1 - normalCDF(Math.abs(z)));

        return BigDecimal.valueOf(pValue).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Determine winner based on success metric
     */
    public String determineWinner(ABTest abTest, List<ABTestVariant> variants) {
        if (variants == null || variants.size() < 2) {
            return null;
        }

        String successMetric = abTest.getSuccessMetric();
        String winnerId = null;
        BigDecimal bestRate = BigDecimal.ZERO;

        for (ABTestVariant variant : variants) {
            BigDecimal rate = getMetricRate(abTest.getId(), variant.getId(), successMetric);
            if (rate.compareTo(bestRate) > 0) {
                bestRate = rate;
                winnerId = variant.getId();
            }
        }

        // Check statistical significance
        final String finalWinnerId = winnerId; // Make effectively final for lambda
        if (variants.size() >= 2 && finalWinnerId != null) {
            String otherVariantId = variants.stream()
                    .filter(v -> !v.getId().equals(finalWinnerId))
                    .findFirst()
                    .map(ABTestVariant::getId)
                    .orElse(null);

            if (otherVariantId != null) {
                BigDecimal pValue = calculatePValue(abTest.getId(), finalWinnerId, otherVariantId, successMetric);
                // Only declare winner if p-value < 0.05 (statistically significant)
                if (pValue.compareTo(new BigDecimal("0.05")) >= 0) {
                    return null; // Not statistically significant
                }
            }
        }

        return winnerId;
    }

    private long getSuccessCount(String abTestId, String variantId, String metric) {
        return switch (metric) {
            case "open_rate" -> resultRepository.countOpensByAbTestIdAndVariantId(abTestId, variantId);
            case "click_rate" -> resultRepository.countClicksByAbTestIdAndVariantId(abTestId, variantId);
            case "conversion_rate" -> resultRepository.countConversionsByAbTestIdAndVariantId(abTestId, variantId);
            default -> resultRepository.countOpensByAbTestIdAndVariantId(abTestId, variantId);
        };
    }

    private BigDecimal getMetricRate(String abTestId, String variantId, String metric) {
        long total = resultRepository.countByAbTestIdAndVariantId(abTestId, variantId);
        if (total == 0) {
            return BigDecimal.ZERO;
        }

        long success = getSuccessCount(abTestId, variantId, metric);
        return BigDecimal.valueOf(success)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Simplified normal CDF approximation
     */
    private double normalCDF(double z) {
        // Approximation using error function
        return 0.5 * (1 + erf(z / Math.sqrt(2)));
    }

    /**
     * Error function approximation
     */
    private double erf(double x) {
        // Simplified error function approximation
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }

    /**
     * Calculate confidence interval
     */
    public Map<String, BigDecimal> calculateConfidenceInterval(String abTestId, String variantId, 
                                                                 String metric, BigDecimal confidenceLevel) {
        long total = resultRepository.countByAbTestIdAndVariantId(abTestId, variantId);
        if (total == 0) {
            return Map.of("lower", BigDecimal.ZERO, "upper", BigDecimal.ZERO);
        }

        long success = getSuccessCount(abTestId, variantId, metric);
        double rate = (double) success / total;

        // Z-score for confidence level
        double z = getZScore(confidenceLevel.doubleValue() / 100.0);
        double margin = z * Math.sqrt(rate * (1 - rate) / total);

        BigDecimal lower = BigDecimal.valueOf(Math.max(0, rate - margin))
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal upper = BigDecimal.valueOf(Math.min(1, rate + margin))
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("lower", lower);
        result.put("upper", upper);
        return result;
    }

    private double getZScore(double confidenceLevel) {
        // Z-scores for common confidence levels
        return switch ((int) (confidenceLevel * 100)) {
            case 90 -> 1.645;
            case 95 -> 1.96;
            case 99 -> 2.576;
            default -> 1.96; // Default to 95%
        };
    }
}

