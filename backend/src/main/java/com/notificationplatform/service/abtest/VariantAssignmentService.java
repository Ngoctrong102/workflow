package com.notificationplatform.service.abtest;

import com.notificationplatform.entity.ABTest;
import com.notificationplatform.entity.ABTestVariant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * Service for assigning users to A/B test variants based on traffic split
 */
@Component
public class VariantAssignmentService {

    private final Random random = new Random();

    /**
     * Assign user to a variant based on traffic split
     * @param abTest A/B test configuration
     * @param userId User ID (for consistent assignment)
     * @return Variant ID
     */
    public String assignVariant(ABTest abTest, String userId) {
        List<ABTestVariant> variants = abTest.getVariants();
        if (variants == null || variants.isEmpty()) {
            throw new IllegalStateException("A/B test has no variants");
        }

        // Filter out deleted variants
        variants = variants.stream()
                .filter(v -> v.getDeletedAt() == null)
                .toList();

        if (variants.isEmpty()) {
            throw new IllegalStateException("A/B test has no active variants");
        }

        String trafficSplitType = abTest.getTrafficSplitType();

        if ("equal".equals(trafficSplitType)) {
            return assignEqualSplit(variants, userId);
        } else if ("custom".equals(trafficSplitType) || "weighted".equals(trafficSplitType)) {
            return assignWeightedSplit(variants, userId);
        } else {
            // Default to equal split
            return assignEqualSplit(variants, userId);
        }
    }

    private String assignEqualSplit(List<ABTestVariant> variants, String userId) {
        // Equal distribution: each variant gets 1/n of traffic
        int index = getConsistentIndex(userId, variants.size());
        return variants.get(index).getId();
    }

    private String assignWeightedSplit(List<ABTestVariant> variants, String userId) {
        // Weighted distribution based on traffic percentage
        double randomValue = getConsistentRandom(userId);
        double cumulative = 0.0;

        // Normalize percentages to ensure they sum to 100
        double totalPercentage = variants.stream()
                .mapToDouble(v -> v.getTrafficPercentage() != null ? 
                        v.getTrafficPercentage().doubleValue() : 0.0)
                .sum();

        if (totalPercentage == 0) {
            // Fallback to equal split if percentages not set
            return assignEqualSplit(variants, userId);
        }

        for (ABTestVariant variant : variants) {
            BigDecimal percentage = variant.getTrafficPercentage();
            if (percentage == null) {
                continue;
            }
            cumulative += (percentage.doubleValue() / totalPercentage);
            if (randomValue <= cumulative) {
                return variant.getId();
            }
        }

        // Fallback to last variant
        return variants.get(variants.size() - 1).getId();
    }

    /**
     * Get consistent index for user (same user always gets same variant)
     */
    private int getConsistentIndex(String userId, int size) {
        return Math.abs(userId.hashCode()) % size;
    }

    /**
     * Get consistent random value for user (0.0 to 1.0)
     */
    private double getConsistentRandom(String userId) {
        // Use hash code to generate consistent "random" value
        int hash = userId.hashCode();
        // Normalize to 0.0 - 1.0 range
        return (double) (Math.abs(hash) % 10000) / 10000.0;
    }
}

