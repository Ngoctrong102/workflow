package com.notificationplatform.dto.request;

import java.util.List;
import java.util.Map;

public class WaitForEventsConfigDTO {

    private ApiCallConfigDTO apiCall;
    private KafkaEventConfigDTO kafkaEvent;
    private String aggregationStrategy; // all, any, required_only, custom
    private List<String> requiredEvents; // For custom strategy
    private Integer timeout; // Overall timeout in seconds
    private String onTimeout; // fail, continue, continue_with_partial
    private Map<String, String> outputMapping; // Output mapping configuration

    // Getters and Setters
    public ApiCallConfigDTO getApiCall() {
        return apiCall;
    }

    public void setApiCall(ApiCallConfigDTO apiCall) {
        this.apiCall = apiCall;
    }

    public KafkaEventConfigDTO getKafkaEvent() {
        return kafkaEvent;
    }

    public void setKafkaEvent(KafkaEventConfigDTO kafkaEvent) {
        this.kafkaEvent = kafkaEvent;
    }

    public String getAggregationStrategy() {
        return aggregationStrategy;
    }

    public void setAggregationStrategy(String aggregationStrategy) {
        this.aggregationStrategy = aggregationStrategy;
    }

    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    public void setRequiredEvents(List<String> requiredEvents) {
        this.requiredEvents = requiredEvents;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getOnTimeout() {
        return onTimeout;
    }

    public void setOnTimeout(String onTimeout) {
        this.onTimeout = onTimeout;
    }

    public Map<String, String> getOutputMapping() {
        return outputMapping;
    }

    public void setOutputMapping(Map<String, String> outputMapping) {
        this.outputMapping = outputMapping;
    }

    // Inner classes for nested configuration
    public static class ApiCallConfigDTO {
        private Boolean enabled = true;
        private String url;
        private String method = "POST";
        private Map<String, String> headers;
        private Object body; // Can be Map or String
        private String correlationIdField = "correlation_id";
        private String correlationIdHeader = "X-Correlation-Id";
        private String executionIdField = "execution_id";
        private String executionIdHeader = "X-Execution-Id";
        private Integer timeout = 300;
        private Boolean required = true;

        // Getters and Setters
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

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

        public String getCorrelationIdField() {
            return correlationIdField;
        }

        public void setCorrelationIdField(String correlationIdField) {
            this.correlationIdField = correlationIdField;
        }

        public String getCorrelationIdHeader() {
            return correlationIdHeader;
        }

        public void setCorrelationIdHeader(String correlationIdHeader) {
            this.correlationIdHeader = correlationIdHeader;
        }

        public String getExecutionIdField() {
            return executionIdField;
        }

        public void setExecutionIdField(String executionIdField) {
            this.executionIdField = executionIdField;
        }

        public String getExecutionIdHeader() {
            return executionIdHeader;
        }

        public void setExecutionIdHeader(String executionIdHeader) {
            this.executionIdHeader = executionIdHeader;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }

    public static class KafkaEventConfigDTO {
        private Boolean enabled = true;
        private String topic;
        private String correlationIdField = "correlation_id";
        private String executionIdField = "execution_id";
        private Map<String, Object> filter;
        private Integer timeout = 300;
        private Boolean required = true;

        // Getters and Setters
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getCorrelationIdField() {
            return correlationIdField;
        }

        public void setCorrelationIdField(String correlationIdField) {
            this.correlationIdField = correlationIdField;
        }

        public String getExecutionIdField() {
            return executionIdField;
        }

        public void setExecutionIdField(String executionIdField) {
            this.executionIdField = executionIdField;
        }

        public Map<String, Object> getFilter() {
            return filter;
        }

        public void setFilter(Map<String, Object> filter) {
            this.filter = filter;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }
}

