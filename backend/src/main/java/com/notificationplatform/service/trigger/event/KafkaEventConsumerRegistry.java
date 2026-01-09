package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;


import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
/**
 * Manages dynamic Kafka consumer registration per trigger
 */
@Slf4j
@Component
public class KafkaEventConsumerRegistry implements EventConsumerRegistry {

    private final KafkaEventProcessor eventProcessor;
    
    // Track active containers by trigger ID
    private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers = 
        new ConcurrentHashMap<>();

    public KafkaEventConsumerRegistry(KafkaEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @Override
    public String getSupportedQueueType() {
        return "kafka";
    }

    /**
     * Register and start a Kafka consumer for a trigger
     */
    public void registerConsumer(Trigger trigger) {
        String triggerId = trigger.getId();
        
        // Check if already registered
        if (containers.containsKey(triggerId)) {
            log.debug("Kafka consumer already registered for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            Map<String, Object> config = trigger.getConfig() != null ? 
                (Map<String, Object>) trigger.getConfig() : Map.of();
            
            String topic = (String) config.get("topic");
            
            // Safely parse brokers list from config
            List<String> brokers = null;
            Object brokersObj = config.get("brokers");
            if (brokersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> brokersList = (List<Object>) brokersObj;
                brokers = brokersList.stream()
                    .filter(item -> item != null)
                    .map(String::valueOf)
                    .toList();
            }
            
            String consumerGroup = (String) config.getOrDefault("consumerGroup", "notification-platform-consumer");
            String offset = (String) config.getOrDefault("offset", "latest");

            if (topic == null || topic.isEmpty()) {
                log.warn("Cannot register Kafka consumer: topic is missing for trigger: triggerId={}", triggerId);
                return;
            }

            // Use brokers from config, fallback to localhost:9093 (host machine listener)
            // Note: Docker network clients should use kafka:9092, host clients should use localhost:9093
            List<String> finalBrokers = (brokers != null && !brokers.isEmpty()) ? brokers : List.of("localhost:9093");
            
            log.info("Registering Kafka consumer: triggerId={}, topic={}, brokers={}, consumerGroup={}", 
                       triggerId, topic, finalBrokers, consumerGroup + "-" + triggerId);

            // Create consumer factory with trigger-specific config
            ConsumerFactory<String, String> consumerFactory = createConsumerFactory(
                finalBrokers,
                consumerGroup + "-" + triggerId,
                offset
            );

            // Create container factory
            ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

            // Create message listener container
            ConcurrentMessageListenerContainer<String, String> container = 
                factory.createContainer(topic);
            
            // Set message listener with acknowledgment support
            container.setupMessageListener(
                new org.springframework.kafka.listener.AcknowledgingMessageListener<String, String>() {
                    @Override
                    public void onMessage(@org.springframework.lang.NonNull ConsumerRecord<String, String> record, 
                                         @org.springframework.lang.Nullable Acknowledgment acknowledgment) {
                        eventProcessor.processEvent(trigger, record, acknowledgment);
                    }
                }
            );

            // Start container
            container.start();
            
            containers.put(triggerId, container);
            
            log.info("Registered and started Kafka consumer: triggerId={}, topic={}, consumerGroup={}", 
                       triggerId, topic, consumerGroup + "-" + triggerId);
                       
        } catch (Exception e) {
            log.error("Failed to register Kafka consumer for trigger: triggerId={}", triggerId, e);
            throw new RuntimeException("Failed to register Kafka consumer", e);
        }
    }

    /**
     * Unregister and stop a Kafka consumer for a trigger
     */
    public void unregisterConsumer(String triggerId) {
        ConcurrentMessageListenerContainer<String, String> container = containers.remove(triggerId);
        
        if (container == null) {
            log.debug("No Kafka consumer found for trigger: triggerId={}", triggerId);
            return;
        }

        try {
            container.stop();
            log.info("Stopped and unregistered Kafka consumer: triggerId={}", triggerId);
        } catch (Exception e) {
            log.error("Error stopping Kafka consumer for trigger: triggerId={}", triggerId, e);
        }
    }

    /**
     * Create consumer factory with specific broker configuration
     */
    private ConsumerFactory<String, String> createConsumerFactory(List<String> brokers, 
                                                                   String consumerGroup, 
                                                                   String offset) {
        Map<String, Object> props = new HashMap<>();
        String bootstrapServers = String.join(",", brokers);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        log.debug("Creating Kafka consumer factory: bootstrapServers={}, consumerGroup={}", 
                    bootstrapServers, consumerGroup);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Cleanup all Kafka consumers on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        log.info("Shutting down all Kafka consumers: count={}", containers.size());
        for (Map.Entry<String, ConcurrentMessageListenerContainer<String, String>> entry : containers.entrySet()) {
            try {
                entry.getValue().stop();
                log.debug("Stopped Kafka consumer: triggerId={}", entry.getKey());
            } catch (Exception e) {
                log.error("Error stopping Kafka consumer: triggerId={}", entry.getKey(), e);
            }
        }
        containers.clear();
        log.info("All Kafka consumers stopped");
    }
}

