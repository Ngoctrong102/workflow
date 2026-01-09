package com.notificationplatform.constants;

/**
 * Application-wide constants.
 * Centralizes magic strings, default values, and configuration keys.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Default values
     */
    public static final class Defaults {
        private Defaults() {}

        public static final int WORKFLOW_VERSION = 1;
        public static final int OBJECT_TYPE_VERSION = 1;
        public static final int TEMPLATE_VERSION = 1;
        public static final int NODES_EXECUTED_INITIAL = 0;
        public static final int NOTIFICATIONS_SENT_INITIAL = 0;
        public static final String KAFKA_CONSUMER_GROUP_PREFIX = "notification-platform-consumer";
        public static final String KAFKA_OFFSET_LATEST = "latest";
        public static final String KAFKA_OFFSET_EARLIEST = "earliest";
    }

    /**
     * Database column names
     */
    public static final class ColumnNames {
        private ColumnNames() {}

        public static final String ID = "id";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String DELETED_AT = "deleted_at";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String WORKFLOW_ID = "workflow_id";
        public static final String TRIGGER_ID = "trigger_id";
        public static final String EXECUTION_ID = "execution_id";
    }

    /**
     * Configuration keys
     */
    public static final class ConfigKeys {
        private ConfigKeys() {}

        public static final String TOPIC = "topic";
        public static final String BROKERS = "brokers";
        public static final String CONSUMER_GROUP = "consumerGroup";
        public static final String OFFSET = "offset";
        public static final String EVENT_FILTER = "eventFilter";
        public static final String EVENT_TYPE = "eventType";
        public static final String QUEUE_TYPE = "queueType";
        public static final String NODE_ID = "nodeId";
        public static final String CRON_EXPRESSION = "cronExpression";
        public static final String TIMEZONE = "timezone";
    }

    /**
     * Context keys for workflow execution
     */
    public static final class ContextKeys {
        private ContextKeys() {}

        public static final String TRIGGER_DATA = "triggerData";
        public static final String VARIABLES = "variables";
        public static final String NODE_OUTPUTS = "_nodeOutputs";
        public static final String METADATA = "metadata";
        public static final String EVENT = "_event";
        public static final String TOPIC = "_topic";
        public static final String PARTITION = "_partition";
        public static final String OFFSET = "_offset";
        public static final String TIMESTAMP = "_timestamp";
        public static final String RABBITMQ = "_rabbitmq";
        public static final String ROUTING_KEY = "routingKey";
        public static final String EXCHANGE = "exchange";
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        private ErrorMessages() {}

        public static final String WORKFLOW_NOT_FOUND = "Workflow not found";
        public static final String TRIGGER_NOT_FOUND = "Trigger not found";
        public static final String EXECUTION_NOT_FOUND = "Execution not found";
        public static final String INVALID_STATUS = "Invalid status value";
        public static final String INVALID_TYPE = "Invalid type value";
        public static final String WORKFLOW_NOT_ACTIVE = "Workflow is not active";
        public static final String TRIGGER_NOT_ACTIVE = "Trigger is not active";
    }

    /**
     * Kafka/RabbitMQ related constants
     */
    public static final class MessageQueue {
        private MessageQueue() {}

        public static final String KAFKA = "kafka";
        public static final String RABBITMQ = "rabbitmq";
        public static final String EXECUTION_ID = "execution_id";
        public static final String CORRELATION_ID = "correlation_id";
    }
}

