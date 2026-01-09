package com.notificationplatform.service.ratelimit;

import com.notificationplatform.entity.Channel;

/**
 * Service for managing rate limiting per channel
 */
public interface RateLimitingService {

    /**
     * Check if request is allowed based on rate limit
     * @param channelId Channel ID
     * @return true if allowed, false if rate limit exceeded
     */
    boolean isAllowed(String channelId);

    /**
     * Record a request for rate limiting tracking
     * @param channelId Channel ID
     */
    void recordRequest(String channelId);

    /**
     * Get remaining requests in current window
     * @param channelId Channel ID
     * @return Remaining requests count
     */
    int getRemainingRequests(String channelId);

    /**
     * Get rate limit configuration for channel
     * @param channelId Channel ID
     * @return Rate limit value (requests per window)
     */
    int getRateLimit(String channelId);

    /**
     * Get time until rate limit resets
     * @param channelId Channel ID
     * @return Seconds until reset
     */
    long getResetTime(String channelId);

    /**
     * Check if channel has custom rate limit configuration
     * @param channel Channel entity
     * @return Rate limit value or null if using default
     */
    Integer getCustomRateLimit(Channel channel);
}

