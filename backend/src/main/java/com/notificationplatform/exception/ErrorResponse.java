package com.notificationplatform.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private ErrorInfo error;
    private String requestId;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, Map<String, Object> details, LocalDateTime timestamp) {
        this.error = new ErrorInfo(code, message, details, timestamp);
    }

    public ErrorResponse(String code, String message, Map<String, Object> details) {
        this.error = new ErrorInfo(code, message, details, LocalDateTime.now());
    }

    public ErrorResponse(ErrorInfo error, String requestId) {
        this.error = error;
        this.requestId = requestId;
    }

    public static class ErrorInfo {
        private String code;
        private String message;
        private Map<String, Object> details;
        private String requestId;
        private LocalDateTime timestamp;

        public ErrorInfo() {
        }

        public ErrorInfo(String code, String message, Map<String, Object> details, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }

        public ErrorInfo(String code, String message, Map<String, Object> details, String requestId, LocalDateTime timestamp) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.requestId = requestId;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }
    }

    // Getters and Setters
    public ErrorInfo getError() {
        return error;
    }

    public void setError(ErrorInfo error) {
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

