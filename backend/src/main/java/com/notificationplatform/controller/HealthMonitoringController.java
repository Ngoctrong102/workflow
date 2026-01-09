package com.notificationplatform.controller;

import com.notificationplatform.dto.response.ProviderHealthResponse;
import com.notificationplatform.service.healthmonitoring.HealthMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health-monitoring")
public class HealthMonitoringController {

    private final HealthMonitoringService healthMonitoringService;

    public HealthMonitoringController(HealthMonitoringService healthMonitoringService) {
        this.healthMonitoringService = healthMonitoringService;
    }

    @PostMapping("/channels/{channelId}/check")
    public ResponseEntity<ProviderHealthResponse> checkChannelHealth(@PathVariable String channelId) {
        ProviderHealthResponse response = healthMonitoringService.checkChannelHealth(channelId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/channels/{channelId}")
    public ResponseEntity<ProviderHealthResponse> getChannelHealth(@PathVariable String channelId) {
        ProviderHealthResponse response = healthMonitoringService.getChannelHealth(channelId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/channels")
    public ResponseEntity<List<ProviderHealthResponse>> getAllChannelsHealth() {
        List<ProviderHealthResponse> responses = healthMonitoringService.getAllChannelsHealth();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unhealthy")
    public ResponseEntity<List<ProviderHealthResponse>> getUnhealthyProviders() {
        List<ProviderHealthResponse> responses = healthMonitoringService.getUnhealthyProviders();
        return ResponseEntity.ok(responses);
    }
}

