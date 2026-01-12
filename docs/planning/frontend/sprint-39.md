# Sprint 39: Action Editor - Config Template Schema

## Mục tiêu

Update ActionEditor để:
1. Keep Input Schema và Output Schema sections (đã có)
2. Add Config Template Schema Editor (giống Input Schema) - **REQUIRED**
3. Config Template Schema định nghĩa cấu trúc config fields (url, method, headers, etc.)
4. Add Output Mapping Editor - **REQUIRED** - MVEL expressions để map từ raw response vào output schema
5. Auto-generate config template schema và output mapping từ action type (optional, user có thể customize)

## Prerequisites

- ActionEditor đã có Input Schema và Output Schema sections
- SchemaEditor component đã có và hoạt động tốt
- ActionConfigTemplate type cần được update để include `configTemplate` field

## Dependencies

- Sprint này không phụ thuộc vào sprint khác
- Sprint 40 (PropertiesPanel) sẽ phụ thuộc vào sprint này

## Tasks

### Task 1: Update ActionEditor - Add Config Template Schema Section

**File**: `frontend/src/pages/ActionEditor.tsx`

**Changes**:
1. Keep Input Schema và Output Schema sections (đã có)
2. Add "Config Template Schema" section giữa Input Schema và Output Schema
3. Use `SchemaEditor` component (đã có) cho config template schema
4. Config Template Schema định nghĩa cấu trúc config fields (url, method, headers, body, etc.)

**Structure**:
```
ActionEditor
├── Basic Info
├── Input Schema (SchemaEditor) - Data từ previous nodes
├── Config Template Schema (SchemaEditor) [NEW] - Config fields structure
├── Output Schema (SchemaEditor) - Output structure
└── Output Mapping (OutputMappingEditor) [NEW] - MVEL expressions để map response
```

### Task 2: Auto-generate Config Template Schema

**File**: `frontend/src/pages/ActionEditor.tsx`

**Features**:
- Add "Auto-generate" button cho Config Template Schema section
- Auto-generate config template schema từ action type
- User có thể customize sau khi auto-generate

**Auto-generation Logic**:
```typescript
function generateConfigTemplateSchema(
  actionType: ActionType
): SchemaDefinition[] {
  switch (actionType) {
    case "api-call":
      return [
        {
          name: "url",
          type: "url",
          required: true,
          description: "API endpoint URL. Can be static or MVEL expression: /users/@{userID}",
        },
        {
          name: "method",
          type: "string",
          required: true,
          enum: ["GET", "POST", "PUT", "PATCH", "DELETE"],
          defaultValue: "GET",
          description: "HTTP method",
        },
        {
          name: "headers",
          type: "object",
          required: false,
          description: "HTTP headers. Can use MVEL expressions",
        },
        {
          name: "body",
          type: "json",
          required: false,
          description: "Request body. Can use MVEL expressions",
        },
        {
          name: "authentication",
          type: "object",
          required: false,
          description: "Authentication configuration",
          fields: [
            {
              name: "type",
              type: "string",
              enum: ["none", "api-key", "bearer-token"],
              defaultValue: "none",
            },
            {
              name: "apiKey",
              type: "string",
              required: false,
              description: "API key. Can use MVEL: @{apiKey}",
            },
            {
              name: "token",
              type: "string",
              required: false,
              description: "Bearer token. Can use MVEL: @{bearerToken}",
            },
          ],
        },
        {
          name: "timeout",
          type: "number",
          required: false,
          defaultValue: 5000,
          description: "Request timeout in milliseconds",
        },
      ]
    case "publish-event":
      return [
        {
          name: "kafka",
          type: "object",
          required: true,
          description: "Kafka configuration",
          fields: [
            {
              name: "brokers",
              type: "array",
              required: true,
              description: "Kafka broker addresses",
              items: {
                type: "string",
                pattern: "^[^:]+:[0-9]+$",
              },
            },
            {
              name: "topic",
              type: "string",
              required: true,
              description: "Kafka topic name. Can use MVEL: events-@{eventType}",
            },
            {
              name: "key",
              type: "string",
              required: false,
              description: "Message key. Can use MVEL: @{userId}",
            },
            {
              name: "headers",
              type: "object",
              required: false,
              description: "Kafka message headers",
            },
          ],
        },
        {
          name: "message",
          type: "json",
          required: false,
          description: "Message payload. Can use MVEL expressions",
        },
      ]
    case "function":
      return [
        {
          name: "expression",
          type: "string",
          required: true,
          description: "MVEL expression to evaluate. Can reference previous nodes: @{user.firstName} + ' ' + @{user.lastName}",
        },
        {
          name: "outputField",
          type: "string",
          required: false,
          defaultValue: "result",
          description: "Output field name",
        },
      ]
    default:
      return []
  }
}
```

