package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Fixed delay retry strategy.
 * Retries with fixed delay between attempts.
 */
@Slf4j
@Component
public class FixedDelayRetryStrategy implements RetryStrategyCalculator {

    @Override
    public LocalDateTime calculateNextRetryTime(RetrySchedule retrySchedule) {
        int delaySeconds = retrySchedule.getDelaySeconds() != null ? 
                          retrySchedule.getDelaySeconds() : 300;
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}

