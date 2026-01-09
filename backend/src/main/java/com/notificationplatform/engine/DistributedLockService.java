package com.notificationplatform.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for distributed locks using Redis.
 * Provides lock acquisition, release, and renewal mechanism for execution coordination.
 * 
 * See: @import(features/distributed-execution-management.md#distributed-lock-implementation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:execution:";
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(5);
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${spring.application.instance-id:${HOSTNAME:unknown}}")
    private String instanceId;
    
    // Track active lock renewals
    private final ConcurrentHashMap<String, ScheduledExecutorService> renewalSchedulers = new ConcurrentHashMap<>();

    /**
     * Acquire distributed lock for execution.
     * 
     * @param executionId Execution ID
     * @param lockDuration Lock duration
     * @return true if lock acquired, false otherwise
     */
    public boolean acquireLock(String executionId, Duration lockDuration) {
        String lockKey = LOCK_KEY_PREFIX + executionId;
        String lockValue = instanceId + ":" + System.currentTimeMillis();
        
        // Try to acquire lock with SET NX EX (set if not exists, with expiration)
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, lockDuration.toMillis(), TimeUnit.MILLISECONDS);
        
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Lock acquired: executionId={}, instanceId={}", executionId, instanceId);
            // Start lock renewal thread
            startLockRenewal(executionId, lockDuration);
            return true;
        }
        
        log.debug("Lock acquisition failed (already locked): executionId={}", executionId);
        return false;
    }

    /**
     * Acquire lock with default duration.
     */
    public boolean acquireLock(String executionId) {
        return acquireLock(executionId, DEFAULT_LOCK_DURATION);
    }

    /**
     * Release distributed lock for execution.
     * Only releases if lock is owned by this instance.
     * 
     * @param executionId Execution ID
     * @return true if lock released, false otherwise
     */
    public boolean releaseLock(String executionId) {
        String lockKey = LOCK_KEY_PREFIX + executionId;
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        
        // Only release if we own the lock
        if (currentValue != null && currentValue.startsWith(instanceId + ":")) {
            redisTemplate.delete(lockKey);
            stopLockRenewal(executionId);
            log.debug("Lock released: executionId={}, instanceId={}", executionId, instanceId);
            return true;
        }
        
        log.debug("Lock release failed (not owned by this instance): executionId={}", executionId);
        return false;
    }

    /**
     * Check if execution is locked.
     * 
     * @param executionId Execution ID
     * @return true if locked, false otherwise
     */
    public boolean isLocked(String executionId) {
        String lockKey = LOCK_KEY_PREFIX + executionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * Check if execution is locked by this instance.
     * 
     * @param executionId Execution ID
     * @return true if locked by this instance, false otherwise
     */
    public boolean isLockedByMe(String executionId) {
        String lockKey = LOCK_KEY_PREFIX + executionId;
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        return currentValue != null && currentValue.startsWith(instanceId + ":");
    }

    /**
     * Start lock renewal scheduler.
     * Renews lock every lockDuration / 3 to ensure it doesn't expire.
     */
    private void startLockRenewal(String executionId, Duration lockDuration) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        renewalSchedulers.put(executionId, scheduler);
        
        long renewalInterval = lockDuration.toMillis() / 3;
        
        scheduler.scheduleAtFixedRate(() -> {
            if (isLockedByMe(executionId)) {
                String lockKey = LOCK_KEY_PREFIX + executionId;
                redisTemplate.expire(lockKey, lockDuration.toMillis(), TimeUnit.MILLISECONDS);
                log.trace("Lock renewed: executionId={}", executionId);
            } else {
                // Lock no longer owned by us, stop renewal
                stopLockRenewal(executionId);
            }
        }, renewalInterval, renewalInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop lock renewal scheduler.
     */
    private void stopLockRenewal(String executionId) {
        ScheduledExecutorService scheduler = renewalSchedulers.remove(executionId);
        if (scheduler != null) {
            scheduler.shutdown();
            log.debug("Lock renewal stopped: executionId={}", executionId);
        }
    }
}

