package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.entity.enums.NodeType;
import com.notificationplatform.service.registry.ActionRegistryService;
import com.notificationplatform.service.template.TemplateRenderer;
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
 * See: @import(features/node-types.md#api-call-action)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallNodeExecutor implements NodeExecutor {

    private final RestTemplate restTemplate;
    private final TemplateRenderer templateRenderer;
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
            
            try {
                actionRegistryService.getActionById(registryId);
            } catch (com.notificationplatform.exception.ResourceNotFoundException e) {
                throw new IllegalArgumentException("Action not found in registry: " + registryId);
            }
            
            // Parse node configuration
            ApiCallConfig config = parseConfig(nodeData, context);
            
            // Make HTTP request with retry
            ResponseEntity<Map<String, Object>> response = executeApiCall(config);
            
            // Build output
            Map<String, Object> output = new HashMap<>();
            output.put("statusCode", response.getStatusCode().value());
            output.put("status", response.getStatusCode().is2xxSuccessful() ? "success" : "error");
            output.put("headers", response.getHeaders().toSingleValueMap());
            output.put("body", response.getBody());
            
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
     * Parse API call configuration from node data.
     */
    @SuppressWarnings("unchecked")
    private ApiCallConfig parseConfig(Map<String, Object> nodeData, ExecutionContext context) {
        ApiCallConfig config = new ApiCallConfig();
        
        // Get variables from context for template rendering
        Map<String, Object> variables = context.getDataForNode((String) nodeData.get("nodeId"));
        
        // Parse URL
        String url = (String) nodeData.get("url");
        if (url != null && url.contains("${")) {
            url = templateRenderer.render(url, variables);
        }
        config.setUrl(url);
        
        // Parse method
        String method = (String) nodeData.getOrDefault("method", "GET");
        config.setMethod(method);
        
        // Parse headers
        Map<String, Object> headersObj = (Map<String, Object>) nodeData.get("headers");
        if (headersObj != null) {
            Map<String, String> headers = new HashMap<>();
            headersObj.forEach((key, value) -> {
                String headerValue = value != null ? value.toString() : "";
                if (headerValue.contains("${")) {
                    headerValue = templateRenderer.render(headerValue, variables);
                }
                headers.put(key, headerValue);
            });
            config.setHeaders(headers);
        }
        
        // Parse body
        Object body = nodeData.get("body");
        if (body instanceof String && ((String) body).contains("${")) {
            body = templateRenderer.render((String) body, variables);
        } else if (body instanceof Map) {
            // Render nested values in body
            body = renderBodyTemplate((Map<String, Object>) body, variables);
        }
        config.setBody(body);
        
        // Parse authentication
        @SuppressWarnings("unchecked")
        Map<String, Object> auth = (Map<String, Object>) nodeData.get("authentication");
        if (auth != null) {
            config.setAuthentication(auth);
        }
        
        return config;
    }

    /**
     * Render body template recursively.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> renderBodyTemplate(Map<String, Object> body, Map<String, Object> variables) {
        Map<String, Object> rendered = new HashMap<>();
        body.forEach((key, value) -> {
            if (value instanceof String && ((String) value).contains("${")) {
                rendered.put(key, templateRenderer.render((String) value, variables));
            } else if (value instanceof Map) {
                rendered.put(key, renderBodyTemplate((Map<String, Object>) value, variables));
            } else {
                rendered.put(key, value);
            }
        });
        return rendered;
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

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION;
    }
}

