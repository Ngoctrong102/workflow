package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.repository.TriggerRepository;
import jakarta.annotation.PostConstruct;


import org.springframework.stereotype.Component;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
/**
 * Initializes event consumers for all active triggers on application startup
 */
@Slf4j
@Component
public class EventConsumerInitializer {

    private final TriggerRepository triggerRepository;
    private final EventConsumerManager consumerManager;

    public EventConsumerInitializer(TriggerRepository triggerRepository,
                                   EventConsumerManager consumerManager) {
        this.triggerRepository = triggerRepository;
        this.consumerManager = consumerManager;
    }

    @PostConstruct
    public void initializeConsumers() {
        try {
            log.info("Initializing event consumers for active triggers...");
            
            // Find all active event triggers
            List<Trigger> activeTriggers = triggerRepository.findByTriggerTypeAndStatus(TriggerType.EVENT, TriggerStatus.ACTIVE);
            
            log.info("Found {} active event triggers to initialize", activeTriggers.size());
            
            // Initialize consumers
            consumerManager.initializeConsumers(activeTriggers);
            
            log.info("Event consumer initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize event consumers", e);
            // Don't fail application startup - consumers can be registered later
        }
    }
}

