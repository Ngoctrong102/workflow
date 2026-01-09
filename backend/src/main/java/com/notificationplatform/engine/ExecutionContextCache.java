package com.notificationplatform.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for caching execution context in Redis.
 * Context is cached during execution and persisted to database when paused/completed.
 * 
 * See: @import(features/distributed-execution-management.md#context-storage-strategy)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionContextCache {

    private static final String CACHE_KEY_PREFIX = "execution:context:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Load execution context from cache.
     * Returns null if not found in cache (should load from database).
     */
    public ExecutionContext loadContext(String executionId) {
        String cacheKey = CACHE_KEY_PREFIX + executionId;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue == null) {
            log.debug("Context not found in cache: executionId={}", executionId);
            return null;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> contextMap = objectMapper.readValue(cachedValue, Map.class);
            return deserializeContext(contextMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize context from cache: executionId={}", executionId, e);
            return null;
        }
    }

    /**
     * Cache execution context in Redis.
     * Context is stored with TTL (default 24 hours).
     */
    public void cacheContext(String executionId, ExecutionContext context) {
        String cacheKey = CACHE_KEY_PREFIX + executionId;
        
        try {
            Map<String, Object> contextMap = serializeContext(context);
            String jsonValue = objectMapper.writeValueAsString(contextMap);
            
            redisTemplate.opsForValue().set(cacheKey, jsonValue != null ? jsonValue : "{}", DEFAULT_TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Context cached: executionId={}", executionId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize context for cache: executionId={}", executionId, e);
        }
    }

    /**
     * Update execution context in cache.
     * Updates existing cache entry or creates new one.
     */
    public void updateContext(String executionId, ExecutionContext context) {
        cacheContext(executionId, context);
    }

    /**
     * Persist context to database and remove from cache.
     * This is called when execution is paused or completed.
     */
    public void persistContext(String executionId, ExecutionContext context) {
        // Remove from cache - context will be persisted to database by ExecutionStateService
        String cacheKey = CACHE_KEY_PREFIX + executionId;
        redisTemplate.delete(cacheKey);
        log.debug("Context removed from cache (will be persisted to database): executionId={}", executionId);
    }

    /**
     * Remove context from cache.
     * Used when execution is completed or failed.
     */
    public void removeContext(String executionId) {
        String cacheKey = CACHE_KEY_PREFIX + executionId;
        redisTemplate.delete(cacheKey);
        log.debug("Context removed from cache: executionId={}", executionId);
    }

    /**
     * Check if context exists in cache.
     */
    public boolean existsInCache(String executionId) {
        String cacheKey = CACHE_KEY_PREFIX + executionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    /**
     * Serialize ExecutionContext to Map for JSON storage.
     */
    private Map<String, Object> serializeContext(ExecutionContext context) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("executionId", context.getExecutionId());
        map.put("workflowId", context.getWorkflowId());
        map.put("variables", context.getVariables());
        map.put("nodeOutputs", context.getNodeOutputs());
        map.put("metadata", context.getMetadata());
        map.put("waitStateId", context.getWaitStateId());
        map.put("waitingNodeId", context.getWaitingNodeId());
        
        // Serialize triggerDataMap
        map.put("triggerDataMap", context.getTriggerDataMap());
        
        return map;
    }

    /**
     * Deserialize Map to ExecutionContext.
     */
    @SuppressWarnings("unchecked")
    private ExecutionContext deserializeContext(Map<String, Object> map) {
        String executionId = (String) map.get("executionId");
        String workflowId = (String) map.get("workflowId");
        
        ExecutionContext context = new ExecutionContext(executionId, workflowId);
        
        if (map.containsKey("variables")) {
            Map<String, Object> variables = (Map<String, Object>) map.get("variables");
            variables.forEach(context::setVariable);
        }
        
        if (map.containsKey("nodeOutputs")) {
            Map<String, Object> nodeOutputs = (Map<String, Object>) map.get("nodeOutputs");
            nodeOutputs.forEach(context::setNodeOutput);
        }
        
        if (map.containsKey("metadata")) {
            Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
            metadata.forEach(context::setMetadata);
        }
        
        if (map.containsKey("waitStateId")) {
            String waitStateId = (String) map.get("waitStateId");
            String waitingNodeId = (String) map.get("waitingNodeId");
            if (waitStateId != null && waitingNodeId != null) {
                context.setWaitState(waitStateId, waitingNodeId);
            }
        }
        
        // Deserialize triggerDataMap
        if (map.containsKey("triggerDataMap")) {
            Map<String, Map<String, Object>> triggerDataMap = (Map<String, Map<String, Object>>) map.get("triggerDataMap");
            if (triggerDataMap != null) {
                triggerDataMap.forEach(context::setTriggerDataForNode);
            }
        }
        
        return context;
    }
}

