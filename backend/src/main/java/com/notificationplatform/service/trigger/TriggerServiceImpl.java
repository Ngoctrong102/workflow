package com.notificationplatform.service.trigger;

import com.notificationplatform.dto.mapper.TriggerMapper;
import com.notificationplatform.dto.request.CreateTriggerConfigRequest;
import com.notificationplatform.dto.request.UpdateTriggerConfigRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.schedule.CronValidator;
import com.notificationplatform.service.trigger.schedule.ScheduleTriggerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for managing trigger configs.
 * Trigger configs are independent and can be shared across multiple workflows.
 */
@Slf4j
@Service
@Transactional
public class TriggerServiceImpl implements TriggerService {

    private final TriggerRepository triggerRepository;
    private final TriggerMapper triggerMapper;
    private final CronValidator cronValidator;
    private final ScheduleTriggerService scheduleTriggerService;

    public TriggerServiceImpl(TriggerRepository triggerRepository,
                             TriggerMapper triggerMapper,
                             CronValidator cronValidator,
                             ScheduleTriggerService scheduleTriggerService) {
        this.triggerRepository = triggerRepository;
        this.triggerMapper = triggerMapper;
        this.cronValidator = cronValidator;
        this.scheduleTriggerService = scheduleTriggerService;
    }

    @Override
    public TriggerResponse createTriggerConfig(CreateTriggerConfigRequest request) {
        // Validate trigger type
        TriggerType triggerType = TriggerType.fromValue(request.getTriggerType());
        if (triggerType == null) {
            throw new IllegalArgumentException("Invalid trigger type: " + request.getTriggerType() + 
                ". Supported types: api-call, scheduler, event");
        }

        // Type-specific validation
        validateTriggerConfig(triggerType, request.getConfig());

        // Create trigger entity
        Trigger trigger = triggerMapper.toEntity(request);
        trigger = triggerRepository.save(trigger);

        log.info("Created trigger config: triggerId={}, name={}, type={}", 
                 trigger.getId(), trigger.getName(), trigger.getTriggerType());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional(readOnly = true)
    public TriggerResponse getTriggerConfigById(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger config not found with id: " + id));
        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TriggerResponse> listTriggerConfigs(String triggerType, String status, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        // Get all active triggers
        List<Trigger> triggers = triggerRepository.findAllActive();

        // Filter by type if provided
        if (triggerType != null && !triggerType.isEmpty()) {
            TriggerType type = TriggerType.fromValue(triggerType);
            if (type != null) {
                triggers = triggers.stream()
                        .filter(t -> type.equals(t.getTriggerType()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            TriggerStatus triggerStatus = TriggerStatus.fromValue(status);
            if (triggerStatus != null) {
                triggers = triggers.stream()
                        .filter(t -> triggerStatus.equals(t.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            triggers = triggers.stream()
                    .filter(t -> {
                        // Search in name
                        if (t.getName() != null && t.getName().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        // Search in config if available
                        if (t.getConfig() != null) {
                            String configStr = t.getConfig().toString().toLowerCase();
                            if (configStr.contains(searchLower)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        long total = triggers.size();

        // Apply pagination
        int fromIndex = Math.min(offset, triggers.size());
        int toIndex = Math.min(offset + limit, triggers.size());
        List<Trigger> pagedTriggers = triggers.subList(fromIndex, toIndex);

        List<TriggerResponse> responses = pagedTriggers.stream()
                .map(triggerMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public TriggerResponse updateTriggerConfig(String id, UpdateTriggerConfigRequest request) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger config not found with id: " + id));

        // Validate config if being updated
        if (request.getConfig() != null) {
            validateTriggerConfig(trigger.getTriggerType(), request.getConfig());
        }

        // Update trigger
        triggerMapper.updateEntity(trigger, request);
        trigger = triggerRepository.save(trigger);

        // Re-register schedule if it's a schedule trigger and status changed to active
        if (trigger.getTriggerType() == TriggerType.SCHEDULER) {
            if (trigger.getStatus() == TriggerStatus.ACTIVE) {
                scheduleTriggerService.registerSchedule(trigger);
            } else {
                scheduleTriggerService.cancelSchedule(trigger.getId());
            }
        }

        log.info("Updated trigger config: triggerId={}", trigger.getId());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    public void deleteTriggerConfig(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger config not found with id: " + id));

        // Soft delete
        trigger.setDeletedAt(LocalDateTime.now());
        trigger.setStatus(TriggerStatus.INACTIVE);
        triggerRepository.save(trigger);

        // Cancel schedule if it's a schedule trigger
        if (trigger.getTriggerType() == TriggerType.SCHEDULER) {
            scheduleTriggerService.cancelSchedule(trigger.getId());
        }

        log.info("Deleted trigger config: triggerId={}", trigger.getId());
    }

    @Override
    public TriggerActivationResponse activateApiTrigger(String path, String method, 
                                                        Map<String, Object> requestData, 
                                                        String apiKey) {
        log.info("Activating API trigger: path={}, method={}", path, method);

        // Find trigger config by path and method
        List<Trigger> triggers = triggerRepository.findByPathAndMethodAndActive(path, method);
        if (triggers.isEmpty()) {
            throw new ResourceNotFoundException("Active trigger config not found for path: " + path + " method: " + method);
        }
        Trigger trigger = triggers.get(0);

        // Validate API key if configured
        Map<String, Object> config = trigger.getConfig() != null ? trigger.getConfig() : new HashMap<>();
        if (config.containsKey("apiKey")) {
            String configuredApiKey = (String) config.get("apiKey");
            if (configuredApiKey != null && !configuredApiKey.isEmpty()) {
                if (apiKey == null || !apiKey.equals(configuredApiKey)) {
                    throw new SecurityException("Invalid API key");
                }
            }
        }

        // Find workflow(s) using this trigger config
        // This needs to be implemented by searching workflow definitions for nodes with triggerConfigId = trigger.getId()
        throw new UnsupportedOperationException(
            "activateApiTrigger() needs to find workflow(s) using trigger config. " +
            "This requires searching workflow definitions for triggerConfigId. " +
            "Will be implemented in a later sprint."
        );

        // Prepare trigger data
        // Map<String, Object> triggerData = new HashMap<>();
        // if (requestData != null) {
        //     triggerData.putAll(requestData);
        // }

        // Execute workflow
        // Execution execution = workflowExecutor.execute(workflow, triggerData, trigger.getId());

        // Build response
        // TriggerActivationResponse response = new TriggerActivationResponse();
        // response.setWorkflowId(workflow.getId());
        // response.setExecutionId(execution.getId());
        // response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        // response.setMessage("Workflow execution started");

        // return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TriggerResponse getTriggerByPath(String path, String method) {
        List<Trigger> triggers = triggerRepository.findByPathAndMethodAndActive(path, method);
        if (triggers.isEmpty()) {
            throw new ResourceNotFoundException("Active trigger config not found for path: " + path);
        }
        Trigger trigger = triggers.get(0);
        return triggerMapper.toResponse(trigger);
    }

    /**
     * Validate trigger config based on trigger type.
     */
    private void validateTriggerConfig(TriggerType triggerType, Map<String, Object> config) {
        if (config == null) {
            throw new IllegalArgumentException("Config is required");
        }

        switch (triggerType) {
            case API_CALL:
                validateApiTriggerConfig(config);
                break;
            case SCHEDULER:
                validateScheduleTriggerConfig(config);
                break;
            case EVENT:
                validateEventTriggerConfig(config);
                break;
            default:
                throw new IllegalArgumentException("Unsupported trigger type: " + triggerType);
        }
    }

    /**
     * Validate API trigger config.
     */
    private void validateApiTriggerConfig(Map<String, Object> config) {
        if (!config.containsKey("endpointPath")) {
            throw new IllegalArgumentException("API trigger config must contain 'endpointPath'");
        }
        if (!config.containsKey("httpMethod")) {
            throw new IllegalArgumentException("API trigger config must contain 'httpMethod'");
        }
        
        // Validate endpoint path to avoid conflicts with reserved paths
        String endpointPath = (String) config.get("endpointPath");
        if (endpointPath != null) {
            validateEndpointPath(endpointPath);
        }
    }
    
    /**
     * Validate endpoint path to avoid conflicts with reserved API paths.
     * Spring Boot prioritizes exact paths over wildcard paths, so we need to prevent
     * users from creating triggers with paths that conflict with management endpoints.
     */
    private void validateEndpointPath(String endpointPath) {
        if (endpointPath == null || endpointPath.isEmpty()) {
            return;
        }
        
        // Normalize path for comparison
        String normalizedPath = endpointPath.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        if (normalizedPath.endsWith("/") && normalizedPath.length() > 1) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        
        // Reserved paths that conflict with management endpoints
        // These are exact paths that will match before /trigger/** wildcard
        String[] reservedPaths = {
            "/triggers",           // TriggerController - exact path
            "/triggers/registry",  // TriggerController registry endpoints - exact path
            "/api/v1/triggers",    // If API versioning is used
            "/api/v1/triggers/registry"
        };
        
        // Check for exact match
        for (String reservedPath : reservedPaths) {
            if (normalizedPath.equals(reservedPath)) {
                throw new IllegalArgumentException(
                    "Endpoint path conflicts with reserved path: " + endpointPath + 
                    ". Reserved paths: /triggers, /triggers/registry"
                );
            }
        }
        
        // Check if path starts with reserved prefix (to catch /triggers/{id} conflicts)
        // Note: /triggers/{id} is handled by TriggerController, so we should prevent
        // creating triggers with paths like /triggers/anything
        if (normalizedPath.startsWith("/triggers/") && !normalizedPath.startsWith("/trigger/")) {
            throw new IllegalArgumentException(
                "Endpoint path cannot start with '/triggers/' as it conflicts with trigger management endpoints. " +
                "Use '/trigger/' prefix instead. Path: " + endpointPath
            );
        }
        
        // Ensure path starts with /trigger/ for API triggers (recommended pattern)
        // This ensures it matches the /trigger/** wildcard in ApiTriggerController
        if (!normalizedPath.startsWith("/trigger/")) {
            log.warn("API trigger endpoint path does not start with '/trigger/': {}. " +
                     "This may cause routing conflicts. Recommended pattern: /trigger/...", endpointPath);
        }
    }

    /**
     * Validate Schedule trigger config.
     */
    private void validateScheduleTriggerConfig(Map<String, Object> config) {
        if (!config.containsKey("cronExpression")) {
            throw new IllegalArgumentException("Schedule trigger config must contain 'cronExpression'");
        }
        String cronExpression = (String) config.get("cronExpression");
        if (!cronValidator.isValid(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }
    }

    /**
     * Validate Event trigger config.
     */
    private void validateEventTriggerConfig(Map<String, Object> config) {
        // Check for Kafka config
        if (config.containsKey("kafka")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kafka = (Map<String, Object>) config.get("kafka");
            if (!kafka.containsKey("topic")) {
                throw new IllegalArgumentException("Event trigger config with Kafka must contain 'kafka.topic'");
            }
        }
        // Check for RabbitMQ config
        else if (config.containsKey("rabbitmq")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rabbitmq = (Map<String, Object>) config.get("rabbitmq");
            if (!rabbitmq.containsKey("queue")) {
                throw new IllegalArgumentException("Event trigger config with RabbitMQ must contain 'rabbitmq.queue'");
            }
        }
        // Check for direct topic/queue field (legacy support)
        else if (config.containsKey("topic")) {
            // Valid
        } else {
            throw new IllegalArgumentException("Event trigger config must contain either 'kafka' or 'rabbitmq' config, or 'topic' field");
        }
    }
}
