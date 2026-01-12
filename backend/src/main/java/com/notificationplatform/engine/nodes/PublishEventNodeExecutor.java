package com.notificationplatform.engine.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.entity.Action;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.workflow.ExecutionContextBuilder;
import com.notificationplatform.util.MvelEvaluator;
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
 * This executor is called by ActionNodeExecutor for PUBLISH_EVENT action type.
 * 
 * See: @import(features/node-types.md#publish-event-action-kafka)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublishEventNodeExecutor implements ActionExecutor {

    private final KafkaTemplate<String, String> kafkaTemplate;
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
            
            Action action;
            try {
                action = actionRegistryService.getActionById(registryId);
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Get config values (new structure) or parse from nodeData (backward compatibility)
            Map<String, Object> configValues = getConfigValues(nodeData);
            
            // Build MVEL execution context
            Map<String, Object> mvelContext = ExecutionContextBuilder.buildContext(context);
            
            // Evaluate MVEL expressions in config values
            Map<String, Object> resolvedConfig = (Map<String, Object>) 
                MvelEvaluator.evaluateObject(configValues, mvelContext);
            
            // Parse resolved config to KafkaPublishConfig
            KafkaPublishConfig config = parseResolvedConfig(resolvedConfig);
            
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
            
            // Build raw response
            Map<String, Object> rawResponse = new HashMap<>();
            rawResponse.put("success", true);
            rawResponse.put("topic", config.getTopic());
            rawResponse.put("partition", result.getRecordMetadata().partition());
            rawResponse.put("offset", result.getRecordMetadata().offset());
            rawResponse.put("timestamp", result.getRecordMetadata().timestamp());
            
            // Build output context for output mapping
            Map<String, Object> outputContext = ExecutionContextBuilder.buildOutputContext(context, rawResponse);
            
            // Apply output mapping (if available from action registry or node config)
            Map<String, Object> output = applyOutputMapping(action, nodeData, outputContext, rawResponse);
            
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
     * Get config values from node data.
     * Supports both new structure (config.configValues) and old structure (direct fields).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getConfigValues(Map<String, Object> nodeData) {
        // Try new structure first: nodeData.config.configValues
        Object configObj = nodeData.get("config");
        if (configObj instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) configObj;
            Object configValuesObj = config.get("configValues");
            if (configValuesObj instanceof Map) {
                return new HashMap<>((Map<String, Object>) configValuesObj);
            }
        }
        
        // Fallback to old structure: direct fields in nodeData
        Map<String, Object> configValues = new HashMap<>();
        
        // Parse Kafka config
        Object kafkaObj = nodeData.get("kafka");
        if (kafkaObj instanceof Map) {
            configValues.put("kafka", kafkaObj);
        } else {
            // Fallback: parse from root level
            Map<String, Object> kafkaConfig = new HashMap<>();
            if (nodeData.containsKey("topic")) {
                kafkaConfig.put("topic", nodeData.get("topic"));
            }
            if (nodeData.containsKey("key")) {
                kafkaConfig.put("key", nodeData.get("key"));
            }
            if (nodeData.containsKey("brokers")) {
                kafkaConfig.put("brokers", nodeData.get("brokers"));
            }
            if (nodeData.containsKey("headers")) {
                kafkaConfig.put("headers", nodeData.get("headers"));
            }
            if (!kafkaConfig.isEmpty()) {
                configValues.put("kafka", kafkaConfig);
            }
        }
        
        if (nodeData.containsKey("message")) {
            configValues.put("message", nodeData.get("message"));
        }
        
        return configValues;
    }
    
    /**
     * Parse resolved config (after MVEL evaluation) to KafkaPublishConfig.
     */
    @SuppressWarnings("unchecked")
    private KafkaPublishConfig parseResolvedConfig(Map<String, Object> resolvedConfig) {
        KafkaPublishConfig config = new KafkaPublishConfig();
        
        // Parse Kafka config
        Object kafkaObj = resolvedConfig.get("kafka");
        if (kafkaObj instanceof Map) {
            Map<String, Object> kafkaConfig = (Map<String, Object>) kafkaObj;
            
            // Parse brokers
            Object brokersObj = kafkaConfig.get("brokers");
            if (brokersObj instanceof List) {
                config.setBrokers((List<String>) brokersObj);
            }
            
            // Parse topic
            Object topicObj = kafkaConfig.get("topic");
            if (topicObj != null) {
                config.setTopic(topicObj.toString());
            }
            
            // Parse key
            Object keyObj = kafkaConfig.get("key");
            if (keyObj != null) {
                config.setKey(keyObj.toString());
            }
            
            // Parse headers
            Object headersObj = kafkaConfig.get("headers");
            if (headersObj instanceof Map) {
                config.setHeaders((Map<String, Object>) headersObj);
            }
        }
        
        // Parse message
        Object message = resolvedConfig.get("message");
        config.setMessage(message);
        
        return config;
    }
    
    /**
     * Apply output mapping to raw response.
     * Uses output mapping from action registry or node config (if provided).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> applyOutputMapping(
            Action action, 
            Map<String, Object> nodeData, 
            Map<String, Object> outputContext,
            Map<String, Object> rawResponse) {
        
        // Get output mapping from node config (if provided) or action registry
        Map<String, String> outputMapping = null;
        
        // Try node config first (custom override)
        Object configObj = nodeData.get("config");
        if (configObj instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) configObj;
            Object outputMappingObj = config.get("outputMapping");
            if (outputMappingObj instanceof Map) {
                outputMapping = (Map<String, String>) outputMappingObj;
            }
        }
        
        // Fallback to action registry output mapping
        if (outputMapping == null && action.getConfigTemplate() != null) {
            Object outputMappingObj = action.getConfigTemplate().get("outputMapping");
            if (outputMappingObj instanceof Map) {
                outputMapping = (Map<String, String>) outputMappingObj;
            }
        }
        
        // If no output mapping, return raw response with status
        if (outputMapping == null || outputMapping.isEmpty()) {
            Map<String, Object> output = new HashMap<>(rawResponse);
            Boolean success = (Boolean) rawResponse.get("success");
            output.put("status", success != null && success ? "success" : "failed");
            return output;
        }
        
        // Apply output mapping with MVEL evaluation
        Map<String, Object> mappedOutput = new HashMap<>();
        for (Map.Entry<String, String> entry : outputMapping.entrySet()) {
            String fieldName = entry.getKey();
            String mvelExpression = entry.getValue();
            
            try {
                Object value = MvelEvaluator.evaluateExpression(mvelExpression, outputContext);
                mappedOutput.put(fieldName, value);
            } catch (Exception e) {
                log.warn("Failed to evaluate output mapping for field '{}': {}", fieldName, e.getMessage());
                // Use raw response value if available
                mappedOutput.put(fieldName, rawResponse.get(fieldName));
            }
        }
        
        return mappedOutput;
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

}

