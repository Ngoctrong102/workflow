package com.notificationplatform.service.ratelimit;

import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.RateLimitTracking;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.repository.RateLimitTrackingRepository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class RateLimitingServiceImpl implements RateLimitingService {

    private final RateLimitTrackingRepository rateLimitTrackingRepository;
    private final ChannelRepository channelRepository;

    // Default rate limits per channel type (requests per minute)
    private static final Map<String, Integer> DEFAULT_RATE_LIMITS = new HashMap<>();
    static {
        DEFAULT_RATE_LIMITS.put("email", 100); // 100 emails per minute
        DEFAULT_RATE_LIMITS.put("sms", 50); // 50 SMS per minute
        DEFAULT_RATE_LIMITS.put("push", 200); // 200 push notifications per minute
        DEFAULT_RATE_LIMITS.put("slack", 20); // 20 Slack messages per minute
        DEFAULT_RATE_LIMITS.put("discord", 50); // 50 Discord messages per minute
        DEFAULT_RATE_LIMITS.put("teams", 30); // 30 Teams messages per minute
        DEFAULT_RATE_LIMITS.put("webhook", 100); // 100 webhooks per minute
        DEFAULT_RATE_LIMITS.put("in-app", 500); // 500 in-app notifications per minute
    }

    @Value("${rate-limiting.window-type:minute}")
    private String defaultWindowType;

    @Value("${rate-limiting.default-limit:100}")
    private int defaultRateLimit;

    public RateLimitingServiceImpl(RateLimitTrackingRepository rateLimitTrackingRepository,
                                   ChannelRepository channelRepository) {
        this.rateLimitTrackingRepository = rateLimitTrackingRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowed(String channelId) {
        int remaining = getRemainingRequests(channelId);
        return remaining > 0;
    }

    @Override
    public void recordRequest(String channelId) {
        LocalDateTime now = LocalDateTime.now();
        String windowType = defaultWindowType;
        
        RateLimitTracking tracking = getOrCreateTracking(channelId, now, windowType);
        tracking.setRequestCount(tracking.getRequestCount() + 1);
        rateLimitTrackingRepository.save(tracking);

        log.debug("Recorded request for channel: channelId={}, count={}, limit={}",
                    channelId, tracking.getRequestCount(), tracking.getLimitValue());
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingRequests(String channelId) {
        LocalDateTime now = LocalDateTime.now();
        String windowType = defaultWindowType;
        
        RateLimitTracking tracking = getOrCreateTracking(channelId, now, windowType);
        int limit = tracking.getLimitValue();
        int count = tracking.getRequestCount();
        
        return Math.max(0, limit - count);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRateLimit(String channelId) {
        LocalDateTime now = LocalDateTime.now();
        String windowType = defaultWindowType;
        
        RateLimitTracking tracking = getOrCreateTracking(channelId, now, windowType);
        return tracking.getLimitValue();
    }

    @Override
    @Transactional(readOnly = true)
    public long getResetTime(String channelId) {
        LocalDateTime now = LocalDateTime.now();
        String windowType = defaultWindowType;
        
        RateLimitTracking tracking = getOrCreateTracking(channelId, now, windowType);
        LocalDateTime resetTime = tracking.getWindowEnd();
        
        if (resetTime.isAfter(now)) {
            return java.time.Duration.between(now, resetTime).getSeconds();
        }
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCustomRateLimit(Channel channel) {
        if (channel == null || channel.getConfig() == null) {
            return null;
        }

        Map<String, Object> config = (Map<String, Object>) channel.getConfig();
        
        if (config.containsKey("rateLimit")) {
            Object rateLimitObj = config.get("rateLimit");
            if (rateLimitObj instanceof Number) {
                return ((Number) rateLimitObj).intValue();
            }
        }
        
        return null;
    }

    private RateLimitTracking getOrCreateTracking(String channelId, LocalDateTime now, String windowType) {
        LocalDateTime windowStart = calculateWindowStart(now, windowType);
        LocalDateTime windowEnd = calculateWindowEnd(windowStart, windowType);
        
        return rateLimitTrackingRepository.findCurrentWindow(channelId, now, windowType)
                .orElseGet(() -> {
                    int limit = getRateLimitForChannel(channelId);
                    
                    RateLimitTracking tracking = new RateLimitTracking();
                    tracking.setId(UUID.randomUUID().toString());
                    tracking.setChannelId(channelId);
                    tracking.setWindowStart(windowStart);
                    tracking.setWindowEnd(windowEnd);
                    tracking.setWindowType(windowType);
                    tracking.setLimitValue(limit);
                    tracking.setRequestCount(0);
                    
                    return rateLimitTrackingRepository.save(tracking);
                });
    }

    private int getRateLimitForChannel(String channelId) {
        // Try to get custom rate limit from channel configuration
        Channel channel = channelRepository.findById(channelId).orElse(null);
        if (channel != null) {
            Integer customLimit = getCustomRateLimit(channel);
            if (customLimit != null && customLimit > 0) {
                return customLimit;
            }
            
            // Use default for channel type
            String channelType = channel.getType();
            if (channelType != null && DEFAULT_RATE_LIMITS.containsKey(channelType)) {
                return DEFAULT_RATE_LIMITS.get(channelType);
            }
        }
        
        return defaultRateLimit;
    }

    private LocalDateTime calculateWindowStart(LocalDateTime now, String windowType) {
        return switch (windowType) {
            case "minute" -> now.withSecond(0).withNano(0);
            case "hour" -> now.withMinute(0).withSecond(0).withNano(0);
            case "day" -> now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            default -> now.withSecond(0).withNano(0);
        };
    }

    private LocalDateTime calculateWindowEnd(LocalDateTime windowStart, String windowType) {
        return switch (windowType) {
            case "minute" -> windowStart.plusMinutes(1);
            case "hour" -> windowStart.plusHours(1);
            case "day" -> windowStart.plusDays(1);
            default -> windowStart.plusMinutes(1);
        };
    }
}

