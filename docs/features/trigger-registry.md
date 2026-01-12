# Trigger Registry

## Overview

The Trigger Registry system provides a catalog of **trigger configurations** that users can select from when building workflows. The system supports **3 hardcoded trigger types** (API Call, Scheduler, Event), but allows users to create multiple **trigger configs** for each type.

## Design Principles

1. **Hardcoded Types**: Trigger types are fixed in code (api-call, scheduler, event)
2. **Config-Based**: Users create trigger configs (stored in `triggers` table) that can be reused
3. **Instance Management**: When a trigger config is added to a workflow, a trigger instance is created
4. **Shareable Configs**: Multiple trigger nodes can share the same trigger config

## Concept Hierarchy

```
Trigger Type (Hardcoded)
    ↓
Trigger Config (Database: `triggers` table)
    ↓
Trigger Instance (Workflow Definition: node data)
    ↓
Runtime Execution (When workflow is activated)
```

### 1. Trigger Types (Hardcoded)

Three trigger types are hardcoded in the system:

- **API Call Trigger** (`api-call`): Receives HTTP request to start workflow
- **Scheduler Trigger** (`scheduler`): Cron-based scheduled execution
- **Event Trigger** (`event`): Listens to Kafka topic events

These types cannot be modified or extended by users. They define the base structure and behavior.

### 2. Trigger Configs (Database)

Users create **trigger configs** that define specific configurations for a trigger type. Each config is stored in the `triggers` table and can be reused across multiple workflows.

**Example**: A user can create:
- 10 API Call trigger configs (different endpoints, authentication methods)
- 25 Scheduler trigger configs (different cron expressions, timezones)
- 5 Event trigger configs (different Kafka topics, brokers)

### 3. Trigger Instances (Workflow Definition)

When a trigger config is added to a workflow, a **trigger instance** is created. The instance is stored in the workflow definition node data and includes:
- Reference to trigger config (`triggerConfigId`)
- Trigger type (`triggerType`)
- Instance-specific overrides (`instanceConfig`)

### 4. Runtime Execution

When a workflow is activated, trigger instances are started based on their configuration. Runtime state (ACTIVE, PAUSED, STOPPED) is stored in the workflow definition.

## Trigger Config Structure

A trigger config contains:

