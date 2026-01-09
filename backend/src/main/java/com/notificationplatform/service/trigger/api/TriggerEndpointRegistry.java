package com.notificationplatform.service.trigger.api;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.engine.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for active trigger endpoints.
 * Stores mapping between endpoint paths and triggers.
 * Uses distributed lock for concurrent access in multi-instance deployments.
 * 
 * See: @import(features/trigger-registry.md#trigger-instance-lifecycle)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerEndpointRegistry {

    private final DistributedLockService lockService;
    
    // Map<endpointPath, Trigger>
    private final Map<String, Trigger> endpointMap = new ConcurrentHashMap<>();
    
    // Map<triggerId, endpointPath> for reverse lookup
    private final Map<String, String> triggerToEndpointMap = new ConcurrentHashMap<>();
    
    private static final String LOCK_KEY_PREFIX = "lock:trigger-endpoint:";

    /**
     * Register endpoint for trigger.
     * 
     * @param trigger Trigger to register
     * @return Endpoint path
     */
    public String registerEndpoint(Trigger trigger) {
        String endpointPath = extractEndpointPath(trigger);
        String lockKey = LOCK_KEY_PREFIX + endpointPath;
        
        // Acquire lock for registration
        if (!lockService.acquireLock(lockKey)) {
            log.warn("Failed to acquire lock for endpoint registration: endpointPath={}", endpointPath);
            throw new RuntimeException("Failed to acquire lock for endpoint registration: " + endpointPath);
        }
        
        try {
            // Check if endpoint already registered
            if (endpointMap.containsKey(endpointPath)) {
                Trigger existingTrigger = endpointMap.get(endpointPath);
                if (!existingTrigger.getId().equals(trigger.getId())) {
                    log.warn("Endpoint already registered by different trigger: endpointPath={}, existingTriggerId={}, newTriggerId={}", 
                             endpointPath, existingTrigger.getId(), trigger.getId());
                    throw new RuntimeException("Endpoint already registered: " + endpointPath);
                }
                // Same trigger, already registered
                return endpointPath;
            }
            
            // Register endpoint
            endpointMap.put(endpointPath, trigger);
            triggerToEndpointMap.put(trigger.getId(), endpointPath);
            
            log.info("Registered trigger endpoint: endpointPath={}, triggerId={}, workflowId={}", 
                     endpointPath, trigger.getId(), trigger.getWorkflow().getId());
            
            return endpointPath;
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    /**
     * Unregister endpoint for trigger.
     * 
     * @param triggerId Trigger ID
     */
    public void unregisterEndpoint(String triggerId) {
        String endpointPath = triggerToEndpointMap.remove(triggerId);
        if (endpointPath == null) {
            log.debug("Endpoint not found for trigger: triggerId={}", triggerId);
            return;
        }
        
        String lockKey = LOCK_KEY_PREFIX + endpointPath;
        
        // Acquire lock for unregistration
        if (!lockService.acquireLock(lockKey)) {
            log.warn("Failed to acquire lock for endpoint unregistration: endpointPath={}", endpointPath);
            return;
        }
        
        try {
            endpointMap.remove(endpointPath);
            log.info("Unregistered trigger endpoint: endpointPath={}, triggerId={}", 
                     endpointPath, triggerId);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    /**
     * Get trigger by endpoint path.
     * 
     * @param endpointPath Endpoint path
     * @return Trigger if found, null otherwise
     */
    public Trigger getTriggerByEndpoint(String endpointPath) {
        return endpointMap.get(endpointPath);
    }

    /**
     * Check if endpoint is registered.
     * 
     * @param endpointPath Endpoint path
     * @return true if registered, false otherwise
     */
    public boolean isEndpointRegistered(String endpointPath) {
        return endpointMap.containsKey(endpointPath);
    }

    /**
     * Extract endpoint path from trigger configuration.
     */
    @SuppressWarnings("unchecked")
    private String extractEndpointPath(Trigger trigger) {
        Map<String, Object> config = trigger.getConfig();
        if (config == null) {
            throw new RuntimeException("Trigger config is null: triggerId=" + trigger.getId());
        }
        
        String endpointPath = (String) config.get("endpointPath");
        if (endpointPath == null || endpointPath.isEmpty()) {
            // Default endpoint path: /api/v1/trigger/{workflowId}
            endpointPath = "/api/v1/trigger/" + trigger.getWorkflow().getId();
        }
        
        // Normalize path (remove trailing slash, ensure starts with /)
        if (!endpointPath.startsWith("/")) {
            endpointPath = "/" + endpointPath;
        }
        if (endpointPath.endsWith("/") && endpointPath.length() > 1) {
            endpointPath = endpointPath.substring(0, endpointPath.length() - 1);
        }
        
        return endpointPath;
    }
}

