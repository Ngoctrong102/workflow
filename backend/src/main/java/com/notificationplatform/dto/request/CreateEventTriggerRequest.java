package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class CreateEventTriggerRequest {

    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    @NotBlank(message = "Queue type is required")
    private String queueType; // kafka or rabbitmq

    @NotBlank(message = "Topic/Queue name is required")
    private String topic; // Kafka topic or RabbitMQ queue name

    private String consumerGroup; // For Kafka

    private List<String> brokers; // Kafka broker addresses

    private String offset = "latest"; // earliest or latest

    private Map<String, Object> filter; // Event filtering configuration

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }
}

