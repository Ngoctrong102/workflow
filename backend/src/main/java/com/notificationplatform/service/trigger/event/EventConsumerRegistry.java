package com.notificationplatform.service.trigger.event;

import com.notificationplatform.entity.Trigger;

/**
 * Strategy interface for event consumer registries.
 * Each queue type (Kafka, RabbitMQ, etc.) has its own registry implementation.
 */
public interface EventConsumerRegistry {

    /**
     * Get the queue type this registry supports.
     * @return queue type string (e.g., "kafka", "rabbitmq")
     */
    String getSupportedQueueType();

    /**
     * Register and start a consumer for a trigger.
     * @param trigger Trigger entity with configuration
     */
    void registerConsumer(Trigger trigger);

    /**
     * Unregister and stop a consumer for a trigger.
     * @param triggerId ID of the trigger
     */
    void unregisterConsumer(String triggerId);
}

