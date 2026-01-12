# API Contract

## API Contract Definition

This document defines the contract between frontend and backend for API integration.

## Base Contract

### Base URL
- **Development**: `http://localhost:8080/api/v1`
- **Production**: Configurable via environment

### Request Format
- **Content-Type**: `application/json`
- **Accept**: `application/json`
- **Method**: HTTP methods (GET, POST, PUT, DELETE)

### Response Format
- **Content-Type**: `application/json`
- **Status Codes**: Standard HTTP status codes
- **Error Format**: Standardized error responses

## Authentication (Future)

### API Key Authentication
- **Header**: `X-API-Key: <api-key>`
- **Query Parameter**: `?api_key=<api-key>` (alternative)

## Request/Response Patterns

### List Resources
```
GET /resources?limit=10&offset=0&status=active

Response:
{
  "data": [...],
  "pagination": {
    "total": 100,
    "limit": 10,
    "offset": 0,
    "has_more": true
  }
}
```

### Get Resource
```
GET /resources/{id}

Response:
{
  "id": "resource-123",
  "name": "Resource Name",
  ...
}
```

### Create Resource
```
POST /resources
Content-Type: application/json

{
  "name": "Resource Name",
  ...
}

Response:
{
  "id": "resource-123",
  "name": "Resource Name",
  ...
}
```

### Update Resource
```
PUT /resources/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  ...
}

Response:
{
  "id": "resource-123",
  "name": "Updated Name",
  ...
}
```

### Delete Resource
```
DELETE /resources/{id}

Response:
{
  "message": "Resource deleted successfully"
}
```

## Error Response Format

### Standard Error
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

### Validation Error
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
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
  }
}
```

## API Endpoints

### Workflows
- `GET /workflows` - List workflows
- `GET /workflows/{id}` - Get workflow
- `POST /workflows` - Create workflow
- `PUT /workflows/{id}` - Update workflow
- `DELETE /workflows/{id}` - Delete workflow
- `POST /workflows/{id}/execute` - Execute workflow

### Templates
- `GET /templates` - List templates
- `GET /templates/{id}` - Get template
- `POST /templates` - Create template
- `PUT /templates/{id}` - Update template
- `DELETE /templates/{id}` - Delete template

### Trigger Configs
- `POST /triggers` - Create trigger config
- `GET /triggers` - List trigger configs
- `GET /triggers/{id}` - Get trigger config
- `PUT /triggers/{id}` - Update trigger config
- `DELETE /triggers/{id}` - Delete trigger config
- `GET /triggers/registry` - Get trigger configs from registry
- `GET /workflows/{id}/triggers` - Get trigger instances for workflow
- `POST /trigger/{path}` - Trigger workflow (API trigger endpoint)

**Note**: Trigger configs are created independently and can be reused across multiple workflows. Trigger instances are created when trigger configs are linked to workflow nodes.

### Notifications
- `POST /notifications/send` - Send notification
- `GET /notifications/{id}` - Get notification
- `GET /notifications/{id}/status` - Get notification status

### Analytics
- `GET /analytics/workflows/{id}` - Get workflow analytics
- `GET /analytics/deliveries` - Get delivery analytics
- `GET /analytics/channels` - Get channel analytics

### Channels
- `GET /channels` - List channels
- `GET /channels/{id}` - Get channel
- `POST /channels` - Create channel
- `PUT /channels/{id}` - Update channel
- `DELETE /channels/{id}` - Delete channel
- `POST /channels/{id}/test` - Test channel connection

## Data Types

### Timestamps
- **Format**: ISO 8601
- **Example**: `2024-01-01T00:00:00Z`

### IDs
- **Format**: UUID or string
- **Example**: `workflow-123` or `550e8400-e29b-41d4-a716-446655440000`

### Pagination
- **limit**: Integer (default: 20, max: 100)
- **offset**: Integer (default: 0)

## Related Documentation

- [API Endpoints](../../api/endpoints.md) - Detailed endpoint specifications
- [API Schemas](../../api/schemas.md) - Request/response schemas
- [Error Handling](../../api/error-handling.md) - Error handling details

