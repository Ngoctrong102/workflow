package com.notificationplatform.service.healthmonitoring;

import com.notificationplatform.dto.response.ProviderHealthResponse;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.ProviderHealth;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.repository.ProviderHealthRepository;
import com.notificationplatform.service.alerting.AlertingService;
import com.notificationplatform.service.channel.ChannelService;


import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class HealthMonitoringServiceImpl implements HealthMonitoringService {

    private final ProviderHealthRepository providerHealthRepository;
    private final ChannelRepository channelRepository;
    private final ChannelService channelService;
    private final AlertingService alertingService;

    public HealthMonitoringServiceImpl(ProviderHealthRepository providerHealthRepository,
                                     ChannelRepository channelRepository,
                                     ChannelService channelService,
                                     @Lazy AlertingService alertingService) {
        this.providerHealthRepository = providerHealthRepository;
        this.channelRepository = channelRepository;
        this.channelService = channelService;
        this.alertingService = alertingService;
    }

    @Override
    public ProviderHealthResponse checkChannelHealth(String channelId) {
        Channel channel = channelRepository.findByIdAndNotDeleted(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelId));

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String error = null;

        try {
            // Test connection
            channelService.testConnection(channelId);
            success = true;
        } catch (Exception e) {
            error = e.getMessage();
            log.warn("Health check failed for channel: channelId={}, error={}", channelId, error);
        }

        long responseTime = System.currentTimeMillis() - startTime;

        // Update health metrics
        if (success) {
            recordSuccess(channelId, (int) responseTime);
        } else {
            recordFailure(channelId, (int) responseTime, error);
        }

        // Evaluate alert rules after health check
        try {
            alertingService.evaluateAlertRules(channelId);
        } catch (Exception e) {
            log.warn("Error evaluating alert rules for channel: channelId={}", channelId, e);
        }

        return getChannelHealth(channelId);
    }

    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkAllChannelsHealth() {
        log.info("Starting scheduled health check for all channels");
        List<Channel> activeChannels = channelRepository.findAllActive().stream()
                .filter(c -> "active".equals(c.getStatus()))
                .collect(Collectors.toList());

        for (Channel channel : activeChannels) {
            try {
                checkChannelHealth(channel.getId());
            } catch (Exception e) {
                log.error("Error checking health for channel: channelId={}", channel.getId(), e);
            }
        }
        log.info("Completed scheduled health check for {} channels", activeChannels.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderHealthResponse getChannelHealth(String channelId) {
        Optional<ProviderHealth> healthOpt = providerHealthRepository.findByChannelId(channelId);
        if (healthOpt.isEmpty()) {
            // Return default healthy status if no health record exists
            ProviderHealthResponse response = new ProviderHealthResponse();
            response.setChannelId(channelId);
            response.setStatus("healthy");
            response.setHealthScore(100.0);
            return response;
        }

        return toResponse(healthOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderHealthResponse> getAllChannelsHealth() {
        return providerHealthRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderHealthResponse> getUnhealthyProviders() {
        return providerHealthRepository.findUnhealthyProviders().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void recordSuccess(String channelId, int responseTimeMs) {
        ProviderHealth health = getOrCreateHealth(channelId);
        
        health.setSuccessCount((health.getSuccessCount() != null ? health.getSuccessCount() : 0) + 1);
        health.setLastSuccessAt(LocalDateTime.now());
        health.setLastCheckAt(LocalDateTime.now());
        health.setResponseTimeMs(responseTimeMs);
        health.setConsecutiveFailures(0);

        // Update error rate
        int total = (health.getSuccessCount() != null ? health.getSuccessCount() : 0) +
                    (health.getFailureCount() != null ? health.getFailureCount() : 0);
        if (total > 0) {
            double errorRate = (health.getFailureCount() != null ? health.getFailureCount() : 0) * 100.0 / total;
            health.setErrorRate(errorRate);
        }

        // Calculate health score and status
        updateHealthStatus(health);

        providerHealthRepository.save(health);
    }

    @Override
    public void recordFailure(String channelId, int responseTimeMs, String error) {
        ProviderHealth health = getOrCreateHealth(channelId);
        
        health.setFailureCount((health.getFailureCount() != null ? health.getFailureCount() : 0) + 1);
        health.setLastFailureAt(LocalDateTime.now());
        health.setLastCheckAt(LocalDateTime.now());
        health.setResponseTimeMs(responseTimeMs);
        health.setConsecutiveFailures((health.getConsecutiveFailures() != null ? health.getConsecutiveFailures() : 0) + 1);

        // Update error rate
        int total = (health.getSuccessCount() != null ? health.getSuccessCount() : 0) +
                    (health.getFailureCount() != null ? health.getFailureCount() : 0);
        if (total > 0) {
            double errorRate = (health.getFailureCount() != null ? health.getFailureCount() : 0) * 100.0 / total;
            health.setErrorRate(errorRate);
        }

        // Calculate health score and status
        updateHealthStatus(health);

        providerHealthRepository.save(health);
    }

    private ProviderHealth getOrCreateHealth(String channelId) {
        Optional<ProviderHealth> healthOpt = providerHealthRepository.findByChannelId(channelId);
        if (healthOpt.isPresent()) {
            return healthOpt.get();
        }

        // Create new health record
        Channel channel = channelRepository.findByIdAndNotDeleted(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelId));

        ProviderHealth health = new ProviderHealth();
        health.setId(UUID.randomUUID().toString());
        health.setChannelId(channelId);
        health.setChannelType(channel.getType());
        health.setStatus("healthy");
        health.setHealthScore(100.0);
        health.setErrorRate(0.0);
        health.setSuccessCount(0);
        health.setFailureCount(0);
        health.setConsecutiveFailures(0);

        return providerHealthRepository.save(health);
    }

    private void updateHealthStatus(ProviderHealth health) {
        // Calculate health score (0-100)
        double healthScore = 100.0;
        
        // Deduct points for error rate
        if (health.getErrorRate() != null) {
            healthScore -= health.getErrorRate() * 0.5; // Max 50 points deduction
        }

        // Deduct points for consecutive failures
        if (health.getConsecutiveFailures() != null && health.getConsecutiveFailures() > 0) {
            healthScore -= health.getConsecutiveFailures() * 10; // 10 points per consecutive failure
        }

        // Deduct points for slow response time (> 5 seconds)
        if (health.getResponseTimeMs() != null && health.getResponseTimeMs() > 5000) {
            healthScore -= (health.getResponseTimeMs() - 5000) / 100.0; // 1 point per 100ms over 5s
        }

        healthScore = Math.max(0.0, Math.min(100.0, healthScore));
        health.setHealthScore(healthScore);

        // Determine status
        if (healthScore >= 80.0) {
            health.setStatus("healthy");
        } else if (healthScore >= 50.0) {
            health.setStatus("degraded");
        } else {
            health.setStatus("down");
        }
    }

    private ProviderHealthResponse toResponse(ProviderHealth health) {
        ProviderHealthResponse response = new ProviderHealthResponse();
        response.setId(health.getId());
        response.setChannelId(health.getChannelId());
        response.setChannelType(health.getChannelType());
        response.setStatus(health.getStatus());
        response.setResponseTimeMs(health.getResponseTimeMs());
        response.setErrorRate(health.getErrorRate());
        response.setSuccessCount(health.getSuccessCount());
        response.setFailureCount(health.getFailureCount());
        response.setLastCheckAt(health.getLastCheckAt());
        response.setLastSuccessAt(health.getLastSuccessAt());
        response.setLastFailureAt(health.getLastFailureAt());
        response.setConsecutiveFailures(health.getConsecutiveFailures());
        response.setHealthScore(health.getHealthScore());
        return response;
    }
}

