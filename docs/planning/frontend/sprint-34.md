# Sprint 34: Action Config Fields - Workflow Builder Integration

## Goal
Update PropertiesPanel to show and allow editing of action configuration fields when configuring action nodes in workflow builder. Support dynamic values using FieldSourceSelector.

## Phase
Registry Integration - Workflow Builder

## Complexity
Complex

## Dependencies
Sprint 31, Sprint 32, Sprint 33 (ActionConfigFieldsEditor and integration)

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-builder.md)` - Understand workflow builder
2. ✅ Read `@import(features/schema-definition.md)` - Understand schema usage
3. ✅ Verify Sprint 31, 32, 33 are completed
4. ✅ Check existing PropertiesPanel component
5. ✅ Check FieldSourceSelector component (from Sprint 30)

## Tasks

### Load Config Template from Registry
- [ ] When action node is selected:
  - Load action registry item by registryId
  - Extract configTemplate from registry
  - Get type-specific config fields (url, method, kafka, expression, etc.)
  - Get inputSchema/outputSchema from configTemplate

### Render Config Fields in PropertiesPanel
- [ ] For action nodes (api-call, publish-event, function):
  - Show config fields section
  - Render fields based on action type
  - Use FieldSourceSelector for dynamic values
  - Allow static values for simple fields

### API Call Config Fields in Workflow
- [ ] Render fields:
  - **URL**: FieldSourceSelector (can be from previous node or static)
  - **Method**: Select dropdown (static, from configTemplate)
  - **Headers**: Key-value pairs with FieldSourceSelector for values
  - **Body**: JSON editor with FieldSourceSelector support
  - **Authentication**: Form fields with FieldSourceSelector for apiKey/token
  - **Timeout**: Number input (static or from previous node)
  - **Retry**: Form fields (static or from previous node)
- [ ] Load defaults from configTemplate
- [ ] Allow override in workflow node config
- [ ] Save overrides to node config

### Publish Event Config Fields in Workflow
- [ ] Render fields:
  - **Kafka Brokers**: Textarea (static, from configTemplate)
  - **Topic**: FieldSourceSelector (can be dynamic)
  - **Key**: FieldSourceSelector (optional, can be dynamic)
  - **Headers**: Key-value pairs with FieldSourceSelector
  - **Message**: JSON editor with FieldSourceSelector support
- [ ] Load defaults from configTemplate
- [ ] Allow override in workflow node config

### Function Config Fields in Workflow
- [ ] Render fields:
  - **Expression**: Textarea with FieldSourceSelector support
  - **Output Field Name**: Text input (static, from configTemplate)
- [ ] Load defaults from configTemplate
- [ ] Allow override in workflow node config

### Config Override Storage
- [ ] Save config overrides in node config:
  ```typescript
  config: {
    // Override configTemplate values
    url?: string | FieldMapping
    method?: string
    headers?: Record<string, string | FieldMapping>
    kafka?: {
      topic?: string | FieldMapping
      key?: string | FieldMapping
      // ...
    }
    expression?: string | FieldMapping
    // Field mappings for inputSchema
    fieldMappings?: {
      [fieldName]: FieldMapping
    }
  }
  ```
- [ ] Load config overrides when editing node
- [ ] Merge with configTemplate defaults

### FieldSourceSelector Integration
- [ ] For each config field that can be dynamic:
  - Use FieldSourceSelector component
  - Show options: Static Value, From Previous Node, From Trigger Data, From Variables
  - Validate field type matches selected source
  - Save as FieldMapping or string value

## Deliverables

- ✅ PropertiesPanel loads configTemplate from registry
- ✅ Config fields rendered for action nodes
- ✅ API Call config fields editable in workflow
- ✅ Publish Event config fields editable in workflow
- ✅ Function config fields editable in workflow
- ✅ FieldSourceSelector integrated for dynamic values
- ✅ Config overrides saved correctly
- ✅ Config overrides loaded correctly

## Technical Details

### Config Override Structure

When user overrides config in workflow:
```typescript
{
  nodeId: "api-call-1",
  type: "api-call",
  registryId: "api-call-action-standard",
  config: {
    // Override registry configTemplate
    url: "https://api.example.com/users", // Static
    // OR
    url: {
      source: "_nodeOutputs.previousNode.apiUrl",
      type: "string"
    }, // Dynamic
    
    method: "POST", // Static (from registry or override)
    
    headers: {
      "Authorization": {
        source: "_nodeOutputs.authNode.token",
        type: "string"
      },
      "Content-Type": "application/json" // Static
    },
    
    // Field mappings for inputSchema
    fieldMappings: {
      userId: {
        source: "_triggerData.userId",
        type: "string"
      }
    }
  }
}
```

### FieldSourceSelector Usage

For config fields that support dynamic values:
- URL: Can be from previous node (e.g., API endpoint from config node)
- Headers values: Can be from previous node
- Body values: Can be from previous node
- Kafka topic: Can be from previous node
- Message values: Can be from previous node
- Expression: Can use template variables

## Compliance Verification

- [ ] PropertiesPanel loads configTemplate correctly
- [ ] Config fields rendered for all action types
- [ ] FieldSourceSelector works for config fields
- [ ] Config overrides saved correctly
- [ ] Config overrides loaded correctly
- [ ] Static and dynamic values work
- [ ] Validation works
- [ ] No linter errors

## Related Documentation

- `@import(features/workflow-builder.md)` ⚠️ **MUST FOLLOW**
- `@import(features/schema-definition.md)`
- `docs/planning/frontend/action-config-fields-plan.md`
- Sprint 30 (FieldSourceSelector)
- Sprint 31, Sprint 32, Sprint 33

