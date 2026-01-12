# Action Node Configuration - Frontend Implementation

> **Tài liệu này mô tả cách frontend configure action nodes trong workflow builder, bao gồm config fields, input mapping, và output mapping.**

## Tổng quan

Khi user configure một action node trong workflow builder, frontend cần:
1. Load action registry để get schemas (inputSchema, configTemplate, outputSchema, outputMapping)
2. Render config fields section từ configTemplate schema
3. Render input fields section (nếu có inputSchema)
4. Render output mapping section (optional - user có thể customize)
5. Store configuration trong node config

## PropertiesPanel Structure

```
PropertiesPanel (khi configure action node)
├── Node Label
├── Config Fields Section (từ configTemplate schema)
│   └── Render fields từ configTemplate.fields
│   └── User nhập static value hoặc MVEL expression
│   └── Ví dụ: url = "/users/1234" hoặc "/users/@{userID}"
│   └── Show field type, required indicator, description
├── Input Fields Section (từ inputSchema) [Optional]
│   └── Render fields từ inputSchema (nếu có)
│   └── Use FieldSourceSelector để map từ previous nodes
│   └── Data này có thể được reference trong MVEL expressions
├── Output Mapping Section (từ outputMapping) [Optional]
│   └── Render output mapping fields từ outputMapping
│   └── User có thể customize MVEL expressions để map response
│   └── Show preview của output structure
│   └── Ví dụ: statusCode = @{_response.statusCode}
└── Output Preview (từ outputSchema)
    └── Show available fields sẽ được output
    └── Show structure sau khi apply output mapping
```

## Implementation Details

### 1. Load Action Registry

```typescript
// In PropertiesPanel.tsx
const loadActionRegistry = async (registryId: string) => {
  const action = await actionRegistryService.getById(registryId)
  
  return {
    inputSchema: action.configTemplate.inputSchema,
    configTemplateSchema: action.configTemplate.configTemplate,
    outputSchema: action.configTemplate.outputSchema,
    outputMapping: action.configTemplate.outputMapping
  }
}
```

### 2. Render Config Fields Section

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

### 3. Render Config Field (với MVEL Support)

```typescript
const renderConfigField = (field: SchemaDefinition, value: unknown) => {
  switch (field.type) {
    case 'string':
    case 'url':
      return (
        <MvelExpressionEditor
          value={value as string || ''}
          onChange={(newValue) => {
            handleConfigValueChange(field.name, newValue)
          }}
          field={field}
          nodes={nodes}
          currentNodeId={selectedNode?.id}
          placeholder={field.defaultValue as string}
        />
      )
    
    case 'json':
      return (
        <JsonEditor
          value={value as object || {}}
          onChange={(newValue) => {
            handleConfigValueChange(field.name, newValue)
          }}
          mvelEnabled={true}
          context={buildMvelContext()}
        />
      )
    
    case 'number':
      return (
        <Input
          type="number"
          value={value as number || field.defaultValue}
          onChange={(e) => {
            handleConfigValueChange(field.name, parseFloat(e.target.value))
          }}
        />
      )
    
    case 'boolean':
      return (
        <Switch
          checked={value as boolean || field.defaultValue}
          onCheckedChange={(checked) => {
            handleConfigValueChange(field.name, checked)
          }}
        />
      )
    
    case 'enum':
      return (
        <Select
          value={value as string || field.defaultValue}
          onValueChange={(newValue) => {
            handleConfigValueChange(field.name, newValue)
          }}
        >
          {field.enum?.map((option) => (
            <SelectItem key={option} value={option}>
              {option}
            </SelectItem>
          ))}
        </Select>
      )
    
    case 'object':
      return (
        <ObjectFieldEditor
          field={field}
          value={value as object || {}}
          onChange={(newValue) => {
            handleConfigValueChange(field.name, newValue)
          }}
          mvelEnabled={true}
        />
      )
    
    default:
      return <Input value={String(value || '')} />
  }
}
```

### 4. Render Input Fields Section (Optional)

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

### 5. Render Output Mapping Section (Optional)

```typescript
{outputSchema?.fields && outputMapping && (
  <div className="space-y-4">
    <div className="space-y-2">
      <Label className="text-sm font-semibold">Output Mapping</Label>
      <p className="text-xs text-secondary-500">
        Map raw response into output schema. Raw response is available as @{_response}.
        You can customize these mappings to transform the response.
      </p>
    </div>
    
    {outputSchema.fields.map((field) => (
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
        
        <MvelExpressionEditor
          value={nodeConfig.outputMapping?.[field.name] || outputMapping[field.name] || ""}
          onChange={(value) => {
            handleOutputMappingChange(field.name, value)
          }}
          field={field}
          nodes={nodes}
          currentNodeId={selectedNode?.id}
          placeholder={`@{_response.${field.name}}`}
          contextHint="_response"
        />
      </div>
    ))}
  </div>
)}
```

