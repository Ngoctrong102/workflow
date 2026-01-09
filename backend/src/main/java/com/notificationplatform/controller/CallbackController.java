package com.notificationplatform.controller;

import com.notificationplatform.service.eventaggregation.EventAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/callback")
@Tag(name = "Callbacks", description = "Callback endpoints for async event aggregation")
@Slf4j
public class CallbackController {

    private final EventAggregationService eventAggregationService;

    public CallbackController(EventAggregationService eventAggregationService) {
        this.eventAggregationService = eventAggregationService;
    }

    @PostMapping("/api/{correlationId}")
    @Operation(summary = "Receive API callback", 
               description = "Receive API response callback for async event aggregation. " +
                            "Request body should contain execution_id and correlation_id for validation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback received and processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing required fields"),
            @ApiResponse(responseCode = "404", description = "Wait state not found")
    })
    public ResponseEntity<Map<String, String>> receiveApiCallback(
            @Parameter(description = "Correlation ID from the original API call", required = true)
            @PathVariable String correlationId,
            @RequestBody Map<String, Object> requestBody) {
        
        log.info("Received API callback: correlationId={}", correlationId);

        // Extract execution_id and correlation_id from request body
        String executionId = extractString(requestBody, "execution_id");
        String requestCorrelationId = extractString(requestBody, "correlation_id");

        // Validate correlation_id matches path parameter
        if (requestCorrelationId != null && !requestCorrelationId.equals(correlationId)) {
            log.warn("Correlation ID mismatch: path={}, body={}", correlationId, requestCorrelationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Correlation ID mismatch"));
        }

        // If execution_id not in body, try to find it from wait state
        if (executionId == null) {
            log.warn("Execution ID not found in request body for correlationId: {}", correlationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "execution_id is required in request body"));
        }

        // Extract response data (everything except execution_id and correlation_id)
        Map<String, Object> responseData = new java.util.HashMap<>(requestBody);
        responseData.remove("execution_id");
        responseData.remove("correlation_id");

        try {
            // Handle API response via EventAggregationService
            eventAggregationService.handleApiResponse(executionId, correlationId, responseData);

            log.info("Successfully processed API callback: executionId={}, correlationId={}", 
                     executionId, correlationId);

            return ResponseEntity.ok(Map.of(
                    "status", "received",
                    "message", "Callback received and processed successfully"
            ));
        } catch (Exception e) {
            log.error("Error processing API callback: executionId={}, correlationId={}", 
                      executionId, correlationId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to process callback: " + e.getMessage()));
        }
    }

    @PostMapping("/kafka/{correlationId}")
    @Operation(summary = "Receive Kafka event callback", 
               description = "Receive Kafka event callback for async event aggregation. " +
                            "Request body should contain execution_id and correlation_id for validation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback received and processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing required fields"),
            @ApiResponse(responseCode = "404", description = "Wait state not found")
    })
    public ResponseEntity<Map<String, String>> receiveKafkaCallback(
            @Parameter(description = "Correlation ID from the original event", required = true)
            @PathVariable String correlationId,
            @RequestBody Map<String, Object> requestBody) {
        
        log.info("Received Kafka callback: correlationId={}", correlationId);

        // Extract execution_id and correlation_id from request body
        String executionId = extractString(requestBody, "execution_id");
        String requestCorrelationId = extractString(requestBody, "correlation_id");

        // Validate correlation_id matches path parameter
        if (requestCorrelationId != null && !requestCorrelationId.equals(correlationId)) {
            log.warn("Correlation ID mismatch: path={}, body={}", correlationId, requestCorrelationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Correlation ID mismatch"));
        }

        // If execution_id not in body, try to find it from wait state
        if (executionId == null) {
            log.warn("Execution ID not found in request body for correlationId: {}", correlationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "execution_id is required in request body"));
        }

        // Extract topic from request body or use default
        String topic = extractString(requestBody, "_topic");
        if (topic == null) {
            topic = "default";
        }

        // Extract event data (everything except execution_id, correlation_id, _topic)
        Map<String, Object> eventData = new java.util.HashMap<>(requestBody);
        eventData.remove("execution_id");
        eventData.remove("correlation_id");
        eventData.remove("_topic");
        eventData.put("_topic", topic); // Add topic back for EventAggregationService

        try {
            // Handle Kafka event via EventAggregationService
            eventAggregationService.handleKafkaEvent(topic, eventData);

            log.info("Successfully processed Kafka callback: executionId={}, correlationId={}, topic={}", 
                     executionId, correlationId, topic);

            return ResponseEntity.ok(Map.of(
                    "status", "received",
                    "message", "Callback received and processed successfully"
            ));
        } catch (Exception e) {
            log.error("Error processing Kafka callback: executionId={}, correlationId={}", 
                      executionId, correlationId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to process callback: " + e.getMessage()));
        }
    }

    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}

