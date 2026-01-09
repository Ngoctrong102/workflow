# Webhook Test Service

Mock webhook test service for workflow integration testing and demos.

## Purpose

This service replaces the Java `WebhookTestController` and provides a standalone Node.js service for:
- Testing webhook integrations with workflows
- Demonstrating workflow execution with webhook triggers
- Mocking external webhook endpoints for development and testing

## Installation

```bash
cd mock-server/webhook-test
npm install
```

## Usage

### Start the service

```bash
# Production mode
npm start

# Development mode (with auto-reload)
npm run dev
```

The service will start on port `3002` by default (configurable via `PORT` environment variable).

### Endpoints

#### POST /webhook-test
Receive and log webhook requests.

**Request:**
```bash
curl -X POST http://localhost:3002/webhook-test \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

**Response:**
```json
{
  "status": "received",
  "timestamp": "2026-01-06T15:30:00.000Z",
  "message": "Webhook received and logged successfully"
}
```

#### GET /webhook-test
Health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "service": "webhook-test",
  "timestamp": "2026-01-06T15:30:00.000Z"
}
```

#### GET /webhook-test/health
Additional health check with uptime information.

**Response:**
```json
{
  "status": "healthy",
  "service": "webhook-test",
  "uptime": 123.45,
  "timestamp": "2026-01-06T15:30:00.000Z"
}
```

#### POST /webhook-test/:scenario
Test different webhook scenarios for workflow integration.

**Available scenarios:**
- `success` - Returns success response
- `error` - Returns error response
- `delay` - Simulates delayed response (2 seconds)
- `custom` - Echo back request body

**Example:**
```bash
curl -X POST http://localhost:3002/webhook-test/success \
  -H "Content-Type: application/json" \
  -d '{"event": "user.created", "data": {"userId": "123"}}'
```

#### GET /webhook-test/scenarios
List all available test scenarios.

**Response:**
```json
{
  "scenarios": [
    {
      "name": "success",
      "description": "Returns success response",
      "endpoint": "POST /webhook-test/success"
    },
    ...
  ]
}
```

## Integration with Workflow

### Using as Webhook Action Target

In your workflow definition, configure a webhook action node to call this service:

```json
{
  "type": "action",
  "action": "webhook",
  "config": {
    "url": "http://localhost:3002/webhook-test",
    "method": "POST",
    "headers": {
      "Content-Type": "application/json"
    },
    "body": {
      "workflow_id": "{{workflow.id}}",
      "execution_id": "{{execution.id}}",
      "data": "{{trigger.data}}"
    }
  }
}
```

### Using as Webhook Trigger Source

Configure an API trigger in your workflow to receive webhooks from external systems:

1. Create a workflow with an API trigger
2. Configure the trigger endpoint (e.g., `/api/v1/trigger/workflow-123`)
3. External systems can call this endpoint, which will trigger the workflow
4. The workflow can then call this mock service to simulate webhook delivery

## Environment Variables

- `PORT` - Server port (default: `3002`)

## Logging

All webhook requests are logged to the console with:
- Timestamp
- HTTP method and path
- Headers
- Request body
- Query parameters

## Development

### Adding New Scenarios

Edit `server.js` and add new cases to the scenario switch statement:

```javascript
case 'new-scenario':
  response.data = { custom: 'data' };
  break;
```

### Extending Functionality

This service can be extended to:
- Store webhook history in a database
- Provide webhook replay functionality
- Add authentication/authorization
- Support webhook signatures verification
- Add rate limiting
- Provide webhook testing UI

## Migration from WebhookTestController

The Java `WebhookTestController` has been replaced by this service. To migrate:

1. Update workflow webhook action URLs from `http://backend:8080/webhook-test` to `http://localhost:3002/webhook-test`
2. Ensure this service is running when testing workflows
3. Update any scripts or documentation referencing the old endpoint

