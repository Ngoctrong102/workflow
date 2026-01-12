package com.notificationplatform.engine.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.request.WaitForEventsConfigDTO;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.service.eventaggregation.EventAggregationService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Executor for wait_events node type
 * Handles waiting for multiple asynchronous events (API response and Kafka event)
 */
@Slf4j
@Component
public class WaitForEventsNodeExecutor implements NodeExecutor {

    private final EventAggregationService eventAggregationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WaitForEventsNodeExecutor(EventAggregationService eventAggregationService,
                                    @Autowired(required = false) RestTemplate restTemplate,
                                    ObjectMapper objectMapper) {
        this.eventAggregationService = eventAggregationService;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing wait_events node: nodeId={}, executionId={}", nodeId, context.getExecutionId());

        try {
            // Parse configuration
            WaitForEventsConfigDTO config = parseConfig(nodeData, context);

            // Check if any events are enabled
            boolean apiCallEnabled = config.getApiCall() != null && 
                                    Boolean.TRUE.equals(config.getApiCall().getEnabled());
            boolean kafkaEventEnabled = config.getKafkaEvent() != null && 
                                       Boolean.TRUE.equals(config.getKafkaEvent().getEnabled());

            if (!apiCallEnabled && !kafkaEventEnabled) {
                // No events to wait for, continue immediately
                log.info("No events enabled for wait_events node: nodeId={}", nodeId);
                Map<String, Object> output = new HashMap<>();
                output.put("status", "skipped");
                output.put("reason", "No events enabled");
                return new NodeExecutionResult(true, output);
            }

            // Register wait state (generates correlation ID internally)
            ExecutionWaitState waitState = eventAggregationService.registerWaitState(
                    context.getExecutionId(),
                    nodeId,
                    config
            );

            String correlationId = waitState.getCorrelationId();
            String executionId = context.getExecutionId();

            log.info("Wait state registered: waitStateId={}, correlationId={}, executionId={}, nodeId={}", 
                       waitState.getId(), correlationId, executionId, nodeId);

            // Make API call if enabled
            if (apiCallEnabled) {
                try {
                    makeApiCall(config.getApiCall(), executionId, correlationId, context);
                } catch (Exception e) {
                    // Log error but don't fail workflow (event may still arrive)
                    log.error("Error making API call for wait_events node: nodeId={}, executionId={}", 
                               nodeId, executionId, e);
                }
            }

            // Kafka subscription is handled by CorrelationAwareKafkaConsumer
            // No explicit subscription needed - consumer filters by correlation_id

            // Return waiting result
            Map<String, Object> output = new HashMap<>();
            output.put("correlationId", correlationId);
            output.put("executionId", executionId);
            output.put("status", "waiting");
            output.put("waitStateId", waitState.getId());
            output.put("enabledEvents", waitState.getEnabledEvents());

            NodeExecutionResult result = new NodeExecutionResult(true, output);
            result.setWaiting(true);
            return result;

        } catch (Exception e) {
            log.error("Error executing wait_events node: nodeId={}", nodeId, e);
            Map<String, Object> output = new HashMap<>();
            output.put("status", "failed");
            output.put("error", e.getMessage());
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }

    /**
     * Parse node configuration to WaitForEventsConfigDTO
     */
    private WaitForEventsConfigDTO parseConfig(Map<String, Object> nodeData, ExecutionContext context) {
        WaitForEventsConfigDTO config = new WaitForEventsConfigDTO();

        // Parse apiCall configuration
        Map<String, Object> apiCallData = (Map<String, Object>) nodeData.get("apiCall");
        if (apiCallData != null) {
            WaitForEventsConfigDTO.ApiCallConfigDTO apiCall = new WaitForEventsConfigDTO.ApiCallConfigDTO();
            apiCall.setEnabled((Boolean) apiCallData.getOrDefault("enabled", true));
            apiCall.setUrl((String) apiCallData.get("url"));
            apiCall.setMethod((String) apiCallData.getOrDefault("method", "POST"));
            
            Map<String, String> headers = (Map<String, String>) apiCallData.get("headers");
            apiCall.setHeaders(headers);
            
            apiCall.setBody(apiCallData.get("body"));
            apiCall.setCorrelationIdField((String) apiCallData.getOrDefault("correlationIdField", "correlation_id"));
            apiCall.setCorrelationIdHeader((String) apiCallData.getOrDefault("correlationIdHeader", "X-Correlation-Id"));
            apiCall.setExecutionIdField((String) apiCallData.getOrDefault("executionIdField", "execution_id"));
            apiCall.setExecutionIdHeader((String) apiCallData.getOrDefault("executionIdHeader", "X-Execution-Id"));
            apiCall.setTimeout(getIntegerValue(apiCallData.get("timeout"), 300));
            apiCall.setRequired((Boolean) apiCallData.getOrDefault("required", true));
            
            config.setApiCall(apiCall);
        }

        // Parse kafkaEvent configuration
        Map<String, Object> kafkaEventData = (Map<String, Object>) nodeData.get("kafkaEvent");
        if (kafkaEventData != null) {
            WaitForEventsConfigDTO.KafkaEventConfigDTO kafkaEvent = new WaitForEventsConfigDTO.KafkaEventConfigDTO();
            kafkaEvent.setEnabled((Boolean) kafkaEventData.getOrDefault("enabled", true));
            kafkaEvent.setTopic((String) kafkaEventData.get("topic"));
            kafkaEvent.setCorrelationIdField((String) kafkaEventData.getOrDefault("correlationIdField", "correlation_id"));
            kafkaEvent.setExecutionIdField((String) kafkaEventData.getOrDefault("executionIdField", "execution_id"));
            
            Map<String, Object> filter = (Map<String, Object>) kafkaEventData.get("filter");
            kafkaEvent.setFilter(filter);
            
            kafkaEvent.setTimeout(getIntegerValue(kafkaEventData.get("timeout"), 300));
            kafkaEvent.setRequired((Boolean) kafkaEventData.getOrDefault("required", true));
            
            config.setKafkaEvent(kafkaEvent);
        }

        // Parse aggregation strategy
        config.setAggregationStrategy((String) nodeData.getOrDefault("aggregationStrategy", "all"));
        
        java.util.List<String> requiredEvents = (java.util.List<String>) nodeData.get("requiredEvents");
        config.setRequiredEvents(requiredEvents);

        // Parse timeout settings
        config.setTimeout(getIntegerValue(nodeData.get("timeout"), 300));
        config.setOnTimeout((String) nodeData.getOrDefault("onTimeout", "fail"));

        // Parse output mapping
        Map<String, String> outputMapping = (Map<String, String>) nodeData.get("outputMapping");
        config.setOutputMapping(outputMapping);

        return config;
    }

    /**
     * Make API call with correlation data injection
     */
    private void makeApiCall(WaitForEventsConfigDTO.ApiCallConfigDTO apiCall, 
                            String executionId, 
                            String correlationId, 
                            ExecutionContext context) {
        log.info("Making API call: url={}, method={}, executionId={}, correlationId={}", 
                   apiCall.getUrl(), apiCall.getMethod(), executionId, correlationId);

        try {
            // Get variables from context for template rendering
            // Trigger data is now accessed via _nodeOutputs.{triggerNodeId}
            Map<String, Object> variables = context.getDataForNode(context.getExecutionId());

            // Add execution_id and correlation_id to variables for template rendering
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put("_execution_id", executionId);
            variables.put("_correlation_id", correlationId);

            // Build request body with correlation data injection
            Object body = injectCorrelationData(apiCall.getBody(), executionId, correlationId, 
                                               apiCall.getCorrelationIdField(), 
                                               apiCall.getExecutionIdField(), 
                                               variables);

            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            if (apiCall.getHeaders() != null) {
                // Render header values if they contain templates
                for (Map.Entry<String, String> entry : apiCall.getHeaders().entrySet()) {
                    String headerValue = entry.getValue();
                    if (headerValue != null && headerValue.contains("{{")) {
                        headerValue = renderSimpleTemplateHelper(headerValue, variables);
                    }
                    headers.set(entry.getKey(), headerValue);
                }
            }

            // Add correlation ID and execution ID to headers if specified
            if (apiCall.getCorrelationIdHeader() != null) {
                headers.set(apiCall.getCorrelationIdHeader(), correlationId);
            }
            if (apiCall.getExecutionIdHeader() != null) {
                headers.set(apiCall.getExecutionIdHeader(), executionId);
            }

            // Set content type
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Render URL if it contains templates
            String url = apiCall.getUrl();
            if (url != null && url.contains("{{")) {
                url = renderSimpleTemplateHelper(url, variables);
            }

            // Create request entity
            HttpEntity<Object> request = new HttpEntity<>(body, headers);

            // Determine HTTP method
            HttpMethod httpMethod = HttpMethod.valueOf(apiCall.getMethod().toUpperCase());

            // Send request (asynchronously would be better, but for MVP we do it synchronously)
            // Note: This is a fire-and-forget call - we don't wait for response
            // The response will come via callback
            restTemplate.exchange(url, httpMethod, request, String.class);

            log.info("API call sent successfully: url={}, method={}", url, apiCall.getMethod());

        } catch (Exception e) {
            log.error("Error making API call: url={}, method={}", apiCall.getUrl(), apiCall.getMethod(), e);
            throw e;
        }
    }

    /**
     * Inject correlation data into request body
     */
    private Object injectCorrelationData(Object body, 
                                        String executionId, 
                                        String correlationId,
                                        String correlationIdField,
                                        String executionIdField,
                                        Map<String, Object> variables) {
        if (body == null) {
            return null;
        }

        // If body is a string (template), render it first
        if (body instanceof String) {
            String bodyString = (String) body;
            if (bodyString.contains("{{")) {
                // Render template
                bodyString = renderSimpleTemplateHelper(bodyString, variables);
            }
            
            // Try to parse as JSON and inject correlation data
            try {
                Map<String, Object> bodyMap = objectMapper.readValue(bodyString, Map.class);
                bodyMap.put(executionIdField, executionId);
                bodyMap.put(correlationIdField, correlationId);
                return bodyMap;
            } catch (Exception e) {
                // Not JSON, return as is (correlation data should be in template)
                return bodyString;
            }
        }

        // If body is a Map, inject directly
        if (body instanceof Map) {
            Map<String, Object> bodyMap = new HashMap<>((Map<String, Object>) body);
            bodyMap.put(executionIdField, executionId);
            bodyMap.put(correlationIdField, correlationId);
            return bodyMap;
        }

        // For other types, try to convert to Map
        try {
            Map<String, Object> bodyMap = objectMapper.convertValue(body, Map.class);
            bodyMap.put(executionIdField, executionId);
            bodyMap.put(correlationIdField, correlationId);
            return bodyMap;
        } catch (Exception e) {
            log.warn("Could not inject correlation data into body, returning as is", e);
            return body;
        }
    }

    /**
     * Get integer value from object
     */
    private Integer getIntegerValue(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public com.notificationplatform.entity.enums.NodeType getNodeType() {
        // Wait for Events is a logic node subtype, not a separate node type
        return com.notificationplatform.entity.enums.NodeType.LOGIC;
    }
    
    /**
     * Simple template rendering helper - replaces @{variable} placeholders
     */
    private String renderSimpleTemplateHelper(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "@{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}

