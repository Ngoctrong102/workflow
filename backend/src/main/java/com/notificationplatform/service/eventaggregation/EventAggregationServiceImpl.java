package com.notificationplatform.service.eventaggregation;

import com.notificationplatform.dto.request.WaitForEventsConfigDTO;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.repository.NodeExecutionRepository;


import org.springframework.context.annotation.Lazy;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class EventAggregationServiceImpl implements EventAggregationService {

    private final ExecutionWaitStateRepository waitStateRepository;
    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final WorkflowExecutor workflowExecutor;

    public EventAggregationServiceImpl(ExecutionWaitStateRepository waitStateRepository,
                                      ExecutionRepository executionRepository,
                                      NodeExecutionRepository nodeExecutionRepository,
                                      @Lazy WorkflowExecutor workflowExecutor) {
        this.waitStateRepository = waitStateRepository;
        this.executionRepository = executionRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.workflowExecutor = workflowExecutor;
    }

    @Override
    public ExecutionWaitState registerWaitState(String executionId, String nodeId, WaitForEventsConfigDTO config) {
        log.info("Registering wait state: executionId={}, nodeId={}", executionId, nodeId);

        // Validate execution exists
        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + executionId));

        // Generate unique correlation ID
        String correlationId = UUID.randomUUID().toString();

        // Determine enabled events
        List<String> enabledEvents = new ArrayList<>();
        if (config.getApiCall() != null && Boolean.TRUE.equals(config.getApiCall().getEnabled())) {
            enabledEvents.add("api_response");
        }
        if (config.getKafkaEvent() != null && Boolean.TRUE.equals(config.getKafkaEvent().getEnabled())) {
            enabledEvents.add("kafka_event");
        }

        // Determine required events based on strategy
        List<String> requiredEvents = determineRequiredEvents(config, enabledEvents);

        // Calculate expires_at based on timeout
        int timeoutSeconds = config.getTimeout() != null ? config.getTimeout() : 300;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(timeoutSeconds);

        // Create wait state
        ExecutionWaitState waitState = new ExecutionWaitState();
        waitState.setId(UUID.randomUUID().toString());
        waitState.setExecution(execution);
        waitState.setNodeId(nodeId);
        waitState.setCorrelationId(correlationId);
        waitState.setAggregationStrategy(config.getAggregationStrategy() != null ? 
                                        config.getAggregationStrategy() : "all");
        waitState.setRequiredEvents(requiredEvents);
        waitState.setEnabledEvents(enabledEvents);
        waitState.setApiCallEnabled(config.getApiCall() != null && 
                                   Boolean.TRUE.equals(config.getApiCall().getEnabled()));
        waitState.setKafkaEventEnabled(config.getKafkaEvent() != null && 
                                      Boolean.TRUE.equals(config.getKafkaEvent().getEnabled()));
        waitState.setStatus("waiting");
        waitState.setReceivedEvents(new ArrayList<>());
        waitState.setExpiresAt(expiresAt);

        waitState = waitStateRepository.save(waitState);

        log.info("Wait state registered: waitStateId={}, correlationId={}, executionId={}, nodeId={}", 
                   waitState.getId(), correlationId, executionId, nodeId);

        return waitState;
    }

    @Override
    public void handleApiResponse(String executionId, String correlationId, Map<String, Object> responseData) {
        log.debug("Handling API response: executionId={}, correlationId={}", executionId, correlationId);

        // Validate both IDs are present
        if (executionId == null || correlationId == null) {
            log.warn("API response missing execution_id or correlation_id: executionId={}, correlationId={}", 
                       executionId, correlationId);
            return;
        }

        // Find wait state with lock
        Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                .findByExecutionIdAndCorrelationIdAndStatus(executionId, correlationId, "waiting");

        if (waitStateOpt.isEmpty()) {
            log.debug("No waiting execution found for API response: executionId={}, correlationId={}", 
                        executionId, correlationId);
            return;
        }

        ExecutionWaitState waitState = waitStateOpt.get();

        // Validate execution_id matches (prevent cross-execution contamination)
        if (!executionId.equals(waitState.getExecution().getId())) {
            log.error("Execution ID mismatch in API response: expected={}, actual={}, correlationId={}", 
                        waitState.getExecution().getId(), executionId, correlationId);
            return;
        }

        // Check if already received (idempotency)
        List<String> receivedEvents = waitState.getReceivedEvents() != null ? 
                                      waitState.getReceivedEvents() : new ArrayList<>();
        if (receivedEvents.contains("api_response")) {
            log.debug("API response already received: executionId={}, correlationId={}", 
                        executionId, correlationId);
            return;
        }

        // Store API response data
        waitState.setApiResponseData(responseData);

        // Add to received events
        receivedEvents.add("api_response");
        waitState.setReceivedEvents(receivedEvents);

        // Update wait state
        waitState = waitStateRepository.save(waitState);

        log.info("API response received: executionId={}, correlationId={}", executionId, correlationId);

        // Check completion condition
        if (isCompletionConditionMet(waitState)) {
            checkAndResumeExecution(executionId, waitState.getNodeId());
        }
    }

    @Override
    public void handleKafkaEvent(String topic, Map<String, Object> eventData) {
        // Extract execution_id and correlation_id from event data
        String executionId = extractField(eventData, "execution_id");
        String correlationId = extractField(eventData, "correlation_id");

        log.debug("Handling Kafka event: topic={}, executionId={}, correlationId={}", 
                    topic, executionId, correlationId);

        // Validate both are present
        if (executionId == null || correlationId == null) {
            log.debug("Kafka event missing execution_id or correlation_id: topic={}", topic);
            return;
        }

        // Find wait state with lock
        Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                .findByExecutionIdAndCorrelationIdAndStatus(executionId, correlationId, "waiting");

        if (waitStateOpt.isEmpty()) {
            log.debug("No waiting execution found for Kafka event: executionId={}, correlationId={}", 
                        executionId, correlationId);
            return;
        }

        ExecutionWaitState waitState = waitStateOpt.get();

        // Validate execution_id matches (prevent cross-execution contamination)
        if (!executionId.equals(waitState.getExecution().getId())) {
            log.error("Execution ID mismatch in Kafka event: expected={}, actual={}, correlationId={}", 
                        waitState.getExecution().getId(), executionId, correlationId);
            return;
        }

        // Validate kafka_event_enabled is true
        if (!Boolean.TRUE.equals(waitState.getKafkaEventEnabled())) {
            log.warn("Kafka event received but not enabled for wait state: executionId={}, nodeId={}", 
                       executionId, waitState.getNodeId());
            return;
        }


        // Check if already received (idempotency)
        List<String> receivedEvents = waitState.getReceivedEvents() != null ? 
                                      waitState.getReceivedEvents() : new ArrayList<>();
        if (receivedEvents.contains("kafka_event")) {
            log.debug("Kafka event already received: executionId={}, correlationId={}", 
                        executionId, correlationId);
            return;
        }

        // Store Kafka event data
        waitState.setKafkaEventData(eventData);

        // Add to received events
        receivedEvents.add("kafka_event");
        waitState.setReceivedEvents(receivedEvents);

        // Update wait state
        waitState = waitStateRepository.save(waitState);

        log.info("Kafka event received: executionId={}, correlationId={}, topic={}", 
                   executionId, correlationId, topic);

        // Check completion condition
        if (isCompletionConditionMet(waitState)) {
            checkAndResumeExecution(executionId, waitState.getNodeId());
        }
    }

    @Override
    public boolean isCompletionConditionMet(ExecutionWaitState waitState) {
        List<String> receivedEvents = waitState.getReceivedEvents() != null ? 
                                     waitState.getReceivedEvents() : new ArrayList<>();
        List<String> enabledEvents = waitState.getEnabledEvents() != null ? 
                                    waitState.getEnabledEvents() : new ArrayList<>();
        List<String> requiredEvents = waitState.getRequiredEvents() != null ? 
                                     waitState.getRequiredEvents() : new ArrayList<>();

        String strategy = waitState.getAggregationStrategy();

        switch (strategy) {
            case "all":
                // All enabled events received
                return receivedEvents.containsAll(enabledEvents) && !enabledEvents.isEmpty();
            
            case "any":
                // At least one enabled event received
                return !receivedEvents.isEmpty() && 
                       receivedEvents.stream().anyMatch(enabledEvents::contains);
            
            case "required_only":
                // All required events received
                return receivedEvents.containsAll(requiredEvents) && !requiredEvents.isEmpty();
            
            case "custom":
                // All events in required_events array received
                return receivedEvents.containsAll(requiredEvents) && !requiredEvents.isEmpty();
            
            default:
                log.warn("Unknown aggregation strategy: {}", strategy);
                return false;
        }
    }

    @Override
    public boolean checkAndResumeExecution(String executionId, String nodeId) {
        log.info("Checking and resuming execution: executionId={}, nodeId={}", executionId, nodeId);

        // Load wait state with current version
        Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                .findByExecutionIdAndNodeId(executionId, nodeId);

        if (waitStateOpt.isEmpty()) {
            log.warn("Wait state not found: executionId={}, nodeId={}", executionId, nodeId);
            return false;
        }

        ExecutionWaitState waitState = waitStateOpt.get();

        // Check if already resumed (idempotency check via resumed_at)
        if (waitState.getResumedAt() != null) {
            log.debug("Execution already resumed: executionId={}, nodeId={}, resumedAt={}", 
                        executionId, nodeId, waitState.getResumedAt());
            return false;
        }

        // Check completion condition
        if (!isCompletionConditionMet(waitState)) {
            log.debug("Completion condition not met: executionId={}, nodeId={}", executionId, nodeId);
            return false;
        }

        // Use optimistic locking: increment version
        int currentVersion = waitState.getVersion();
        waitState.setStatus("resuming");
        waitState.setResumedAt(LocalDateTime.now());
        waitState.setResumedBy(getInstanceId());
        waitState.setVersion(currentVersion + 1);

        try {
            // This will fail if version changed (another instance updated it)
            waitState = waitStateRepository.save(waitState);

            // Successfully acquired lock, proceed with resume
            // Aggregate data from received events
            Map<String, Object> aggregatedData = aggregateEventData(waitState);

            // Resume workflow execution
            workflowExecutor.resumeExecution(executionId, nodeId, aggregatedData);

            // Set status to completed
            waitState.setStatus("completed");
            waitStateRepository.save(waitState);

            log.info("Execution resumed successfully: executionId={}, nodeId={}, instance={}", 
                       executionId, nodeId, getInstanceId());

            return true;

        } catch (OptimisticLockingFailureException e) {
            // Another instance already resumed
            log.debug("Another instance already resumed: executionId={}, nodeId={}", 
                        executionId, nodeId);
            return false;
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void handleTimeout(String executionId, String nodeId) {
        log.info("Handling timeout: executionId={}, nodeId={}", executionId, nodeId);

        Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                .findByExecutionIdAndNodeId(executionId, nodeId);

        if (waitStateOpt.isEmpty()) {
            log.warn("Wait state not found for timeout: executionId={}, nodeId={}", executionId, nodeId);
            return;
        }

        ExecutionWaitState waitState = waitStateOpt.get();

        // Check if already processed (status != 'waiting')
        if (!"waiting".equals(waitState.getStatus())) {
            log.debug("Wait state already processed (may have been handled by another instance): " +
                        "executionId={}, nodeId={}, status={}", executionId, nodeId, waitState.getStatus());
            return;
        }

        // Acquire lock
        Optional<ExecutionWaitState> lockedStateOpt = waitStateRepository
                .findByIdWithLock(waitState.getId());

        if (lockedStateOpt.isEmpty() || !"waiting".equals(lockedStateOpt.get().getStatus())) {
            log.debug("Wait state already processed by another instance: executionId={}, nodeId={}", 
                        executionId, nodeId);
            return;
        }

        ExecutionWaitState lockedState = lockedStateOpt.get();

        // Get timeout behavior from workflow definition
        String onTimeout = getOnTimeoutFromWorkflow(executionId, nodeId);
        if (onTimeout == null || onTimeout.isEmpty()) {
            onTimeout = "fail"; // Default behavior
        }

        log.info("Timeout behavior: executionId={}, nodeId={}, onTimeout={}", 
                   executionId, nodeId, onTimeout);

        // Determine if we can continue based on received events and aggregation strategy
        boolean canContinue = canContinueOnTimeout(lockedState, onTimeout);

        if ("fail".equals(onTimeout) || !canContinue) {
            // Fail: Mark as timeout and stop workflow
            handleTimeoutFail(lockedState, executionId, nodeId);
        } else if ("continue".equals(onTimeout) || "continue_with_partial".equals(onTimeout)) {
            // Continue: Aggregate available data and resume execution
            boolean isPartial = "continue_with_partial".equals(onTimeout);
            handleTimeoutContinue(lockedState, executionId, nodeId, isPartial);
        } else {
            // Unknown behavior, default to fail
            log.warn("Unknown timeout behavior: {}, defaulting to fail", onTimeout);
            handleTimeoutFail(lockedState, executionId, nodeId);
        }

        log.info("Timeout handled: executionId={}, nodeId={}, behavior={}", 
                   executionId, nodeId, onTimeout);
    }

    /**
     * Handle timeout with fail behavior
     */
    private void handleTimeoutFail(ExecutionWaitState waitState, String executionId, String nodeId) {
        try {
            waitState.setStatus("timeout");
            waitState.setResumedAt(LocalDateTime.now());
            waitState.setResumedBy(getInstanceId());
            waitStateRepository.save(waitState);

            // Mark node execution as failed
            markNodeExecutionFailed(executionId, nodeId, "Timeout waiting for events");

            // Update execution status
            Execution execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null && execution.getStatus() == ExecutionStatus.WAITING) {
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setError("Timeout waiting for events");
                execution.setCompletedAt(LocalDateTime.now());
                
                // Calculate duration from start to completion
                if (execution.getStartedAt() != null) {
                    long duration = java.time.Duration.between(
                        execution.getStartedAt(), 
                        LocalDateTime.now()
                    ).toMillis();
                    execution.setDuration((int) duration);
                }
                
                executionRepository.save(execution);
            } else if (execution != null) {
                log.debug("Execution not in waiting status, skipping status update: " +
                           "executionId={}, status={}", executionId, execution.getStatus());
            }

            log.info("Timeout handled with fail behavior: executionId={}, nodeId={}, instance={}", 
                       executionId, nodeId, getInstanceId());
        } catch (Exception e) {
            log.error("Error handling timeout fail: executionId={}, nodeId={}", executionId, nodeId, e);
            throw new RuntimeException("Error handling timeout fail: " + e.getMessage(), e);
        }
    }

    /**
     * Handle timeout with continue behavior
     */
    private void handleTimeoutContinue(ExecutionWaitState waitState, String executionId, String nodeId, boolean isPartial) {
        try {
            if (isPartial) {
                log.warn("Continuing with partial data on timeout: executionId={}, nodeId={}, " +
                           "receivedEvents={}, enabledEvents={}", 
                           executionId, nodeId, 
                           waitState.getReceivedEvents() != null ? waitState.getReceivedEvents().size() : 0,
                           waitState.getEnabledEvents() != null ? waitState.getEnabledEvents().size() : 0);
            }

            // Aggregate available data
            Map<String, Object> aggregatedData = aggregateAvailableData(waitState);

            // Mark wait state as completed
            waitState.setStatus("completed");
            waitState.setResumedAt(LocalDateTime.now());
            waitState.setResumedBy(getInstanceId());
            waitStateRepository.save(waitState);

            // Resume workflow execution with available data
            workflowExecutor.resumeExecution(executionId, nodeId, aggregatedData);

            log.info("Timeout handled with continue behavior: executionId={}, nodeId={}, isPartial={}, instance={}", 
                       executionId, nodeId, isPartial, getInstanceId());
        } catch (Exception e) {
            log.error("Error handling timeout continue: executionId={}, nodeId={}, isPartial={}", 
                        executionId, nodeId, isPartial, e);
            // Try to mark as failed if resume fails
            try {
                waitState.setStatus("failed");
                waitState.setResumedAt(LocalDateTime.now());
                waitState.setResumedBy(getInstanceId());
                waitStateRepository.save(waitState);
                markNodeExecutionFailed(executionId, nodeId, "Error resuming after timeout: " + e.getMessage());
            } catch (Exception e2) {
                log.error("Error marking wait state as failed after resume error: executionId={}, nodeId={}", 
                           executionId, nodeId, e2);
            }
            throw new RuntimeException("Error handling timeout continue: " + e.getMessage(), e);
        }
    }

    /**
     * Check if we can continue on timeout based on received events and aggregation strategy
     */
    private boolean canContinueOnTimeout(ExecutionWaitState waitState, String onTimeout) {
        if ("continue_with_partial".equals(onTimeout)) {
            // Always allow continue_with_partial
            return true;
        }

        if (!"continue".equals(onTimeout)) {
            return false;
        }

        // For "continue", check if we have at least some events
        List<String> receivedEvents = waitState.getReceivedEvents() != null ? 
                                     waitState.getReceivedEvents() : new ArrayList<>();
        
        if (receivedEvents.isEmpty()) {
            // No events received, cannot continue
            return false;
        }

        // Check aggregation strategy
        String strategy = waitState.getAggregationStrategy();
        List<String> requiredEvents = waitState.getRequiredEvents() != null ? 
                                     waitState.getRequiredEvents() : new ArrayList<>();

        switch (strategy) {
            case "all":
                // For "all", we need all enabled events
                List<String> enabledEvents = waitState.getEnabledEvents() != null ? 
                                             waitState.getEnabledEvents() : new ArrayList<>();
                return receivedEvents.containsAll(enabledEvents);
            
            case "any":
                // For "any", we just need at least one event (already checked)
                return true;
            
            case "required_only":
            case "custom":
                // For "required_only" and "custom", we need all required events
                return !requiredEvents.isEmpty() && receivedEvents.containsAll(requiredEvents);
            
            default:
                return false;
        }
    }

    /**
     * Aggregate available data from received events
     */
    private Map<String, Object> aggregateAvailableData(ExecutionWaitState waitState) {
        Map<String, Object> aggregated = new HashMap<>();

        // Add API response data if available
        if (waitState.getApiResponseData() != null && !waitState.getApiResponseData().isEmpty()) {
            aggregated.put("api_response", waitState.getApiResponseData());
        }

        // Add Kafka event data if available
        if (waitState.getKafkaEventData() != null && !waitState.getKafkaEventData().isEmpty()) {
            aggregated.put("kafka_event", waitState.getKafkaEventData());
        }

        // Add metadata
        aggregated.put("correlation_id", waitState.getCorrelationId());
        if (waitState.getExecution() != null) {
            aggregated.put("execution_id", waitState.getExecution().getId());
        }
        aggregated.put("timeout", true);
        
        // Determine if data is partial
        List<String> receivedEvents = waitState.getReceivedEvents() != null ? 
                                     waitState.getReceivedEvents() : new ArrayList<>();
        List<String> enabledEvents = waitState.getEnabledEvents() != null ? 
                                    waitState.getEnabledEvents() : new ArrayList<>();
        boolean isPartial = receivedEvents.size() < enabledEvents.size();
        aggregated.put("partial", isPartial);
        
        // Add received events info for debugging
        aggregated.put("received_events", receivedEvents);
        aggregated.put("enabled_events", enabledEvents);
        aggregated.put("aggregation_strategy", waitState.getAggregationStrategy());

        log.debug("Aggregated available data: executionId={}, nodeId={}, hasApiResponse={}, " +
                    "hasKafkaEvent={}, isPartial={}, receivedEventsCount={}, enabledEventsCount={}",
                    waitState.getExecution() != null ? waitState.getExecution().getId() : "unknown",
                    waitState.getNodeId(),
                    waitState.getApiResponseData() != null && !waitState.getApiResponseData().isEmpty(),
                    waitState.getKafkaEventData() != null && !waitState.getKafkaEventData().isEmpty(),
                    isPartial,
                    receivedEvents.size(),
                    enabledEvents.size());

        return aggregated;
    }

    /**
     * Mark node execution as failed
     */
    private void markNodeExecutionFailed(String executionId, String nodeId, String errorMessage) {
        try {
            List<com.notificationplatform.entity.NodeExecution> nodeExecutions = 
                nodeExecutionRepository.findByExecutionIdAndNodeId(executionId, nodeId);

            boolean found = false;
            for (com.notificationplatform.entity.NodeExecution nodeExecution : nodeExecutions) {
                if (nodeId.equals(nodeExecution.getNodeId()) && 
                    ("waiting".equals(nodeExecution.getStatus()) || 
                     "waiting_for_events".equals(nodeExecution.getStatus()))) {
                    nodeExecution.setStatus("failed");
                    nodeExecution.setError(errorMessage);
                    nodeExecution.setCompletedAt(LocalDateTime.now());
                    
                    // Calculate duration from start to completion
                    if (nodeExecution.getStartedAt() != null) {
                        long duration = java.time.Duration.between(
                            nodeExecution.getStartedAt(), 
                            LocalDateTime.now()
                        ).toMillis();
                        nodeExecution.setDuration((int) duration);
                    }
                    
                    nodeExecutionRepository.save(nodeExecution);
                    found = true;
                    log.info("Node execution marked as failed: executionId={}, nodeId={}, error={}, duration={}ms", 
                               executionId, nodeId, errorMessage, nodeExecution.getDuration());
                    break;
                }
            }

            if (!found) {
                log.warn("Node execution not found or not in waiting status for marking as failed: " +
                           "executionId={}, nodeId={}, availableExecutions={}", 
                           executionId, nodeId, nodeExecutions.size());
            }
        } catch (Exception e) {
            log.error("Error marking node execution as failed: executionId={}, nodeId={}, error={}", 
                        executionId, nodeId, errorMessage, e);
            // Don't throw - this is a helper method, let caller handle errors
        }
    }

    /**
     * Get onTimeout configuration from workflow definition
     */
    private String getOnTimeoutFromWorkflow(String executionId, String nodeId) {
        try {
            Execution execution = executionRepository.findById(executionId).orElse(null);
            if (execution == null || execution.getWorkflow() == null) {
                return null;
            }

            Workflow workflow = execution.getWorkflow();
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            
            if (definition == null) {
                return null;
            }

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
            
            if (nodes == null) {
                return null;
            }

            // Find the node
            for (Map<String, Object> node : nodes) {
                String nodeIdFromDef = (String) node.get("id");
                if (nodeId.equals(nodeIdFromDef)) {
                    Map<String, Object> nodeData = node.containsKey("data") ? 
                        (Map<String, Object>) node.get("data") : new HashMap<>();
                    
                    return (String) nodeData.get("onTimeout");
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("Error getting onTimeout from workflow: executionId={}, nodeId={}", 
                       executionId, nodeId, e);
            return null;
        }
    }

    @Override
    public String getCorrelationId(String executionId, String nodeId) {
        Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                .findByExecutionIdAndNodeId(executionId, nodeId);

        if (waitStateOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Wait state not found for executionId: " + executionId + ", nodeId: " + nodeId);
        }

        return waitStateOpt.get().getCorrelationId();
    }

    /**
     * Determine required events based on aggregation strategy and config
     */
    private List<String> determineRequiredEvents(WaitForEventsConfigDTO config, List<String> enabledEvents) {
        String strategy = config.getAggregationStrategy() != null ? 
                         config.getAggregationStrategy() : "all";

        switch (strategy) {
            case "all":
                return new ArrayList<>(enabledEvents);
            
            case "any":
                return new ArrayList<>(); // No specific requirements
            
            case "required_only":
                List<String> required = new ArrayList<>();
                if (config.getApiCall() != null && Boolean.TRUE.equals(config.getApiCall().getRequired())) {
                    required.add("api_response");
                }
                if (config.getKafkaEvent() != null && Boolean.TRUE.equals(config.getKafkaEvent().getRequired())) {
                    required.add("kafka_event");
                }
                return required;
            
            case "custom":
                return config.getRequiredEvents() != null ? 
                      new ArrayList<>(config.getRequiredEvents()) : new ArrayList<>();
            
            default:
                return new ArrayList<>(enabledEvents);
        }
    }

    /**
     * Aggregate data from received events
     */
    private Map<String, Object> aggregateEventData(ExecutionWaitState waitState) {
        Map<String, Object> aggregated = new HashMap<>();

        if (waitState.getApiResponseData() != null) {
            aggregated.put("api_response", waitState.getApiResponseData());
        }

        if (waitState.getKafkaEventData() != null) {
            aggregated.put("kafka_event", waitState.getKafkaEventData());
        }

        aggregated.put("correlation_id", waitState.getCorrelationId());
        aggregated.put("received_events", waitState.getReceivedEvents());

        return aggregated;
    }

    /**
     * Extract field from nested JSON structure
     * Supports dot notation (e.g., "data.correlation_id")
     */
    private String extractField(Map<String, Object> data, String fieldName) {
        if (data == null || fieldName == null) {
            return null;
        }

        // Check direct field
        if (data.containsKey(fieldName)) {
            Object value = data.get(fieldName);
            return value != null ? value.toString() : null;
        }

        // Check nested field (dot notation)
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.", 2);
            Object nested = data.get(parts[0]);
            if (nested instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) nested;
                return extractField(nestedMap, parts[1]);
            }
        }

        return null;
    }

    /**
     * Process expired wait states
     * Runs every minute to check for expired wait states
     * Only one instance processes each expired state (via locking)
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    @org.springframework.transaction.annotation.Transactional
    public void processExpiredWaitStates() {
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        
        List<ExecutionWaitState> expiredStates = waitStateRepository
                .findByStatusAndExpiresAtBefore("waiting", now);

        if (expiredStates.isEmpty()) {
            log.debug("No expired wait states found at {}", now);
            return;
        }

        log.info("Found {} expired wait states to process at {}", expiredStates.size(), now);

        int processed = 0;
        int failed = 0;
        int skipped = 0;
        String instanceId = getInstanceId();

        for (ExecutionWaitState state : expiredStates) {
            String executionId = state.getExecution() != null ? state.getExecution().getId() : "unknown";
            String nodeId = state.getNodeId();
            
            try {
                // Try to acquire lock (only one instance will succeed)
                Optional<ExecutionWaitState> lockedStateOpt = waitStateRepository
                        .findByIdWithLock(state.getId());

                if (lockedStateOpt.isPresent()) {
                    ExecutionWaitState lockedState = lockedStateOpt.get();
                    if ("waiting".equals(lockedState.getStatus())) {
                        // This instance acquired the lock
                        log.debug("Processing expired wait state: executionId={}, nodeId={}, instance={}, expiresAt={}", 
                                   executionId, nodeId, instanceId, lockedState.getExpiresAt());
                        handleTimeout(executionId, nodeId);
                        processed++;
                    } else {
                        // Already processed by another instance
                        skipped++;
                        log.debug("Wait state already processed by another instance: executionId={}, nodeId={}, status={}", 
                                   executionId, nodeId, lockedState.getStatus());
                    }
                } else {
                    // Lock acquisition failed (another instance processing)
                    skipped++;
                    log.debug("Could not acquire lock for wait state (another instance processing): " +
                               "executionId={}, nodeId={}", executionId, nodeId);
                }
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                // Another instance processed this
                skipped++;
                log.debug("Optimistic locking failure (another instance processed): executionId={}, nodeId={}", 
                           executionId, nodeId);
            } catch (Exception e) {
                failed++;
                log.error("Error processing expired wait state: executionId={}, nodeId={}, instance={}", 
                           executionId, nodeId, instanceId, e);
                // Continue processing other expired states
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Processed expired wait states: total={}, processed={}, failed={}, skipped={}, " +
                   "time={}ms, instance={}", 
                   expiredStates.size(), processed, failed, skipped, processingTime, instanceId);
    }

    /**
     * Get unique instance identifier
     */
    private String getInstanceId() {
        String instanceId = System.getenv("INSTANCE_ID");
        if (instanceId != null && !instanceId.isEmpty()) {
            return instanceId;
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.warn("Failed to get hostname for instance ID", e);
            return "unknown";
        }
    }
}

