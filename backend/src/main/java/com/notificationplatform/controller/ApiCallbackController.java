package com.notificationplatform.controller;

import com.notificationplatform.service.eventaggregation.EventAggregationService;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Controller for receiving API callbacks from external services
 * Used by wait_events nodes to receive asynchronous API responses
 * 
 * Supports two endpoints:
 * - /api/callbacks (generic callback endpoint)
 * - /api/internal/callbacks/external-service (specific endpoint for external services)
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiCallbackController {

    private final EventAggregationService eventAggregationService;

    public ApiCallbackController(EventAggregationService eventAggregationService) {
        this.eventAggregationService = eventAggregationService;
    }

    /**
     * Generic callback endpoint for API responses
     * Extracts execution_id and correlation_id from request body or headers
     */
    @PostMapping("/callbacks")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Execution-Id", required = false) String executionIdHeader,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationIdHeader) {
        
        return processCallback(body, executionIdHeader, correlationIdHeader);
    }

    /**
     * Specific callback endpoint for external services
     * POST /api/internal/callbacks/external-service
     * 
     * Supports:
     * - Header-based IDs: X-Correlation-Id, X-Execution-Id
     * - Body-based IDs: correlation_id, execution_id (supports nested structures)
     * - Priority: Header first, then body
     */
    @PostMapping(value = "/internal/callbacks/external-service", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleExternalServiceCallback(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationIdHeader,
            @RequestHeader(value = "X-Execution-Id", required = false) String executionIdHeader) {
        
        long startTime = System.currentTimeMillis();
        
        // Log incoming callback
        int requestSize = body != null ? body.toString().length() : 0;
        log.info("Received external service callback: requestSize={} bytes, correlationIdHeader={}, executionIdHeader={}", 
                   requestSize, correlationIdHeader, executionIdHeader);

        try {
            // Validate request
            if (!validateRequest(body)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Invalid request: body is required");
                log.warn("Invalid callback request: body is null or empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Extract IDs with priority: header first, then body
            String correlationId = extractIdWithPriority(correlationIdHeader, body, "correlation_id");
            String executionId = extractIdWithPriority(executionIdHeader, body, "execution_id");

            // Validate both IDs are present
            if (correlationId == null || correlationId.isEmpty()) {
                log.warn("API callback missing correlation_id: executionId={}", executionId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Missing correlation_id (required in X-Correlation-Id header or correlation_id field)");
                return ResponseEntity.badRequest().body(response);
            }

            if (executionId == null || executionId.isEmpty()) {
                log.warn("API callback missing execution_id: correlationId={}", correlationId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Missing execution_id (required in X-Execution-Id header or execution_id field)");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("Processing callback: executionId={}, correlationId={}", executionId, correlationId);

            // Prepare response data (excluding IDs from body for cleaner data)
            Map<String, Object> responseData = prepareResponseData(body);

            // Handle API response
            eventAggregationService.handleApiResponse(executionId, correlationId, responseData);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Callback processed successfully: executionId={}, correlationId={}, processingTime={}ms", 
                       executionId, correlationId, processingTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Callback received and processed");
            response.put("executionId", executionId);
            response.put("correlationId", correlationId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid callback request: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Error processing API callback: processingTime={}ms", processingTime, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Internal server error processing callback");
            // Don't expose internal error details to external services
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Process callback (shared logic for both endpoints)
     */
    private ResponseEntity<Map<String, Object>> processCallback(
            Map<String, Object> body,
            String executionIdHeader,
            String correlationIdHeader) {
        
        log.info("Received API callback: executionIdHeader={}, correlationIdHeader={}", 
                   executionIdHeader, correlationIdHeader);

        try {
            // Extract execution_id and correlation_id from body or headers
            String executionId = extractIdWithPriority(executionIdHeader, body, "execution_id");
            String correlationId = extractIdWithPriority(correlationIdHeader, body, "correlation_id");

            if (executionId == null || correlationId == null) {
                log.warn("API callback missing execution_id or correlation_id: executionId={}, correlationId={}", 
                           executionId, correlationId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Missing execution_id or correlation_id");
                return ResponseEntity.badRequest().body(response);
            }

            // Prepare response data
            Map<String, Object> responseData = prepareResponseData(body);

            // Handle API response
            eventAggregationService.handleApiResponse(executionId, correlationId, responseData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Callback received and processed");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing API callback", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Extract ID with priority: header first, then body
     * Supports nested structures in body (e.g., data.correlation_id)
     */
    private String extractIdWithPriority(String headerValue, Map<String, Object> body, String bodyKey) {
        // Priority 1: Check header value
        if (headerValue != null && !headerValue.trim().isEmpty()) {
            return headerValue.trim();
        }

        // Priority 2: Check body (supports nested structures)
        if (body != null) {
            return extractIdFromBody(body, bodyKey);
        }

        return null;
    }

    /**
     * Extract ID from body, supporting nested structures
     * Supports: "correlation_id", "data.correlation_id", "data[0].correlation_id"
     */
    private String extractIdFromBody(Map<String, Object> body, String key) {
        if (body == null || key == null) {
            return null;
        }

        // Try direct key first
        if (body.containsKey(key)) {
            Object value = body.get(key);
            return value != null ? value.toString() : null;
        }

        // Try nested path (e.g., "data.correlation_id")
        String[] parts = key.split("\\.");
        Object current = body;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            
            if (current == null) {
                return null;
            }

            // Check if part contains array access (e.g., "data[0]")
            if (part.contains("[") && part.contains("]")) {
                int bracketIndex = part.indexOf('[');
                String arrayKey = part.substring(0, bracketIndex);
                String indexStr = part.substring(bracketIndex + 1, part.indexOf(']'));
                
                try {
                    int index = Integer.parseInt(indexStr);
                    
                    if (current instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) current;
                        Object arrayObj = map.get(arrayKey);
                        
                        if (arrayObj instanceof java.util.List) {
                            java.util.List<Object> list = (java.util.List<Object>) arrayObj;
                            if (index >= 0 && index < list.size()) {
                                current = list.get(index);
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid array index in key path: {}", part);
                    return null;
                }
            } else {
                // Regular nested field access
                if (current instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) current;
                    current = map.get(part);
                } else {
                    return null;
                }
            }
        }

        return current != null ? current.toString() : null;
    }

    /**
     * Validate request
     */
    private boolean validateRequest(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        
        // Additional validation can be added here (e.g., size limits, content type)
        return true;
    }

    /**
     * Prepare response data by removing IDs (they're metadata, not part of the response)
     */
    private Map<String, Object> prepareResponseData(Map<String, Object> body) {
        Map<String, Object> responseData = new HashMap<>();
        
        if (body != null) {
            responseData.putAll(body);
            // Remove IDs from response data (they're metadata, not part of the response)
            responseData.remove("execution_id");
            responseData.remove("correlation_id");
            
            // Also remove from nested structures if present
            removeIdFromNested(responseData, "execution_id");
            removeIdFromNested(responseData, "correlation_id");
        }
        
        return responseData;
    }

    /**
     * Remove ID from nested structures recursively
     */
    private void removeIdFromNested(Map<String, Object> data, String idKey) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                nestedMap.remove(idKey);
                // Recursively process nested maps
                removeIdFromNested(nestedMap, idKey);
            } else if (value instanceof java.util.List) {
                java.util.List<Object> list = (java.util.List<Object>) value;
                for (Object item : list) {
                    if (item instanceof Map) {
                        removeIdFromNested((Map<String, Object>) item, idKey);
                    }
                }
            }
        }
    }
}

