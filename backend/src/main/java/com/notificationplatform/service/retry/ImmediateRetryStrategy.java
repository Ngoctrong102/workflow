package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Immediate retry strategy.
 * Retries immediately when node/execution fails.
 */
@Slf4j
@Component
public class ImmediateRetryStrategy implements RetryStrategyCalculator {

    @Override
    public LocalDateTime calculateNextRetryTime(RetrySchedule retrySchedule) {
        // Retry immediately
        return LocalDateTime.now();
    }
}

