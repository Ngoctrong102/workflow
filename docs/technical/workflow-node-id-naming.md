# Workflow Node ID Naming Convention

## Vấn đề Hiện tại

NodeId hiện tại được generate theo format: `{nodeType}-{timestamp}`, ví dụ:
- `event-trigger-1767365803643`
- `send-webhook-1767365809980`

Format này có các vấn đề:

1. **Khó đọc và khó reference**: Khi cần truy cập output từ node khác, phải dùng:
   ```
   _nodeOutputs.event-trigger-1767365803643.field
   ```
   Điều này rất khó đọc và khó maintain.

2. **Không có ý nghĩa**: Timestamp không mang ý nghĩa gì về chức năng của node.

3. **Khó debug**: Khi xem logs hoặc execution context, khó biết node nào đang được thực thi.

## Giải pháp Đề xuất

### Option 1: UUID (Recommended)

Sử dụng UUID để đảm bảo tính duy nhất và tránh conflict:

**Format:**
```
{nodeType}-{uuid}
```

**Ví dụ:**
- `event-trigger-a1b2c3d4-e5f6-7890-abcd-ef1234567890`
- `send-webhook-b2c3d4e5-f6a7-8901-bcde-f12345678901`

**Ưu điểm:**
- Đảm bảo tính duy nhất
- Dễ generate
- Không cần quản lý state

**Nhược điểm:**
- Vẫn khó đọc
- Vẫn khó reference trong context

### Option 2: Short UUID (Recommended for Context Access)

Sử dụng short UUID (8-12 ký tự) để cân bằng giữa tính duy nhất và khả năng đọc:

**Format:**
```
{nodeType}-{shortUuid}
```

**Ví dụ:**
- `event-trigger-a1b2c3d4`
- `send-webhook-e5f6a7b8`

**Ưu điểm:**
- Ngắn gọn hơn UUID đầy đủ
- Vẫn đảm bảo tính duy nhất
- Dễ đọc hơn một chút

**Nhược điểm:**
- Vẫn khó reference trong context

### Option 3: Semantic Name + Counter (Best for Context Access)

Sử dụng tên có ý nghĩa kết hợp với counter để đảm bảo tính duy nhất:

**Format:**
```
{nodeType}-{semanticName}-{counter}
```

**Ví dụ:**
- `event-trigger-user-created-1`
- `send-webhook-notification-1`
- `map-user-data-1`

**Ưu điểm:**
- Dễ đọc và có ý nghĩa
- Dễ reference trong context: `_nodeOutputs.event-trigger-user-created-1.field`
- Dễ debug và maintain

**Nhược điểm:**
- Cần quản lý counter để đảm bảo tính duy nhất
- Cần validate tên để tránh ký tự đặc biệt

### Option 4: UUID với Label Mapping (Best Overall)

Sử dụng UUID làm nodeId chính thức, nhưng cho phép user đặt label/alias để reference:

**Format:**
- NodeId: `{uuid}` (ví dụ: `a1b2c3d4-e5f6-7890-abcd-ef1234567890`)
- Label: User-defined (ví dụ: `fetchUserData`)

**Reference trong context:**
```
_nodeOutputs.{label}.field
```

**Ví dụ:**
- Node có label `fetchUserData` → reference: `_nodeOutputs.fetchUserData.userId`
- Node có label `sendEmail` → reference: `_nodeOutputs.sendEmail.status`

**Ưu điểm:**
- Đảm bảo tính duy nhất với UUID
- Dễ đọc và có ý nghĩa với label
- Linh hoạt - user tự đặt tên
- Dễ reference trong context

**Nhược điểm:**
- Cần implement mapping giữa label và nodeId
- Cần validate label để đảm bảo tính duy nhất trong workflow

## Recommendation: Option 4 (UUID + Label Mapping)

### Implementation Plan

#### 1. Frontend Changes

**a. Update Node Generation:**

```typescript
// utils/node-id-generator.ts
import { v4 as uuidv4 } from 'uuid'

export function generateNodeId(nodeType: string): string {
  return uuidv4()
}

export function generateNodeLabel(nodeType: string, existingLabels: Set<string>): string {
  const baseLabel = nodeType.replace(/-/g, '')
  let counter = 1
  let label = `${baseLabel}${counter}`
  
  while (existingLabels.has(label)) {
    counter++
    label = `${baseLabel}${counter}`
  }
  
  return label
}
```

**b. Update WorkflowCanvas:**

```typescript
// components/workflow/WorkflowCanvas.tsx
const newNode: Node = {
  id: generateNodeId(nodeType),  // UUID
  type: "workflow",
  position,
  data: {
    label: generateNodeLabel(nodeType, existingLabels),  // Semantic label
    type: nodeType,
  },
}
```

