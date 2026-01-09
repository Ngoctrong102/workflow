package com.notificationplatform.service.trigger.api;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.handler.TriggerHandler;
import com.notificationplatform.service.trigger.handler.TriggerHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing trigger instances.
 * Handles trigger lifecycle: initialize, start, pause, resume, stop, destroy.
 * 
 * See: @import(features/trigger-registry.md#trigger-instance-lifecycle)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerInstanceService {

    private final TriggerRepository triggerRepository;
    private final TriggerHandlerRegistry handlerRegistry;
    private final TriggerEndpointRegistry endpointRegistry;

    /**
     * Initialize trigger instance.
     * Creates the instance but doesn't start it.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void initializeTrigger(String triggerId) {
        log.info("Initializing trigger: triggerId={}", triggerId);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        
        // Trigger is initialized when created, no special action needed
        log.info("Trigger initialized: triggerId={}", triggerId);
    }

    /**
     * Start trigger instance.
     * Registers endpoint and starts processing.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void startTrigger(String triggerId) {
        log.info("Starting trigger: triggerId={}", triggerId);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        
        // Update status to ACTIVE
        trigger.setStatus(TriggerStatus.ACTIVE);
        triggerRepository.save(trigger);
        
        // Register endpoint if API trigger
        if (trigger.getTriggerType() == com.notificationplatform.entity.enums.TriggerType.API_CALL) {
            endpointRegistry.registerEndpoint(trigger);
        }
        
        // Call handler's onActivate
        TriggerHandler handler = handlerRegistry.getHandler(trigger);
        if (handler != null) {
            handler.onActivate(trigger);
        }
        
        log.info("Trigger started: triggerId={}", triggerId);
    }

    /**
     * Resume trigger instance.
     * Same as start for API triggers.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void resumeTrigger(String triggerId) {
        startTrigger(triggerId);
    }

    /**
     * Pause trigger instance.
     * Unregisters endpoint but keeps trigger configuration.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void pauseTrigger(String triggerId) {
        log.info("Pausing trigger: triggerId={}", triggerId);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        
        // Unregister endpoint if API trigger
        if (trigger.getTriggerType() == com.notificationplatform.entity.enums.TriggerType.API_CALL) {
            endpointRegistry.unregisterEndpoint(triggerId);
        }
        
        // Call handler's onDeactivate
        TriggerHandler handler = handlerRegistry.getHandler(trigger);
        if (handler != null) {
            handler.onDeactivate(trigger);
        }
        
        log.info("Trigger paused: triggerId={}", triggerId);
    }

    /**
     * Stop trigger instance.
     * Completely stops processing and unregisters endpoint.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void stopTrigger(String triggerId) {
        log.info("Stopping trigger: triggerId={}", triggerId);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        
        // Update status to INACTIVE
        trigger.setStatus(TriggerStatus.INACTIVE);
        triggerRepository.save(trigger);
        
        // Unregister endpoint if API trigger
        if (trigger.getTriggerType() == com.notificationplatform.entity.enums.TriggerType.API_CALL) {
            endpointRegistry.unregisterEndpoint(triggerId);
        }
        
        // Call handler's onDeactivate
        TriggerHandler handler = handlerRegistry.getHandler(trigger);
        if (handler != null) {
            handler.onDeactivate(trigger);
        }
        
        log.info("Trigger stopped: triggerId={}", triggerId);
    }

    /**
     * Destroy trigger instance.
     * Removes all resources and deletes trigger.
     * 
     * @param triggerId Trigger ID
     */
    @Transactional
    public void destroyTrigger(String triggerId) {
        log.info("Destroying trigger: triggerId={}", triggerId);
        
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new RuntimeException("Trigger not found: " + triggerId));
        
        // Unregister endpoint if API trigger
        if (trigger.getTriggerType() == com.notificationplatform.entity.enums.TriggerType.API_CALL) {
            endpointRegistry.unregisterEndpoint(triggerId);
        }
        
        // Call handler's onDelete
        TriggerHandler handler = handlerRegistry.getHandler(trigger);
        if (handler != null) {
            handler.onDelete(trigger);
        }
        
        // Delete trigger
        triggerRepository.delete(trigger);
        
        log.info("Trigger destroyed: triggerId={}", triggerId);
    }
}

