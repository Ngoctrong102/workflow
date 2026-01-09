package com.notificationplatform.service.execution;

import com.notificationplatform.engine.DistributedLockService;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for recovering stuck executions.
 * Handles pod failures and expired locks gracefully.
 * 
 * See: @import(features/distributed-execution-management.md#pod-failure-handling)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionRecoveryService {

    private final ExecutionRepository executionRepository;
    private final DistributedLockService lockService;
    private final ExecutionResumeService resumeService;
    
    // Lock expiration threshold (5 minutes)
    private static final Duration LOCK_EXPIRATION_THRESHOLD = Duration.ofMinutes(5);

    /**
     * Recover stuck executions.
     * Runs every 5 minutes to check for executions with expired locks.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void recoverStuckExecutions() {
        log.debug("Checking for stuck executions");
        
        // Find executions that are RUNNING or WAITING but haven't been updated recently
        LocalDateTime threshold = LocalDateTime.now().minus(LOCK_EXPIRATION_THRESHOLD);
        // Find by each status separately since findByStatusInAndUpdatedAtBefore doesn't exist
        List<Execution> stuckExecutions = new java.util.ArrayList<>();
        stuckExecutions.addAll(executionRepository.findByStatus("running"));
        stuckExecutions.addAll(executionRepository.findByStatus("waiting"));
        stuckExecutions.addAll(executionRepository.findByStatus("paused"));
        
        // Filter by updatedAt
        stuckExecutions = stuckExecutions.stream()
                .filter(e -> e.getUpdatedAt() != null && e.getUpdatedAt().isBefore(threshold))
                .collect(java.util.stream.Collectors.toList());
        
        for (Execution execution : stuckExecutions) {
            try {
                // Check if execution is actually locked
                if (lockService.isLocked(execution.getId())) {
                    // Lock exists but execution hasn't been updated - might be stuck
                    log.warn("Potential stuck execution detected: executionId={}, status={}, updatedAt={}", 
                             execution.getId(), execution.getStatus(), execution.getUpdatedAt());
                    
                    // Try to release lock if it's expired
                    // Note: This is a safety mechanism - in production, you might want more sophisticated logic
                    if (shouldReleaseLock(execution)) {
                        log.info("Releasing expired lock for execution: executionId={}", execution.getId());
                        lockService.releaseLock(execution.getId());
                        
                        // Mark execution as failed if it's been stuck for too long
                        if (execution.getUpdatedAt().isBefore(LocalDateTime.now().minus(Duration.ofHours(1)))) {
                            execution.setStatus(ExecutionStatus.FAILED);
                            execution.setError("Execution stuck - recovered by system");
                            execution.setUpdatedAt(LocalDateTime.now());
                            executionRepository.save(execution);
                            log.warn("Marked stuck execution as failed: executionId={}", execution.getId());
                        }
                    }
                } else {
                    // No lock but execution is in RUNNING/WAITING state - might need recovery
                    log.warn("Execution without lock detected: executionId={}, status={}", 
                             execution.getId(), execution.getStatus());
                    
                    // If execution is PAUSED, try to resume it
                    if (execution.getStatus() == ExecutionStatus.PAUSED) {
                        // Find wait state and try to resume
                        // This would require access to waitStateRepository
                        log.info("Attempting to recover paused execution: executionId={}", execution.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Error recovering stuck execution: executionId={}", execution.getId(), e);
            }
        }
        
        if (!stuckExecutions.isEmpty()) {
            log.info("Recovery check completed: checked {} executions", stuckExecutions.size());
        }
    }

    /**
     * Check if lock should be released.
     * 
     * @param execution Execution to check
     * @return true if lock should be released
     */
    private boolean shouldReleaseLock(Execution execution) {
        // Release lock if execution hasn't been updated for more than 5 minutes
        LocalDateTime threshold = LocalDateTime.now().minus(LOCK_EXPIRATION_THRESHOLD);
        return execution.getUpdatedAt().isBefore(threshold);
    }
}