**c. Add Label Management:**

- Allow users to edit node label in PropertiesPanel
- Validate label uniqueness within workflow
- Use label for display, UUID for internal reference

#### 2. Backend Changes

**a. Update ExecutionContext:**

```java
// engine/ExecutionContext.java
public Map<String, Object> getDataForNode(String nodeId, Map<String, String> nodeLabelMap) {
    Map<String, Object> data = new HashMap<>();
    data.putAll(triggerData);
    data.putAll(variables);
    
    // Create nodeOutputs with labels as keys
    Map<String, Object> labeledOutputs = new HashMap<>();
    for (Map.Entry<String, Object> entry : nodeOutputs.entrySet()) {
        String label = nodeLabelMap.getOrDefault(entry.getKey(), entry.getKey());
        labeledOutputs.put(label, entry.getValue());
    }
    data.put("_nodeOutputs", labeledOutputs);
    data.put("_metadata", metadata);
    return data;
}
```

**b. Store Node Label Mapping:**

- Store node label mapping in workflow definition
- Pass mapping to ExecutionContext when executing

#### 3. Workflow Definition Structure

```json
{
  "nodes": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "label": "fetchUserData",
      "type": "data",
      "position": { "x": 100, "y": 100 },
      "data": {
        "label": "Fetch User Data",
        "config": { ... }
      }
    }
  ],
  "nodeLabels": {
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890": "fetchUserData"
  }
}
```

## Migration Strategy

### Phase 1: Support Both Formats

1. Update code to support both old format (nodeType-timestamp) and new format (UUID + label)
2. When loading old workflows, auto-generate labels from nodeId
3. Allow users to edit labels

### Phase 2: Default to New Format

1. New nodes use UUID + label by default
2. Old nodes can be migrated on save
3. Show migration warning for old workflows

### Phase 3: Enforce New Format

1. Require all workflows to use new format
2. Auto-migrate on load
3. Remove support for old format

## Best Practices

### 1. Label Naming

- Use camelCase: `fetchUserData`, `sendEmail`, `checkStatus`
- Be descriptive: `fetchUserData` not `fetch1`
- Avoid special characters: Only alphanumeric and underscore
- Keep it short: Max 50 characters

### 2. Label Uniqueness

- Labels must be unique within a workflow
- Auto-append counter if duplicate: `fetchUserData`, `fetchUserData2`
- Validate on save

### 3. Context Reference

- Always use label in context reference: `_nodeOutputs.fetchUserData.userId`
- Never use UUID in templates or configs
- Show label in UI, UUID in logs/debug

### 4. Backward Compatibility

- Support old format during migration
- Auto-generate labels for old nodes
- Show migration warnings

## Examples

### Example 1: Simple Workflow

```json
{
  "nodes": [
    {
      "id": "uuid-1",
      "label": "eventTrigger",
      "type": "event-trigger"
    },
    {
      "id": "uuid-2",
      "label": "fetchUser",
      "type": "data"
    },
    {
      "id": "uuid-3",
      "label": "sendEmail",
      "type": "action"
    }
  ]
}
```

**Context Reference:**
```
_nodeOutputs.eventTrigger.eventType
_nodeOutputs.fetchUser.userId
_nodeOutputs.sendEmail.status
```

### Example 2: Complex Workflow

```json
{
  "nodes": [
    {
      "id": "uuid-1",
      "label": "kafkaEvent",
      "type": "event-trigger"
    },
    {
      "id": "uuid-2",
      "label": "parseEvent",
      "type": "data"
    },
    {
      "id": "uuid-3",
      "label": "checkUserStatus",
      "type": "logic"
    },
    {
      "id": "uuid-4",
      "label": "fetchUserProfile",
      "type": "data"
    },
    {
      "id": "uuid-5",
      "label": "sendWelcomeEmail",
      "type": "action"
    }
  ]
}
```

**Context Reference:**
```
_nodeOutputs.kafkaEvent.eventType
_nodeOutputs.parseEvent.userId
_nodeOutputs.checkUserStatus.result
_nodeOutputs.fetchUserProfile.email
_nodeOutputs.sendWelcomeEmail.messageId
```

## Implementation Checklist

- [ ] Create node ID generator utility
- [ ] Create node label generator utility
- [ ] Update WorkflowCanvas to use new ID generation
- [ ] Add label editing in PropertiesPanel
- [ ] Add label uniqueness validation
- [ ] Update ExecutionContext to support label mapping
- [ ] Update workflow definition structure
- [ ] Add migration logic for old workflows
- [ ] Update documentation
- [ ] Add tests for ID generation
- [ ] Add tests for label validation
- [ ] Add tests for context reference