### 6. MvelExpressionEditor Component

```typescript
interface MvelExpressionEditorProps {
  value: string
  onChange: (value: string) => void
  field: SchemaDefinition
  nodes: Node[]
  currentNodeId?: string
  placeholder?: string
  contextHint?: string // "_response" for output mapping
}

const MvelExpressionEditor: React.FC<MvelExpressionEditorProps> = ({
  value,
  onChange,
  field,
  nodes,
  currentNodeId,
  placeholder,
  contextHint
}) => {
  const [autocompleteSuggestions, setAutocompleteSuggestions] = useState<string[]>([])
  
  // Build available variables for autocomplete
  const availableVariables = useMemo(() => {
    const vars: string[] = []
    
    // Previous node outputs
    nodes.forEach(node => {
      if (node.id !== currentNodeId && node.data?.outputSchema) {
        node.data.outputSchema.fields.forEach(field => {
          vars.push(`@{${node.id}.${field.name}}`)
        })
      }
    })
    
    // Trigger data
    vars.push('@{_trigger.*}')
    
    // Workflow variables
    vars.push('@{_vars.*}')
    
    // Built-in functions
    vars.push('@{_now()}', '@{_uuid()}', '@{_date()}')
    
    // Response context (for output mapping)
    if (contextHint === '_response') {
      vars.push('@{_response.*}')
    }
    
    return vars
  }, [nodes, currentNodeId, contextHint])
  
  return (
    <div className="space-y-2">
      <Textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="font-mono text-sm"
      />
      <Autocomplete
        suggestions={autocompleteSuggestions}
        onSelect={(suggestion) => {
          // Insert suggestion at cursor position
          onChange(insertAtCursor(value, suggestion))
        }}
        trigger="@{"
      />
      <div className="text-xs text-muted-foreground">
        Available: {availableVariables.slice(0, 5).join(', ')}...
      </div>
    </div>
  )
}
```

### 7. Node Config Structure

```typescript
interface NodeConfig {
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

// Example node config
const nodeConfig: NodeConfig = {
  configValues: {
    url: "/users/@{userID}",
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer @{getToken.token}"
    },
    body: {
      "userId": "@{userID}",
      "name": "@{userName}"
    }
  },
  inputMappings: {
    userId: {
      source: "previousNode",
      nodeId: "fetchUser",
      field: "userId"
    }
  },
  outputMapping: {
    statusCode: "@{_response.statusCode}",
    status: "@{_response.statusCode} >= 200 && @{_response.statusCode} < 300 ? 'success' : 'error'"
  }
}
```

**Note**: 
- `outputMapping` trong node config là optional
- Nếu không có, system sẽ sử dụng outputMapping từ action registry
- Nếu có, sẽ override registry default

### 8. Save Node Configuration

```typescript
const handleSave = () => {
  const updatedNode = {
    ...selectedNode,
    data: {
      ...selectedNode.data,
      config: {
        configValues: configValues,
        inputMappings: inputMappings,
        outputMapping: outputMapping // Optional
      }
    }
  }
  
  onNodeUpdate(updatedNode)
}
```

## Data Flow

### 1. Load Action Registry
```
User selects action from registry
  ↓
Load action registry by registryId
  ↓
Extract schemas: inputSchema, configTemplate, outputSchema, outputMapping
```

### 2. Render Configuration UI
```
Render Config Fields Section (từ configTemplate)
  ↓
Render Input Fields Section (nếu có inputSchema)
  ↓
Render Output Mapping Section (optional)
  ↓
Show Output Preview (từ outputSchema)
```

### 3. User Configuration
```
User nhập config values (static hoặc MVEL)
  ↓
User map input data (nếu có inputSchema)
  ↓
User customize output mapping (optional)
  ↓
Store trong node.data.config
```

## Related Documentation

- [MVEL Expression System](../../features/mvel-expression-system.md) - MVEL syntax và evaluation
- [Action Registry](../../features/action-registry.md) - Action schema structure
- [Action Execution](../backend/action-execution.md) - Backend execution flow
- [Planning: Sprint 40](../../planning/frontend/sprint-40.md) - PropertiesPanel MVEL Support

