# Schema Definition for Workflow Nodes

## Overview

Schema definition provides data type information for workflow nodes. Schemas are **defined when defining triggers and actions in the registry**. When building workflows, users use these pre-defined schemas to configure nodes through a visual UI with field selectors, rather than writing JSON manually.

## Design Principles

1. **Registry-Based**: Schemas are defined in Trigger Registry and Action Registry when creating trigger/action definitions
2. **Reusable**: Schemas defined in registry can be reused across multiple workflows
3. **UI-Driven Configuration**: When configuring workflow nodes, UI renders fields from schema and allows users to select values instead of writing JSON
4. **Type Safety**: Schema definitions enable type validation, autocomplete, and field selection in UI
5. **Source Selection**: Users select data sources (from previous nodes, trigger data, variables) through dropdowns/selectors

## Schema Definition Flow

### Step 1: Define Schema in Registry

Schemas are defined when creating trigger or action definitions in the registry:

**Trigger Registry Example:**
```json
{
  "id": "kafka-event-trigger-standard",
  "name": "Kafka Event Trigger",
  "type": "event",
  "configTemplate": {
    "kafka": { ... },
    "schemas": [
      {
        "schemaId": "user-created",
        "eventType": "user.created",
        "fields": [
          {
            "name": "userId",
            "type": "string",
            "required": true,
            "description": "User ID"
          },
          {
            "name": "email",
            "type": "email",
            "required": true,
            "description": "User email"
          }
        ]
      }
    ]
  }
}
```

**Action Registry Example:**
```json
{
  "id": "send-email-action",
  "name": "Send Email",
  "type": "custom-action",
  "actionType": "send-email",
  "configTemplate": {
    "inputSchema": {
      "fields": [
        {
          "name": "recipient",
          "type": "email",
          "required": true,
          "description": "Email recipient"
        },
        {
          "name": "subject",
          "type": "string",
          "required": true,
          "description": "Email subject"
        },
        {
          "name": "body",
          "type": "string",
          "required": true,
          "description": "Email body"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "messageId",
          "type": "string",
          "description": "Email message ID"
        },
        {
          "name": "status",
          "type": "string",
          "enum": ["sent", "failed"],
          "description": "Delivery status"
        }
      ]
    }
  }
}
```

### Step 2: Use Schema in Workflow Builder

When configuring a workflow node:

1. **User selects trigger/action from registry**
   - System loads the schema from registry definition
   - UI renders form fields based on schema

2. **UI renders fields from schema**
   - For each field in `inputSchema`, UI shows:
     - Field label (from `name` or `displayName`)
     - Field type indicator
     - Input component based on type (text input, dropdown, date picker, etc.)
     - Required indicator (if `required: true`)
     - Description/help text

3. **User selects data source (not writes JSON)**
   - For each field, user sees a dropdown/selector to choose data source:
     - **From Previous Node**: Select node → Select output field
     - **From Trigger Data**: Select trigger field
     - **From Variables**: Select workflow variable
     - **Static Value**: Enter value directly
   - UI validates field type matches selected source

4. **Configuration saved**
   - System saves field mappings (not raw JSON)
   - Format: `{ "fieldName": { "source": "_nodeOutputs.nodeId.fieldName", "type": "string" } }`

## Schema Types

### 1. Trigger Event Schema (Kafka)

Trigger event schemas define the structure of events received from Kafka topics. Since a single Kafka topic can contain multiple event types, the schema system supports:

- **Multiple schemas per topic**: Different event types in the same topic
- **Event filtering**: Filter events based on schema matching
- **Field mapping**: Map Kafka event fields to workflow context
- **Kafka Connect integration**: Use Kafka Connect for schema registry

#### Schema Structure

