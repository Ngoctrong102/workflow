package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.ExecutionContextCache;
import com.notificationplatform.engine.ExecutionStateService;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Executor for Delay node.
 * Waits for specified time duration and supports pause/resume with persistent state.
 * 
 * See: @import(features/workflow-execution-state.md#delay-node-with-persistent-state)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayNodeExecutor implements NodeExecutor {

    private final ExecutionWaitStateRepository waitStateRepository;
    private final ExecutionContextCache contextCache;
    private final ExecutionStateService executionStateService;
    private final TaskScheduler taskScheduler;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing delay node: nodeId={}, executionId={}", nodeId, context.getExecutionId());
        
        DelayNodeConfig config = parseConfig(nodeData);
        long delaySeconds = convertToSeconds(config.getDuration(), config.getUnit());
        
        if (delaySeconds <= 0) {
            log.warn("Invalid delay duration: delaySeconds={}, nodeId={}", delaySeconds, nodeId);
            Map<String, Object> output = new HashMap<>();
            output.put("delayCompleted", true);
            output.put("delaySeconds", 0);
            return new NodeExecutionResult(true, output);
        }
        
        // For short delays (< 1 minute), use Thread.sleep
        // For longer delays, create wait state and schedule resume
        boolean persistState = config.isPersistState() || delaySeconds > 60;
        
        if (persistState) {
            return createWaitState(nodeId, context, delaySeconds);
        } else {
            // Short delay - use Thread.sleep
            try {
                Thread.sleep(delaySeconds * 1000);
                log.info("Delay completed: delaySeconds={}, nodeId={}", delaySeconds, nodeId);
                Map<String, Object> output = new HashMap<>();
                output.put("delayCompleted", true);
                output.put("delaySeconds", delaySeconds);
                return new NodeExecutionResult(true, output);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Delay interrupted: nodeId={}", nodeId, e);
                Map<String, Object> output = new HashMap<>();
                output.put("delayCompleted", false);
                output.put("error", "Delay interrupted");
                NodeExecutionResult result = new NodeExecutionResult(false, output);
                result.setError("Delay interrupted");
                return result;
            }
        }
    }

    /**
     * Create wait state for delay and schedule resume.
     */
    private NodeExecutionResult createWaitState(String nodeId, ExecutionContext context, long delaySeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resumeAt = now.plusSeconds(delaySeconds);
        LocalDateTime expiresAt = now.plusDays(1); // Max 1 day delay
        
        // Get execution entity
        var executionOpt = executionStateService.getExecution(context.getExecutionId());
        if (executionOpt.isEmpty()) {
            log.error("Execution not found: executionId={}", context.getExecutionId());
            Map<String, Object> output = new HashMap<>();
            output.put("error", "Execution not found");
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError("Execution not found");
            return result;
        }
        
        // Create wait state
        ExecutionWaitState waitState = new ExecutionWaitState();
        waitState.setId(UUID.randomUUID().toString());
        waitState.setExecution(executionOpt.get());
        waitState.setNodeId(nodeId);
        waitState.setCorrelationId(UUID.randomUUID().toString());
        waitState.setWaitType("DELAY");
        waitState.setStatus("waiting");
        waitState.setExpiresAt(expiresAt);
        
        waitState = waitStateRepository.save(waitState);
        
        // Persist context to database
        contextCache.persistContext(context.getExecutionId(), context);
        
        // Update execution status to PAUSED
        executionStateService.updateExecutionStatus(context.getExecutionId(), ExecutionStatus.PAUSED);
        
        // Schedule resume task
        String waitStateId = waitState.getId();
        String executionId = context.getExecutionId();
        if (executionId != null && waitStateId != null) {
            java.time.Instant resumeInstant = java.time.Instant.from(resumeAt.atZone(java.time.ZoneId.systemDefault()));
            taskScheduler.schedule(
                () -> resumeDelayExecution(waitStateId, executionId, nodeId),
                resumeInstant
            );
        }
        
        log.info("Delay wait state created: waitStateId={}, executionId={}, nodeId={}, resumeAt={}", 
                 waitStateId, executionId, nodeId, resumeAt);
        
        Map<String, Object> output = new HashMap<>();
        output.put("waitStateId", waitStateId);
        output.put("correlationId", waitState.getCorrelationId());
        output.put("resumeAt", resumeAt.toString());
        output.put("status", "waiting");
        
        NodeExecutionResult result = new NodeExecutionResult(true, output);
        result.setWaiting(true);
        return result;
    }

    /**
     * Resume delay execution.
     */
    private void resumeDelayExecution(String waitStateId, String executionId, String nodeId) {
        log.info("Resuming delay execution: waitStateId={}, executionId={}, nodeId={}", 
                 waitStateId, executionId, nodeId);
        
        // Update wait state status
        waitStateRepository.findById(waitStateId).ifPresent(waitState -> {
            waitState.setStatus("completed");
            waitState.setResumedAt(LocalDateTime.now());
            waitStateRepository.save(waitState);
        });
        
        // Update execution status to RUNNING
        executionStateService.updateExecutionStatus(executionId, ExecutionStatus.RUNNING);
        
        // Note: Actual workflow resume is handled by WorkflowExecutor.resumeExecution()
        // This method just marks the delay as completed
    }

    /**
     * Parse delay node configuration.
     */
    private DelayNodeConfig parseConfig(Map<String, Object> nodeData) {
        DelayNodeConfig config = new DelayNodeConfig();
        
        Object durationObj = nodeData.get("duration");
        if (durationObj instanceof Number) {
            config.setDuration(((Number) durationObj).longValue());
        } else if (durationObj instanceof String) {
            try {
                config.setDuration(Long.parseLong((String) durationObj));
            } catch (NumberFormatException e) {
                log.warn("Invalid duration format: {}", durationObj);
            }
        }
        
        String unit = (String) nodeData.getOrDefault("unit", "seconds");
        config.setUnit(unit);
        
        Object persistStateObj = nodeData.get("persistState");
        if (persistStateObj instanceof Boolean) {
            config.setPersistState((Boolean) persistStateObj);
        }
        
        return config;
    }

    /**
     * Convert duration to seconds.
     */
    private long convertToSeconds(long duration, String unit) {
        return switch (unit.toLowerCase()) {
            case "seconds" -> duration;
            case "minutes" -> duration * 60;
            case "hours" -> duration * 3600;
            case "days" -> duration * 86400;
            default -> duration; // Default to seconds
        };
    }

    /**
     * Delay node configuration.
     */
    private static class DelayNodeConfig {
        private long duration = 0;
        private String unit = "seconds";
        private boolean persistState = false;

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public boolean isPersistState() {
            return persistState;
        }

        public void setPersistState(boolean persistState) {
            this.persistState = persistState;
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LOGIC;
    }
}

