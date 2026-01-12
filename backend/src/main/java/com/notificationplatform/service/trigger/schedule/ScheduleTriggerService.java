package com.notificationplatform.service.trigger.schedule;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.slf4j.Slf4j;
/**
 * Service for managing scheduled triggers
 */
@Slf4j
@Service
@Transactional
public class ScheduleTriggerService {

    private final TriggerRepository triggerRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutor workflowExecutor;
    private final TaskScheduler taskScheduler;
    private final CronValidator cronValidator;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public ScheduleTriggerService(TriggerRepository triggerRepository,
                                 WorkflowRepository workflowRepository,
                                 @Lazy WorkflowExecutor workflowExecutor,
                                 TaskScheduler taskScheduler,
                                 CronValidator cronValidator) {
        this.triggerRepository = triggerRepository;
        this.workflowRepository = workflowRepository;
        this.workflowExecutor = workflowExecutor;
        this.taskScheduler = taskScheduler;
        this.cronValidator = cronValidator;
    }

    /**
     * Register a schedule trigger
     */
    public void registerSchedule(Trigger trigger) {
        if (trigger.getTriggerType() != TriggerType.SCHEDULER) {
            return;
        }

        if (trigger.getStatus() != com.notificationplatform.entity.enums.TriggerStatus.ACTIVE) {
            return;
        }

        String triggerId = trigger.getId();
        
        // Cancel existing task if any
        cancelSchedule(triggerId);

        // Get cron expression from config
        Map<String, Object> config = trigger.getConfig() != null ? trigger.getConfig() : new HashMap<>();
        String cronExpression = (String) config.get("cronExpression");
        
        if (cronExpression == null || cronExpression.isEmpty()) {
            log.warn("No cron expression found for trigger: {}", triggerId);
            return;
        }

        // Validate cron expression
        if (!cronValidator.isValid(cronExpression)) {
            log.error("Invalid cron expression for trigger: {}, expression: {}", triggerId, cronExpression);
            return;
        }

        // Convert to 6-field format if needed
        cronExpression = cronValidator.convertTo6Field(cronExpression);

        // Get timezone
        String timezone = (String) config.getOrDefault("timezone", "UTC");
        if (timezone == null || timezone.isEmpty()) {
            timezone = "UTC";
        }
        ZoneId zoneId = ZoneId.of(timezone);

        // Get start/end dates
        LocalDateTime startDate = config.containsKey("startDate") ? 
            LocalDateTime.parse((String) config.get("startDate")) : null;
        LocalDateTime endDate = config.containsKey("endDate") ? 
            LocalDateTime.parse((String) config.get("endDate")) : null;

        // Check if schedule is active
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            log.info("Schedule not yet active: triggerId={}, startDate={}", triggerId, startDate);
            return;
        }

        if (endDate != null && now.isAfter(endDate)) {
            log.info("Schedule has expired: triggerId={}, endDate={}", triggerId, endDate);
            return;
        }

        // Create cron trigger with timezone
        CronTrigger cronTrigger = new CronTrigger(cronExpression, TimeZone.getTimeZone(zoneId));

        // Schedule the task - only capture triggerId to avoid ApplicationContext issues
        String triggerIdToExecute = triggerId;
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
            () -> executeScheduledWorkflow(triggerIdToExecute),
            cronTrigger
        );

        scheduledTasks.put(triggerId, scheduledTask);
        log.info("Registered schedule trigger: triggerId={}, cron={}, timezone={}", 
                   triggerId, cronExpression, timezone);
    }

    /**
     * Cancel a scheduled trigger
     */
    public void cancelSchedule(String triggerId) {
        ScheduledFuture<?> task = scheduledTasks.remove(triggerId);
        if (task != null) {
            task.cancel(false);
            log.info("Cancelled schedule trigger: triggerId={}", triggerId);
        }
    }

    /**
     * Execute workflow for scheduled trigger
     * Must be transactional to avoid LazyInitializationException
     * Takes triggerId instead of Trigger entity to avoid ApplicationContext issues in async threads
     */
    @Transactional
    private void executeScheduledWorkflow(String triggerId) {
        try {
            log.info("Executing scheduled workflow: triggerId={}", triggerId);

            // Check if trigger is still active
            Trigger currentTrigger = triggerRepository.findById(triggerId).orElse(null);
            if (currentTrigger == null || 
                currentTrigger.getStatus() != com.notificationplatform.entity.enums.TriggerStatus.ACTIVE) {
                log.warn("Trigger is no longer active, cancelling: triggerId={}", triggerId);
                cancelSchedule(triggerId);
                return;
            }

            // Check end date
            Map<String, Object> config = currentTrigger.getConfig() != null ? 
                (Map<String, Object>) currentTrigger.getConfig() : new HashMap<>();
            
            if (config.containsKey("endDate")) {
                LocalDateTime endDate = LocalDateTime.parse((String) config.get("endDate"));
                if (LocalDateTime.now().isAfter(endDate)) {
                    log.info("Schedule has expired, cancelling: triggerId={}", triggerId);
                    cancelSchedule(triggerId);
                    return;
                }
            }

            // Find workflows that use this trigger config by parsing workflow definitions
            // In the new design, triggers are independent configs referenced via triggerConfigId in workflow nodes
            // Need to:
            // 1. Query all active workflows
            // 2. Parse workflow definitions to find nodes with triggerConfigId matching this trigger
            // 3. Execute those workflows
            log.warn("ScheduleTriggerService.executeScheduledWorkflow - need to find workflows using trigger config: triggerId={}", currentTrigger.getId());
            return;

        } catch (Exception e) {
            log.error("Error executing scheduled workflow: triggerId={}", triggerId, e);
        }
    }

    /**
     * Load and register all active schedule triggers
     */
    public void loadActiveSchedules() {
        List<Trigger> scheduleTriggers = triggerRepository.findByTriggerTypeAndStatus(
            TriggerType.SCHEDULER, 
            com.notificationplatform.entity.enums.TriggerStatus.ACTIVE
        );
        log.info("Loading {} active schedule triggers", scheduleTriggers.size());
        
        for (Trigger trigger : scheduleTriggers) {
            registerSchedule(trigger);
        }
    }
}

