package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Custom retry strategy.
 * Supports long-term retries (days, weeks) based on custom schedule.
 */
@Slf4j
@Component
public class CustomRetryStrategy implements RetryStrategyCalculator {

    @Override
    public LocalDateTime calculateNextRetryTime(RetrySchedule retrySchedule) {
        Map<String, Object> customSchedule = retrySchedule.getCustomSchedule();
        if (customSchedule == null) {
            // Default to 1 day if no custom schedule
            return LocalDateTime.now().plusDays(1);
        }
        
        // Get attempts array
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attempts = (List<Map<String, Object>>) customSchedule.get("attempts");
        if (attempts == null || attempts.isEmpty()) {
            return LocalDateTime.now().plusDays(1);
        }
        
        // Find schedule for current attempt
        int currentAttempt = retrySchedule.getCurrentAttempt();
        for (Map<String, Object> attemptConfig : attempts) {
            Object attemptNumObj = attemptConfig.get("attempt");
            int attemptNum = attemptNumObj instanceof Number ? 
                           ((Number) attemptNumObj).intValue() : 0;
            
            if (attemptNum == currentAttempt) {
                // Found schedule for this attempt
                Object delayDaysObj = attemptConfig.get("delayDays");
                Object delayHoursObj = attemptConfig.get("delayHours");
                Object delaySecondsObj = attemptConfig.get("delaySeconds");
                
                LocalDateTime nextTime = LocalDateTime.now();
                
                if (delayDaysObj instanceof Number) {
                    nextTime = nextTime.plusDays(((Number) delayDaysObj).longValue());
                }
                if (delayHoursObj instanceof Number) {
                    nextTime = nextTime.plusHours(((Number) delayHoursObj).longValue());
                }
                if (delaySecondsObj instanceof Number) {
                    nextTime = nextTime.plusSeconds(((Number) delaySecondsObj).longValue());
                }
                
                return nextTime;
            }
        }
        
        // No schedule found for current attempt, use last schedule or default
        Map<String, Object> lastAttempt = attempts.get(attempts.size() - 1);
        Object delayDaysObj = lastAttempt.get("delayDays");
        if (delayDaysObj instanceof Number) {
            return LocalDateTime.now().plusDays(((Number) delayDaysObj).longValue());
        }
        
        return LocalDateTime.now().plusDays(1);
    }
}

