package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for trigger factories using Factory Pattern.
 * Automatically collects all TriggerFactory implementations via Spring's List injection
 * and creates a lookup map for O(1) access by trigger type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerFactoryRegistry {

    private final List<TriggerFactory> factories;
    private final Map<TriggerType, TriggerFactory> factoryMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (TriggerFactory factory : factories) {
            try {
                TriggerType type = factory.getSupportedType();
                if (type == null) {
                    log.warn("Factory {} returned null trigger type, skipping", factory.getClass().getSimpleName());
                    continue;
                }
                if (factoryMap.containsKey(type)) {
                    log.warn("Multiple factories found for trigger type: {}. Using: {}", 
                            type, factory.getClass().getSimpleName());
                }
                factoryMap.put(type, factory);
                log.debug("Registered trigger factory: {} for type: {}", 
                        factory.getClass().getSimpleName(), type);
            } catch (UnsupportedOperationException e) {
                log.warn("Skipping factory {}: {}", factory.getClass().getSimpleName(), e.getMessage());
            } catch (Exception e) {
                log.error("Error initializing factory {}: {}", factory.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        log.info("Initialized TriggerFactoryRegistry with {} factories", factoryMap.size());
    }

    /**
     * Get factory for a specific trigger type.
     */
    public TriggerFactory getFactory(TriggerType type) {
        TriggerFactory factory = factoryMap.get(type);
        if (factory == null) {
            log.warn("No factory found for trigger type: {}", type);
        }
        return factory;
    }

    /**
     * Create API trigger using appropriate factory.
     */
    public Trigger createApiTrigger(CreateApiTriggerRequest request) {
        TriggerFactory factory = getFactory(TriggerType.API_CALL);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for API trigger type");
        }
        return factory.createFromApiRequest(request);
    }

    /**
     * Create Schedule trigger using appropriate factory.
     */
    public Trigger createScheduleTrigger(CreateScheduleTriggerRequest request) {
        TriggerFactory factory = getFactory(TriggerType.SCHEDULER);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for Schedule trigger type");
        }
        return factory.createFromScheduleRequest(request);
    }

    /**
     * Create File trigger using appropriate factory.
     * File triggers use EVENT type with file-specific configuration.
     */
    public Trigger createFileTrigger(CreateFileTriggerRequest request) {
        // Use EVENT factory for file triggers
        TriggerFactory factory = getFactory(TriggerType.EVENT);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for Event trigger type (used for file triggers)");
        }
        return factory.createFromFileRequest(request);
    }

    /**
     * Create Event trigger using appropriate factory.
     */
    public Trigger createEventTrigger(CreateEventTriggerRequest request) {
        TriggerFactory factory = getFactory(TriggerType.EVENT);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for Event trigger type");
        }
        return factory.createFromEventRequest(request);
    }
}