### Task 3: Update ActionConfigTemplate Type

**File**: `frontend/src/components/registry/types.ts`

**Changes**:
- Update `ActionConfigTemplate` interface
- Keep `inputSchema` (optional) và `outputSchema` (required)
- Add `configTemplate` field (SchemaDefinition[] - schema cho config fields) - **REQUIRED**
- Add `outputMapping` field (Record<string, string> - MVEL expressions) - **REQUIRED**
- Remove old config fields (url, method, etc.) - sẽ được thay thế bởi configTemplate schema

```typescript
export interface ActionConfigTemplate {
  inputSchema?: SchemaDefinition[] // Optional: Data từ previous nodes để map
  outputSchema: SchemaDefinition[] // Required: Output structure
  configTemplate: SchemaDefinition[] // Required: Config fields structure (url, method, headers, etc.)
  outputMapping: Record<string, string> // Required: MVEL expressions để map từ _response vào output schema
}
```

**Output Mapping Structure**:
- Key: field name trong output schema
- Value: MVEL expression để evaluate với context có `_response`
- Example: `{ "statusCode": "@{_response.statusCode}", "body": "@{_response.body}" }`

**Migration Note**: 
- Existing actions với config fields cũ (url, method, etc.) cần migrate sang configTemplate schema
- Có thể tạo migration script để auto-convert old format sang new format

### Task 4: Create Output Mapping Editor Component

**File**: `frontend/src/components/registry/OutputMappingEditor.tsx`

**Features**:
- Render output mapping fields từ outputSchema
- Mỗi field trong outputSchema có một MVEL expression input
- Use MvelExpressionEditor component (sẽ tạo trong Sprint 40)
- Show preview của output structure
- Auto-generate default output mapping từ action type

**Props**:
```typescript
interface OutputMappingEditorProps {
  outputSchema: SchemaDefinition[]
  outputMapping: Record<string, string>
  onChange: (outputMapping: Record<string, string>) => void
}
```

### Task 5: Update ActionEditor Form Handling

**File**: `frontend/src/pages/ActionEditor.tsx`

**Changes**:
- Update form defaultValues để include `configTemplate` và `outputMapping`
- Handle `configTemplate` và `outputMapping` changes
- Save `configTemplate` và `outputMapping` cùng với `inputSchema` và `outputSchema`

**Form Structure**:
```typescript
{
  id: string
  name: string
  type: ActionType
  description: string
  configTemplate: {
    inputSchema?: SchemaDefinition[]
    outputSchema: SchemaDefinition[]
    configTemplate: SchemaDefinition[] // Schema cho config fields
    outputMapping: Record<string, string> // MVEL expressions để map response
  }
  metadata: {...}
}
```

## Implementation Details

### ActionEditor Structure