```json
{
  "nodeId": "event-trigger-123",
  "nodeType": "trigger",
  "subType": "event",
  "config": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "user-events",
      "consumerGroup": "workflow-consumer-group",
      "offset": "latest"
    },
    "schemas": [
      {
        "schemaId": "user-created",
        "eventType": "user.created",
        "description": "Schema for user creation events",
        "filter": {
          "field": "eventType",
          "operator": "equals",
          "value": "user.created"
        },
        "fields": [
          {
            "name": "eventType",
            "type": "string",
            "required": true,
            "description": "Type of event"
          },
          {
            "name": "userId",
            "type": "string",
            "required": true,
            "description": "User ID"
          },
          {
            "name": "email",
            "type": "email",
            "required": true,
            "description": "User email"
          },
          {
            "name": "timestamp",
            "type": "datetime",
            "required": true,
            "description": "Event timestamp"
          }
        ],
        "mapping": {
          "workflowContext": {
            "userId": "userId",
            "userEmail": "email",
            "eventTime": "timestamp"
          }
        }
      },
      {
        "schemaId": "user-updated",
        "eventType": "user.updated",
        "description": "Schema for user update events",
        "filter": {
          "field": "eventType",
          "operator": "equals",
          "value": "user.updated"
        },
        "fields": [
          {
            "name": "eventType",
            "type": "string",
            "required": true
          },
          {
            "name": "userId",
            "type": "string",
            "required": true
          },
          {
            "name": "changes",
            "type": "object",
            "required": false,
            "fields": [
              {
                "name": "email",
                "type": "email",
                "required": false
              },
              {
                "name": "status",
                "type": "string",
                "required": false
              }
            ]
          }
        ],
        "mapping": {
          "workflowContext": {
            "userId": "userId",
            "emailChanges": "changes.email",
            "statusChanges": "changes.status"
          }
        }
      }
    ],
    "kafkaConnect": {
      "enabled": true,
      "schemaRegistryUrl": "http://localhost:8081",
      "subject": "user-events-value",
      "version": "latest"
    }
  }
}
```

#### Event Filtering

When multiple schemas are defined for a topic, the system uses filters to determine which schema matches an incoming event:

```json
{
  "filter": {
    "field": "eventType",
    "operator": "equals|contains|matches|in",
    "value": "user.created",
    "conditions": [
      {
        "field": "source",
        "operator": "equals",
        "value": "api"
      }
    ],
    "logic": "AND|OR"
  }
}
```

**Filter Operators**:
- `equals`: Exact match
- `contains`: String contains
- `matches`: Regex match
- `in`: Value in array
- `exists`: Field exists
- `gt`, `gte`, `lt`, `lte`: Numeric comparisons

#### Field Mapping

Maps Kafka event fields to workflow context variables:

```json
{
  "mapping": {
    "workflowContext": {
      "userId": "userId",           // Direct mapping
      "userEmail": "email",         // Direct mapping
      "eventTime": "timestamp",     // Direct mapping
      "fullName": "profile.name"    // Nested field mapping
    },
    "transformations": {
      "userEmail": {
        "type": "lowercase",
        "source": "email"
      },
      "timestamp": {
        "type": "format",
        "source": "timestamp",
        "format": "yyyy-MM-dd HH:mm:ss"
      }
    }
  }
}
```

#### Kafka Connect Integration

For schema registry support:

```json
{
  "kafkaConnect": {
    "enabled": true,
    "schemaRegistryUrl": "http://localhost:8081",
    "subject": "user-events-value",
    "version": "latest",
    "format": "avro|json|protobuf",
    "fallbackToLocalSchema": true
  }
}
```

**Kafka Connect Flow**:
1. System connects to Kafka Connect/Schema Registry
2. Retrieves schema for the topic/subject
3. Validates incoming events against registered schema
4. Falls back to local schema definition if registry unavailable
5. Uses schema for deserialization and validation

### 2. Action Input/Output Schema

Action nodes define schemas for their input data (from previous nodes) and output data (to next nodes).

#### Input Schema

Defines the expected structure of data coming from previous nodes:

```json
{
  "nodeId": "api-call-action-456",
  "nodeType": "action",
  "subType": "api-call",
  "config": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "inputSchema": {
      "description": "Expected input data for API call",
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": true,
          "description": "User ID to fetch",
          "source": "_nodeOutputs.previousNode.userId"
        },
        {
          "name": "includeProfile",
          "type": "boolean",
          "required": false,
          "defaultValue": false,
          "description": "Include user profile in response"
        }
      ],
      "validation": {
        "userId": {
          "type": "string",
          "minLength": 1,
          "pattern": "^[a-zA-Z0-9-]+$"
        }
      }
    }
  }
}
```

