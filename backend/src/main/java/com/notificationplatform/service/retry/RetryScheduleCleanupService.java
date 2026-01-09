package com.notificationplatform.service.retry;

import com.notificationplatform.entity.RetrySchedule;
import com.notificationplatform.entity.enums.RetryStatus;
import com.notificationplatform.repository.RetryScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up expired and completed retry schedules.
 * Archives completed retry schedules and removes expired ones.
 * 
 * See: @import(features/retry-mechanism.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryScheduleCleanupService {

    private final RetryScheduleRepository retryScheduleRepository;
    
    // Cleanup threshold: 30 days
    private static final int CLEANUP_DAYS = 30;

    /**
     * Cleanup expired and old retry schedules.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupRetrySchedules() {
        log.info("Starting retry schedule cleanup");
        
        LocalDateTime threshold = LocalDateTime.now().minusDays(CLEANUP_DAYS);
        
        // Find expired retry schedules
        List<RetrySchedule> expiredSchedules = retryScheduleRepository
                .findExpiredRetrySchedules(LocalDateTime.now(), 
                    List.of(RetryStatus.PENDING, RetryStatus.SCHEDULED, RetryStatus.RETRYING));
        
        for (RetrySchedule schedule : expiredSchedules) {
            schedule.setStatus(RetryStatus.FAILED);
            retryScheduleRepository.save(schedule);
            log.debug("Marked expired retry schedule as failed: retryScheduleId={}", schedule.getId());
        }
        
        // Find old completed/failed retry schedules
        List<RetrySchedule> oldSchedules = retryScheduleRepository
                .findByStatusIn(List.of(RetryStatus.COMPLETED, RetryStatus.FAILED, RetryStatus.CANCELLED));
        
        int archivedCount = 0;
        for (RetrySchedule schedule : oldSchedules) {
            if (schedule.getUpdatedAt() != null && schedule.getUpdatedAt().isBefore(threshold)) {
                // Archive or delete old retry schedules
                // For now, we'll just delete them
                retryScheduleRepository.delete(schedule);
                archivedCount++;
                log.debug("Deleted old retry schedule: retryScheduleId={}, status={}, updatedAt={}", 
                         schedule.getId(), schedule.getStatus(), schedule.getUpdatedAt());
            }
        }
        
        log.info("Retry schedule cleanup completed: expired={}, archived={}", 
                 expiredSchedules.size(), archivedCount);
    }
}

