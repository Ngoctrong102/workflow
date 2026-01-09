package com.notificationplatform.controller;

import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.service.trigger.api.ApiTriggerHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Dynamic handler for API trigger endpoints.
 * This controller handles requests to trigger paths defined by users.
 * Routes requests to appropriate trigger handler based on endpoint path.
 * 
 * See: @import(features/triggers.md#1-api-call-trigger)
 */
@Slf4j
@RestController
@RequestMapping("/trigger")
public class ApiTriggerController {

    private final ApiTriggerHandler apiTriggerHandler;

    public ApiTriggerController(@Qualifier("apiTriggerRequestHandler") ApiTriggerHandler apiTriggerHandler) {
        this.apiTriggerHandler = apiTriggerHandler;
    }

    /**
     * Catch-all handler for API trigger paths
     * Paths are dynamically registered based on trigger configuration
     */
    @PostMapping("/**")
    public ResponseEntity<TriggerActivationResponse> handlePostTrigger(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam Map<String, String> queryParams) {
        
        String path = getRequestPath(request);
        return handleTrigger(path, "POST", requestBody, apiKey, queryParams);
    }

    @GetMapping("/**")
    public ResponseEntity<TriggerActivationResponse> handleGetTrigger(
            HttpServletRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam Map<String, String> queryParams) {
        
        String path = getRequestPath(request);
        return handleTrigger(path, "GET", null, apiKey, queryParams);
    }

    @PutMapping("/**")
    public ResponseEntity<TriggerActivationResponse> handlePutTrigger(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam Map<String, String> queryParams) {
        
        String path = getRequestPath(request);
        return handleTrigger(path, "PUT", requestBody, apiKey, queryParams);
    }

    @PatchMapping("/**")
    public ResponseEntity<TriggerActivationResponse> handlePatchTrigger(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam Map<String, String> queryParams) {
        
        String path = getRequestPath(request);
        return handleTrigger(path, "PATCH", requestBody, apiKey, queryParams);
    }

    private ResponseEntity<TriggerActivationResponse> handleTrigger(
            String path, String method, Map<String, Object> requestBody,
            String apiKey, Map<String, String> queryParams) {
        
        try {
            TriggerActivationResponse response = apiTriggerHandler.handleRequest(
                path, method, requestBody, apiKey, queryParams);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error handling API trigger: path={}, method={}", path, method, e);
            TriggerActivationResponse errorResponse = new TriggerActivationResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error handling API trigger: path={}, method={}", path, method, e);
            TriggerActivationResponse errorResponse = new TriggerActivationResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        // Get the actual request path
        String requestURI = request.getRequestURI();
        // Remove context path if present
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }
        return requestURI;
    }
}

