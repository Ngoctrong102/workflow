package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
/**
 * Manages dynamic RabbitMQ consumer registration per trigger
 */
@Slf4j
@Component
public class RabbitMQEventConsumerRegistry implements EventConsumerRegistry {

    private final ConnectionFactory connectionFactory;
    private final RabbitMQEventProcessor eventProcessor;
    
    // Track active containers by trigger ID
    private final Map<String, SimpleMessageListenerContainer> containers = 
        new ConcurrentHashMap<>();

    public RabbitMQEventConsumerRegistry(ConnectionFactory connectionFactory,
                                        RabbitMQEventProcessor eventProcessor) {
        this.connectionFactory = connectionFactory;
        this.eventProcessor = eventProcessor;
    }

    @Override
    public String getSupportedQueueType() {
        return "rabbitmq";
    }

    /**
     * Register and start a RabbitMQ consumer for a trigger
     */
    public void registerConsumer(Trigger trigger) {
        String triggerId = trigger.getId();
        
        // Check if already registered
        if (containers.containsKey(triggerId)) {
            log.debug("RabbitMQ consumer already registered for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            Map<String, Object> config = trigger.getConfig() != null ? 
                (Map<String, Object>) trigger.getConfig() : Map.of();
            
            String queueName = (String) config.get("topic");

            if (queueName == null || queueName.isEmpty()) {
                log.warn("Cannot register RabbitMQ consumer: queue name is missing for trigger: triggerId={}", triggerId);
                return;
            }

            // Create container factory
            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
            factory.setPrefetchCount(10);
            factory.setConcurrentConsumers(1);
            factory.setMaxConcurrentConsumers(10);

            // Create message listener container
            SimpleMessageListenerContainer container = factory.createListenerContainer();
            container.setQueueNames(queueName);
            
            // Set message listener
            container.setMessageListener(
                new org.springframework.amqp.core.MessageListener() {
                    @Override
                    public void onMessage(Message message) {
                        String routingKey = (String) message.getMessageProperties().getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY);
                        String exchange = (String) message.getMessageProperties().getHeaders().get(AmqpHeaders.RECEIVED_EXCHANGE);
                        // Get channel from message properties if available
                        com.rabbitmq.client.Channel channel = null;
                        try {
                            Object channelObj = message.getMessageProperties().getHeaders().get("amqp_channel");
                            if (channelObj instanceof com.rabbitmq.client.Channel) {
                                channel = (com.rabbitmq.client.Channel) channelObj;
                            }
                        } catch (Exception e) {
                            // Channel not available, will use null
                        }
                        eventProcessor.processEvent(trigger, message, routingKey, exchange, channel);
                    }
                }
            );

            // Start container
            container.start();
            
            containers.put(triggerId, container);
            
            log.info("Registered and started RabbitMQ consumer: triggerId={}, queue={}", 
                       triggerId, queueName);
                       
        } catch (Exception e) {
            log.error("Failed to register RabbitMQ consumer for trigger: triggerId={}", triggerId, e);
            throw new RuntimeException("Failed to register RabbitMQ consumer", e);
        }
    }

    /**
     * Unregister and stop a RabbitMQ consumer for a trigger
     */
    public void unregisterConsumer(String triggerId) {
        SimpleMessageListenerContainer container = containers.remove(triggerId);
        
        if (container == null) {
            log.debug("No RabbitMQ consumer found for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            container.stop();
            log.info("Stopped and unregistered RabbitMQ consumer: triggerId={}", triggerId);
        } catch (Exception e) {
            log.error("Error stopping RabbitMQ consumer for trigger: triggerId={}", triggerId, e);
        }
    }
}

