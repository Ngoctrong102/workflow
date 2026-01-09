package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
/**
 * Manages dynamic registration and lifecycle of event consumers
 * Supports both Kafka and RabbitMQ event triggers using Strategy Pattern
 */
@Slf4j
@Service
public class EventConsumerManager {

    private final EventConsumerRegistryRegistry registryRegistry;

    // Track active consumers by trigger ID (stores queue type)
    private final Map<String, String> activeConsumers = new ConcurrentHashMap<>();

    public EventConsumerManager(EventConsumerRegistryRegistry registryRegistry) {
        this.registryRegistry = registryRegistry;
    }

    /**
     * Register and start a consumer for an active event trigger
     */
    public void registerConsumer(Trigger trigger) {
        if (trigger == null || trigger.getStatus() != TriggerStatus.ACTIVE) {
            log.warn("Cannot register consumer for inactive or null trigger: triggerId={}", 
                       trigger != null ? trigger.getId() : "null");
            return;
        }

        String triggerId = trigger.getId();
        
        // Check if consumer already registered
        if (activeConsumers.containsKey(triggerId)) {
            log.debug("Consumer already registered for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            Map<String, Object> config = trigger.getConfig() != null ? 
                (Map<String, Object>) trigger.getConfig() : Map.of();
            
            String queueType = (String) config.get("queueType");
            
            if (queueType == null || queueType.isEmpty()) {
                log.warn("Queue type is missing for trigger: triggerId={}", triggerId);
                return;
            }
            
            // Get registry for queue type using Strategy Pattern
            EventConsumerRegistry registry = registryRegistry.getRegistry(queueType);
            if (registry == null) {
                log.warn("No registry found for queue type: triggerId={}, queueType={}", triggerId, queueType);
                return;
            }
            
            registry.registerConsumer(trigger);
            activeConsumers.put(triggerId, queueType);
            log.info("Registered {} consumer for trigger: triggerId={}", queueType, triggerId);
        } catch (Exception e) {
            log.error("Failed to register consumer for trigger: triggerId={}", triggerId, e);
        }
    }

    /**
     * Unregister and stop a consumer for an event trigger
     */
    public void unregisterConsumer(Trigger trigger) {
        if (trigger == null) {
            return;
        }

        String triggerId = trigger.getId();
        String queueType = activeConsumers.remove(triggerId);

        if (queueType == null) {
            log.debug("No consumer registered for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            // Get registry for queue type using Strategy Pattern
            EventConsumerRegistry registry = registryRegistry.getRegistry(queueType);
            if (registry == null) {
                log.warn("No registry found for queue type: triggerId={}, queueType={}", triggerId, queueType);
                return;
            }
            
            registry.unregisterConsumer(triggerId);
            log.info("Unregistered {} consumer for trigger: triggerId={}", queueType, triggerId);
        } catch (Exception e) {
            log.error("Failed to unregister consumer for trigger: triggerId={}", triggerId, e);
        }
    }

    /**
     * Update consumer configuration when trigger config changes
     */
    public void updateConsumer(Trigger trigger) {
        if (trigger == null) {
            return;
        }

        // Unregister old consumer
        unregisterConsumer(trigger);

        // Register new consumer with updated config
        if (trigger.getStatus() == TriggerStatus.ACTIVE) {
            registerConsumer(trigger);
        }
    }

    /**
     * Initialize consumers for all active event triggers
     * Called on application startup
     */
    public void initializeConsumers(java.util.List<Trigger> activeTriggers) {
        log.info("Initializing consumers for {} active event triggers", activeTriggers.size());
        
        for (Trigger trigger : activeTriggers) {
            if (trigger.getTriggerType() == TriggerType.EVENT && trigger.getStatus() == TriggerStatus.ACTIVE) {
                registerConsumer(trigger);
            }
        }
        
        log.info("Initialized {} consumers", activeConsumers.size());
    }
}

