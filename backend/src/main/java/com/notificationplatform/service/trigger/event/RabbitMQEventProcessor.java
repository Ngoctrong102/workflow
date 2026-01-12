package com.notificationplatform.service.trigger.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.repository.TriggerRepository;
import com.rabbitmq.client.Channel;


import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Processes RabbitMQ events for a specific trigger
 */
@Slf4j
@Component
public class RabbitMQEventProcessor {

    private final WorkflowExecutor workflowExecutor;
    private final EventFilterService eventFilterService;
    private final TriggerRepository triggerRepository;
    private final ObjectMapper objectMapper;

    public RabbitMQEventProcessor(WorkflowExecutor workflowExecutor,
                                  EventFilterService eventFilterService,
                                  TriggerRepository triggerRepository,
                                  ObjectMapper objectMapper) {
        this.workflowExecutor = workflowExecutor;
        this.eventFilterService = eventFilterService;
        this.triggerRepository = triggerRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a RabbitMQ event for a specific trigger
     */
    public void processEvent(Trigger trigger, Message message, String routingKey, 
                            String exchange, Channel channel) {
        try {
            String queueName = message.getMessageProperties().getConsumerQueue();
            log.debug("Processing RabbitMQ event for trigger: triggerId={}, queue={}, routingKey={}", 
                       trigger.getId(), queueName, routingKey);

            // Parse message body
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            Map<String, Object> eventData = parseMessage(messageBody);

            // Process trigger
            boolean processed = processTrigger(trigger, eventData, routingKey, exchange);

            // Acknowledge message
            if (processed && channel != null) {
                try {
                    long deliveryTag = message.getMessageProperties().getDeliveryTag();
                    channel.basicAck(deliveryTag, false);
                } catch (Exception e) {
                    log.error("Error acknowledging message for trigger: triggerId={}", trigger.getId(), e);
                }
            } else if (channel != null) {
                // Nack for retry if not processed
                try {
                    long deliveryTag = message.getMessageProperties().getDeliveryTag();
                    channel.basicNack(deliveryTag, false, true);
                } catch (Exception e) {
                    log.error("Error nacking message for trigger: triggerId={}", trigger.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error processing RabbitMQ event for trigger: triggerId={}", trigger.getId(), e);
            if (channel != null) {
                try {
                    long deliveryTag = message.getMessageProperties().getDeliveryTag();
                    channel.basicNack(deliveryTag, false, true);
                } catch (Exception nackException) {
                    log.error("Error nacking message after error", nackException);
                }
            }
        }
    }

    /**
     * Process trigger in a new transaction.
     * Must be public for @Transactional to work in async contexts.
     */
    @Transactional
    public boolean processTrigger(Trigger trigger, Map<String, Object> eventData, 
                                  String routingKey, String exchange) {
        try {
            // Load trigger config
            Trigger reloadedTrigger = triggerRepository.findByIdAndNotDeleted(trigger.getId())
                    .orElse(null);
            
            if (reloadedTrigger == null) {
                log.warn("Trigger config not found: triggerId={}", trigger.getId());
                return false;
            }
            
            Map<String, Object> config = reloadedTrigger.getConfig() != null ? 
                (Map<String, Object>) reloadedTrigger.getConfig() : new HashMap<>();
            Map<String, Object> filter = (Map<String, Object>) config.get("filter");

            // Apply filter if configured
            if (filter != null && !filter.isEmpty()) {
                if (!eventFilterService.matchesFilter(eventData, filter)) {
                    log.debug("Event does not match filter for trigger: triggerId={}", reloadedTrigger.getId());
                    return false;
                }
            }
            
            // Find workflows that use this trigger config by parsing workflow definitions
            // In the new design, triggers are independent configs referenced via triggerConfigId in workflow nodes
            // Need to:
            // 1. Query all active workflows
            // 2. Parse workflow definitions to find nodes with triggerConfigId matching this trigger
            // 3. Execute those workflows
            log.warn("RabbitMQEventProcessor - need to find workflows using trigger config: triggerId={}", trigger.getId());
            return false;

        } catch (Exception e) {
            log.error("Error processing trigger: triggerId={}", trigger.getId(), e);
            return false;
        }
    }

    private Map<String, Object> parseMessage(String messageBody) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (messageBody.startsWith("{") && messageBody.endsWith("}")) {
                try {
                    Map<String, Object> parsed = objectMapper.readValue(messageBody, Map.class);
                    data.putAll(parsed);
                    data.put("raw", messageBody);
                } catch (Exception e) {
                    data.put("body", messageBody);
                    data.put("raw", messageBody);
                }
            } else {
                data.put("message", messageBody);
                data.put("raw", messageBody);
            }
        } catch (Exception e) {
            log.warn("Error parsing message body, treating as plain text", e);
            data.put("message", messageBody);
            data.put("raw", messageBody);
        }
        return data;
    }
}

