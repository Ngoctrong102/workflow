# Triggers Feature

## Overview

Triggers are mechanisms that activate workflow execution. The platform supports **3 trigger types**: API call triggers, scheduled triggers, and event-based triggers (Kafka).

## Trigger Types

### 1. API Call Trigger

#### Description
Workflow is triggered when an HTTP request is received at a specific endpoint.

#### Configuration
- **Endpoint Path**: Unique path for the trigger (e.g., `/api/v1/trigger/workflow-123`)
- **HTTP Method**: GET, POST, PUT, PATCH (typically POST)
- **Authentication**: Optional API key or token authentication
- **Request Validation**: JSON schema validation for request body

#### Request Format
```json
{
  "data": {
    "key1": "value1",
    "key2": "value2"
  },
  "recipients": [
    {
      "email": "user@example.com",
      "phone": "+1234567890"
    }
  ]
}
```

#### Response Format
```json
{
  "workflow_id": "workflow-123",
  "execution_id": "exec-456",
  "status": "triggered",
  "message": "Workflow execution started"
}
```

#### Use Cases
- External systems triggering notifications
- User actions triggering workflows
- Integration with third-party services

### 2. Scheduled Trigger

#### Description
Workflow is triggered automatically based on a schedule (cron expression).

#### Configuration
- **Cron Expression**: Standard cron format (e.g., `0 9 * * *` for daily at 9 AM)
- **Timezone**: Timezone for schedule
- **Start Date**: When schedule becomes active
- **End Date**: When schedule expires (optional)
- **Repeat**: One-time or recurring

#### Cron Examples
- `0 9 * * *` - Daily at 9:00 AM
- `0 */6 * * *` - Every 6 hours
- `0 0 * * 1` - Every Monday at midnight
- `0 0 1 * *` - First day of every month

#### Schedule Data
- **Static Data**: Fixed data passed to workflow
- **Dynamic Data**: Data fetched from API or database (optional for MVP)
- **Context**: Execution context (date, time, etc.)

#### Use Cases
- Daily digest emails
- Weekly reports
- Monthly notifications
- Reminder notifications

### 3. Event Trigger (Kafka)

#### Description
Workflow is triggered when a message is received from a Kafka topic.

#### Configuration
- **Kafka Brokers**: Kafka broker addresses
- **Topic**: Kafka topic name to subscribe to
- **Consumer Group**: Consumer group ID
- **Offset**: Start from beginning or latest
- **Message Format**: JSON message format
- **Filter**: Optional message filtering

#### Message Format
```json
{
  "event_type": "user.created",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "user_id": "123",
    "email": "user@example.com"
  }
}
```

#### Event Filtering
- **Event Type**: Filter by event type
- **Condition**: Filter by message content
- **Pattern Matching**: Regex or JSON path matching

#### Use Cases
- Real-time event processing
- Microservices integration
- Event-driven notifications
- System integration

## Trigger Registry

Triggers must be **defined in the Trigger Registry** before they can be used in workflows. See [Trigger Registry](./trigger-registry.md) for details on registry management.

### Using Triggers in Workflows

When a trigger is used in a workflow:
1. User selects trigger definition from registry
2. System creates a **separate consumer/scheduler instance** for that workflow
3. Instance can be managed independently (pause/resume/init/destroy)

## Trigger Instance Management

### Instance Lifecycle

Each trigger instance in a workflow has its own lifecycle that can be managed independently:

#### Initialize
Creates the consumer/scheduler instance but doesn't start it.

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/init
```

#### Start/Resume
Starts or resumes the consumer/scheduler.

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/start
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/resume
```

#### Pause
Pauses the consumer/scheduler (stops processing but keeps connection).

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/pause
```

#### Stop
Stops the consumer/scheduler completely.

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/stop
```

#### Destroy
Destroys the instance and releases all resources.

```http
DELETE /api/v1/workflows/{workflowId}/triggers/{triggerId}
```

### Instance States

- **INITIALIZED**: Instance created but not started
- **ACTIVE**: Instance is running and processing events/schedules
- **PAUSED**: Instance is paused (stops processing but keeps connection)
- **STOPPED**: Instance is stopped completely
- **ERROR**: Instance has error (e.g., invalid cron, connection failure)

### Independent Instance Management

Each trigger instance in a workflow is **independent**:
- Can be paused/resumed without affecting other workflows
- Has its own consumer/scheduler
- Can be destroyed independently
- Resource cleanup is handled per instance

See [Trigger Registry](./trigger-registry.md) for detailed implementation.

### Trigger Validation
- **Cron Validation**: Validate cron expressions
- **Endpoint Validation**: Ensure unique endpoint paths
- **Kafka Connection Validation**: Test Kafka broker connections

## Workflow Context

### Context Data
When a workflow is triggered, it receives context data:

```json
{
  "trigger_type": "api-call|scheduler|event",
  "trigger_id": "trigger-123",
  "execution_id": "exec-456",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    // Trigger-specific data
  },
  "metadata": {
    // Additional metadata
  }
}
```

### Data Flow
1. Trigger receives input (API request, schedule, Kafka event)
2. Trigger processes input and creates context
3. Context is passed to workflow
4. Workflow nodes access context data
5. Workflow executes with context

## Error Handling

### Trigger Errors
- **Invalid Configuration**: Cron syntax error, invalid endpoint
- **Kafka Connection Failure**: Kafka broker connection failure
- **Rate Limiting**: Too many trigger requests

### Error Recovery
- **Retry Logic**: Automatic retries for transient errors
- **Dead Letter Queue**: Store failed trigger events
- **Notifications**: Alert on trigger failures
- **Logging**: Comprehensive error logging

## Security

### API Call Trigger Security
- **API Keys**: Optional API key authentication
- **Rate Limiting**: Prevent abuse
- **IP Whitelisting**: Optional IP restrictions
- **Request Validation**: Validate request format

## Data Model

See [Database Schema - Triggers](../database-schema/entities.md#triggers)

## API Endpoints

See [API - Triggers](../api/endpoints.md#triggers)

## Related Features

- [Trigger Registry](./trigger-registry.md) - Registry system for trigger definitions
- [Workflow Builder](./workflow-builder.md) - Triggers activate workflows
- [Analytics](./analytics.md) - Trigger execution analytics


