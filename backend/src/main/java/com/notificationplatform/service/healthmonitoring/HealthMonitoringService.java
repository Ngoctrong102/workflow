package com.notificationplatform.service.healthmonitoring;

import com.notificationplatform.dto.response.ProviderHealthResponse;

import java.util.List;

/**
 * Service for monitoring provider health
 */
public interface HealthMonitoringService {

    /**
     * Check health of a specific channel
     */
    ProviderHealthResponse checkChannelHealth(String channelId);

    /**
     * Check health of all channels
     */
    void checkAllChannelsHealth();

    /**
     * Get health status for a channel
     */
    ProviderHealthResponse getChannelHealth(String channelId);

    /**
     * Get health status for all channels
     */
    List<ProviderHealthResponse> getAllChannelsHealth();

    /**
     * Get unhealthy providers
     */
    List<ProviderHealthResponse> getUnhealthyProviders();

    /**
     * Update health metrics after a successful operation
     */
    void recordSuccess(String channelId, int responseTimeMs);

    /**
     * Update health metrics after a failed operation
     */
    void recordFailure(String channelId, int responseTimeMs, String error);
}

