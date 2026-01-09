package com.notificationplatform.engine.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Executor for Publish Event Action node (Kafka).
 * Publishes messages to Kafka topics.
 * 
 * See: @import(features/node-types.md#publish-event-action-kafka)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublishEventNodeExecutor implements NodeExecutor {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TemplateRenderer templateRenderer;
    private final ActionRegistryService actionRegistryService;
    private final ObjectMapper objectMapper;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing publish event action node: nodeId={}", nodeId);
        
        try {
            // Get registry ID and load action from registry
            String registryId = (String) nodeData.get("registryId");
            if (registryId == null) {
                throw new IllegalArgumentException("Registry ID is required for publish event action");
            }
            
            try {
                actionRegistryService.getActionById(registryId);
            } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Parse Kafka configuration
            KafkaPublishConfig config = parseConfig(nodeData, context);
            
            // Serialize message to JSON
            String messageJson = objectMapper.writeValueAsString(config.getMessage());
            
            // Publish to Kafka
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                config.getTopic(),
                config.getKey(),
                messageJson
            );
            
            // Wait for result (synchronous for now)
            SendResult<String, String> result = future.get();
            
            // Build output
            Map<String, Object> output = new HashMap<>();
            output.put("status", "success");
            output.put("topic", config.getTopic());
            output.put("key", config.getKey());
            output.put("partition", result.getRecordMetadata().partition());
            output.put("offset", result.getRecordMetadata().offset());
            output.put("timestamp", result.getRecordMetadata().timestamp());
            
            log.info("Message published to Kafka: topic={}, partition={}, offset={}", 
                     config.getTopic(), result.getRecordMetadata().partition(), 
                     result.getRecordMetadata().offset());
            
            return new NodeExecutionResult(true, output);
            
        } catch (Exception e) {
            log.error("Error executing publish event action: nodeId={}", nodeId, e);
            Map<String, Object> output = new HashMap<>();
            output.put("status", "failed");
            output.put("error", e.getMessage());
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }

    /**
     * Parse Kafka publish configuration from node data.
     */
    @SuppressWarnings("unchecked")
    private KafkaPublishConfig parseConfig(Map<String, Object> nodeData, ExecutionContext context) {
        KafkaPublishConfig config = new KafkaPublishConfig();
        
        // Get variables from context for template rendering
        Map<String, Object> variables = context.getDataForNode((String) nodeData.get("nodeId"));
        
        // Parse Kafka config
        Map<String, Object> kafkaConfig = (Map<String, Object>) nodeData.get("kafka");
        if (kafkaConfig != null) {
            // Parse brokers (not used directly, but for validation)
            List<String> brokers = (List<String>) kafkaConfig.get("brokers");
            config.setBrokers(brokers);
            
            // Parse topic
            String topic = (String) kafkaConfig.get("topic");
            if (topic != null && topic.contains("${")) {
                topic = templateRenderer.render(topic, variables);
            }
            config.setTopic(topic);
            
            // Parse key
            String key = (String) kafkaConfig.get("key");
            if (key != null && key.contains("${")) {
                key = templateRenderer.render(key, variables);
            }
            config.setKey(key);
            
            // Parse headers
            Map<String, Object> headers = (Map<String, Object>) kafkaConfig.get("headers");
            if (headers != null) {
                config.setHeaders(headers);
            }
        } else {
            // Fallback: parse from root level
            String topic = (String) nodeData.get("topic");
            if (topic != null && topic.contains("${")) {
                topic = templateRenderer.render(topic, variables);
            }
            config.setTopic(topic);
            
            String key = (String) nodeData.get("key");
            if (key != null && key.contains("${")) {
                key = templateRenderer.render(key, variables);
            }
            config.setKey(key);
        }
        
        // Parse message
        Object message = nodeData.get("message");
        if (message instanceof String && ((String) message).contains("${")) {
            message = templateRenderer.render((String) message, variables);
        } else if (message instanceof Map) {
            // Render nested values in message
            message = renderMessageTemplate((Map<String, Object>) message, variables);
        }
        config.setMessage(message);
        
        return config;
    }

    /**
     * Render message template recursively.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> renderMessageTemplate(Map<String, Object> message, Map<String, Object> variables) {
        Map<String, Object> rendered = new HashMap<>();
        message.forEach((key, value) -> {
            if (value instanceof String && ((String) value).contains("${")) {
                rendered.put(key, templateRenderer.render((String) value, variables));
            } else if (value instanceof Map) {
                rendered.put(key, renderMessageTemplate((Map<String, Object>) value, variables));
            } else {
                rendered.put(key, value);
            }
        });
        return rendered;
    }

    /**
     * Kafka publish configuration.
     */
    private static class KafkaPublishConfig {
        private List<String> brokers;
        private String topic;
        private String key;
        private Map<String, Object> headers;
        private Object message;

        public List<String> getBrokers() {
            return brokers;
        }

        public void setBrokers(List<String> brokers) {
            this.brokers = brokers;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Map<String, Object> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, Object> headers) {
            this.headers = headers;
        }

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION;
    }
}

