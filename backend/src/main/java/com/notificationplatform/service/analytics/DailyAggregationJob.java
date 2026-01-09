package com.notificationplatform.service.analytics;



import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
/**
 * Scheduled job for daily analytics aggregation
 * Runs daily at 1 AM to aggregate previous day's metrics
 */
@Slf4j
@Component
public class DailyAggregationJob {

    private final AnalyticsService analyticsService;

    public DailyAggregationJob(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Aggregate daily metrics for previous day
     * Runs at 1 AM every day
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void aggregatePreviousDay() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily aggregation for date: {}", yesterday);
        
        try {
            analyticsService.aggregateDailyMetrics(yesterday);
            log.info("Daily aggregation completed for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Error aggregating daily metrics for date: {}", yesterday, e);
        }
    }
}

