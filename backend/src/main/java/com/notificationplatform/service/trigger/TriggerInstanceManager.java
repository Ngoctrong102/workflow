package com.notificationplatform.service.trigger;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.api.TriggerInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages trigger instances lifecycle.
 * Tracks runtime state of trigger instances separately from trigger configuration.
 * 
 * See: @import(features/trigger-registry.md#trigger-instance-lifecycle)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerInstanceManager {

    private final TriggerRepository triggerRepository;
    private final TriggerInstanceService triggerInstanceService;
    
    // Track runtime state of trigger instances
    // Key: triggerId, Value: instance state (ACTIVE, PAUSED, STOPPED, ERROR)
    private final Map<String, TriggerInstanceState> instanceStates = new ConcurrentHashMap<>();

    /**
     * Initialize trigger instances when workflow is activated.
     * Creates instances but doesn't start them.
     * 
     * @param workflowId Workflow ID
     */
    @Transactional
    public void initializeInstancesForWorkflow(String workflowId) {
        log.info("Initializing trigger instances for workflow: workflowId={}", workflowId);
        
        List<Trigger> triggers = triggerRepository.findByWorkflowId(workflowId);
        
        for (Trigger trigger : triggers) {
            try {
                triggerInstanceService.initializeTrigger(trigger.getId());
                instanceStates.put(trigger.getId(), TriggerInstanceState.INITIALIZED);
                log.info("Initialized trigger instance: triggerId={}", trigger.getId());
            } catch (Exception e) {
                log.error("Error initializing trigger instance: triggerId={}", trigger.getId(), e);
                instanceStates.put(trigger.getId(), TriggerInstanceState.ERROR);
            }
        }
        
        log.info("Initialized trigger instances for workflow: workflowId={}, count={}", 
                 workflowId, triggers.size());
    }

    /**
     * Destroy trigger instances when workflow is deactivated.
     * 
     * @param workflowId Workflow ID
     */
    @Transactional
    public void destroyInstancesForWorkflow(String workflowId) {
        log.info("Destroying trigger instances for workflow: workflowId={}", workflowId);
        
        List<Trigger> triggers = triggerRepository.findByWorkflowId(workflowId);
        
        for (Trigger trigger : triggers) {
            try {
                triggerInstanceService.destroyTrigger(trigger.getId());
                instanceStates.remove(trigger.getId());
                log.info("Destroyed trigger instance: triggerId={}", trigger.getId());
            } catch (Exception e) {
                log.error("Error destroying trigger instance: triggerId={}", trigger.getId(), e);
            }
        }
        
        log.info("Destroyed trigger instances for workflow: workflowId={}, count={}", 
                 workflowId, triggers.size());
    }

    /**
     * Get instance state for a trigger.
     * 
     * @param triggerId Trigger ID
     * @return Instance state, or null if not initialized
     */
    public TriggerInstanceState getInstanceState(String triggerId) {
        return instanceStates.get(triggerId);
    }

    /**
     * Update instance state.
     * 
     * @param triggerId Trigger ID
     * @param state New state
     */
    public void updateInstanceState(String triggerId, TriggerInstanceState state) {
        instanceStates.put(triggerId, state);
        log.debug("Updated trigger instance state: triggerId={}, state={}", triggerId, state);
    }

    /**
     * Handle trigger error and update status.
     * 
     * @param triggerId Trigger ID
     * @param error Error message
     */
    @Transactional
    public void handleTriggerError(String triggerId, String error) {
        log.error("Trigger error: triggerId={}, error={}", triggerId, error);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId).orElse(null);
        if (trigger != null) {
            trigger.setErrorMessage(error);
            triggerRepository.save(trigger);
        }
        
        instanceStates.put(triggerId, TriggerInstanceState.ERROR);
    }

    /**
     * Clear error for trigger.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void clearTriggerError(String triggerId) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId).orElse(null);
        if (trigger != null) {
            trigger.setErrorMessage(null);
            triggerRepository.save(trigger);
        }
    }

    /**
     * Enum representing trigger instance states.
     */
    public enum TriggerInstanceState {
        INITIALIZED,  // Instance created but not started
        ACTIVE,       // Instance is running
        PAUSED,       // Instance is paused
        STOPPED,      // Instance is stopped
        ERROR         // Instance has error
    }
}

