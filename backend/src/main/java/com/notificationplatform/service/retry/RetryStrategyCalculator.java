package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;

import java.time.LocalDateTime;

/**
 * Interface for retry strategy calculators.
 * Each strategy implements this interface to calculate next retry time.
 */
public interface RetryStrategyCalculator {

    /**
     * Calculate next retry time based on retry schedule.
     * 
     * @param retrySchedule Retry schedule
     * @return Next retry time
     */
    LocalDateTime calculateNextRetryTime(RetrySchedule retrySchedule);
}

