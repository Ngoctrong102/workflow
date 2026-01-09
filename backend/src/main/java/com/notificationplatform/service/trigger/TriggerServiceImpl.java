package com.notificationplatform.service.trigger;

import com.notificationplatform.dto.mapper.TriggerMapper;
import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.dto.request.UpdateTriggerRequest;
import com.notificationplatform.service.trigger.schedule.CronValidator;
import com.notificationplatform.service.trigger.schedule.ScheduleTriggerService;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.repository.WorkflowRepository;
import com.notificationplatform.service.trigger.factory.TriggerFactoryRegistry;
import com.notificationplatform.service.trigger.handler.TriggerHandlerRegistry;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class TriggerServiceImpl implements TriggerService {

    private final TriggerRepository triggerRepository;
    private final WorkflowRepository workflowRepository;
    private final TriggerMapper triggerMapper;
    private final WorkflowExecutor workflowExecutor;
    private final ScheduleTriggerService scheduleTriggerService;
    private final CronValidator cronValidator;
    private final TriggerHandlerRegistry triggerHandlerRegistry;
    private final TriggerFactoryRegistry triggerFactoryRegistry;

    public TriggerServiceImpl(TriggerRepository triggerRepository,
                             WorkflowRepository workflowRepository,
                             TriggerMapper triggerMapper,
                             WorkflowExecutor workflowExecutor,
                             ScheduleTriggerService scheduleTriggerService,
                             CronValidator cronValidator,
                             TriggerHandlerRegistry triggerHandlerRegistry,
                             TriggerFactoryRegistry triggerFactoryRegistry) {
        this.triggerRepository = triggerRepository;
        this.workflowRepository = workflowRepository;
        this.triggerMapper = triggerMapper;
        this.workflowExecutor = workflowExecutor;
        this.scheduleTriggerService = scheduleTriggerService;
        this.cronValidator = cronValidator;
        this.triggerHandlerRegistry = triggerHandlerRegistry;
        this.triggerFactoryRegistry = triggerFactoryRegistry;
    }

    @Override
    public TriggerResponse createApiTrigger(CreateApiTriggerRequest request) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(request.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + request.getWorkflowId()));

        // Validate path uniqueness - TODO: implement existsByPathAndMethodAndNotDeleted in repository
        // Temporarily skip this check

        // Use Factory Pattern to create trigger
        Trigger trigger = triggerFactoryRegistry.createApiTrigger(request);
        trigger.setWorkflow(workflow);
        trigger = triggerRepository.save(trigger);

        log.info("Created API trigger: triggerId={}", trigger.getId());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    public TriggerResponse createScheduleTrigger(CreateScheduleTriggerRequest request) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(request.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + request.getWorkflowId()));

        // Validate cron expression
        if (!cronValidator.isValid(request.getCronExpression())) {
            throw new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression());
        }

        // Use Factory Pattern to create trigger
        Trigger trigger = triggerFactoryRegistry.createScheduleTrigger(request);
        trigger.setWorkflow(workflow);
        trigger = triggerRepository.save(trigger);

        // Use Strategy Pattern to handle trigger-specific creation logic
        triggerHandlerRegistry.handleActivate(trigger);

        log.info("Created schedule trigger: triggerId={}, cron={}", 
                   trigger.getId(), request.getCronExpression());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    public TriggerResponse createFileTrigger(CreateFileTriggerRequest request) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(request.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + request.getWorkflowId()));

        // Use Factory Pattern to create trigger
        Trigger trigger = triggerFactoryRegistry.createFileTrigger(request);
        trigger.setWorkflow(workflow);
        trigger = triggerRepository.save(trigger);

        log.info("Created file trigger: triggerId={}, formats={}", 
                   trigger.getId(), request.getFileFormats());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    public TriggerResponse createEventTrigger(CreateEventTriggerRequest request) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(request.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + request.getWorkflowId()));

        // Validate queue type
        if (!"kafka".equals(request.getQueueType()) && !"rabbitmq".equals(request.getQueueType())) {
            throw new IllegalArgumentException("Unsupported queue type: " + request.getQueueType() + ". Supported: kafka, rabbitmq");
        }

        // Use Factory Pattern to create trigger
        Trigger trigger = triggerFactoryRegistry.createEventTrigger(request);
        trigger.setWorkflow(workflow);
        trigger = triggerRepository.save(trigger);

        log.info("Created event trigger: triggerId={}, topic/queue={}, queueType={}", 
                   trigger.getId(), request.getTopic(), request.getQueueType());

        // Note: In production, would register consumer dynamically here
        // For MVP, using static listeners with topic/queue discovery
        // Kafka: @KafkaListener
        // RabbitMQ: @RabbitListener

        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional(readOnly = true)
    public TriggerResponse getTriggerById(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + id));
        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriggerResponse> listTriggers(String workflowId) {
        List<Trigger> triggers;
        if (workflowId != null && !workflowId.isEmpty()) {
            triggers = triggerRepository.findByWorkflowIdAndNotDeleted(workflowId);
        } else {
            triggers = triggerRepository.findAllActive();
        }
        return triggers.stream()
                .map(triggerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TriggerResponse> listTriggersPaged(String workflowId, String type, String status, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Trigger> triggers;
        if (workflowId != null && !workflowId.isEmpty()) {
            triggers = triggerRepository.findByWorkflowIdAndNotDeleted(workflowId);
        } else {
            triggers = triggerRepository.findAllActive();
        }

        // Filter by type if provided
        if (type != null && !type.isEmpty()) {
            TriggerType triggerType = TriggerType.fromValue(type);
            if (triggerType != null) {
                triggers = triggers.stream()
                        .filter(t -> triggerType.equals(t.getTriggerType()))
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
    public TriggerResponse updateTrigger(String id, UpdateTriggerRequest request) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + id));

        // Validate path uniqueness if path is being updated
        // TODO: implement path validation when getPath() method is available
        // Temporarily disabled path validation

        // Update trigger
        triggerMapper.updateEntity(trigger, request);
        trigger = triggerRepository.save(trigger);

        // Re-register schedule if it's a schedule trigger
        if (trigger.getTriggerType() == TriggerType.SCHEDULER) {
            if (trigger.getStatus() == TriggerStatus.ACTIVE) {
                scheduleTriggerService.registerSchedule(trigger);
            } else {
                scheduleTriggerService.cancelSchedule(trigger.getId());
            }
        }

        log.info("Updated trigger: triggerId={}", trigger.getId());

        return triggerMapper.toResponse(trigger);
    }

    @Override
    public void deleteTrigger(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + id));

        // Soft delete
        trigger.setDeletedAt(LocalDateTime.now());
        trigger.setStatus(TriggerStatus.INACTIVE);
        triggerRepository.save(trigger);

        // Use Strategy Pattern to handle trigger-specific deletion logic
        triggerHandlerRegistry.handleDelete(trigger);

        log.info("Deleted trigger: triggerId={}", trigger.getId());
    }

    @Override
    public TriggerActivationResponse activateApiTrigger(String path, String method, 
                                                        Map<String, Object> requestData, 
                                                        String apiKey) {
        log.info("Activating API trigger: path={}, method={}", path, method);

        // Find trigger
        List<Trigger> triggers = triggerRepository.findByPathAndMethodAndActive(path, method);
        if (triggers.isEmpty()) {
            throw new ResourceNotFoundException("Active trigger not found for path: " + path + " method: " + method);
        }
        Trigger trigger = triggers.get(0);

        // Validate API key if configured
        Map<String, Object> config = trigger.getConfig() != null ? trigger.getConfig() : new java.util.HashMap<>();
        if (config.containsKey("apiKey")) {
            String configuredApiKey = (String) config.get("apiKey");
            if (configuredApiKey != null && !configuredApiKey.isEmpty()) {
                if (apiKey == null || !apiKey.equals(configuredApiKey)) {
                    throw new SecurityException("Invalid API key");
                }
            }
        }

        // Get workflow
        Workflow workflow = trigger.getWorkflow();
        if (workflow == null) {
            throw new IllegalStateException("Trigger has no associated workflow");
        }

        if (workflow.getStatus() != WorkflowStatus.ACTIVE) {
            throw new IllegalStateException("Workflow is not active: " + workflow.getStatus());
        }

        // Prepare trigger data
        Map<String, Object> triggerData = new HashMap<>();
        if (requestData != null) {
            triggerData.putAll(requestData);
        }

        // Execute workflow
        Execution execution = workflowExecutor.execute(workflow, triggerData, trigger.getId());

        // Build response
        TriggerActivationResponse response = new TriggerActivationResponse();
        response.setWorkflowId(workflow.getId());
        response.setExecutionId(execution.getId());
        response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        response.setMessage("Workflow execution started");

        log.info("Workflow execution started: executionId={}, workflowId={}", 
                   execution.getId(), workflow.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TriggerResponse getTriggerByPath(String path, String method) {
        List<Trigger> triggers = triggerRepository.findByPathAndMethodAndActive(path, method);
        if (triggers.isEmpty()) {
            throw new ResourceNotFoundException("Active trigger not found for path: " + path);
        }
        Trigger trigger = triggers.get(0);
        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional
    public TriggerResponse activateTrigger(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + id));

        trigger.setStatus(TriggerStatus.ACTIVE);
        trigger = triggerRepository.save(trigger);

        // Use Strategy Pattern to handle trigger-specific activation logic
        triggerHandlerRegistry.handleActivate(trigger);

        log.info("Activated trigger: triggerId={}", trigger.getId());
        return triggerMapper.toResponse(trigger);
    }

    @Override
    @Transactional
    public TriggerResponse deactivateTrigger(String id) {
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + id));

        trigger.setStatus(TriggerStatus.INACTIVE);
        trigger = triggerRepository.save(trigger);

        // Use Strategy Pattern to handle trigger-specific deactivation logic
        triggerHandlerRegistry.handleDeactivate(trigger);

        log.info("Deactivated trigger: triggerId={}", trigger.getId());
        return triggerMapper.toResponse(trigger);
    }
}

