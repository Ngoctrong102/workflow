package com.notificationplatform.service.trigger.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.eventaggregation.EventAggregationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;


import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * Processes Kafka events for a specific trigger
 */
@Slf4j
@Component
public class KafkaEventProcessor {

    private final WorkflowExecutor workflowExecutor;
    private final EventFilterService eventFilterService;
    private final EventAggregationService eventAggregationService;
    private final ExecutionWaitStateRepository waitStateRepository;
    private final TriggerRepository triggerRepository;
    private final ObjectMapper objectMapper;

    public KafkaEventProcessor(WorkflowExecutor workflowExecutor,
                              EventFilterService eventFilterService,
                              EventAggregationService eventAggregationService,
                              ExecutionWaitStateRepository waitStateRepository,
                              TriggerRepository triggerRepository,
                              ObjectMapper objectMapper) {
        this.workflowExecutor = workflowExecutor;
        this.eventFilterService = eventFilterService;
        this.eventAggregationService = eventAggregationService;
        this.waitStateRepository = waitStateRepository;
        this.triggerRepository = triggerRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a Kafka event for a specific trigger
     */
    public void processEvent(Trigger trigger, ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.debug("Processing Kafka event for trigger: triggerId={}, topic={}, partition={}, offset={}", 
                       trigger.getId(), record.topic(), record.partition(), record.offset());

            // Parse event data
            Map<String, Object> eventData = parseEventData(record.value());

            // Check if this is a callback event for a wait state
            String executionId = extractField(eventData, "execution_id");
            String correlationId = extractField(eventData, "correlation_id");
            
            if (executionId != null && correlationId != null) {
                handleWaitStateEvent(executionId, correlationId, record.topic(), eventData);
            }

            // Process as regular event trigger
            processTriggerEvent(trigger, record, eventData);

            // Acknowledge message
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("Error processing Kafka event for trigger: triggerId={}, topic={}", 
                        trigger.getId(), record.topic(), e);
            // Acknowledge to avoid blocking consumer
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }

    /**
     * Process trigger event in a new transaction.
     * Must be public for @Transactional to work in async contexts.
     */
    @Transactional
    public void processTriggerEvent(Trigger trigger, ConsumerRecord<String, String> record, Map<String, Object> eventData) {
        try {
            // Load trigger config
            Trigger reloadedTrigger = triggerRepository.findByIdAndNotDeleted(trigger.getId())
                    .orElse(null);
            
            if (reloadedTrigger == null) {
                log.warn("Trigger config not found: triggerId={}", trigger.getId());
                return;
            }
            
            // Find workflows that use this trigger config by parsing workflow definitions
            // In the new design, triggers are independent configs referenced via triggerConfigId in workflow nodes
            // Need to:
            // 1. Query all active workflows
            // 2. Parse workflow definitions to find nodes with triggerConfigId matching this trigger
            // 3. Execute those workflows
            // This functionality needs to be implemented - for now, log warning and return
            log.warn("KafkaEventProcessor - need to find workflows using trigger config: triggerId={}", trigger.getId());
            return;

        } catch (IllegalStateException e) {
            // ApplicationContext may be closed during shutdown
            if (e.getMessage() != null && e.getMessage().contains("has been closed")) {
                log.warn("ApplicationContext is closed, skipping event processing: triggerId={}", trigger.getId());
                return;
            }
            log.error("Error processing trigger event: triggerId={}", trigger.getId(), e);
        } catch (Exception e) {
            log.error("Error processing trigger event: triggerId={}", trigger.getId(), e);
        }
    }

    private void handleWaitStateEvent(String executionId, String correlationId, String topic, Map<String, Object> eventData) {
        try {
            Optional<ExecutionWaitState> waitStateOpt = findWaitingExecution(executionId, correlationId);
            
            if (waitStateOpt.isPresent()) {
                ExecutionWaitState waitState = waitStateOpt.get();
                
                if (applyKafkaEventFilter(waitState, eventData)) {
                    log.info("Forwarding Kafka event to EventAggregationService: executionId={}, correlationId={}, topic={}", 
                               executionId, correlationId, topic);
                    eventAggregationService.handleKafkaEvent(topic, eventData);
                }
            }
        } catch (Exception e) {
            log.error("Error handling wait state event: executionId={}, correlationId={}", 
                        executionId, correlationId, e);
        }
    }

    private Map<String, Object> parseEventData(String value) {
        Map<String, Object> data = new HashMap<>();
        
        if (value == null || value.isEmpty()) {
            return data;
        }
        
        try {
            if (value.trim().startsWith("{") || value.trim().startsWith("[")) {
                Map<String, Object> parsed = objectMapper.readValue(value, Map.class);
                data.putAll(parsed);
            } else {
                data.put("_raw", value);
            }
        } catch (Exception e) {
            log.warn("Error parsing Kafka event data, using raw value: {}", e.getMessage());
            data.put("_raw", value);
        }
        
        return data;
    }

    private String extractField(Map<String, Object> eventData, String fieldName) {
        if (eventData == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        try {
            Object value = eventData.get(fieldName);
            if (value != null) {
                return value.toString();
            }

            // Try nested path
            String[] parts = fieldName.split("\\.");
            Object current = eventData;
            
            for (String part : parts) {
                if (current == null) {
                    return null;
                }

                if (part.contains("[") && part.contains("]")) {
                    int bracketIndex = part.indexOf('[');
                    String arrayKey = part.substring(0, bracketIndex);
                    String indexStr = part.substring(bracketIndex + 1, part.indexOf(']'));
                    
                    try {
                        int index = Integer.parseInt(indexStr);
                        
                        if (current instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) current;
                            Object arrayObj = map.get(arrayKey);
                            
                            if (arrayObj instanceof java.util.List) {
                                java.util.List<Object> list = (java.util.List<Object>) arrayObj;
                                if (index >= 0 && index < list.size()) {
                                    current = list.get(index);
                                } else {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else {
                    if (current instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) current;
                        current = map.get(part);
                    } else {
                        return null;
                    }
                }
            }
            
            return current != null ? current.toString() : null;
            
        } catch (Exception e) {
            log.debug("Error extracting field '{}': {}", fieldName, e.getMessage());
            return null;
        }
    }

    private Optional<ExecutionWaitState> findWaitingExecution(String executionId, String correlationId) {
        try {
            Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                    .findByExecutionIdAndCorrelationIdAndStatus(executionId, correlationId, "waiting");

            if (waitStateOpt.isPresent()) {
                ExecutionWaitState waitState = waitStateOpt.get();
                
                // Check if Kafka event is enabled for this wait state
                // waitType can be: 'api_response', 'kafka_event', 'both'
                String waitType = waitState.getWaitType();
                if (waitType == null || 
                    (!waitType.equals("kafka_event") && !waitType.equals("both"))) {
                    return Optional.empty();
                }
                
                return waitStateOpt;
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error finding waiting execution: executionId={}, correlationId={}", 
                         executionId, correlationId, e);
            return Optional.empty();
        }
    }

    private boolean applyKafkaEventFilter(ExecutionWaitState waitState, Map<String, Object> eventData) {
        // TODO: Implement filter from workflow definition
        return true;
    }
}

