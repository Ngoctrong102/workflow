package com.notificationplatform.service.trigger.handler;

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
 * Registry for trigger handlers using Strategy Pattern.
 * Automatically collects all TriggerHandler implementations via Spring's List injection
 * and creates a lookup map for O(1) access by trigger type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerHandlerRegistry {

    private final List<TriggerHandler> handlers;
    private final Map<TriggerType, TriggerHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (TriggerHandler handler : handlers) {
            try {
                TriggerType type = handler.getSupportedType();
                if (type == null) {
                    log.warn("Handler {} returned null trigger type, skipping", handler.getClass().getSimpleName());
                    continue;
                }
                if (handlerMap.containsKey(type)) {
                    log.warn("Multiple handlers found for trigger type: {}. Using: {}", 
                            type, handler.getClass().getSimpleName());
                }
                handlerMap.put(type, handler);
                log.debug("Registered trigger handler: {} for type: {}", 
                        handler.getClass().getSimpleName(), type);
            } catch (UnsupportedOperationException e) {
                log.warn("Skipping handler {}: {}", handler.getClass().getSimpleName(), e.getMessage());
            } catch (Exception e) {
                log.error("Error initializing handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        log.info("Initialized TriggerHandlerRegistry with {} handlers", handlerMap.size());
    }

    /**
     * Get handler for a specific trigger type.
     */
    public TriggerHandler getHandler(TriggerType type) {
        TriggerHandler handler = handlerMap.get(type);
        if (handler == null) {
            log.warn("No handler found for trigger type: {}", type);
        }
        return handler;
    }

    /**
     * Get handler for a trigger.
     */
    public TriggerHandler getHandler(Trigger trigger) {
        return getHandler(trigger.getTriggerType());
    }

    /**
     * Execute handler method for a trigger.
     */
    public void handleActivate(Trigger trigger) {
        TriggerHandler handler = getHandler(trigger);
        if (handler != null) {
            handler.onActivate(trigger);
        }
    }

    public void handleDeactivate(Trigger trigger) {
        TriggerHandler handler = getHandler(trigger);
        if (handler != null) {
            handler.onDeactivate(trigger);
        }
    }

    public void handleUpdate(Trigger trigger) {
        TriggerHandler handler = getHandler(trigger);
        if (handler != null) {
            handler.onUpdate(trigger);
        }
    }

    public void handleDelete(Trigger trigger) {
        TriggerHandler handler = getHandler(trigger);
        if (handler != null) {
            handler.onDelete(trigger);
        }
    }
}

