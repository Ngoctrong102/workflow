package com.notificationplatform.service.workflow;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.schedule.ScheduleTriggerService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
/**
 * Service to sync schedule triggers from workflow definition nodes
 * When a workflow has schedule-trigger nodes, this service automatically creates
 * Schedule Triggers in the database to enable automatic execution
 */
@Slf4j
@Service
public class WorkflowScheduleSyncService {

    private final TriggerRepository triggerRepository;
    private final ScheduleTriggerService scheduleTriggerService;

    public WorkflowScheduleSyncService(TriggerRepository triggerRepository,
                                      ScheduleTriggerService scheduleTriggerService) {
        this.triggerRepository = triggerRepository;
        this.scheduleTriggerService = scheduleTriggerService;
    }

    /**
     * Sync schedule triggers from workflow definition
     * Extracts schedule-trigger nodes and creates/updates Schedule Triggers
     * Runs in a separate transaction to avoid rollback of main workflow update
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncScheduleTriggers(Workflow workflow) {
        // Use the workflow object passed in (which has the latest status)
        // Don't refresh from DB as the update transaction may not be committed yet
        // This ensures we use the correct status from the current update operation
        if (workflow == null || workflow.getDefinition() == null) {
            log.warn("Workflow or definition is null: workflowId={}", 
                       workflow != null ? workflow.getId() : "null");
            return;
        }

        // Use the status from the workflow object passed in (not from DB)
        WorkflowStatus workflowStatus = workflow.getStatus();
        log.debug("Syncing schedule triggers for workflow: workflowId={}, status={}", 
                    workflow.getId(), workflowStatus);

        // Only sync if workflow is active
        // If workflow is paused, inactive, or archived, cancel all schedule triggers
        if (workflowStatus != WorkflowStatus.ACTIVE) {
            log.info("Workflow is not active (status={}), cancelling schedule triggers: workflowId={}", 
                        workflowStatus, workflow.getId());
            // Cancel existing schedule triggers for non-active workflows (paused, inactive, archived)
            cancelScheduleTriggers(workflow.getId());
            return;
        }

        try {
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            
            if (!definition.containsKey("nodes")) {
                return;
            }

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

            // Find all schedule-trigger nodes
            List<Map<String, Object>> scheduleNodes = nodes.stream()
                    .filter(node -> {
                        Object typeObj = node.get("type");
                        if (!(typeObj instanceof String)) {
                            return false;
                        }
                        String typeStr = (String) typeObj;
                        // Compare with enum name directly (SCHEDULE_TRIGGER) or converted format
                        return NodeType.SCHEDULE_TRIGGER.name().equals(typeStr) ||
                               NodeType.SCHEDULE_TRIGGER.name().equals(typeStr.toUpperCase().replace("-", "_"));
                    })
                    .toList();

            if (scheduleNodes.isEmpty()) {
                log.debug("No schedule-trigger nodes found in workflow: workflowId={}", workflow.getId());
                // Cancel existing schedule triggers if no schedule nodes
                cancelScheduleTriggers(workflow.getId());
                return;
            }

            log.info("Found {} schedule-trigger node(s) in workflow: workflowId={}", 
                       scheduleNodes.size(), workflow.getId());

            // Get existing schedule triggers for this workflow
            List<Trigger> existingTriggers = triggerRepository.findByWorkflowIdAndTriggerType(
                    workflow.getId(), TriggerType.SCHEDULER);

            // Create or update schedule triggers
            for (Map<String, Object> scheduleNode : scheduleNodes) {
                String nodeId = (String) scheduleNode.get("id");
                Map<String, Object> nodeData = scheduleNode.containsKey("data") ? 
                    (Map<String, Object>) scheduleNode.get("data") : new HashMap<>();
                
                Map<String, Object> config = nodeData.containsKey("config") ? 
                    (Map<String, Object>) nodeData.get("config") : new HashMap<>();

                String cronExpression = (String) config.getOrDefault("cron", "");
                String timezone = (String) config.getOrDefault("timezone", "");
                if (timezone == null || timezone.isEmpty()) {
                    timezone = "UTC";
                }
                
                if (cronExpression == null || cronExpression.isEmpty()) {
                    log.warn("Schedule node has no cron expression: workflowId={}, nodeId={}", 
                              workflow.getId(), nodeId);
                    continue;
                }

                // Find existing trigger for this node (by nodeId in config)
                Trigger existingTrigger = existingTriggers.stream()
                        .filter(t -> {
                            Map<String, Object> tConfig = t.getConfig() != null ? 
                                (Map<String, Object>) t.getConfig() : new HashMap<>();
                            return nodeId.equals(tConfig.get("nodeId"));
                        })
                        .findFirst()
                        .orElse(null);

                if (existingTrigger != null) {
                    // Update existing trigger - always ensure it's active when workflow is active
                    Map<String, Object> triggerConfig = existingTrigger.getConfig() != null ? 
                        (Map<String, Object>) existingTrigger.getConfig() : new HashMap<>();
                    
                    String oldCron = (String) triggerConfig.get("cronExpression");
                    String oldTimezone = (String) triggerConfig.get("timezone");
                    
                    // Use null-safe comparison
                    boolean cronChanged = (cronExpression == null && oldCron != null) || 
                                        (cronExpression != null && !cronExpression.equals(oldCron));
                    boolean timezoneChanged = (timezone == null && oldTimezone != null) || 
                                            (timezone != null && !timezone.equals(oldTimezone));
                    boolean needsUpdate = cronChanged || timezoneChanged || existingTrigger.getStatus() != TriggerStatus.ACTIVE;
                    
                    if (needsUpdate) {
                        if (cronChanged || timezoneChanged) {
                            log.info("Updating schedule trigger config: triggerId={}, workflowId={}, nodeId={}, oldCron={}, newCron={}, oldTimezone={}, newTimezone={}", 
                                       existingTrigger.getId(), workflow.getId(), nodeId, oldCron, cronExpression, oldTimezone, timezone);
                            
                            // Create new LinkedHashMap to ensure proper serialization
                            Map<String, Object> newConfig = new java.util.LinkedHashMap<>(triggerConfig);
                            newConfig.put("cronExpression", cronExpression);
                            newConfig.put("timezone", timezone);
                            newConfig.put("nodeId", nodeId);
                            existingTrigger.setConfig(newConfig);
                            
                            // Re-register schedule if config changed
                            scheduleTriggerService.cancelSchedule(existingTrigger.getId());
                        }
                        
                        // Always activate if workflow is active
                        if (existingTrigger.getStatus() != TriggerStatus.ACTIVE) {
                            log.info("Activating schedule trigger: triggerId={}, workflowId={}, nodeId={}, currentStatus={}", 
                                       existingTrigger.getId(), workflow.getId(), nodeId, existingTrigger.getStatus());
                            existingTrigger.setStatus(TriggerStatus.ACTIVE);
                        }
                        
                        existingTrigger = triggerRepository.save(existingTrigger);
                        
                        // Register schedule (either new or re-register after cancel)
                        if (cronChanged || timezoneChanged || existingTrigger.getStatus() != TriggerStatus.ACTIVE) {
                            scheduleTriggerService.registerSchedule(existingTrigger);
                        }
                        
                        log.info("Saved schedule trigger: triggerId={}, status={}, workflowId={}", 
                                   existingTrigger.getId(), existingTrigger.getStatus(), workflow.getId());
                    } else {
                        log.debug("Schedule trigger already active and config unchanged: triggerId={}, workflowId={}, nodeId={}", 
                                    existingTrigger.getId(), workflow.getId(), nodeId);
                    }
                } else {
                    // Create new trigger
                    log.info("Creating schedule trigger from node: workflowId={}, nodeId={}, cron={}", 
                               workflow.getId(), nodeId, cronExpression);
                    
                    Trigger newTrigger = new Trigger();
                    newTrigger.setId(UUID.randomUUID().toString());
                    newTrigger.setWorkflow(workflow);
                    newTrigger.setTriggerType(TriggerType.SCHEDULER);
                    newTrigger.setStatus(TriggerStatus.ACTIVE);
                    
                    // Use LinkedHashMap to maintain order and ensure proper JSON serialization
                    Map<String, Object> triggerConfig = new java.util.LinkedHashMap<>();
                    triggerConfig.put("cronExpression", cronExpression);
                    triggerConfig.put("timezone", timezone);
                    triggerConfig.put("nodeId", nodeId);
                    newTrigger.setConfig(triggerConfig);
                    
                    newTrigger = triggerRepository.save(newTrigger);
                    
                    // Register schedule
                    scheduleTriggerService.registerSchedule(newTrigger);
                }
            }

            // Cancel triggers for nodes that no longer exist
            // Note: Triggers for existing nodes are already handled in the loop above
            Set<String> currentNodeIds = new HashSet<>();
            for (Map<String, Object> scheduleNode : scheduleNodes) {
                currentNodeIds.add((String) scheduleNode.get("id"));
            }

            // Get all triggers again to check for removed nodes (triggers may have been updated in previous loop)
            List<Trigger> allScheduleTriggers = triggerRepository.findByWorkflowIdAndTriggerType(workflow.getId(), TriggerType.SCHEDULER);
            
            for (Trigger trigger : allScheduleTriggers) {
                Map<String, Object> triggerConfig = trigger.getConfig() != null ? 
                    trigger.getConfig() : new HashMap<>();
                String nodeId = (String) triggerConfig.get("nodeId");
                
                if (nodeId != null && !currentNodeIds.contains(nodeId)) {
                    // Node no longer exists, cancel and deactivate trigger
                    if (trigger.getStatus() != TriggerStatus.INACTIVE) {
                        log.info("Cancelling schedule trigger for removed node: triggerId={}, nodeId={}, workflowId={}", 
                                   trigger.getId(), nodeId, workflow.getId());
                        scheduleTriggerService.cancelSchedule(trigger.getId());
                        trigger.setStatus(TriggerStatus.INACTIVE);
                        triggerRepository.save(trigger);
                    }
                }
                // Note: We don't need to activate triggers for existing nodes here
                // because they are already handled in the main loop above
            }

        } catch (Exception e) {
            log.error("Error syncing schedule triggers for workflow: workflowId={}", 
                        workflow.getId(), e);
        }
    }

    /**
     * Cancel all schedule triggers for a workflow
     */
    private void cancelScheduleTriggers(String workflowId) {
        List<Trigger> triggers = triggerRepository.findByWorkflowIdAndTriggerType(workflowId, TriggerType.SCHEDULER);
        for (Trigger trigger : triggers) {
            scheduleTriggerService.cancelSchedule(trigger.getId());
        }
        log.debug("Cancelled {} schedule trigger(s) for workflow: workflowId={}", 
                    triggers.size(), workflowId);
    }
}

