package com.notificationplatform.service.waitstate;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.service.eventaggregation.EventAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class ExecutionWaitStateServiceImpl implements ExecutionWaitStateService {

    private final ExecutionWaitStateRepository waitStateRepository;
    private final ExecutionRepository executionRepository;
    private final EventAggregationService eventAggregationService;

    public ExecutionWaitStateServiceImpl(ExecutionWaitStateRepository waitStateRepository,
                                        ExecutionRepository executionRepository,
                                        EventAggregationService eventAggregationService) {
        this.waitStateRepository = waitStateRepository;
        this.executionRepository = executionRepository;
        this.eventAggregationService = eventAggregationService;
    }

    @Override
    public ExecutionWaitState createWaitState(String executionId, String nodeId, 
                                             String correlationId, String waitType,
                                             LocalDateTime expiresAt) {
        log.debug("Creating wait state: executionId={}, nodeId={}, correlationId={}, waitType={}", 
                  executionId, nodeId, correlationId, waitType);

        // Check if wait state already exists
        var existing = waitStateRepository.findByExecutionIdAndNodeId(executionId, nodeId);
        if (existing.isPresent()) {
            log.warn("Wait state already exists for executionId={}, nodeId={}", executionId, nodeId);
            return existing.get();
        }

        // Load execution entity
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        // Create new wait state
        ExecutionWaitState waitState = new ExecutionWaitState();
        waitState.setId(UUID.randomUUID().toString());
        waitState.setExecution(execution);
        waitState.setNodeId(nodeId);
        waitState.setCorrelationId(correlationId);
        waitState.setWaitType(waitType);
        waitState.setStatus("waiting");
        waitState.setExpiresAt(expiresAt);
        waitState.setReceivedEvents(new ArrayList<>());

        waitState = waitStateRepository.save(waitState);

        log.info("Created wait state: id={}, executionId={}, nodeId={}, correlationId={}", 
                 waitState.getId(), executionId, nodeId, correlationId);

        return waitState;
    }

    @Override
    @Transactional
    public ExecutionWaitState updateWaitState(String executionId, String correlationId, 
                                             String eventType, Map<String, Object> eventData) {
        log.debug("Updating wait state: executionId={}, correlationId={}, eventType={}", 
                  executionId, correlationId, eventType);

        // Use EventAggregationService to handle the update
        if ("api_response".equals(eventType)) {
            eventAggregationService.handleApiResponse(executionId, correlationId, eventData);
        } else if ("kafka_event".equals(eventType)) {
            // For Kafka events, we need to extract topic from eventData or pass it separately
            // For now, use a default topic or extract from eventData
            String topic = eventData.containsKey("_topic") ? 
                    (String) eventData.get("_topic") : "default";
            eventAggregationService.handleKafkaEvent(topic, eventData);
        }

        // Get updated wait state
        return getWaitStateByExecutionIdAndCorrelationId(executionId, correlationId);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionWaitState getWaitStateByCorrelationId(String correlationId) {
        log.debug("Getting wait state by correlationId: {}", correlationId);

        List<ExecutionWaitState> waitStates = waitStateRepository.findByCorrelationId(correlationId);
        if (waitStates.isEmpty()) {
            return null;
        }
        if (waitStates.size() > 1) {
            log.warn("Multiple wait states found for correlationId: {}, returning first", correlationId);
        }
        return waitStates.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionWaitState getWaitStateByExecutionIdAndCorrelationId(String executionId, String correlationId) {
        log.debug("Getting wait state: executionId={}, correlationId={}", executionId, correlationId);

        return waitStateRepository.findByExecutionIdAndCorrelationIdAndStatus(
                executionId, correlationId, "waiting")
                .orElse(null);
    }

    @Override
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkTimeouts() {
        log.debug("Checking for expired wait states");

        LocalDateTime now = LocalDateTime.now();
        List<ExecutionWaitState> expiredStates = waitStateRepository
                .findByStatusAndExpiresAtBefore("waiting", now);

        if (expiredStates.isEmpty()) {
            return;
        }

        log.info("Found {} expired wait states", expiredStates.size());

        for (ExecutionWaitState waitState : expiredStates) {
            try {
                log.info("Handling timeout for wait state: id={}, executionId={}, nodeId={}", 
                         waitState.getId(), waitState.getExecution().getId(), waitState.getNodeId());
                
                eventAggregationService.handleTimeout(
                        waitState.getExecution().getId(), 
                        waitState.getNodeId());
            } catch (Exception e) {
                log.error("Error handling timeout for wait state: id={}", waitState.getId(), e);
            }
        }
    }
}