```json
{
  "id": "trigger-config-123",
  "name": "Daily Report Scheduler",
  "triggerType": "scheduler",
  "status": "active",
  "config": {
    "cronExpression": "0 9 * * *",
    "timezone": "UTC",
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": null,
    "repeat": true,
    "data": {}
  },
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### Trigger Config Fields

- **id**: Unique identifier
- **name**: User-friendly name
- **triggerType**: One of the 3 hardcoded types (api-call, scheduler, event)
- **status**: active/inactive (metadata only, not runtime state)
- **config**: Type-specific configuration (JSONB)
- **created_at/updated_at**: Timestamps

## Trigger Types Configuration

### 1. API Call Trigger Config

```json
{
  "id": "api-trigger-config-1",
  "name": "User Registration API",
  "triggerType": "api-call",
  "config": {
    "endpointPath": "/api/v1/trigger/user-registration",
    "httpMethod": "POST",
    "authentication": {
      "type": "api-key",
      "header": "X-API-Key",
      "key": "optional-default-key"
    },
    "requestSchema": {
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": true
        }
      ]
    }
  }
}
```

### 2. Scheduler Trigger Config

```json
{
  "id": "scheduler-trigger-config-1",
  "name": "Daily 9 AM Report",
  "triggerType": "scheduler",
  "config": {
    "cronExpression": "0 9 * * *",
    "timezone": "Asia/Ho_Chi_Minh",
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": null,
    "repeat": true,
    "data": {
      "reportType": "daily"
    }
  }
}
```

### 3. Event Trigger Config (Kafka)

```json
{
  "id": "event-trigger-config-1",
  "name": "User Events Listener",
  "triggerType": "event",
  "config": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "user.events",
      "offset": "latest"
    },
    "filter": {
      "eventType": "user.created"
    }
  }
}
```

## Registry API

### Get Available Trigger Configs

```http
GET /api/v1/triggers/registry
```

**Response:**
```json
{
  "triggers": [
    {
      "id": "api-trigger-config-1",
      "name": "User Registration API",
      "triggerType": "api-call",
      "status": "active",
      "config": {
        "endpointPath": "/api/v1/trigger/user-registration",
        "httpMethod": "POST"
      }
    },
    {
      "id": "scheduler-trigger-config-1",
      "name": "Daily 9 AM Report",
      "triggerType": "scheduler",
      "status": "active",
      "config": {
        "cronExpression": "0 9 * * *",
        "timezone": "Asia/Ho_Chi_Minh"
      }
    }
  ]
}
```

**Note**: This endpoint returns trigger configs from the `triggers` table, not hardcoded definitions.

### Get Trigger Config by ID

```http
GET /api/v1/triggers/registry/{id}
```

### Get Trigger Configs by Type

```http
GET /api/v1/triggers/registry/type/{type}
```

**Query Parameters:**
- `type`: Trigger type (api-call, scheduler, event)

## Trigger Instance Structure

When a trigger config is added to a workflow, a trigger instance is created in the workflow definition:

```json
{
  "id": "node-1",
  "nodeType": "trigger",
  "nodeConfig": {
    "triggerConfigId": "trigger-config-123",
    "triggerType": "event",
    "instanceConfig": {
      "consumerGroup": "workflow-456-consumer"
    }
  }
}
```

### Instance Config Fields

**Instance-specific fields** can override or supplement trigger config fields. Currently supported:

- **consumerGroup** (for Event triggers): Each workflow must have a unique consumer group to operate independently

**Note**: The system is designed to easily add more instance-specific fields in the future.

## Schema Definition

Each trigger type has a **schema definition** (defined in Java) that specifies:

- **Shared Fields**: Fields from trigger config (cannot be overridden at instance level)
- **Instance Fields**: Fields that can be configured at workflow level (e.g., consumerGroup)
- **Required/Optional**: Field validation rules
- **Field Types**: Data types and constraints

This schema is used by:
- **Backend**: TriggerExecutor implementation
- **Frontend**: UI form rendering in PropertiesPanel

## Trigger Instance Lifecycle

### Instance Creation

When a trigger config is added to a workflow:
1. Trigger instance is created in workflow definition node data
2. Instance references trigger config via `triggerConfigId`
3. Instance includes instance-specific overrides in `instanceConfig`

### Instance Activation

When workflow is activated:
1. System reads trigger instances from workflow definition
2. For each trigger instance:
   - Load trigger config from database
   - Merge config with instance-specific overrides
   - Create runtime consumer/scheduler
   - Start processing
3. Runtime state (ACTIVE, PAUSED, STOPPED) is stored in workflow definition

### Instance States

Runtime states stored in workflow definition:
- **INITIALIZED**: Instance created but not started
- **ACTIVE**: Instance is running and processing
- **PAUSED**: Instance is paused (stops processing but keeps connection)
- **STOPPED**: Instance is stopped completely
- **ERROR**: Instance has error

### Lifecycle Operations

All lifecycle operations are managed through workflow activation/deactivation:

- **Start**: When workflow is activated
- **Stop**: When workflow is deactivated
- **Pause**: When workflow is paused
- **Resume**: When workflow is resumed

## Sharing Trigger Configs

Multiple trigger nodes (across different workflows) can share the same trigger config:

**Example:**
- Trigger Config "Daily 9 AM Report" is used in:
  - Workflow A (with consumerGroup: "workflow-a-consumer")
  - Workflow B (with consumerGroup: "workflow-b-consumer")
  - Workflow C (with consumerGroup: "workflow-c-consumer")

When trigger config is updated, changes apply to all workflows using it (except instance-specific overrides).

## Benefits

1. **Reusability**: Create trigger configs once, use in multiple workflows
2. **Consistency**: Shared configs ensure consistent behavior
3. **Flexibility**: Instance-specific overrides allow per-workflow customization
4. **Maintainability**: Update trigger config once, affects all workflows
5. **Scalability**: Each workflow has independent consumer/scheduler instance

## Related Documentation

- [Triggers](./triggers.md) - Trigger mechanisms and configuration
- [Workflow Builder](./workflow-builder.md) - How to use triggers in workflows
- [Action Registry](./action-registry.md) - Action registry system
- [Node Types](./node-types.md) - Node type specifications
- [Schema Definition](./schema-definition.md) - Schema definitions