```typescript
// In ActionEditor.tsx
const { register, handleSubmit, control, watch, reset, setValue } = useForm<ActionRegistryItem & { 
  configTemplate: ActionConfigTemplate 
}>({
  defaultValues: {
    id: "",
    name: "",
    type: "custom-action",
    description: "",
    configTemplate: {
      inputSchema: [],
      outputSchema: [],
      configTemplate: [], // NEW: Config template schema
      outputMapping: {}, // NEW: Output mapping với MVEL expressions
    },
    metadata: { /* ... */ },
  },
})

// Render sections
<Card>
  <CardHeader>
    <CardTitle>Input Schema</CardTitle>
    <CardDescription>
      Define input data structure from previous nodes.
    </CardDescription>
  </CardHeader>
  <CardContent>
    <SchemaEditor
      schema={watch("configTemplate.inputSchema")}
      onChange={(schema) => {
        setValue("configTemplate.inputSchema", schema, { shouldDirty: true })
      }}
    />
  </CardContent>
</Card>

<Card>
  <CardHeader>
    <CardTitle>Config Template Schema</CardTitle>
    <CardDescription>
      Define config fields structure (url, method, headers, etc.).
      Users can provide static values or MVEL expressions in workflow builder.
    </CardDescription>
  </CardHeader>
  <CardContent>
    <div className="flex items-center justify-between mb-4">
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={() => {
          const generated = generateConfigTemplateSchema(watch("type"))
          setValue("configTemplate.configTemplate", generated, { shouldDirty: true })
        }}
      >
        Auto-generate
      </Button>
    </div>
    <SchemaEditor
      schema={watch("configTemplate.configTemplate")}
      onChange={(schema) => {
        setValue("configTemplate.configTemplate", schema, { shouldDirty: true })
      }}
    />
  </CardContent>
</Card>

<Card>
  <CardHeader>
    <CardTitle>Output Schema</CardTitle>
    <CardDescription>
      Define response/output structure.
    </CardDescription>
  </CardHeader>
  <CardContent>
    <SchemaEditor
      schema={watch("configTemplate.outputSchema")}
      onChange={(schema) => {
        setValue("configTemplate.outputSchema", schema, { shouldDirty: true })
      }}
    />
  </CardContent>
</Card>
```

## Testing Checklist

- [ ] Config Template Schema section renders correctly
- [ ] SchemaEditor works for config template schema
- [ ] Output Mapping Editor renders correctly
- [ ] Output Mapping Editor shows fields từ outputSchema
- [ ] Auto-generation works for Config Template Schema (API Call, Publish Event, Function)
- [ ] Auto-generation works for Output Mapping (API Call, Publish Event, Function)
- [ ] Form save/load works correctly với outputMapping
- [ ] Config template schema và outputMapping saved to backend

## Notes

- **Config Template Schema** là schema definition (giống Input Schema) - **REQUIRED**
  - Định nghĩa cấu trúc config fields (url, method, headers, etc.)
  - User sẽ nhập values cho các fields này trong PropertiesPanel (Sprint 40)
  - Values có thể là static hoặc MVEL expressions

- **Output Schema** là schema definition - **REQUIRED**
  - Định nghĩa cấu trúc output fields
  - Structure này sẽ được output vào execution context

- **Output Mapping** là MVEL expressions - **REQUIRED**
  - Map từ raw response (`_response`) vào output schema structure
  - Mỗi field trong output schema có một MVEL expression
  - Context cho output mapping bao gồm `_response` chứa raw action result

- **Input Schema** là **OPTIONAL**
  - Chỉ cần nếu action muốn map data từ previous nodes một cách explicit
  - Nếu không có, user vẫn có thể reference data trực tiếp trong MVEL expressions (ví dụ: `@{fetchUser.userId}`)

- **MVEL format**: `@{variable}` hoặc `@{nodeId.field}` (not `${variable}`)
- **Response context**: `@{_response.field}` trong output mapping

## Acceptance Criteria

- [ ] Config Template Schema section hiển thị trong ActionEditor
- [ ] SchemaEditor hoạt động tốt với config template schema
- [ ] Auto-generation button hoạt động cho tất cả action types
- [ ] Form save/load lưu configTemplate schema đúng cách
- [ ] Backend API nhận và lưu configTemplate schema
- [ ] Existing actions có thể load và hiển thị configTemplate schema

