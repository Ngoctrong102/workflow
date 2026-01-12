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

### Action Schema Structure

**Mọi action cần 4 components:**

1. **Input Schema** (Optional): Định nghĩa các fields cho data mapping từ previous nodes
2. **Config Template Schema** (Required): Định nghĩa cấu trúc của config fields (url, method, headers, etc.)
3. **Output Schema** (Required): Định nghĩa cấu trúc của output fields
4. **Output Mapping** (Required): MVEL expressions để map từ raw action response vào output schema

```json
{
  "id": "api-call-action-standard",
  "name": "API Call Action",
  "type": "api-call",
  "description": "Make HTTP request to external API",
  "category": "integration",
  "configTemplate": {
    "inputSchema": {
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": false,
          "description": "User ID from previous node"
        }
      ]
    },
    "configTemplate": {
      "fields": [
        {
          "name": "url",
          "type": "url",
          "required": true,
          "description": "API endpoint URL"
        },
        {
          "name": "method",
          "type": "string",
          "required": true,
          "enum": ["GET", "POST", "PUT", "PATCH", "DELETE"],
          "defaultValue": "GET"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "statusCode",
          "type": "number",
          "description": "HTTP status code"
        },
        {
          "name": "body",
          "type": "json",
          "description": "Response body"
        }
      ]
    },
    "outputMapping": {
      "statusCode": "@{_response.statusCode}",
      "body": "@{_response.body}"
    }
  },
  "metadata": {
    "icon": "api-call",
    "color": "#22c55e",
    "version": "1.0.0"
  }
}
```

**Config Template Schema**:
- Định nghĩa cấu trúc của config fields (url, method, headers, etc.)
- Mỗi config field có: name, type, required, description, defaultValue, validation
- User sẽ nhập values cho các fields này trong PropertiesPanel (có thể là static values hoặc MVEL expressions)

**Output Mapping**:
- MVEL expressions để map từ raw response vào output schema
- Mỗi field trong output schema có một MVEL expression trong outputMapping
- Context cho output mapping bao gồm `_response` (raw action result)

## Action Types in Registry

### 1. API Call Action

```json
{
  "id": "api-call-action-standard",
  "name": "API Call Action",
  "type": "api-call",
  "description": "Make HTTP request to external API",
  "configTemplate": {
    "inputSchema": {
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": false,
          "description": "User ID from previous node"
        }
      ]
    },
    "configTemplate": {
      "fields": [
        {
          "name": "url",
          "type": "url",
          "required": true,
          "description": "API endpoint URL. Can use MVEL: /users/@{userID}"
        },
        {
          "name": "method",
          "type": "string",
          "required": true,
          "enum": ["GET", "POST", "PUT", "PATCH", "DELETE"],
          "defaultValue": "GET"
        },
        {
          "name": "headers",
          "type": "object",
          "required": false,
          "description": "HTTP headers. Can use MVEL"
        },
        {
          "name": "body",
          "type": "json",
          "required": false,
          "description": "Request body. Can use MVEL expressions"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "statusCode",
          "type": "number",
          "description": "HTTP status code"
        },
        {
          "name": "status",
          "type": "string",
          "enum": ["success", "error"],
          "description": "Request status"
        },
        {
          "name": "body",
          "type": "json",
          "description": "Response body"
        }
      ]
    },
    "outputMapping": {
      "statusCode": "@{_response.statusCode}",
      "status": "@{_response.statusCode} >= 200 && @{_response.statusCode} < 300 ? 'success' : 'error'",
      "body": "@{_response.body}"
    }
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
    "inputSchema": {
      "fields": [
        {
          "name": "eventType",
          "type": "string",
          "required": false,
          "description": "Event type from previous node"
        }
      ]
    },
    "configTemplate": {
      "fields": [
        {
          "name": "kafka",
          "type": "object",
          "required": true,
          "fields": [
            {
              "name": "brokers",
              "type": "array",
              "required": true,
              "description": "Kafka broker addresses"
            },
            {
              "name": "topic",
              "type": "string",
              "required": true,
              "description": "Kafka topic name. Can use MVEL: events-@{eventType}"
            }
          ]
        },
        {
          "name": "message",
          "type": "json",
          "required": false,
          "description": "Message payload. Can use MVEL expressions"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "status",
          "type": "string",
          "enum": ["success", "failed"],
          "description": "Publish status"
        },
        {
          "name": "topic",
          "type": "string",
          "description": "Topic name"
        },
        {
          "name": "partition",
          "type": "number",
          "description": "Partition number"
        }
      ]
    },
    "outputMapping": {
      "status": "@{_response.success == true ? 'success' : 'failed'}",
      "topic": "@{_response.topic}",
      "partition": "@{_response.partition}"
    }
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
    "inputSchema": {
      "fields": [
        {
          "name": "value1",
          "type": "any",
          "required": false,
          "description": "First value from previous node"
        }
      ]
    },
    "configTemplate": {
      "fields": [
        {
          "name": "expression",
          "type": "string",
          "required": true,
          "description": "MVEL expression to evaluate"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "result",
          "type": "any",
          "description": "Expression evaluation result"
        }
      ]
    },
    "outputMapping": {
      "result": "@{_response.result}"
    }
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
CREATE TABLE actions (
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

### Action-First Flow

**Similar to Trigger-First Flow**:
1. User creates action definition in Action Management page (if not exists in registry)
2. User adds action node to workflow
3. User selects action from registry
4. User configures action-specific settings
5. Action configuration is stored in workflow definition node data

### Using Registry in Workflow

When creating an action node in a workflow, users select from the registry:

```json
{
  "id": "node-uuid",
  "nodeType": "action",
  "nodeConfig": {
    "registryId": "send-email-action",
    "actionType": "custom-action",
    "config": {
      "recipient": "@{user.email}",
      "subject": "Welcome!",
      "body": "Welcome to our platform!"
    }
  }
}
```

**Key Points**:
- `registryId`: References the action definition from registry (`actions` table)
- `config`: Action-specific configuration (stored in workflow definition node data)
- Each action node has its own configuration
- **Note**: Unlike triggers, actions do not have a separate config table. Action definitions are in the registry, and action configuration is stored directly in workflow definition.

### Action Definition vs Action Config

**Action Definition**:
- Created in Action Management page
- Stored in `actions` table (registry)
- Contains template and schema definitions
- Can be reused across multiple workflows

**Action Config**:
- Stored in workflow definition node data
- Contains instance-specific configuration
- Not shared between workflows (each workflow has its own config)

## Benefits

1. **Centralized Management**: All actions defined in one place
2. **Easy Selection**: Users select from predefined catalog
3. **Template-Based**: Configuration templates with instance-specific overrides
4. **Consistency**: Standardized action configurations across workflows
5. **Extensibility**: Easy to add new action types to registry

## Related Documentation

- [MVEL Expression System](./mvel-expression-system.md) - MVEL syntax và evaluation
- [Action Node Configuration](../technical/frontend/action-node-configuration.md) - Frontend configuration
- [Action Execution](../technical/backend/action-execution.md) - Backend execution
- [Trigger Registry](./trigger-registry.md) - Trigger registry system
- [Workflow Builder](./workflow-builder.md) - Workflow builder feature
- [Node Types](./node-types.md) - Node type specifications
- [Schema Definition](./schema-definition.md) - Schema definitions

