package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.entity.Action;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.workflow.ExecutionContextBuilder;
import com.notificationplatform.util.MvelEvaluator;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for API Call Action node.
 * Makes HTTP requests to external APIs with retry support.
 * 
 * This executor is called by ActionNodeExecutor for API_CALL action type.
 * 
 * See: @import(features/node-types.md#api-call-action)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallNodeExecutor implements ActionExecutor {

    private final RestTemplate restTemplate;
    private final ActionRegistryService actionRegistryService;

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing API call action node: nodeId={}", nodeId);
        
        try {
            // Get registry ID and load action from registry
            String registryId = (String) nodeData.get("registryId");
            if (registryId == null) {
                throw new IllegalArgumentException("Registry ID is required for API call action");
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
            
            // Parse resolved config to ApiCallConfig
            ApiCallConfig config = parseResolvedConfig(resolvedConfig);
            
            // Make HTTP request with retry
            ResponseEntity<Map<String, Object>> response = executeApiCall(config);
            
            // Build raw response
            Map<String, Object> rawResponse = new HashMap<>();
            rawResponse.put("statusCode", response.getStatusCode().value());
            rawResponse.put("headers", response.getHeaders().toSingleValueMap());
            rawResponse.put("body", response.getBody());
            
            // Build output context for output mapping
            Map<String, Object> outputContext = ExecutionContextBuilder.buildOutputContext(context, rawResponse);
            
            // Apply output mapping (if available from action registry or node config)
            Map<String, Object> output = applyOutputMapping(action, nodeData, outputContext, rawResponse);
            
            return new NodeExecutionResult(true, output);
            
        } catch (Exception e) {
            log.error("Error executing API call action: nodeId={}", nodeId, e);
            Map<String, Object> output = new HashMap<>();
            output.put("status", "failed");
            output.put("error", e.getMessage());
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }

    /**
     * Execute API call with retry support.
     */
    @Retry(name = "apiCall")
    private ResponseEntity<Map<String, Object>> executeApiCall(ApiCallConfig config) {
        HttpMethod method = HttpMethod.valueOf(config.getMethod().toUpperCase());
        HttpHeaders headers = new HttpHeaders();
        
        // Set headers
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(headers::set);
        }
        
        // Set authentication
        if (config.getAuthentication() != null) {
            String authType = (String) config.getAuthentication().get("type");
            if ("api-key".equals(authType)) {
                String apiKey = (String) config.getAuthentication().get("apiKey");
                String headerName = (String) config.getAuthentication().getOrDefault("headerName", "X-API-Key");
                headers.set(headerName, apiKey);
            } else if ("bearer-token".equals(authType)) {
                String token = (String) config.getAuthentication().get("token");
                headers.setBearerAuth(token);
            }
        }
        
        // Create request entity
        HttpEntity<Object> requestEntity = new HttpEntity<>(config.getBody(), headers);
        
        // Make request
        org.springframework.core.ParameterizedTypeReference<Map<String, Object>> responseType = 
            new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {};
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            config.getUrl(),
            method,
            requestEntity,
            responseType
        );
        
        return response;
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
        if (nodeData.containsKey("url")) {
            configValues.put("url", nodeData.get("url"));
        }
        if (nodeData.containsKey("method")) {
            configValues.put("method", nodeData.get("method"));
        } else {
            configValues.put("method", "GET");
        }
        if (nodeData.containsKey("headers")) {
            configValues.put("headers", nodeData.get("headers"));
        }
        if (nodeData.containsKey("body")) {
            configValues.put("body", nodeData.get("body"));
        }
        if (nodeData.containsKey("authentication")) {
            configValues.put("authentication", nodeData.get("authentication"));
        }
        if (nodeData.containsKey("timeout")) {
            configValues.put("timeout", nodeData.get("timeout"));
        }
        if (nodeData.containsKey("retry")) {
            configValues.put("retry", nodeData.get("retry"));
        }
        
        return configValues;
    }
    
    /**
     * Parse resolved config (after MVEL evaluation) to ApiCallConfig.
     */
    @SuppressWarnings("unchecked")
    private ApiCallConfig parseResolvedConfig(Map<String, Object> resolvedConfig) {
        ApiCallConfig config = new ApiCallConfig();
        
        // Parse URL
        Object urlObj = resolvedConfig.get("url");
        if (urlObj != null) {
            config.setUrl(urlObj.toString());
        }
        
        // Parse method
        Object methodObj = resolvedConfig.get("method");
        if (methodObj != null) {
            config.setMethod(methodObj.toString());
        } else {
            config.setMethod("GET");
        }
        
        // Parse headers
        Object headersObj = resolvedConfig.get("headers");
        if (headersObj instanceof Map) {
            Map<String, Object> headersMap = (Map<String, Object>) headersObj;
            Map<String, String> headers = new HashMap<>();
            headersMap.forEach((key, value) -> {
                headers.put(key, value != null ? value.toString() : "");
            });
            config.setHeaders(headers);
        }
        
        // Parse body
        Object body = resolvedConfig.get("body");
        config.setBody(body);
        
        // Parse authentication
        Object authObj = resolvedConfig.get("authentication");
        if (authObj instanceof Map) {
            config.setAuthentication((Map<String, Object>) authObj);
        }
        
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
            Integer statusCode = (Integer) rawResponse.get("statusCode");
            output.put("status", statusCode != null && statusCode >= 200 && statusCode < 300 ? "success" : "error");
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
     * API Call configuration.
     */
    private static class ApiCallConfig {
        private String url;
        private String method = "GET";
        private Map<String, String> headers;
        private Object body;
        private Map<String, Object> authentication;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }

        public Map<String, Object> getAuthentication() {
            return authentication;
        }

        public void setAuthentication(Map<String, Object> authentication) {
            this.authentication = authentication;
        }
    }

}