#### Output Schema

Defines the structure of data produced by the action:

```json
{
  "nodeId": "api-call-action-456",
  "nodeType": "action",
  "subType": "api-call",
  "config": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "outputSchema": {
      "description": "API response structure",
      "fields": [
        {
          "name": "id",
          "type": "string",
          "description": "User ID"
        },
        {
          "name": "email",
          "type": "email",
          "description": "User email"
        },
        {
          "name": "profile",
          "type": "object",
          "description": "User profile",
          "fields": [
            {
              "name": "firstName",
              "type": "string"
            },
            {
              "name": "lastName",
              "type": "string"
            }
          ]
        },
        {
          "name": "status",
          "type": "string",
          "description": "API call status",
          "enum": ["success", "error"]
        }
      ]
    }
  }
}
```

#### Function Action Schema

For Function actions, the schema defines input/output for the calculation:

```json
{
  "nodeId": "function-action-789",
  "nodeType": "action",
  "subType": "function",
  "config": {
    "expression": "concat(user.firstName, ' ', user.lastName)",
    "inputSchema": {
      "fields": [
        {
          "name": "user",
          "type": "object",
          "required": true,
          "fields": [
            {
              "name": "firstName",
              "type": "string",
              "required": true
            },
            {
              "name": "lastName",
              "type": "string",
              "required": true
            }
          ]
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "result",
          "type": "string",
          "description": "Concatenated full name"
        }
      ]
    }
  }
}
```

#### Custom Action Schema

For Custom actions (e.g., Send Email), schemas define input requirements:

```json
{
  "nodeId": "send-email-action-101",
  "nodeType": "action",
  "subType": "custom-action",
  "config": {
    "actionType": "send-email",
    "inputSchema": {
      "fields": [
        {
          "name": "recipient",
          "type": "email",
          "required": true,
          "description": "Email recipient",
          "source": "_nodeOutputs.userData.email"
        },
        {
          "name": "subject",
          "type": "string",
          "required": true,
          "description": "Email subject"
        },
        {
          "name": "body",
          "type": "string",
          "required": true,
          "description": "Email body"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        {
          "name": "messageId",
          "type": "string",
          "description": "Email message ID"
        },
        {
          "name": "status",
          "type": "string",
          "enum": ["sent", "failed"],
          "description": "Delivery status"
        },
        {
          "name": "error",
          "type": "string",
          "required": false,
          "description": "Error message if failed"
        }
      ]
    }
  }
}
```

## Schema Field Definition

### Field Structure

```json
{
  "name": "fieldName",
  "type": "string|number|boolean|date|datetime|email|phone|url|json|array|object",
  "required": true|false,
  "defaultValue": null|value,
  "description": "Field description",
  "validation": {
    "minLength": 0,
    "maxLength": 255,
    "pattern": "^regex$",
    "min": 0,
    "max": 100,
    "enum": ["value1", "value2"]
  },
  "source": "_nodeOutputs.nodeId.fieldName",
  "transform": {
    "type": "lowercase|uppercase|format|parse",
    "params": {}
  }
}
```

### Field Types

- **string**: Text value
- **number**: Numeric value (integer or float)
- **boolean**: true/false
- **date**: Date value (YYYY-MM-DD)
- **datetime**: Date and time value (ISO 8601)
- **email**: Email address
- **phone**: Phone number
- **url**: URL
- **json**: JSON object/array
- **array**: Array of values
- **object**: Nested object with fields

### Source Field Reference

When configuring workflow nodes, users **select** data sources through UI dropdowns/selectors, not write JSON manually:

**UI Flow:**
1. User sees field: "Recipient Email" (type: email, required: true)
2. User clicks dropdown next to field
3. Dropdown shows options:
   - **From Previous Nodes**: 
     - Node "Fetch User" → output field "email"
     - Node "Get Customer" → output field "contactEmail"
   - **From Trigger Data**:
     - Trigger field "userEmail"
     - Trigger field "customerEmail"
   - **From Variables**:
     - Variable "defaultRecipient"
   - **Static Value**: Enter email directly
4. User selects: "From Previous Nodes" → "Fetch User" → "email"
5. System saves: `{ "recipient": { "source": "_nodeOutputs.fetchUser.email", "type": "email" } }`

