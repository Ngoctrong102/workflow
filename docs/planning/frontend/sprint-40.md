# Sprint 40: PropertiesPanel - Config Fields với MVEL Support

## Mục tiêu

Update PropertiesPanel để:
1. Load action registry và extract configTemplate schema và outputMapping
2. Render config fields section từ configTemplate schema
3. User nhập static values hoặc MVEL expressions cho mỗi config field
4. Add MVEL expression editor component với autocomplete (reusable cho tất cả use cases)
5. Show input fields section (nếu có inputSchema - optional)
6. Show output mapping section (nếu user muốn customize) - **NEW**

**Note**: MvelExpressionEditor component sẽ được sử dụng cho tất cả các trường hợp cần MVEL expressions:
- Action config values
- Email/notification templates
- Function expressions
- Condition expressions
- Field mappings
- Output mapping expressions (map từ _response)

## Prerequisites

- Sprint 39 hoàn thành (ActionEditor có configTemplate schema)
- Action registry API trả về configTemplate schema
- PropertiesPanel đã có cơ bản structure

## Dependencies

- **Depends on**: Sprint 39 (ActionEditor - Config Template Schema)
- **Required for**: Sprint 27 (Backend MVEL Evaluation)

## Tasks

### Task 1: Create MVEL Expression Editor Component

**File**: `frontend/src/components/workflow/MvelExpressionEditor.tsx`

**Features**:
- Text input/textarea với MVEL expression support
- Autocomplete dropdown khi user types `@{` - show available context variables
- Context variables include:
  - Previous node outputs: `@{nodeId.field}`
  - Trigger data: `@{_trigger.field}`
  - Workflow variables: `@{_vars.varName}`
  - Built-in functions: `@{_now()}`, `@{_uuid()}`
- Basic validation cho MVEL syntax (check balanced braces)
- Preview available context variables (optional tooltip)

**Props**:
```typescript
interface MvelExpressionEditorProps {
  value: string
  onChange: (value: string) => void
  field: SchemaDefinition
  nodes: Node[]
  currentNodeId: string
  triggerSchema?: SchemaDefinition[]
  errors?: string
  placeholder?: string
}
```

**Implementation**:
- Use `Textarea` component (better for longer expressions)
- Detect `@{` typing và show autocomplete dropdown
- Build context variables list từ:
  - Previous nodes (nodes before currentNodeId)
  - Trigger schema (if available)
  - Workflow variables (if available)
- Basic syntax validation: check `@{` và `}` matching
- Optional: Add syntax highlighting (có thể dùng library như Prism hoặc highlight.js)

### Task 2: Update PropertiesPanel for Action Nodes

**File**: `frontend/src/components/workflow/PropertiesPanel.tsx`

**Changes**:
1. Load action registry by registryId
2. Extract configTemplate schema từ configTemplate
3. Extract inputSchema từ configTemplate (nếu có)
4. Extract outputSchema và outputMapping từ configTemplate
5. Render config fields section với MVEL support
6. Render input fields section (nếu có inputSchema)
7. Render output mapping section (optional - user có thể customize) [NEW]
8. Store config values trong node.data.config.configValues
9. Store custom output mapping trong node.data.config.outputMapping (nếu user customize)

**Structure**:
```typescript
node.data.config = {
  configValues: {
    url: "/users/@{userID}", // Static hoặc MVEL expression
    method: "POST", // Static value
    headers: {
      "Authorization": "Bearer @{token}" // MVEL expression
    },
    body: {
      "userId": "@{userID}", // MVEL expression
      "name": "@{userName}"
    }
  }
}
```

### Task 3: Render Config Fields Section

**Implementation**:
- Loop through configTemplate.fields
- For each field, render appropriate input component
- Support static values và MVEL expressions
- Use MvelExpressionEditor cho string/json fields
- Use standard inputs cho simple types (number, boolean, enum)

**Code**:
```typescript
{configTemplateSchema?.fields && configTemplateSchema.fields.length > 0 && (
  <div className="space-y-4">
    <div className="space-y-2">
      <Label className="text-sm font-semibold">Configuration Fields</Label>
      <p className="text-xs text-secondary-500">
        Configure action settings. Use static values or MVEL expressions (e.g., @{userID}).
      </p>
    </div>
    
    {configTemplateSchema.fields.map((field) => (
      <div key={field.name} className="space-y-2">
        <div className="flex items-center gap-2">
          <Label className="text-sm">
            {field.name}
            {field.required && <span className="text-destructive">*</span>}
          </Label>
          {field.description && (
            <Tooltip>
              <TooltipTrigger>
                <Info className="h-4 w-4 text-muted-foreground" />
              </TooltipTrigger>
              <TooltipContent>{field.description}</TooltipContent>
            </Tooltip>
          )}
        </div>
        
        {renderConfigField(field, nodeConfig.configValues?.[field.name])}
      </div>
    ))}
  </div>
)}
```

### Task 4: Render Input Fields Section (Optional)

**Implementation**:
- Only render if inputSchema exists và has fields
- Use FieldSourceSelector cho input data mapping
- Data này có thể được reference trong MVEL expressions

**Code**:
```typescript
{inputSchema?.fields && inputSchema.fields.length > 0 && (
  <div className="space-y-4">
    <div className="space-y-2">
      <Label className="text-sm font-semibold">Input Data Fields</Label>
      <p className="text-xs text-secondary-500">
        Map data from previous nodes. These can be referenced in MVEL expressions.
      </p>
    </div>
    
    {inputSchema.fields.map((field) => (
      <div key={field.name} className="space-y-2">
        <FieldSourceSelector
          field={field}
          value={nodeConfig.inputMappings?.[field.name]}
          onChange={(mapping) => {
            handleInputMappingChange(field.name, mapping)
          }}
          nodes={nodes}
          currentNodeId={selectedNode?.id}
          triggerSchema={triggerSchema}
        />
      </div>
    ))}
  </div>
)}
```

