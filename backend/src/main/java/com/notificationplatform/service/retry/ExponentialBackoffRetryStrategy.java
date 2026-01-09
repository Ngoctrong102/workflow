package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Exponential backoff retry strategy.
 * Calculates delay as: initialDelay * multiplier^attempt
 * Capped at maxDelaySeconds if specified.
 */
@Slf4j
@Component
public class ExponentialBackoffRetryStrategy implements RetryStrategyCalculator {

    @Override
    public LocalDateTime calculateNextRetryTime(RetrySchedule retrySchedule) {
        int attempt = retrySchedule.getCurrentAttempt();
        int initialDelay = retrySchedule.getInitialDelaySeconds() != null ? 
                          retrySchedule.getInitialDelaySeconds() : 60;
        BigDecimal multiplier = retrySchedule.getMultiplier() != null ? 
                               retrySchedule.getMultiplier() : BigDecimal.valueOf(2.0);
        
        // Calculate delay: initialDelay * multiplier^attempt
        BigDecimal delay = BigDecimal.valueOf(initialDelay)
                .multiply(multiplier.pow(attempt));
        
        // Cap at maxDelaySeconds if specified
        if (retrySchedule.getMaxDelaySeconds() != null) {
            delay = delay.min(BigDecimal.valueOf(retrySchedule.getMaxDelaySeconds()));
        }
        
        long delaySeconds = delay.longValue();
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}