**Source Format** (saved by system, not written by user):
- `_nodeOutputs.{nodeLabel}.{fieldPath}`: Reference output from specific node
- `_triggerData.{fieldPath}`: Reference trigger data
- `_variables.{variableName}`: Reference workflow variable
- `_metadata.{fieldName}`: Reference execution metadata

## Workflow Node Configuration Structure

### Node Configuration Saved Format

When user configures a node in workflow builder, system saves field mappings (not full schema):

```json
{
  "id": "node-uuid",
  "label": "sendEmail",
  "type": "action",
  "subType": "custom-action",
  "registryId": "send-email-action",
  "position": { "x": 100, "y": 100 },
  "data": {
    "config": {
      "fieldMappings": {
        "recipient": {
          "source": "_nodeOutputs.fetchUser.email",
          "type": "email"
        },
        "subject": {
          "source": "_variables.welcomeSubject",
          "type": "string"
        },
        "body": {
          "source": "_nodeOutputs.formatMessage.body",
          "type": "string"
        }
      }
    }
  }
}
```

**Key Points:**
- `registryId`: References the action definition from registry (contains schema)
- `fieldMappings`: User-selected data sources for each field (not full schema definition)
- Schema is loaded from registry during execution, not stored in node config
- Field mappings reference where to get data, not the schema structure itself

## Schema Access During Execution

### Execution Flow

During workflow execution, schemas are loaded from registry based on `registryId`:

```java
// Pseudo-code for schema access
Node node = workflow.getNode(nodeId);
String registryId = node.getRegistryId();

// Load schema from registry (trigger or action registry)
SchemaDefinition definition = registryService.getDefinition(registryId);
Schema inputSchema = definition.getInputSchema();
Schema outputSchema = definition.getOutputSchema();

// Get field mappings from node config
Map<String, FieldMapping> fieldMappings = node.getConfig().getFieldMappings();

// Resolve field values using mappings
Map<String, Object> inputData = resolveFieldMappings(fieldMappings, executionContext);

// Validate input data against schema
ValidationResult validation = validateData(inputData, inputSchema);

// Execute node with validated data
NodeExecutionResult result = executeNode(node, inputData);
```

### Field Mapping Resolution

System resolves field mappings to actual values:

```java
// Resolve field mappings
Map<String, Object> resolveFieldMappings(
    Map<String, FieldMapping> mappings, 
    ExecutionContext context
) {
    Map<String, Object> resolved = new HashMap<>();
    
    for (Map.Entry<String, FieldMapping> entry : mappings.entrySet()) {
        String fieldName = entry.getKey();
        FieldMapping mapping = entry.getValue();
        
        // Resolve source based on mapping.source
        Object value = resolveSource(mapping.getSource(), context);
        
        // Validate type matches schema
        validateType(value, mapping.getType());
        
        resolved.put(fieldName, value);
    }
    
    return resolved;
}

// Resolve source (e.g., "_nodeOutputs.fetchUser.email")
Object resolveSource(String source, ExecutionContext context) {
    if (source.startsWith("_nodeOutputs.")) {
        // Extract node label and field path
        String[] parts = source.substring(13).split("\\.", 2);
        String nodeLabel = parts[0];
        String fieldPath = parts[1];
        
        // Get output from previous node
        NodeOutput output = context.getNodeOutput(nodeLabel);
        return getNestedField(output.getData(), fieldPath);
    } else if (source.startsWith("_triggerData.")) {
        String fieldPath = source.substring(13);
        return getNestedField(context.getTriggerData(), fieldPath);
    } else if (source.startsWith("_variables.")) {
        String varName = source.substring(11);
        return context.getVariable(varName);
    }
    // ... other source types
}
```

### Schema Utilities

Helper functions for schema access:

```java
// Load schema from registry
SchemaDefinition getSchemaFromRegistry(String registryId);

// Get field definition from schema
FieldDefinition getFieldDefinition(Schema schema, String fieldName);

// Get field value with type conversion
Object getFieldValue(Object data, String fieldName, Schema schema);

// Validate data against schema
ValidationResult validateData(Object data, Schema schema);

// Resolve field mappings to actual values
Map<String, Object> resolveFieldMappings(
    Map<String, FieldMapping> mappings, 
    ExecutionContext context
);
```

