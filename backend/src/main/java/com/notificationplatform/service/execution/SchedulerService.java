package com.notificationplatform.service.execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Service for scheduling execution resume tasks.
 * Manages scheduled tasks for delay nodes and other time-based operations.
 * 
 * See: @import(features/workflow-execution-state.md#delay-node-execution-flow)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    private final ExecutionResumeService resumeService;
    
    // Track scheduled tasks
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * Schedule execution resume at a specific time.
     * 
     * @param waitStateId Wait state ID
     * @param resumeAt Time to resume execution
     */
    public void scheduleResume(String waitStateId, Instant resumeAt) {
        log.info("Scheduling resume: waitStateId={}, resumeAt={}", waitStateId, resumeAt);
        
        // Cancel existing task if any
        unscheduleResume(waitStateId);
        
        // Schedule new task
        ScheduledFuture<?> task = taskScheduler.schedule(
            () -> {
                log.info("Scheduled resume triggered: waitStateId={}", waitStateId);
                try {
                    // Get wait state and resume execution
                    // Note: This would need access to waitStateRepository
                    // For now, we'll just log - actual resume is handled by ExecutionResumeService.checkAndResumeExecutions()
                } catch (Exception e) {
                    log.error("Error in scheduled resume: waitStateId={}", waitStateId, e);
                }
            },
            resumeAt
        );
        
        scheduledTasks.put(waitStateId, task);
        log.info("Resume scheduled: waitStateId={}, resumeAt={}", waitStateId, resumeAt);
    }

    /**
     * Cancel scheduled resume task.
     * 
     * @param waitStateId Wait state ID
     */
    public void unscheduleResume(String waitStateId) {
        ScheduledFuture<?> task = scheduledTasks.remove(waitStateId);
        if (task != null) {
            task.cancel(false);
            log.info("Scheduled resume cancelled: waitStateId={}", waitStateId);
        }
    }

    /**
     * Check if a resume task is scheduled.
     * 
     * @param waitStateId Wait state ID
     * @return true if scheduled, false otherwise
     */
    public boolean isScheduled(String waitStateId) {
        ScheduledFuture<?> task = scheduledTasks.get(waitStateId);
        return task != null && !task.isCancelled() && !task.isDone();
    }
}

