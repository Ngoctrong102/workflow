package com.notificationplatform.service.retry;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.RetrySchedule;
import com.notificationplatform.entity.enums.RetryStatus;
import com.notificationplatform.entity.enums.RetryStrategy;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import com.notificationplatform.repository.RetryScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing retry schedules.
 * Handles creation, processing, and execution of retry schedules.
 * 
 * See: @import(features/retry-mechanism.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryScheduleService {

    private final RetryScheduleRepository retryScheduleRepository;
    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final RetryExecutionService retryExecutionService;
    private final RetryStrategyFactory retryStrategyFactory;

    /**
     * Create retry schedule for failed node execution.
     * 
     * @param nodeExecution Failed node execution
     * @param retryConfig Retry configuration from node config
     * @return Created retry schedule
     */
    @Transactional
    public RetrySchedule createRetryScheduleForNode(NodeExecution nodeExecution, Map<String, Object> retryConfig) {
        log.info("Creating retry schedule for node execution: nodeExecutionId={}, nodeId={}", 
                 nodeExecution.getId(), nodeExecution.getNodeId());
        
        RetrySchedule retrySchedule = new RetrySchedule();
        retrySchedule.setId(UUID.randomUUID().toString());
        retrySchedule.setRetryType("node_execution");
        retrySchedule.setTargetId(nodeExecution.getId());
        retrySchedule.setExecution(nodeExecution.getExecution());
        retrySchedule.setNodeId(nodeExecution.getNodeId());
        
        // Parse retry configuration
        parseRetryConfig(retrySchedule, retryConfig);
        
        // Calculate scheduled time based on strategy
        LocalDateTime scheduledAt = calculateScheduledTime(retrySchedule);
        retrySchedule.setScheduledAt(scheduledAt);
        
        retrySchedule.setStatus(RetryStatus.PENDING);
        retrySchedule.setCurrentAttempt(0);
        
        // Store retry context (input data, node config, etc.)
        Map<String, Object> retryContext = new HashMap<>();
        retryContext.put("inputData", nodeExecution.getInputData());
        retryContext.put("nodeConfig", nodeExecution.getNodeConfig());
        retryContext.put("executionContext", nodeExecution.getExecutionContext());
        retrySchedule.setRetryContext(retryContext);
        
        // Initialize error history
        Map<String, Object> errorHistory = new HashMap<>();
        errorHistory.put("errors", new java.util.ArrayList<>());
        retrySchedule.setErrorHistory(errorHistory);
        
        retrySchedule = retryScheduleRepository.save(retrySchedule);
        log.info("Retry schedule created: retryScheduleId={}, scheduledAt={}", 
                 retrySchedule.getId(), scheduledAt);
        
        return retrySchedule;
    }

    /**
     * Create retry schedule for failed execution.
     * 
     * @param execution Failed execution
     * @param retryConfig Retry configuration
     * @return Created retry schedule
     */
    @Transactional
    public RetrySchedule createRetryScheduleForExecution(Execution execution, Map<String, Object> retryConfig) {
        log.info("Creating retry schedule for execution: executionId={}", execution.getId());
        
        RetrySchedule retrySchedule = new RetrySchedule();
        retrySchedule.setId(UUID.randomUUID().toString());
        retrySchedule.setRetryType("execution");
        retrySchedule.setTargetId(execution.getId());
        retrySchedule.setExecution(execution);
        
        // Parse retry configuration
        parseRetryConfig(retrySchedule, retryConfig);
        
        // Calculate scheduled time based on strategy
        LocalDateTime scheduledAt = calculateScheduledTime(retrySchedule);
        retrySchedule.setScheduledAt(scheduledAt);
        
        retrySchedule.setStatus(RetryStatus.PENDING);
        retrySchedule.setCurrentAttempt(0);
        
        // Store retry context
        Map<String, Object> retryContext = new HashMap<>();
        retryContext.put("context", execution.getContext());
        retryContext.put("triggerData", execution.getTriggerData());
        retrySchedule.setRetryContext(retryContext);
        
        // Initialize error history
        Map<String, Object> errorHistory = new HashMap<>();
        errorHistory.put("errors", new java.util.ArrayList<>());
        retrySchedule.setErrorHistory(errorHistory);
        
        retrySchedule = retryScheduleRepository.save(retrySchedule);
        log.info("Retry schedule created: retryScheduleId={}, scheduledAt={}", 
                 retrySchedule.getId(), scheduledAt);
        
        return retrySchedule;
    }

    /**
     * Process retry schedules that are ready to execute.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processRetrySchedules() {
        log.debug("Processing retry schedules");
        
        LocalDateTime now = LocalDateTime.now();
        List<RetrySchedule> readySchedules = retryScheduleRepository
                .findByScheduledAtLessThanEqualAndStatus(now, RetryStatus.PENDING);
        
        for (RetrySchedule schedule : readySchedules) {
            try {
                // Update status to scheduled
                schedule.setStatus(RetryStatus.SCHEDULED);
                retryScheduleRepository.save(schedule);
                
                // Execute retry
                executeRetry(schedule);
            } catch (Exception e) {
                log.error("Error processing retry schedule: retryScheduleId={}", schedule.getId(), e);
                schedule.setStatus(RetryStatus.FAILED);
                retryScheduleRepository.save(schedule);
            }
        }
        
        if (!readySchedules.isEmpty()) {
            log.info("Processed {} retry schedules", readySchedules.size());
        }
    }

    /**
     * Execute retry attempt.
     * 
     * @param retrySchedule Retry schedule to execute
     */
    @Transactional
    public void executeRetry(RetrySchedule retrySchedule) {
        log.info("Executing retry: retryScheduleId={}, retryType={}, targetId={}, attempt={}", 
                 retrySchedule.getId(), retrySchedule.getRetryType(), 
                 retrySchedule.getTargetId(), retrySchedule.getCurrentAttempt() + 1);
        
        // Increment attempt
        retrySchedule.setCurrentAttempt(retrySchedule.getCurrentAttempt() + 1);
        retrySchedule.setLastRetriedAt(LocalDateTime.now());
        retrySchedule.setStatus(RetryStatus.RETRYING);
        retryScheduleRepository.save(retrySchedule);
        
        try {
            boolean success = false;
            
            if ("node_execution".equals(retrySchedule.getRetryType())) {
                // Retry node execution
                success = retryExecutionService.retryNodeExecution(retrySchedule);
            } else if ("execution".equals(retrySchedule.getRetryType())) {
                // Retry execution
                success = retryExecutionService.retryExecution(retrySchedule);
            }
            
            if (success) {
                // Retry successful
                retrySchedule.setStatus(RetryStatus.COMPLETED);
                log.info("Retry successful: retryScheduleId={}, attempt={}", 
                         retrySchedule.getId(), retrySchedule.getCurrentAttempt());
            } else {
                // Retry failed - check if we should retry again
                if (retrySchedule.getCurrentAttempt() >= retrySchedule.getMaxAttempts()) {
                    // Max attempts reached
                    retrySchedule.setStatus(RetryStatus.FAILED);
                    log.warn("Retry failed - max attempts reached: retryScheduleId={}, attempts={}", 
                             retrySchedule.getId(), retrySchedule.getCurrentAttempt());
                } else {
                    // Schedule next retry
                    LocalDateTime nextScheduledAt = calculateScheduledTime(retrySchedule);
                    retrySchedule.setScheduledAt(nextScheduledAt);
                    retrySchedule.setStatus(RetryStatus.PENDING);
                    log.info("Retry failed - scheduling next attempt: retryScheduleId={}, nextScheduledAt={}", 
                             retrySchedule.getId(), nextScheduledAt);
                }
            }
            
            retryScheduleRepository.save(retrySchedule);
        } catch (Exception e) {
            log.error("Error executing retry: retryScheduleId={}", retrySchedule.getId(), e);
            
            // Add error to history
            addErrorToHistory(retrySchedule, e);
            
            // Check if we should retry again
            if (retrySchedule.getCurrentAttempt() >= retrySchedule.getMaxAttempts()) {
                retrySchedule.setStatus(RetryStatus.FAILED);
            } else {
                LocalDateTime nextScheduledAt = calculateScheduledTime(retrySchedule);
                retrySchedule.setScheduledAt(nextScheduledAt);
                retrySchedule.setStatus(RetryStatus.PENDING);
            }
            
            retryScheduleRepository.save(retrySchedule);
        }
    }

    /**
     * Parse retry configuration from node/execution config.
     */
    @SuppressWarnings("unchecked")
    private void parseRetryConfig(RetrySchedule retrySchedule, Map<String, Object> retryConfig) {
        if (retryConfig == null) {
            // Default configuration
            retrySchedule.setRetryStrategy(RetryStrategy.IMMEDIATE);
            retrySchedule.setMaxAttempts(3);
            retrySchedule.setInitialDelaySeconds(0);
            return;
        }
        
        // Parse strategy
        String strategyStr = (String) retryConfig.getOrDefault("strategy", "immediate");
        RetryStrategy strategy = RetryStrategy.fromValue(strategyStr);
        if (strategy == null) {
            strategy = RetryStrategy.IMMEDIATE;
        }
        retrySchedule.setRetryStrategy(strategy);
        
        // Parse max attempts
        Object maxAttemptsObj = retryConfig.get("maxAttempts");
        if (maxAttemptsObj instanceof Number) {
            retrySchedule.setMaxAttempts(((Number) maxAttemptsObj).intValue());
        } else {
            retrySchedule.setMaxAttempts(3);
        }
        
        // Parse timing configuration based on strategy
        switch (strategy) {
            case IMMEDIATE:
                retrySchedule.setInitialDelaySeconds(0);
                break;
                
            case EXPONENTIAL_BACKOFF:
                Object initialDelayObj = retryConfig.get("initialDelaySeconds");
                if (initialDelayObj instanceof Number) {
                    retrySchedule.setInitialDelaySeconds(((Number) initialDelayObj).intValue());
                } else {
                    retrySchedule.setInitialDelaySeconds(60);
                }
                
                Object multiplierObj = retryConfig.get("multiplier");
                if (multiplierObj instanceof Number) {
                    retrySchedule.setMultiplier(BigDecimal.valueOf(((Number) multiplierObj).doubleValue()));
                } else {
                    retrySchedule.setMultiplier(BigDecimal.valueOf(2.0));
                }
                
                Object maxDelayObj = retryConfig.get("maxDelaySeconds");
                if (maxDelayObj instanceof Number) {
                    retrySchedule.setMaxDelaySeconds(((Number) maxDelayObj).intValue());
                }
                break;
                
            case FIXED_DELAY:
                Object delayObj = retryConfig.get("delaySeconds");
                if (delayObj instanceof Number) {
                    retrySchedule.setDelaySeconds(((Number) delayObj).intValue());
                } else {
                    retrySchedule.setDelaySeconds(300);
                }
                break;
                
            case CUSTOM:
                Object customScheduleObj = retryConfig.get("customSchedule");
                if (customScheduleObj instanceof Map) {
                    retrySchedule.setCustomSchedule((Map<String, Object>) customScheduleObj);
                }
                break;
        }
    }

    /**
     * Calculate scheduled time for next retry based on strategy.
     */
    private LocalDateTime calculateScheduledTime(RetrySchedule retrySchedule) {
        RetryStrategyCalculator calculator = retryStrategyFactory.getCalculator(retrySchedule.getRetryStrategy());
        return calculator.calculateNextRetryTime(retrySchedule);
    }

    /**
     * Add error to error history.
     */
    @SuppressWarnings("unchecked")
    private void addErrorToHistory(RetrySchedule retrySchedule, Exception error) {
        Map<String, Object> errorHistory = retrySchedule.getErrorHistory();
        if (errorHistory == null) {
            errorHistory = new HashMap<>();
            errorHistory.put("errors", new java.util.ArrayList<>());
            retrySchedule.setErrorHistory(errorHistory);
        }
        
        List<Map<String, Object>> errors = (List<Map<String, Object>>) errorHistory.get("errors");
        if (errors == null) {
            errors = new java.util.ArrayList<>();
            errorHistory.put("errors", errors);
        }
        
        Map<String, Object> errorEntry = new HashMap<>();
        errorEntry.put("attempt", retrySchedule.getCurrentAttempt());
        errorEntry.put("timestamp", LocalDateTime.now().toString());
        errorEntry.put("message", error.getMessage());
        errorEntry.put("type", error.getClass().getName());
        errors.add(errorEntry);
    }
}

