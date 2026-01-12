# Error Handling

## Error Response Format

All error responses follow a standard format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "Additional error details"
    },
    "request_id": "request-123"
  }
}
```

## HTTP Status Codes

### Success Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Request successful, no content to return

### Client Error Codes
- `400 Bad Request`: Invalid request format or parameters
- `401 Unauthorized`: Authentication required (deferred for MVP)
- `403 Forbidden`: Insufficient permissions (deferred for MVP)
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate name)
- `422 Unprocessable Entity`: Validation errors
- `429 Too Many Requests`: Rate limit exceeded

### Server Error Codes
- `500 Internal Server Error`: Unexpected server error
- `502 Bad Gateway`: Gateway error
- `503 Service Unavailable`: Service temporarily unavailable
- `504 Gateway Timeout`: Gateway timeout

## Error Codes

### Workflow Errors

#### `WORKFLOW_NOT_FOUND`
- **Status**: 404
- **Message**: "Workflow not found"
- **Details**: `{"workflow_id": "workflow-123"}`

#### `WORKFLOW_INVALID_DEFINITION`
- **Status**: 422
- **Message**: "Invalid workflow definition"
- **Details**: `{"errors": ["Node 'node-1' has invalid configuration"]}`

#### `WORKFLOW_ALREADY_EXISTS`
- **Status**: 409
- **Message**: "Workflow with this name already exists"
- **Details**: `{"name": "Welcome Email Workflow"}`

#### `WORKFLOW_EXECUTION_FAILED`
- **Status**: 500
- **Message**: "Workflow execution failed"
- **Details**: `{"execution_id": "exec-456", "error": "Node execution failed"}`

### Template Errors

#### `TEMPLATE_NOT_FOUND`
- **Status**: 404
- **Message**: "Template not found"

#### `TEMPLATE_INVALID_VARIABLES`
- **Status**: 422
- **Message**: "Template contains invalid variables"
- **Details**: `{"invalid_variables": ["@{invalid.var}"]}`

#### `TEMPLATE_RENDER_FAILED`
- **Status**: 500
- **Message**: "Failed to render template"
- **Details**: `{"error": "Missing required variable: user.name"}`

### Trigger Errors

#### `TRIGGER_NOT_FOUND`
- **Status**: 404
- **Message**: "Trigger not found"

#### `TRIGGER_INVALID_CRON`
- **Status**: 422
- **Message**: "Invalid cron expression"
- **Details**: `{"cron_expression": "invalid"}`

#### `TRIGGER_PATH_CONFLICT`
- **Status**: 409
- **Message**: "Trigger path already exists"
- **Details**: `{"path": "/trigger/welcome-email"}`

#### `TRIGGER_CONNECTION_FAILED`
- **Status**: 500
- **Message**: "Failed to connect to message queue"
- **Details**: `{"queue_type": "kafka", "error": "Connection timeout"}`

### Action Execution Errors

#### `ACTION_EXECUTION_FAILED`
- **Status**: 500
- **Message**: "Failed to execute action"
- **Details**: `{"action_type": "api-call", "error": "Connection timeout"}`

#### `ACTION_CONFIGURATION_ERROR`
- **Status**: 422
- **Message**: "Action configuration error"
- **Details**: `{"action_id": "action-123", "error": "Invalid URL format"}`

### Validation Errors

#### `VALIDATION_ERROR`
- **Status**: 422
- **Message**: "Validation failed"
- **Details**: 
```json
{
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    },
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

### Rate Limiting Errors

#### `RATE_LIMIT_EXCEEDED`
- **Status**: 429
- **Message**: "Rate limit exceeded"
- **Details**: 
```json
{
  "limit": 100,
  "remaining": 0,
  "reset_at": "2024-01-01T01:00:00Z"
}
```

### General Errors

#### `INTERNAL_SERVER_ERROR`
- **Status**: 500
- **Message**: "An unexpected error occurred"
- **Details**: `{"request_id": "request-123"}`

#### `SERVICE_UNAVAILABLE`
- **Status**: 503
- **Message**: "Service temporarily unavailable"
- **Details**: `{"retry_after": 60}`

## Error Handling Best Practices

### Client-Side
1. **Check Status Code**: Always check HTTP status code
2. **Parse Error Response**: Extract error code and message
3. **Display User-Friendly Messages**: Show human-readable messages
4. **Handle Retries**: Implement retry logic for transient errors
5. **Log Errors**: Log errors for debugging

### Server-Side
1. **Consistent Format**: Always return errors in standard format
2. **Appropriate Status Codes**: Use correct HTTP status codes
3. **Detailed Error Messages**: Provide helpful error messages
4. **Request ID**: Include request ID for tracking
5. **Error Logging**: Log errors with context
6. **Sanitize Errors**: Don't expose sensitive information

## Retry Logic

### Retryable Errors
- `500 Internal Server Error`
- `502 Bad Gateway`
- `503 Service Unavailable`
- `504 Gateway Timeout`
- Network errors

### Non-Retryable Errors
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `422 Unprocessable Entity`

### Retry Strategy
- **Exponential Backoff**: Increase delay between retries
- **Max Retries**: Limit number of retry attempts
- **Retry-After Header**: Respect `Retry-After` header if present

## Error Logging

### Log Format
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "level": "error",
  "request_id": "request-123",
  "error_code": "WORKFLOW_NOT_FOUND",
  "message": "Workflow not found",
  "details": {
    "workflow_id": "workflow-123"
  },
  "stack_trace": "..."
}
```

### Log Levels
- **Error**: Application errors
- **Warn**: Warning conditions
- **Info**: Informational messages
- **Debug**: Debug messages (development only)


