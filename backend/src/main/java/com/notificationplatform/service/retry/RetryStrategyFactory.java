package com.notificationplatform.service.retry;

import com.notificationplatform.entity.enums.RetryStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for creating retry strategy calculators.
 */
@Component
@RequiredArgsConstructor
public class RetryStrategyFactory {

    private final ImmediateRetryStrategy immediateStrategy;
    private final ExponentialBackoffRetryStrategy exponentialBackoffStrategy;
    private final FixedDelayRetryStrategy fixedDelayStrategy;
    private final CustomRetryStrategy customStrategy;

    /**
     * Get calculator for retry strategy.
     * 
     * @param strategy Retry strategy
     * @return Retry strategy calculator
     */
    public RetryStrategyCalculator getCalculator(RetryStrategy strategy) {
        return switch (strategy) {
            case IMMEDIATE -> immediateStrategy;
            case EXPONENTIAL_BACKOFF -> exponentialBackoffStrategy;
            case FIXED_DELAY -> fixedDelayStrategy;
            case CUSTOM -> customStrategy;
        };
    }
}

