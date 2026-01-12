package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.repository.WorkflowRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
/**
 * Initializes event consumers for all active triggers on application startup.
 * Loads triggers from both:
 * 1. Trigger table with ACTIVE status
 * 2. Active workflows (to ensure triggers in active workflows are also activated)
 */
@Slf4j
@Component
public class EventConsumerInitializer {

    private final TriggerRepository triggerRepository;
    private final WorkflowRepository workflowRepository;
    private final EventConsumerManager consumerManager;

    public EventConsumerInitializer(TriggerRepository triggerRepository,
                                   WorkflowRepository workflowRepository,
                                   EventConsumerManager consumerManager) {
        this.triggerRepository = triggerRepository;
        this.workflowRepository = workflowRepository;
        this.consumerManager = consumerManager;
    }

    @PostConstruct
    public void initializeConsumers() {
        try {
            log.info("Initializing event consumers for active triggers...");
            
            Set<String> processedTriggerIds = new HashSet<>();
            List<Trigger> triggersToActivate = new ArrayList<>();
            
            // 1. Find all active event triggers from trigger table
            List<Trigger> activeTriggers = triggerRepository.findByTriggerTypeAndStatus(TriggerType.EVENT, TriggerStatus.ACTIVE);
            log.info("Found {} active event triggers in trigger table", activeTriggers.size());
            
            for (Trigger trigger : activeTriggers) {
                if (!processedTriggerIds.contains(trigger.getId())) {
                    triggersToActivate.add(trigger);
                    processedTriggerIds.add(trigger.getId());
                }
            }
            
            // 2. Find all active workflows and their triggers
            List<Workflow> activeWorkflows = workflowRepository.findByStatusAndDeletedAtIsNull(WorkflowStatus.ACTIVE);
            log.info("Found {} active workflows", activeWorkflows.size());
            
            for (Workflow workflow : activeWorkflows) {
                Map<String, Object> definition = workflow.getDefinition();
                if (definition == null || !definition.containsKey("nodes")) {
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
                
                for (Map<String, Object> node : nodes) {
                    Object typeObj = node.get("type");
                    if (!(typeObj instanceof String)) {
                        continue;
                    }
                    String typeStr = ((String) typeObj).toUpperCase();
                    if (!NodeType.TRIGGER.name().equals(typeStr)) {
                        continue;
                    }
                    
                    // Get triggerConfigId from node data
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeData = node.containsKey("data") ?
                            (Map<String, Object>) node.get("data") : new HashMap<>();
                    
                    // Get triggerConfigId (support multiple locations)
                    String triggerConfigId = (String) nodeData.get("triggerConfigId");
                    if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> config = (Map<String, Object>) nodeData.get("config");
                        if (config != null) {
                            triggerConfigId = (String) config.get("triggerConfigId");
                        }
                    }
                    
                    if (triggerConfigId == null || triggerConfigId.isEmpty()) {
                        continue;
                    }
                    
                    // Skip if already processed
                    if (processedTriggerIds.contains(triggerConfigId)) {
                        continue;
                    }
                    
                    // Load trigger config from database
                    Trigger triggerConfig = triggerRepository.findByIdAndNotDeleted(triggerConfigId).orElse(null);
                    if (triggerConfig == null) {
                        log.warn("Trigger config not found in active workflow: triggerConfigId={}, workflowId={}", 
                                 triggerConfigId, workflow.getId());
                        continue;
                    }
                    
                    // Only process EVENT triggers
                    if (triggerConfig.getTriggerType() != TriggerType.EVENT) {
                        continue;
                    }
                    
                    // Ensure trigger status is ACTIVE
                    if (triggerConfig.getStatus() != TriggerStatus.ACTIVE) {
                        log.info("Setting trigger status to ACTIVE for active workflow: triggerConfigId={}, workflowId={}", 
                                 triggerConfigId, workflow.getId());
                        triggerConfig.setStatus(TriggerStatus.ACTIVE);
                        triggerRepository.save(triggerConfig);
                    }
                    
                    triggersToActivate.add(triggerConfig);
                    processedTriggerIds.add(triggerConfigId);
                    log.debug("Found trigger in active workflow: triggerConfigId={}, workflowId={}", 
                              triggerConfigId, workflow.getId());
                }
            }
            
            log.info("Total {} event triggers to initialize ({} from trigger table, {} from active workflows)", 
                     triggersToActivate.size(), activeTriggers.size(), 
                     triggersToActivate.size() - activeTriggers.size());
            
            // Initialize consumers
            consumerManager.initializeConsumers(triggersToActivate);
            
            log.info("Event consumer initialization completed. Registered {} consumers", 
                     triggersToActivate.size());
        } catch (Exception e) {
            log.error("Failed to initialize event consumers", e);
            // Don't fail application startup - consumers can be registered later
        }
    }
}

