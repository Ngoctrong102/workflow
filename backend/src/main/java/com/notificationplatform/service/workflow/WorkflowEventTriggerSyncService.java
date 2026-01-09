package com.notificationplatform.service.workflow;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.event.EventConsumerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service to sync event triggers from workflow definition nodes
 * When a workflow has event-trigger nodes, this service automatically creates
 * Event Triggers in the database to enable automatic execution via Kafka/RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventTriggerSyncService {

    private final TriggerRepository triggerRepository;
    private final EventConsumerManager consumerManager;

    /**
     * Sync event triggers from workflow definition
     * Extracts event-trigger nodes and creates/updates Event Triggers
     * Runs in a separate transaction to avoid rollback of main workflow update
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncEventTriggers(Workflow workflow) {
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
        log.debug("Syncing event triggers for workflow: workflowId={}, status={}", 
                    workflow.getId(), workflowStatus);

        // Only sync if workflow is active
        // If workflow is paused, inactive, or archived, deactivate all triggers
        if (workflowStatus != WorkflowStatus.ACTIVE) {
            log.info("Workflow is not active (status={}), deactivating event triggers: workflowId={}", 
                        workflowStatus, workflow.getId());
            // Deactivate existing event triggers for non-active workflows (paused, inactive, archived)
            deactivateEventTriggers(workflow.getId());
            return;
        }

        try {
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            
            if (!definition.containsKey("nodes")) {
                return;
            }

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");

            // Find all event-trigger nodes
            List<Map<String, Object>> eventNodes = nodes.stream()
                    .filter(node -> {
                        Object typeObj = node.get("type");
                        if (!(typeObj instanceof String)) {
                            return false;
                        }
                        String typeStr = (String) typeObj;
                        // Compare with enum name directly (EVENT_TRIGGER) or converted format
                        return NodeType.EVENT_TRIGGER.name().equals(typeStr) ||
                               NodeType.EVENT_TRIGGER.name().equals(typeStr.toUpperCase().replace("-", "_"));
                    })
                    .toList();

            if (eventNodes.isEmpty()) {
                log.debug("No event-trigger nodes found in workflow: workflowId={}", workflow.getId());
                // Deactivate existing event triggers if no event nodes
                deactivateEventTriggers(workflow.getId());
                return;
            }

            log.info("Found {} event-trigger node(s) in workflow: workflowId={}", 
                       eventNodes.size(), workflow.getId());

            // Get existing event triggers for this workflow
            // Refresh to get latest state
            List<Trigger> existingTriggers = triggerRepository.findByWorkflowIdAndTriggerType(
                    workflow.getId(), TriggerType.EVENT);
            log.debug("Found {} existing event trigger(s) for workflow: workflowId={}", 
                        existingTriggers.size(), workflow.getId());

            // Create or update event triggers
            for (Map<String, Object> eventNode : eventNodes) {
                String nodeId = (String) eventNode.get("id");
                Map<String, Object> nodeData = eventNode.containsKey("data") ? 
                    (Map<String, Object>) eventNode.get("data") : new HashMap<>();
                
                Map<String, Object> config = nodeData.containsKey("config") ? 
                    (Map<String, Object>) nodeData.get("config") : new HashMap<>();

                String eventType = (String) config.getOrDefault(ApplicationConstants.ConfigKeys.EVENT_TYPE, ApplicationConstants.MessageQueue.KAFKA);
                String topic = (String) config.getOrDefault(ApplicationConstants.ConfigKeys.TOPIC, "");
                String consumerGroup = (String) config.getOrDefault(ApplicationConstants.ConfigKeys.CONSUMER_GROUP, "");
                String offset = (String) config.getOrDefault(ApplicationConstants.ConfigKeys.OFFSET, ApplicationConstants.Defaults.KAFKA_OFFSET_LATEST);
                List<String> brokers = config.containsKey(ApplicationConstants.ConfigKeys.BROKERS) && config.get(ApplicationConstants.ConfigKeys.BROKERS) instanceof List ?
                    (List<String>) config.get(ApplicationConstants.ConfigKeys.BROKERS) : new ArrayList<>();
                String eventFilter = (String) config.getOrDefault(ApplicationConstants.ConfigKeys.EVENT_FILTER, "");
                
                if (topic == null || topic.isEmpty()) {
                    log.warn("Event node has no topic: workflowId={}, nodeId={}", 
                              workflow.getId(), nodeId);
                    continue;
                }

                // Determine queue type
                String queueType = ApplicationConstants.MessageQueue.KAFKA;
                if (ApplicationConstants.MessageQueue.RABBITMQ.equalsIgnoreCase(eventType) || 
                    ApplicationConstants.MessageQueue.RABBITMQ.equalsIgnoreCase((String) config.get(ApplicationConstants.ConfigKeys.QUEUE_TYPE))) {
                    queueType = ApplicationConstants.MessageQueue.RABBITMQ;
                }

                // Find existing trigger for this node (by nodeId in config)
                Trigger existingTrigger = existingTriggers.stream()
                        .filter(t -> {
                            Map<String, Object> tConfig = t.getConfig() != null ? 
                                (Map<String, Object>) t.getConfig() : new HashMap<>();
                            return nodeId.equals(tConfig.get(ApplicationConstants.ConfigKeys.NODE_ID));
                        })
                        .findFirst()
                        .orElse(null);

                if (existingTrigger != null) {
                    // Update existing trigger - always ensure it's active when workflow is active
                    Map<String, Object> triggerConfig = existingTrigger.getConfig() != null ? 
                        (Map<String, Object>) existingTrigger.getConfig() : new HashMap<>();
                    
                    String oldTopic = (String) triggerConfig.get(ApplicationConstants.ConfigKeys.TOPIC);
                    String oldQueueType = (String) triggerConfig.get(ApplicationConstants.ConfigKeys.QUEUE_TYPE);
                    
                    // Use null-safe comparison
                    boolean topicChanged = (topic == null && oldTopic != null) || 
                                         (topic != null && !topic.equals(oldTopic));
                    boolean queueTypeChanged = (queueType == null && oldQueueType != null) || 
                                              (queueType != null && !queueType.equals(oldQueueType));
                    
                    boolean needsUpdate = topicChanged || queueTypeChanged || existingTrigger.getStatus() != TriggerStatus.ACTIVE;
                    
                    if (needsUpdate) {
                        if (topicChanged || queueTypeChanged) {
                            log.info("Updating event trigger config: triggerId={}, workflowId={}, nodeId={}, oldTopic={}, newTopic={}, oldQueueType={}, newQueueType={}", 
                                       existingTrigger.getId(), workflow.getId(), nodeId, oldTopic, topic, oldQueueType, queueType);
                            
                            // Create new LinkedHashMap to ensure proper serialization
                            Map<String, Object> newConfig = new java.util.LinkedHashMap<>(triggerConfig);
                            newConfig.put(ApplicationConstants.ConfigKeys.TOPIC, topic);
                            newConfig.put(ApplicationConstants.ConfigKeys.QUEUE_TYPE, queueType);
                            newConfig.put(ApplicationConstants.ConfigKeys.CONSUMER_GROUP, consumerGroup);
                            newConfig.put(ApplicationConstants.ConfigKeys.OFFSET, offset);
                            newConfig.put(ApplicationConstants.ConfigKeys.BROKERS, brokers);
                            newConfig.put(ApplicationConstants.ConfigKeys.EVENT_FILTER, eventFilter);
                            newConfig.put(ApplicationConstants.ConfigKeys.NODE_ID, nodeId);
                            existingTrigger.setConfig(newConfig);
                        }
                        
                        // Always activate if workflow is active
                        if (existingTrigger.getStatus() != TriggerStatus.ACTIVE) {
                            log.info("Activating event trigger: triggerId={}, workflowId={}, nodeId={}, currentStatus={}", 
                                       existingTrigger.getId(), workflow.getId(), nodeId, existingTrigger.getStatus());
                            existingTrigger.setStatus(TriggerStatus.ACTIVE);
                        }
                        
                        triggerRepository.save(existingTrigger);
                        log.info("Saved event trigger: triggerId={}, status={}, workflowId={}", 
                                   existingTrigger.getId(), existingTrigger.getStatus(), workflow.getId());
                        
                        // Update consumer with new config
                        consumerManager.updateConsumer(existingTrigger);
                    } else {
                        log.debug("Event trigger already active and config unchanged: triggerId={}, workflowId={}, nodeId={}", 
                                    existingTrigger.getId(), workflow.getId(), nodeId);
                    }
                } else {
                    // Create new trigger
                    log.info("Creating event trigger from node: workflowId={}, nodeId={}, topic={}, queueType={}", 
                               workflow.getId(), nodeId, topic, queueType);
                    
                    Trigger newTrigger = new Trigger();
                    newTrigger.setId(UUID.randomUUID().toString());
                    newTrigger.setWorkflow(workflow);
                    newTrigger.setTriggerType(TriggerType.EVENT);
                    newTrigger.setStatus(TriggerStatus.ACTIVE);
                    
                    // Use LinkedHashMap to maintain order and ensure proper JSON serialization
                    Map<String, Object> triggerConfig = new java.util.LinkedHashMap<>();
                    triggerConfig.put(ApplicationConstants.ConfigKeys.TOPIC, topic);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.QUEUE_TYPE, queueType);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.CONSUMER_GROUP, consumerGroup);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.OFFSET, offset);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.BROKERS, brokers);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.EVENT_FILTER, eventFilter);
                    triggerConfig.put(ApplicationConstants.ConfigKeys.NODE_ID, nodeId);
                    newTrigger.setConfig(triggerConfig);
                    
                    newTrigger = triggerRepository.save(newTrigger);
                    
                    // Register consumer for new trigger
                    consumerManager.registerConsumer(newTrigger);
                }
            }

            // Deactivate triggers for nodes that no longer exist
            // Note: Triggers for existing nodes are already handled in the loop above
            Set<String> currentNodeIds = new HashSet<>();
            for (Map<String, Object> eventNode : eventNodes) {
                currentNodeIds.add((String) eventNode.get("id"));
            }

            // Get all triggers again to check for removed nodes (triggers may have been updated in previous loop)
            List<Trigger> allTriggers = triggerRepository.findByWorkflowIdAndTriggerType(workflow.getId(), TriggerType.EVENT);
            
            for (Trigger trigger : allTriggers) {
                Map<String, Object> triggerConfig = trigger.getConfig() != null ? 
                    trigger.getConfig() : new HashMap<>();
                String nodeId = (String) triggerConfig.get(ApplicationConstants.ConfigKeys.NODE_ID);
                
                if (nodeId != null && !currentNodeIds.contains(nodeId)) {
                    // Node no longer exists, deactivate trigger
                    if (trigger.getStatus() != TriggerStatus.INACTIVE) {
                        log.info("Deactivating event trigger for removed node: triggerId={}, nodeId={}, workflowId={}", 
                                   trigger.getId(), nodeId, workflow.getId());
                        trigger.setStatus(TriggerStatus.INACTIVE);
                        triggerRepository.save(trigger);
                        
                        // Unregister consumer
                        consumerManager.unregisterConsumer(trigger);
                    }
                }
                // Note: We don't need to activate triggers for existing nodes here
                // because they are already handled in the main loop above
            }

        } catch (Exception e) {
            log.error("Error syncing event triggers for workflow: workflowId={}, error={}", 
                        workflow.getId(), e.getMessage(), e);
            // Re-throw to see full stack trace in logs
            throw new RuntimeException("Failed to sync event triggers for workflow: " + workflow.getId(), e);
        }
    }

    /**
     * Deactivate all event triggers for a workflow
     */
    private void deactivateEventTriggers(String workflowId) {
        List<Trigger> triggers = triggerRepository.findByWorkflowIdAndTriggerType(workflowId, TriggerType.EVENT);
        for (Trigger trigger : triggers) {
            if (trigger.getStatus() == TriggerStatus.ACTIVE) {
                trigger.setStatus(TriggerStatus.INACTIVE);
                triggerRepository.save(trigger);
                
                // Unregister consumer
                consumerManager.unregisterConsumer(trigger);
            }
        }
        log.debug("Deactivated {} event trigger(s) for workflow: workflowId={}", 
                    triggers.size(), workflowId);
    }
}