## UI Integration

### Schema-Based Configuration in Properties Panel

When configuring a workflow node, the Properties Panel uses schema from registry:

1. **Load Schema from Registry**
   - When user selects trigger/action from registry, system loads its schema
   - Schema defines which fields are available and their types

2. **Render Fields from Schema**
   - For each field in `inputSchema`, UI renders:
     - **Field Label**: Display name or field name
     - **Field Type Indicator**: Icon/badge showing type (string, email, number, etc.)
     - **Required Indicator**: Asterisk or badge if field is required
     - **Description/Help**: Tooltip or help text below field

3. **Data Source Selector** (not JSON editor)
   - For each field, UI shows dropdown/selector to choose data source:
     - **Previous Node Output**: 
       - Dropdown: Select previous node → Select output field
       - Shows available nodes and their output fields from schema
     - **Trigger Data**:
       - Dropdown: Select trigger field
       - Shows available trigger fields from trigger schema
     - **Workflow Variables**:
       - Dropdown: Select variable
       - Shows available workflow variables
     - **Static Value**:
       - Input field: Enter value directly
       - Type-specific input (email input for email type, number input for number type)

4. **Field Type Validation**
   - UI validates selected source type matches field type
   - Shows error if type mismatch (e.g., selecting string field for email type)
   - Real-time validation feedback

5. **Nested Field Support**
   - For object/array fields, UI shows nested field selector
   - User can drill down: `user.profile.email`
   - Visual path builder for nested fields

### Schema Validation in UI

- **Type Matching**: Validate selected source type matches field type
- **Required Field Check**: Ensure required fields have data source selected
- **Real-time Feedback**: Show validation errors immediately
- **Autocomplete**: Suggest available fields from previous nodes based on schema
- **Field Path Builder**: Visual builder for nested field paths with type hints

## Examples

### Example 1: Event Trigger Configuration Flow

**Step 1: Schema defined in Trigger Registry**
```json
{
  "id": "kafka-event-trigger-standard",
  "name": "Kafka Event Trigger",
  "type": "event",
  "configTemplate": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "",
      "consumerGroup": "workflow-consumer-group"
    },
    "schemas": [
      {
        "schemaId": "user-created",
        "eventType": "user.created",
        "fields": [
          { "name": "userId", "type": "string", "required": true },
          { "name": "email", "type": "email", "required": true }
        ]
      }
    ]
  }
}
```

**Step 2: User configures trigger in workflow builder**
- UI loads schema from registry
- User fills in Kafka connection settings (topic, consumerGroup)
- User selects event schemas to use
- System saves configuration with field mappings

**Step 3: Saved workflow node configuration**
```json
{
  "nodeId": "kafka-trigger-1",
  "type": "trigger",
  "subType": "event",
  "registryId": "kafka-event-trigger-standard",
  "config": {
    "kafka": {
      "topic": "user-events",
      "consumerGroup": "workflow-group"
    },
    "selectedSchemas": ["user-created"],
    "fieldMappings": {
      "userId": { "source": "_triggerData.userId", "type": "string" },
      "email": { "source": "_triggerData.email", "type": "email" }
    }
  }
}
```

### Example 2: API Call Action Configuration Flow

**Step 1: Schema defined in Action Registry**
```json
{
  "id": "api-call-action-standard",
  "name": "API Call Action",
  "type": "api-call",
  "configTemplate": {
    "inputSchema": {
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": true,
          "description": "User ID to fetch"
        }
      ]
    },
    "outputSchema": {
      "fields": [
        { "name": "id", "type": "string" },
        { "name": "email", "type": "email" },
        { "name": "name", "type": "string" }
      ]
    }
  }
}
```

**Step 2: User configures action in workflow builder**
- UI loads schema from registry
- User enters API URL and method
- For "userId" field, user sees dropdown:
  - Options: "From Previous Node" → "Fetch User" → "userId"
  - Options: "From Trigger Data" → "userId"
  - Options: "Static Value" → Enter value
- User selects: "From Trigger Data" → "userId"

