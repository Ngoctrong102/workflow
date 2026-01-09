# Action Registry

## Overview

Actions must be **defined and registered** before they can be used in workflows. The action registry provides a catalog of available actions that users can select from when building workflows. Each registered action has a unique ID and name for easy selection.

## Design Principles

1. **Pre-Definition**: Actions must be defined before use
2. **Registry-Based**: Central catalog of available actions
3. **Instance Configuration**: Each action usage in a workflow has its own configuration
4. **Template-Based**: Actions use configuration templates with instance-specific overrides

## Action Definition

An action definition specifies the type, configuration template, and metadata for an action type.

```json
{
  "id": "api-call-action-standard",
  "name": "API Call Action",
  "type": "api-call",
  "description": "Make HTTP request to external API",
  "category": "integration",
  "configTemplate": {
    "url": "",
    "method": "GET",
    "headers": {},
    "body": {},
    "timeout": 5000,
    "retry": {
      "maxAttempts": 3,
      "backoffStrategy": "exponential"
    },
    "inputSchema": { ... },
    "outputSchema": { ... }
  },
  "metadata": {
    "icon": "api-call",
    "color": "#22c55e",
    "version": "1.0.0"
  }
}
```

## Action Types in Registry

### 1. API Call Action

```json
{
  "id": "api-call-action-standard",
  "name": "API Call Action",
  "type": "api-call",
  "description": "Make HTTP request to external API",
  "configTemplate": {
    "url": "",
    "method": "GET|POST|PUT|PATCH|DELETE",
    "headers": {},
    "body": {},
    "timeout": 5000,
    "retry": { ... }
  }
}
```

### 2. Publish Event Action (Kafka)

```json
{
  "id": "publish-event-action-kafka",
  "name": "Publish Kafka Event",
  "type": "publish-event",
  "description": "Publish message to Kafka topic",
  "configTemplate": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "",
      "key": null,
      "headers": {}
    },
    "message": {},
    "outputSchema": { ... }
  }
}
```

### 3. Function Action

```json
{
  "id": "function-action-standard",
  "name": "Function Action",
  "type": "function",
  "description": "Define simple calculation logic",
  "configTemplate": {
    "expression": "",
    "inputSchema": { ... },
    "outputSchema": { ... }
  }
}
```

### 4. Custom Actions

Custom actions are pre-registered action types with specific implementations:

```json
{
  "id": "send-email-action",
  "name": "Send Email",
  "type": "custom-action",
  "actionType": "send-email",
  "description": "Send email notification",
  "configTemplate": {
    "recipient": "",
    "subject": "",
    "body": "",
    "attachments": [],
    "inputSchema": { ... },
    "outputSchema": { ... }
  }
}
```

**Pre-Registered Custom Actions**:
- `send-email`: Send Email
- `send-sms`: Send SMS
- `send-push`: Send Push Notification
- `send-in-app`: Send In-App Notification
- `send-slack`: Send Slack Message
- `send-discord`: Send Discord Message
- `send-teams`: Send Teams Message
- `send-webhook`: Send Webhook
- `wait-events`: Wait for Events

## Registry Management

### Database Schema

```sql
CREATE TABLE action_definitions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    action_type VARCHAR(50),  -- For custom actions
    description TEXT,
    config_template JSONB NOT NULL,
    metadata JSONB,
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

### Registry API

#### Get Available Actions

```http
GET /api/v1/actions/definitions
```

Response:
```json
{
  "actions": [
    {
      "id": "api-call-action-standard",
      "name": "API Call Action",
      "type": "api-call",
      "description": "Make HTTP request to external API",
      "metadata": {
        "icon": "api-call",
        "color": "#22c55e"
      }
    },
    {
      "id": "publish-event-action-kafka",
      "name": "Publish Kafka Event",
      "type": "publish-event",
      "description": "Publish message to Kafka topic",
      "metadata": {
        "icon": "publish-event",
        "color": "#22c55e"
      }
    },
    {
      "id": "send-email-action",
      "name": "Send Email",
      "type": "custom-action",
      "actionType": "send-email",
      "description": "Send email notification",
      "metadata": {
        "icon": "send-email",
        "color": "#22c55e"
      }
    }
  ]
}
```

## Workflow Node Configuration

### Using Registry in Workflow

When creating an action node in a workflow, users select from the registry:

```json
{
  "id": "node-uuid",
  "label": "sendEmail",
  "type": "action",
  "subType": "custom-action",
  "registryId": "send-email-action",
  "config": {
    "recipient": "${user.email}",
    "subject": "Welcome!",
    "body": "Welcome to our platform!"
  }
}
```

**Key Points**:
- `registryId`: References the action definition from registry
- `config`: Instance-specific configuration (overrides template defaults)
- Each action instance has its own configuration

## Benefits

1. **Centralized Management**: All actions defined in one place
2. **Easy Selection**: Users select from predefined catalog
3. **Template-Based**: Configuration templates with instance-specific overrides
4. **Consistency**: Standardized action configurations across workflows
5. **Extensibility**: Easy to add new action types to registry

## Related Documentation

- [Trigger Registry](./trigger-registry.md) - Trigger registry system
- [Workflow Builder](./workflow-builder.md) - Workflow builder feature
- [Node Types](./node-types.md) - Node type specifications
- [Schema Definition](./schema-definition.md) - Schema definitions

