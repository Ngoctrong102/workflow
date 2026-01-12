package com.notificationplatform.validator;

import com.notificationplatform.entity.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validator for Action Config Template structure.
 * Validates config template based on action type to ensure fields match expected format.
 * 
 * See: @import(features/action-registry.md)
 */
@Slf4j
@Component
public class ActionConfigTemplateValidator {

    private static final List<String> VALID_HTTP_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE");
    private static final List<String> VALID_AUTH_TYPES = List.of("api-key", "bearer-token");

    /**
     * Validate config template for an action.
     * @param actionType Action type
     * @param configTemplate Config template to validate
     * @return ValidationResult with errors if any
     */
    public ValidationResult validate(ActionType actionType, Map<String, Object> configTemplate) {
        if (configTemplate == null) {
            return ValidationResult.error("configTemplate is required");
        }

        return switch (actionType) {
            case API_CALL -> validateApiCallConfig(configTemplate);
            case PUBLISH_EVENT -> validatePublishEventConfig(configTemplate);
            case FUNCTION -> validateFunctionConfig(configTemplate);
            case CUSTOM_ACTION -> ValidationResult.success(); // Custom actions have flexible config
        };
    }

    /**
     * Validate API Call config template.
     */
    private ValidationResult validateApiCallConfig(Map<String, Object> config) {
        List<String> errors = new ArrayList<>();

        // Validate url (required)
        Object urlObj = config.get("url");
        if (urlObj == null) {
            errors.add("configTemplate.url is required for API_CALL action type");
        } else if (!(urlObj instanceof String)) {
            errors.add("configTemplate.url must be a string");
        } else {
            String url = (String) urlObj;
            if (url.trim().isEmpty()) {
                errors.add("configTemplate.url cannot be empty");
            } else {
                // Validate URL format (allow MVEL expressions like @{variable})
                // If URL contains MVEL expression, skip URL format validation
                if (!url.contains("@{")) {
                    try {
                        new URL(url);
                    } catch (MalformedURLException e) {
                        errors.add("configTemplate.url must be a valid URL format");
                    }
                }
            }
        }

        // Validate method (required)
        Object methodObj = config.get("method");
        if (methodObj == null) {
            errors.add("configTemplate.method is required for API_CALL action type");
        } else if (!(methodObj instanceof String)) {
            errors.add("configTemplate.method must be a string");
        } else {
            String method = ((String) methodObj).toUpperCase();
            if (!VALID_HTTP_METHODS.contains(method)) {
                errors.add("configTemplate.method must be one of: " + String.join(", ", VALID_HTTP_METHODS));
            }
        }

        // Validate headers (optional, must be Map<String, String>)
        Object headersObj = config.get("headers");
        if (headersObj != null && !(headersObj instanceof Map)) {
            errors.add("configTemplate.headers must be a map/object");
        } else if (headersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) headersObj;
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                if (!(entry.getValue() instanceof String)) {
                    errors.add("configTemplate.headers." + entry.getKey() + " must be a string");
                }
            }
        }

        // Validate body (optional, can be any JSON)
        // No validation needed - body can be any JSON structure

        // Validate authentication (optional)
        Object authObj = config.get("authentication");
        if (authObj != null) {
            if (!(authObj instanceof Map)) {
                errors.add("configTemplate.authentication must be a map/object");
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> auth = (Map<String, Object>) authObj;
                
                Object authTypeObj = auth.get("type");
                if (authTypeObj == null) {
                    errors.add("configTemplate.authentication.type is required when authentication is provided");
                } else if (!(authTypeObj instanceof String)) {
                    errors.add("configTemplate.authentication.type must be a string");
                } else {
                    String authType = (String) authTypeObj;
                    if (!VALID_AUTH_TYPES.contains(authType)) {
                        errors.add("configTemplate.authentication.type must be one of: " + String.join(", ", VALID_AUTH_TYPES));
                    } else {
                        if ("api-key".equals(authType)) {
                            if (!auth.containsKey("apiKey") || auth.get("apiKey") == null) {
                                errors.add("configTemplate.authentication.apiKey is required when type is 'api-key'");
                            }
                        } else if ("bearer-token".equals(authType)) {
                            if (!auth.containsKey("token") || auth.get("token") == null) {
                                errors.add("configTemplate.authentication.token is required when type is 'bearer-token'");
                            }
                        }
                    }
                }
            }
        }

        // Validate timeout (optional, must be number > 0)
        Object timeoutObj = config.get("timeout");
        if (timeoutObj != null) {
            if (timeoutObj instanceof Number) {
                Number timeout = (Number) timeoutObj;
                if (timeout.doubleValue() <= 0) {
                    errors.add("configTemplate.timeout must be greater than 0");
                }
            } else {
                errors.add("configTemplate.timeout must be a number");
            }
        }

        // Validate retry (optional)
        Object retryObj = config.get("retry");
        if (retryObj != null) {
            if (!(retryObj instanceof Map)) {
                errors.add("configTemplate.retry must be a map/object");
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> retry = (Map<String, Object>) retryObj;
                
                Object maxAttemptsObj = retry.get("maxAttempts");
                if (maxAttemptsObj != null) {
                    if (maxAttemptsObj instanceof Number) {
                        Number maxAttempts = (Number) maxAttemptsObj;
                        if (maxAttempts.intValue() <= 0) {
                            errors.add("configTemplate.retry.maxAttempts must be greater than 0");
                        }
                    } else {
                        errors.add("configTemplate.retry.maxAttempts must be a number");
                    }
                }
                
                Object backoffStrategyObj = retry.get("backoffStrategy");
                if (backoffStrategyObj != null && !(backoffStrategyObj instanceof String)) {
                    errors.add("configTemplate.retry.backoffStrategy must be a string");
                }
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.errors(errors);
    }

    /**
     * Validate Publish Event config template.
     */
    private ValidationResult validatePublishEventConfig(Map<String, Object> config) {
        List<String> errors = new ArrayList<>();

        // Validate kafka (required)
        Object kafkaObj = config.get("kafka");
        if (kafkaObj == null) {
            errors.add("configTemplate.kafka is required for PUBLISH_EVENT action type");
        } else if (!(kafkaObj instanceof Map)) {
            errors.add("configTemplate.kafka must be a map/object");
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> kafka = (Map<String, Object>) kafkaObj;

            // Validate brokers (required, must be List<String>, at least one)
            Object brokersObj = kafka.get("brokers");
            if (brokersObj == null) {
                errors.add("configTemplate.kafka.brokers is required");
            } else if (!(brokersObj instanceof List)) {
                errors.add("configTemplate.kafka.brokers must be an array");
            } else {
                @SuppressWarnings("unchecked")
                List<Object> brokers = (List<Object>) brokersObj;
                if (brokers.isEmpty()) {
                    errors.add("configTemplate.kafka.brokers must contain at least one broker");
                } else {
                    for (int i = 0; i < brokers.size(); i++) {
                        Object broker = brokers.get(i);
                        if (!(broker instanceof String)) {
                            errors.add("configTemplate.kafka.brokers[" + i + "] must be a string");
                        } else if (((String) broker).trim().isEmpty()) {
                            errors.add("configTemplate.kafka.brokers[" + i + "] cannot be empty");
                        }
                    }
                }
            }

            // Validate topic (required, must be non-empty string)
            Object topicObj = kafka.get("topic");
            if (topicObj == null) {
                errors.add("configTemplate.kafka.topic is required");
            } else if (!(topicObj instanceof String)) {
                errors.add("configTemplate.kafka.topic must be a string");
            } else if (((String) topicObj).trim().isEmpty()) {
                errors.add("configTemplate.kafka.topic cannot be empty");
            }

            // Validate key (optional)
            Object keyObj = kafka.get("key");
            if (keyObj != null && !(keyObj instanceof String)) {
                errors.add("configTemplate.kafka.key must be a string");
            }

            // Validate headers (optional, must be Map<String, String>)
            Object headersObj = kafka.get("headers");
            if (headersObj != null && !(headersObj instanceof Map)) {
                errors.add("configTemplate.kafka.headers must be a map/object");
            } else if (headersObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> headers = (Map<String, Object>) headersObj;
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    if (!(entry.getValue() instanceof String)) {
                        errors.add("configTemplate.kafka.headers." + entry.getKey() + " must be a string");
                    }
                }
            }
        }

        // Validate message (optional, can be any JSON)
        // No validation needed - message can be any JSON structure

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.errors(errors);
    }

    /**
     * Validate Function config template.
     */
    private ValidationResult validateFunctionConfig(Map<String, Object> config) {
        List<String> errors = new ArrayList<>();

        // Validate expression (required, must be non-empty string)
        Object expressionObj = config.get("expression");
        if (expressionObj == null) {
            errors.add("configTemplate.expression is required for FUNCTION action type");
        } else if (!(expressionObj instanceof String)) {
            errors.add("configTemplate.expression must be a string");
        } else if (((String) expressionObj).trim().isEmpty()) {
            errors.add("configTemplate.expression cannot be empty");
        }

        // Validate outputField (optional, default: "result")
        Object outputFieldObj = config.get("outputField");
        if (outputFieldObj != null && !(outputFieldObj instanceof String)) {
            errors.add("configTemplate.outputField must be a string");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.errors(errors);
    }

    /**
     * Validation result containing errors if any.
     */
    @Data
    @AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }

        public static ValidationResult error(String error) {
            return new ValidationResult(false, List.of(error));
        }

        public static ValidationResult errors(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public String getErrorMessage() {
            if (valid || errors == null || errors.isEmpty()) {
                return null;
            }
            return String.join("; ", errors);
        }
    }
}