**Step 3: Saved workflow node configuration**
```json
{
  "nodeId": "api-call-1",
  "type": "action",
  "subType": "api-call",
  "registryId": "api-call-action-standard",
  "config": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "fieldMappings": {
      "userId": {
        "source": "_triggerData.userId",
        "type": "string"
      }
    }
  }
}
```

### Example 3: Send Email Action Configuration Flow

**Step 1: Schema defined in Action Registry**
```json
{
  "id": "send-email-action",
  "name": "Send Email",
  "type": "custom-action",
  "actionType": "send-email",
  "configTemplate": {
    "inputSchema": {
      "fields": [
        {
          "name": "recipient",
          "type": "email",
          "required": true,
          "description": "Email recipient"
        },
        {
          "name": "subject",
          "type": "string",
          "required": true,
          "description": "Email subject"
        },
        {
          "name": "body",
          "type": "string",
          "required": true,
          "description": "Email body"
        }
      ]
    }
  }
}
```

**Step 2: User configures action in workflow builder**
- UI renders 3 fields based on schema:
  - **Recipient** (email, required): Dropdown → User selects "From Previous Node" → "Fetch User" → "email"
  - **Subject** (string, required): Dropdown → User selects "From Variables" → "welcomeSubject"
  - **Body** (string, required): Dropdown → User selects "From Previous Node" → "Format Message" → "body"

**Step 3: Saved workflow node configuration**
```json
{
  "nodeId": "send-email-1",
  "type": "action",
  "subType": "custom-action",
  "registryId": "send-email-action",
  "config": {
    "fieldMappings": {
      "recipient": {
        "source": "_nodeOutputs.fetchUser.email",
        "type": "email"
      },
      "subject": {
        "source": "_variables.welcomeSubject",
        "type": "string"
      },
      "body": {
        "source": "_nodeOutputs.formatMessage.body",
        "type": "string"
      }
    }
  }
}
```

## Benefits

1. **Type Safety**: Schemas provide type information for validation and UI rendering
2. **User-Friendly**: No need to write JSON manually - UI guides users through field selection
3. **Reusability**: Schemas defined once in registry can be used across multiple workflows
4. **Consistency**: Standardized field definitions ensure consistent configurations
5. **Validation**: Type checking and required field validation at configuration time
6. **Visual Configuration**: Dropdowns and selectors make it easy to select data sources
7. **Kafka Connect Integration**: Support for schema registry in trigger definitions
8. **Multiple Event Types**: Support for multiple schemas per Kafka topic

## Configuration Flow Example

### Example: Configuring Send Email Action

1. **User adds Action node** → Selects "Send Email" from Action Registry
2. **System loads schema** from `send-email-action` registry definition:
   ```json
   {
     "inputSchema": {
       "fields": [
         { "name": "recipient", "type": "email", "required": true },
         { "name": "subject", "type": "string", "required": true },
         { "name": "body", "type": "string", "required": true }
       ]
     }
   }
   ```

3. **UI renders form** based on schema:
   - Field 1: "Recipient Email" (email type, required) → Dropdown selector
   - Field 2: "Subject" (string type, required) → Dropdown selector  
   - Field 3: "Body" (string type, required) → Dropdown selector

4. **User selects data sources**:
   - Recipient: Selects "From Previous Node" → "Fetch User" → "email"
   - Subject: Selects "From Variables" → "welcomeSubject"
   - Body: Selects "From Previous Node" → "Format Message" → "body"

5. **System saves configuration**:
   ```json
   {
     "registryId": "send-email-action",
     "fieldMappings": {
       "recipient": { "source": "_nodeOutputs.fetchUser.email", "type": "email" },
       "subject": { "source": "_variables.welcomeSubject", "type": "string" },
       "body": { "source": "_nodeOutputs.formatMessage.body", "type": "string" }
     }
   }
   ```

6. **During execution**: System loads schema from registry, resolves field mappings, validates types, and executes action

## Related Documentation

- [Workflow Builder](./workflow-builder.md) - Main workflow builder feature
- [Node Types](./node-types.md) - Detailed node type specifications
- [Triggers](./triggers.md) - Trigger node details
- [Kafka Connect Integration](../technical/integration/kafka-connect.md) - Kafka Connect setup and configuration
