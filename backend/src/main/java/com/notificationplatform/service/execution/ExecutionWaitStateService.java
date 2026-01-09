package com.notificationplatform.service.execution;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing execution wait states.
 * Handles creation, updates, and timeout checking for wait states.
 * 
 * See: @import(features/workflow-execution-state.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionWaitStateService {

    private final ExecutionWaitStateRepository waitStateRepository;
    private final ExecutionRepository executionRepository;
    private final ExecutionResumeService resumeService;

    /**
     * Create wait state for delay/wait-for-events node.
     * 
     * @param execution Execution entity
     * @param nodeId Node ID that is waiting
     * @param waitType Wait type (DELAY, WAIT_EVENTS, etc.)
     * @param expiresAt Expiration time
     * @param context Execution context
     * @return Created wait state
     */
    @Transactional
    public ExecutionWaitState createWaitState(Execution execution, String nodeId, String waitType,
                                               LocalDateTime expiresAt, ExecutionContext context) {
        log.info("Creating wait state: executionId={}, nodeId={}, waitType={}", 
                 execution.getId(), nodeId, waitType);
        
        ExecutionWaitState waitState = new ExecutionWaitState();
        waitState.setId(UUID.randomUUID().toString());
        waitState.setExecution(execution);
        waitState.setNodeId(nodeId);
        waitState.setCorrelationId(UUID.randomUUID().toString());
        waitState.setWaitType(waitType);
        waitState.setStatus("waiting");
        waitState.setExpiresAt(expiresAt);
        waitState.setCreatedAt(LocalDateTime.now());
        waitState.setUpdatedAt(LocalDateTime.now());
        
        waitState = waitStateRepository.save(waitState);
        
        // Update execution status to PAUSED
        execution.setStatus(ExecutionStatus.PAUSED);
        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);
        
        log.info("Wait state created: waitStateId={}, executionId={}", waitState.getId(), execution.getId());
        return waitState;
    }

    /**
     * Update wait state when events are received.
     * 
     * @param waitStateId Wait state ID
     * @param eventType Event type (api_response, kafka_event)
     * @param eventData Event data
     */
    @Transactional
    public void updateWaitState(String waitStateId, String eventType, Map<String, Object> eventData) {
        log.info("Updating wait state: waitStateId={}, eventType={}", waitStateId, eventType);
        
        ExecutionWaitState waitState = waitStateRepository.findById(waitStateId)
                .orElseThrow(() -> new RuntimeException("Wait state not found: " + waitStateId));
        
        // Update event data
        if ("api_response".equals(eventType)) {
            waitState.setApiResponseData(eventData);
        } else if ("kafka_event".equals(eventType)) {
            waitState.setKafkaEventData(eventData);
        }
        
        // Update received events list
        List<String> receivedEvents = waitState.getReceivedEvents();
        if (receivedEvents == null) {
            receivedEvents = new java.util.ArrayList<>();
        }
        if (!receivedEvents.contains(eventType)) {
            receivedEvents.add(eventType);
        }
        waitState.setReceivedEvents(receivedEvents);
        
        waitState.setUpdatedAt(LocalDateTime.now());
        waitStateRepository.save(waitState);
        
        log.info("Wait state updated: waitStateId={}, receivedEvents={}", waitStateId, receivedEvents);
    }

    /**
     * Check for expired wait states and handle timeouts.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void checkTimeouts() {
        log.debug("Checking for expired wait states");
        
        LocalDateTime now = LocalDateTime.now();
        List<ExecutionWaitState> expiredWaitStates = waitStateRepository
                .findByExpiresAtLessThanEqualAndStatus(now, "waiting");
        
        for (ExecutionWaitState waitState : expiredWaitStates) {
            log.warn("Wait state expired: waitStateId={}, executionId={}, nodeId={}", 
                     waitState.getId(), waitState.getExecution().getId(), waitState.getNodeId());
            
            // Update wait state status to timeout
            waitState.setStatus("timeout");
            waitState.setUpdatedAt(now);
            waitStateRepository.save(waitState);
            
            // Update execution status to FAILED
            Execution execution = waitState.getExecution();
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError("Wait state timeout: " + waitState.getWaitType());
            execution.setUpdatedAt(now);
            executionRepository.save(execution);
        }
        
        if (!expiredWaitStates.isEmpty()) {
            log.info("Handled {} expired wait states", expiredWaitStates.size());
        }
    }

    /**
     * Mark wait state as completed.
     * 
     * @param waitStateId Wait state ID
     */
    @Transactional
    public void markWaitStateCompleted(String waitStateId) {
        log.info("Marking wait state as completed: waitStateId={}", waitStateId);
        
        ExecutionWaitState waitState = waitStateRepository.findById(waitStateId)
                .orElseThrow(() -> new RuntimeException("Wait state not found: " + waitStateId));
        
        waitState.setStatus("completed");
        waitState.setResumedAt(LocalDateTime.now());
        waitState.setUpdatedAt(LocalDateTime.now());
        waitStateRepository.save(waitState);
    }
}

