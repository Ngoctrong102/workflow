package com.notificationplatform.service.analytics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class AnalyticsScheduler {

    private final AnalyticsAggregator analyticsAggregator;

    public AnalyticsScheduler(AnalyticsAggregator analyticsAggregator) {
        this.analyticsAggregator = analyticsAggregator;
    }

    /**
     * Schedule daily aggregation to run at 1:00 AM every day
     * Aggregates data for the previous day
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleDailyAggregation() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            log.info("Starting scheduled daily analytics aggregation for date: {}", yesterday);
            analyticsAggregator.aggregateForDate(yesterday);
            log.info("Completed scheduled daily analytics aggregation for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Error in scheduled daily analytics aggregation", e);
        }
    }
}