### Task 5: Handle Config Values Changes

**Implementation**:
- Update node.data.config.configValues khi user changes values
- Save to workflow state
- Validate field types và MVEL syntax (basic)

**Code**:
```typescript
const handleConfigValueChange = (
  fieldName: string,
  value: unknown
) => {
  if (!selectedNode) return
  
  const updatedNodes = nodes.map((node) => {
    if (node.id === selectedNode.id) {
      return {
        ...node,
        data: {
          ...node.data,
          config: {
            ...node.data.config,
            configValues: {
              ...node.data.config.configValues,
              [fieldName]: value,
            },
          },
        },
      }
    }
    return node
  })
  
  setNodes(updatedNodes)
}
```

## Implementation Details

### Config Field Rendering

```typescript
function renderConfigField(
  field: SchemaDefinition,
  currentValue: unknown
) {
  switch (field.type) {
    case "string":
    case "url":
      return (
        <MvelExpressionEditor
          value={currentValue as string || ""}
          onChange={(value) => handleConfigValueChange(field.name, value)}
          field={field}
          nodes={nodes}
          currentNodeId={selectedNode?.id}
          triggerSchema={triggerSchema}
        />
      )
    case "number":
      return (
        <Input
          type="number"
          value={currentValue as number || field.defaultValue || ""}
          onChange={(e) => handleConfigValueChange(field.name, Number(e.target.value))}
        />
      )
    case "boolean":
      return (
        <Switch
          checked={currentValue as boolean || field.defaultValue || false}
          onCheckedChange={(checked) => handleConfigValueChange(field.name, checked)}
        />
      )
    case "enum":
      return (
        <Select
          value={currentValue as string || field.defaultValue || ""}
          onValueChange={(value) => handleConfigValueChange(field.name, value)}
        >
          {field.enum?.map(option => (
            <SelectItem key={option} value={option}>{option}</SelectItem>
          ))}
        </Select>
      )
    case "json":
      return (
        <JsonEditor
          value={JSON.stringify(currentValue || field.defaultValue || {}, null, 2)}
          onChange={(json) => {
            try {
              const parsed = JSON.parse(json)
              handleConfigValueChange(field.name, parsed)
            } catch (e) {
              // Handle error
            }
          }}
        />
      )
    case "object":
      // Render nested fields
      return (
        <div className="space-y-2 pl-4 border-l-2">
          {field.fields?.map(nestedField => (
            <div key={nestedField.name}>
              {renderConfigField(nestedField, (currentValue as Record<string, unknown>)?.[nestedField.name])}
            </div>
          ))}
        </div>
      )
    default:
      return (
        <Input
          value={String(currentValue || "")}
          onChange={(e) => handleConfigValueChange(field.name, e.target.value)}
        />
      )
  }
}
```

### Node Config Structure

```typescript
node.data = {
  label: string
  registryId: string
  config: {
    configValues: {
      [fieldName: string]: unknown // Static value hoặc MVEL expression string
    }
    inputMappings?: {
      [fieldName: string]: FieldMapping // Nếu có inputSchema
    }
    outputMapping?: {
      [fieldName: string]: string // Custom MVEL expressions (optional - override registry default)
    }
  }
}
```

**Note**: 
- `outputMapping` trong node config là optional
- Nếu không có, system sẽ sử dụng outputMapping từ action registry
- Nếu có, sẽ override registry default

## Testing Checklist

- [ ] Config fields render correctly từ configTemplate schema
- [ ] MvelExpressionEditor works for string/url fields
- [ ] Static values work correctly
- [ ] MVEL expressions saved correctly
- [ ] Input fields section renders (nếu có inputSchema)
- [ ] Field type validation works
- [ ] Required fields validation works
- [ ] Nested fields support (object type)
- [ ] JSON fields support MVEL expressions

## Notes

- **Config values** được store trong `node.data.config.configValues`
  - Values có thể là static (string, number, boolean, object, array)
  - Hoặc MVEL expression strings (ví dụ: `"/users/@{userID}"`)
  - Backend sẽ evaluate MVEL expressions ở runtime (Sprint 27)

- **Input mappings** (nếu có inputSchema) được store trong `node.data.config.inputMappings`
  - Optional: Chỉ cần nếu action muốn map data từ previous nodes explicit
  - Data này có thể được reference trong MVEL expressions

- **MVEL format**: `@{variable}` hoặc `@{nodeId.field}` (not `${variable}`)
  - Runtime evaluation sẽ evaluate MVEL expressions với execution context (Sprint 27)

## Acceptance Criteria

- [ ] MvelExpressionEditor component hoạt động tốt
- [ ] Autocomplete dropdown hiển thị khi user types `@{`
- [ ] Config fields section render đúng từ configTemplate schema
- [ ] Static values được lưu đúng format
- [ ] MVEL expressions được lưu đúng format (string với `@{...}`)
- [ ] Input fields section render (nếu có inputSchema)
- [ ] Field type validation hoạt động (required, enum, pattern, etc.)
- [ ] Nested fields support (object type với fields)
- [ ] JSON fields support MVEL expressions trong values
- [ ] Node config structure đúng format: `node.data.config.configValues`

