package com.notificationplatform.service.trigger.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for event consumer registries using Strategy Pattern.
 * Automatically collects all EventConsumerRegistry implementations via Spring's List injection
 * and creates a lookup map for O(1) access by queue type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumerRegistryRegistry {

    private final List<EventConsumerRegistry> registries;
    private final Map<String, EventConsumerRegistry> registryMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (EventConsumerRegistry registry : registries) {
            String queueType = registry.getSupportedQueueType();
            if (registryMap.containsKey(queueType)) {
                log.warn("Multiple registries found for queue type: {}. Using: {}", 
                        queueType, registry.getClass().getSimpleName());
            }
            registryMap.put(queueType, registry);
            log.debug("Registered event consumer registry: {} for queue type: {}", 
                    registry.getClass().getSimpleName(), queueType);
        }
        log.info("Initialized EventConsumerRegistryRegistry with {} registries", registryMap.size());
    }

    /**
     * Get registry for a specific queue type.
     * @param queueType Queue type string (e.g., "kafka", "rabbitmq")
     * @return EventConsumerRegistry for the queue type, or null if not found
     */
    public EventConsumerRegistry getRegistry(String queueType) {
        EventConsumerRegistry registry = registryMap.get(queueType);
        if (registry == null) {
            log.warn("No registry found for queue type: {}", queueType);
        }
        return registry;
    }
}

