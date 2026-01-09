package com.notificationplatform.service.dlq;

import com.notificationplatform.dto.response.DeadLetterQueueResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.DeadLetterQueue;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.DeadLetterQueueRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class DeadLetterQueueServiceImpl implements DeadLetterQueueService {

    private final DeadLetterQueueRepository dlqRepository;

    public DeadLetterQueueServiceImpl(DeadLetterQueueRepository dlqRepository) {
        this.dlqRepository = dlqRepository;
    }

    @Override
    public String addToDLQ(String sourceType, String sourceId, Object originalMessage,
                           String errorMessage, String errorType, Map<String, Object> metadata) {
        // Check if entry already exists
        Optional<DeadLetterQueue> existing = dlqRepository.findBySourceTypeAndSourceId(sourceType, sourceId);
        if (existing.isPresent()) {
            DeadLetterQueue entry = existing.get();
            entry.setRetryCount(entry.getRetryCount() + 1);
            entry.setLastRetryAt(LocalDateTime.now());
            entry.setErrorMessage(errorMessage);
            entry.setErrorType(errorType);
            if (metadata != null) {
                entry.setMetadata(metadata);
            }
            entry = dlqRepository.save(entry);
            log.info("Updated DLQ entry: id={}, retryCount={}", entry.getId(), entry.getRetryCount());
            return entry.getId();
        }

        // Create new DLQ entry
        DeadLetterQueue entry = new DeadLetterQueue();
        entry.setId(UUID.randomUUID().toString());
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setOriginalMessage(originalMessage);
        entry.setErrorMessage(errorMessage);
        entry.setErrorType(errorType);
        entry.setStatus("pending");
        entry.setRetryCount(0);
        entry.setMaxRetries(3);
        entry.setMetadata(metadata);

        // Extract IDs from metadata if available
        if (metadata != null) {
            if (metadata.containsKey("channelId")) {
                entry.setChannelId((String) metadata.get("channelId"));
            }
            if (metadata.containsKey("workflowId")) {
                entry.setWorkflowId((String) metadata.get("workflowId"));
            }
            if (metadata.containsKey("executionId")) {
                entry.setExecutionId((String) metadata.get("executionId"));
            }
            if (metadata.containsKey("notificationId")) {
                entry.setNotificationId((String) metadata.get("notificationId"));
            }
        }

        entry = dlqRepository.save(entry);
        log.info("Added to DLQ: id={}, sourceType={}, sourceId={}, errorType={}",
                   entry.getId(), sourceType, sourceId, errorType);
        return entry.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public DeadLetterQueueResponse getDLQEntry(String id) {
        DeadLetterQueue entry = dlqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DLQ entry not found with id: " + id));
        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DeadLetterQueueResponse> listDLQEntries(String sourceType, String channelId,
                                                                 String status, String errorType,
                                                                 LocalDateTime startDate, LocalDateTime endDate,
                                                                 int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<DeadLetterQueue> entries;

        if (startDate != null && endDate != null) {
            entries = dlqRepository.findByDateRange(startDate, endDate);
        } else if (channelId != null && !channelId.isEmpty()) {
            entries = dlqRepository.findByChannelId(channelId);
        } else if (sourceType != null && !sourceType.isEmpty()) {
            entries = dlqRepository.findAll().stream()
                    .filter(e -> sourceType.equals(e.getSourceType()))
                    .collect(Collectors.toList());
        } else {
            entries = dlqRepository.findAll();
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            entries = entries.stream()
                    .filter(e -> status.equals(e.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by error type if provided
        if (errorType != null && !errorType.isEmpty()) {
            entries = entries.stream()
                    .filter(e -> errorType.equals(e.getErrorType()))
                    .collect(Collectors.toList());
        }

        long total = entries.size();

        // Apply pagination
        int fromIndex = Math.min(offset, entries.size());
        int toIndex = Math.min(offset + limit, entries.size());
        List<DeadLetterQueue> pagedEntries = entries.subList(fromIndex, toIndex);

        List<DeadLetterQueueResponse> responses = pagedEntries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public DeadLetterQueueResponse retryDLQEntry(String id) {
        DeadLetterQueue entry = dlqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DLQ entry not found with id: " + id));

        if (!"pending".equals(entry.getStatus()) && !"failed".equals(entry.getStatus())) {
            throw new IllegalStateException("DLQ entry can only be retried when pending or failed. Current status: " + entry.getStatus());
        }

        entry.setStatus("retrying");
        entry.setRetryCount(entry.getRetryCount() + 1);
        entry.setLastRetryAt(LocalDateTime.now());
        
        // Calculate next retry time with exponential backoff
        long backoffSeconds = (long) Math.pow(2, entry.getRetryCount()) * 60; // 2^retryCount minutes
        entry.setNextRetryAt(LocalDateTime.now().plusSeconds(backoffSeconds));

        if (entry.getRetryCount() >= entry.getMaxRetries()) {
            entry.setStatus("failed");
        }

        entry = dlqRepository.save(entry);

        log.info("Retrying DLQ entry: id={}, retryCount={}", entry.getId(), entry.getRetryCount());

        return toResponse(entry);
    }

    @Override
    public List<DeadLetterQueueResponse> retryDLQEntries(List<String> ids) {
        List<DeadLetterQueueResponse> responses = new ArrayList<>();
        for (String id : ids) {
            try {
                responses.add(retryDLQEntry(id));
            } catch (Exception e) {
                log.error("Error retrying DLQ entry: id={}", id, e);
            }
        }
        return responses;
    }

    @Override
    public DeadLetterQueueResponse resolveDLQEntry(String id, String resolvedBy) {
        DeadLetterQueue entry = dlqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DLQ entry not found with id: " + id));

        entry.setStatus("resolved");
        entry.setResolvedAt(LocalDateTime.now());
        entry.setResolvedBy(resolvedBy);
        entry = dlqRepository.save(entry);

        log.info("Resolved DLQ entry: id={}, resolvedBy={}", entry.getId(), resolvedBy);

        return toResponse(entry);
    }

    @Override
    public void deleteDLQEntry(String id) {
        DeadLetterQueue entry = dlqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DLQ entry not found with id: " + id));

        dlqRepository.delete(entry);
        log.info("Deleted DLQ entry: id={}", id);
    }

    @Override
    public int cleanupOldEntries(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<DeadLetterQueue> oldEntries = dlqRepository.findByDateRange(
                LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate);

        // Only delete resolved or failed entries
        List<DeadLetterQueue> toDelete = oldEntries.stream()
                .filter(e -> "resolved".equals(e.getStatus()) || "failed".equals(e.getStatus()))
                .collect(Collectors.toList());

        dlqRepository.deleteAll(toDelete);
        log.info("Cleaned up {} old DLQ entries older than {} days", toDelete.size(), daysOld);

        return toDelete.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDLQStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", dlqRepository.count());
        stats.put("pending", dlqRepository.countByStatus("pending"));
        stats.put("retrying", dlqRepository.countByStatus("retrying"));
        stats.put("failed", dlqRepository.countByStatus("failed"));
        stats.put("resolved", dlqRepository.countByStatus("resolved"));

        // Count by error type
        Map<String, Long> errorTypeCounts = new HashMap<>();
        dlqRepository.findAll().forEach(entry -> {
            String errorType = entry.getErrorType() != null ? entry.getErrorType() : "unknown";
            errorTypeCounts.put(errorType, errorTypeCounts.getOrDefault(errorType, 0L) + 1);
        });
        stats.put("byErrorType", errorTypeCounts);

        // Count by source type
        Map<String, Long> sourceTypeCounts = new HashMap<>();
        dlqRepository.findAll().forEach(entry -> {
            String sourceType = entry.getSourceType();
            sourceTypeCounts.put(sourceType, sourceTypeCounts.getOrDefault(sourceType, 0L) + 1);
        });
        stats.put("bySourceType", sourceTypeCounts);

        return stats;
    }

    private DeadLetterQueueResponse toResponse(DeadLetterQueue entry) {
        DeadLetterQueueResponse response = new DeadLetterQueueResponse();
        response.setId(entry.getId());
        response.setSourceType(entry.getSourceType());
        response.setSourceId(entry.getSourceId());
        response.setChannelId(entry.getChannelId());
        response.setWorkflowId(entry.getWorkflowId());
        response.setExecutionId(entry.getExecutionId());
        response.setNotificationId(entry.getNotificationId());
        response.setOriginalMessage(entry.getOriginalMessage() != null ?
                (Map<String, Object>) entry.getOriginalMessage() : null);
        response.setErrorMessage(entry.getErrorMessage());
        response.setErrorType(entry.getErrorType());
        response.setRetryCount(entry.getRetryCount());
        response.setMaxRetries(entry.getMaxRetries());
        response.setLastRetryAt(entry.getLastRetryAt());
        response.setNextRetryAt(entry.getNextRetryAt());
        response.setStatus(entry.getStatus());
        response.setResolvedAt(entry.getResolvedAt());
        response.setResolvedBy(entry.getResolvedBy());
        response.setMetadata(entry.getMetadata() != null ?
                (Map<String, Object>) entry.getMetadata() : null);
        response.setCreatedAt(entry.getCreatedAt());
        response.setUpdatedAt(entry.getUpdatedAt());
        return response;
    }
}

