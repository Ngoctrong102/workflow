import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Badge } from "@/components/ui/badge"
import { Plus, Trash2, Edit2, ChevronDown, ChevronRight, Eye } from "lucide-react"
import { cn } from "@/lib/utils"

export interface FieldDefinition {
  name: string
  type: 'string' | 'number' | 'boolean' | 'date' | 'datetime' | 'email' | 'phone' | 'url' | 'json' | 'array' | 'object'
  required: boolean
  description?: string
  defaultValue?: any
  validation?: {
    minLength?: number
    maxLength?: number
    min?: number
    max?: number
    pattern?: string
    enum?: string[]
  }
  fields?: FieldDefinition[] // For nested objects/arrays
}

export interface SchemaDefinition {
  schemaId: string
  eventType?: string // For event triggers
  description?: string
  fields: FieldDefinition[]
  filter?: {
    field: string
    operator: string
    value: any
  }
  mapping?: {
    workflowContext?: Record<string, string>
  }
}

interface SchemaEditorProps {
  schemas: SchemaDefinition[]
  onChange: (schemas: SchemaDefinition[]) => void
  allowMultiple?: boolean // For event triggers
}

const FIELD_TYPES: FieldDefinition['type'][] = [
  'string',
  'number',
  'boolean',
  'date',
  'datetime',
  'email',
  'phone',
  'url',
  'json',
  'array',
  'object',
]

export function SchemaEditor({ schemas, onChange, allowMultiple = false }: SchemaEditorProps) {
  const [editingFieldIndex, setEditingFieldIndex] = useState<{ schemaIndex: number; fieldIndex: number | null } | null>(null)
  const [expandedSchemas, setExpandedSchemas] = useState<Set<number>>(new Set([0]))
  const [showPreview, setShowPreview] = useState(false)

  const handleAddSchema = () => {
    const newSchema: SchemaDefinition = {
      schemaId: `schema-${Date.now()}`,
      fields: [],
    }
    onChange([...schemas, newSchema])
    setExpandedSchemas(new Set([...expandedSchemas, schemas.length]))
  }

  const handleRemoveSchema = (index: number) => {
    const newSchemas = schemas.filter((_, i) => i !== index)
    onChange(newSchemas)
    const newExpanded = new Set(Array.from(expandedSchemas).filter(i => i !== index).map(i => i > index ? i - 1 : i))
    setExpandedSchemas(newExpanded)
  }

  const handleUpdateSchema = (index: number, updates: Partial<SchemaDefinition>) => {
    const newSchemas = [...schemas]
    newSchemas[index] = { ...newSchemas[index], ...updates }
    onChange(newSchemas)
  }

  const handleAddField = (schemaIndex: number, parentFieldIndex: number | null = null) => {
    const newField: FieldDefinition = {
      name: '',
      type: 'string',
      required: false,
      fields: [],
    }

    const newSchemas = [...schemas]
    if (parentFieldIndex === null) {
      newSchemas[schemaIndex].fields.push(newField)
    } else {
      const parentField = getNestedField(newSchemas[schemaIndex].fields, parentFieldIndex)
      if (parentField && (parentField.type === 'object' || parentField.type === 'array')) {
        if (!parentField.fields) {
          parentField.fields = []
        }
        parentField.fields.push(newField)
      }
    }
    onChange(newSchemas)
    setEditingFieldIndex({ schemaIndex, fieldIndex: parentFieldIndex === null ? newSchemas[schemaIndex].fields.length - 1 : parentFieldIndex })
  }

  const handleUpdateField = (schemaIndex: number, fieldIndex: number | null, updates: Partial<FieldDefinition>, parentFieldIndex: number | null = null) => {
    const newSchemas = [...schemas]
    if (parentFieldIndex === null) {
      newSchemas[schemaIndex].fields[fieldIndex!] = { ...newSchemas[schemaIndex].fields[fieldIndex!], ...updates }
    } else {
      const parentField = getNestedField(newSchemas[schemaIndex].fields, parentFieldIndex)
      if (parentField?.fields && fieldIndex !== null) {
        parentField.fields[fieldIndex] = { ...parentField.fields[fieldIndex], ...updates }
      }
    }
    onChange(newSchemas)
  }

  const handleRemoveField = (schemaIndex: number, fieldIndex: number | null, parentFieldIndex: number | null = null) => {
    const newSchemas = [...schemas]
    if (parentFieldIndex === null) {
      newSchemas[schemaIndex].fields = newSchemas[schemaIndex].fields.filter((_, i) => i !== fieldIndex)
    } else {
      const parentField = getNestedField(newSchemas[schemaIndex].fields, parentFieldIndex)
      if (parentField?.fields && fieldIndex !== null) {
        parentField.fields = parentField.fields.filter((_, i) => i !== fieldIndex)
      }
    }
    onChange(newSchemas)
    setEditingFieldIndex(null)
  }

  const getNestedField = (fields: FieldDefinition[], path: number): FieldDefinition | null => {
    // For now, simple implementation - can be enhanced for deeper nesting
    return fields[path] || null
  }

  const toggleSchemaExpanded = (index: number) => {
    const newExpanded = new Set(expandedSchemas)
    if (newExpanded.has(index)) {
      newExpanded.delete(index)
    } else {
      newExpanded.add(index)
    }
    setExpandedSchemas(newExpanded)
  }

  const renderFieldEditor = (
    field: FieldDefinition,
    schemaIndex: number,
    fieldIndex: number | null,
    parentFieldIndex: number | null = null,
    level: number = 0
  ) => {
    const isEditing = editingFieldIndex?.schemaIndex === schemaIndex && editingFieldIndex?.fieldIndex === fieldIndex
    const isNested = parentFieldIndex !== null

    if (isEditing) {
      return (
        <Card className={cn("mb-2", isNested && "ml-6 border-primary-200 bg-primary-50/30")} style={{ marginLeft: `${level * 24}px` }}>
          <CardContent className="pt-4">
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor={`field-name-${schemaIndex}-${fieldIndex}`} className="text-xs">
                    Field Name <span className="text-error-600">*</span>
                  </Label>
                  <Input
                    id={`field-name-${schemaIndex}-${fieldIndex}`}
                    value={field.name}
                    onChange={(e) => handleUpdateField(schemaIndex, fieldIndex, { name: e.target.value }, parentFieldIndex)}
                    placeholder="fieldName"
                    className="h-8 text-sm"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor={`field-type-${schemaIndex}-${fieldIndex}`} className="text-xs">
                    Type <span className="text-error-600">*</span>
                  </Label>
                  <Select
                    value={field.type}
                    onValueChange={(value: FieldDefinition['type']) => {
                      const updates: Partial<FieldDefinition> = { type: value }
                      if (value !== 'object' && value !== 'array') {
                        updates.fields = undefined
                      } else if (!field.fields) {
                        updates.fields = []
                      }
                      handleUpdateField(schemaIndex, fieldIndex, updates, parentFieldIndex)
                    }}
                  >
                    <SelectTrigger className="h-8 text-sm">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {FIELD_TYPES.map((type) => (
                        <SelectItem key={type} value={type}>
                          {type.charAt(0).toUpperCase() + type.slice(1)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <Checkbox
                  id={`field-required-${schemaIndex}-${fieldIndex}`}
                  checked={field.required}
                  onCheckedChange={(checked) => handleUpdateField(schemaIndex, fieldIndex, { required: !!checked }, parentFieldIndex)}
                />
                <Label htmlFor={`field-required-${schemaIndex}-${fieldIndex}`} className="text-xs font-normal cursor-pointer">
                  Required
                </Label>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor={`field-description-${schemaIndex}-${fieldIndex}`} className="text-xs">
                  Description
                </Label>
                <Textarea
                  id={`field-description-${schemaIndex}-${fieldIndex}`}
                  value={field.description || ''}
                  onChange={(e) => handleUpdateField(schemaIndex, fieldIndex, { description: e.target.value }, parentFieldIndex)}
                  placeholder="Field description"
                  rows={2}
                  className="text-sm"
                />
              </div>

              {(field.type === 'object' || field.type === 'array') && (
                <div className="space-y-2 pt-2 border-t">
                  <div className="flex items-center justify-between">
                    <Label className="text-xs font-semibold">
                      {field.type === 'object' ? 'Nested Fields' : 'Array Item Fields'}
                    </Label>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => handleAddField(schemaIndex, fieldIndex)}
                      className="h-7 text-xs cursor-pointer"
                    >
                      <Plus className="h-3 w-3 mr-1" />
                      Add Field
                    </Button>
                  </div>
                  {field.fields && field.fields.length > 0 && (
                    <div className="space-y-2">
                      {field.fields.map((nestedField, nestedIndex) => (
                        <div key={nestedIndex}>
                          {renderFieldEditor(nestedField, schemaIndex, nestedIndex, fieldIndex, level + 1)}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <div className="flex space-x-2 pt-2">
                <Button
                  type="button"
                  size="sm"
                  onClick={() => setEditingFieldIndex(null)}
                  className="flex-1 h-8 text-xs cursor-pointer"
                >
                  Done
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => handleRemoveField(schemaIndex, fieldIndex, parentFieldIndex)}
                  className="h-8 text-xs cursor-pointer"
                >
                  <Trash2 className="h-3 w-3" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )
    }

    return (
      <div
        className={cn(
          "flex items-center justify-between p-2 rounded border border-secondary-200 bg-white hover:bg-secondary-50 transition-colors cursor-pointer",
          isNested && "ml-6"
        )}
        style={{ marginLeft: `${level * 24}px` }}
        onClick={() => setEditingFieldIndex({ schemaIndex, fieldIndex })}
      >
        <div className="flex items-center space-x-2 flex-1 min-w-0">
          <Badge variant="outline" className="text-xs font-mono">
            {field.type}
          </Badge>
          <span className="font-medium text-sm truncate">{field.name || 'Unnamed field'}</span>
          {field.required && <Badge variant="default" className="text-xs">Required</Badge>}
          {field.description && (
            <span className="text-xs text-secondary-500 truncate hidden sm:inline">{field.description}</span>
          )}
        </div>
        <div className="flex items-center space-x-1">
          {(field.type === 'object' || field.type === 'array') && field.fields && field.fields.length > 0 && (
            <Badge variant="secondary" className="text-xs">
              {field.fields.length} field{field.fields.length !== 1 ? 's' : ''}
            </Badge>
          )}
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={(e) => {
              e.stopPropagation()
              setEditingFieldIndex({ schemaIndex, fieldIndex })
            }}
            className="h-7 w-7 p-0 cursor-pointer"
          >
            <Edit2 className="h-3 w-3" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={(e) => {
              e.stopPropagation()
              handleRemoveField(schemaIndex, fieldIndex, parentFieldIndex)
            }}
            className="h-7 w-7 p-0 text-error-600 hover:text-error-700 cursor-pointer"
          >
            <Trash2 className="h-3 w-3" />
          </Button>
        </div>
      </div>
    )
  }

  const renderSchemaPreview = () => {
    return (
      <Card className="mt-4">
        <CardHeader className="pb-3">
          <CardTitle className="text-base">Schema Preview</CardTitle>
          <CardDescription className="text-xs">JSON representation of the schema structure</CardDescription>
        </CardHeader>
        <CardContent>
          <pre className="text-xs bg-secondary-50 p-4 rounded border border-secondary-200 overflow-auto max-h-96">
            {JSON.stringify(schemas, null, 2)}
          </pre>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <Label className="text-base font-semibold">Schemas</Label>
          <p className="text-xs text-secondary-500 mt-1">
            {allowMultiple ? 'Define multiple schemas for different event types' : 'Define the schema structure'}
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => setShowPreview(!showPreview)}
            className="cursor-pointer"
          >
            <Eye className="h-4 w-4 mr-2" />
            {showPreview ? 'Hide' : 'Show'} Preview
          </Button>
          {allowMultiple && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleAddSchema}
              className="cursor-pointer"
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Schema
            </Button>
          )}
        </div>
      </div>

      {schemas.length === 0 ? (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-8">
            <p className="text-sm text-secondary-500 mb-4">No schemas defined</p>
            <Button
              type="button"
              variant="outline"
              onClick={handleAddSchema}
              className="cursor-pointer"
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Schema
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {schemas.map((schema, schemaIndex) => (
            <Card key={schemaIndex} className="border-secondary-200">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2 flex-1">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => toggleSchemaExpanded(schemaIndex)}
                      className="h-6 w-6 p-0 cursor-pointer"
                    >
                      {expandedSchemas.has(schemaIndex) ? (
                        <ChevronDown className="h-4 w-4" />
                      ) : (
                        <ChevronRight className="h-4 w-4" />
                      )}
                    </Button>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <Input
                          value={schema.schemaId}
                          onChange={(e) => handleUpdateSchema(schemaIndex, { schemaId: e.target.value })}
                          placeholder="Schema ID"
                          className="h-8 text-sm font-mono max-w-xs"
                        />
                        {allowMultiple && (
                          <Input
                            value={schema.eventType || ''}
                            onChange={(e) => handleUpdateSchema(schemaIndex, { eventType: e.target.value })}
                            placeholder="Event Type (e.g., user.created)"
                            className="h-8 text-sm flex-1"
                          />
                        )}
                      </div>
                      <Textarea
                        value={schema.description || ''}
                        onChange={(e) => handleUpdateSchema(schemaIndex, { description: e.target.value })}
                        placeholder="Schema description"
                        rows={1}
                        className="mt-2 text-xs"
                      />
                    </div>
                  </div>
                  {allowMultiple && schemas.length > 1 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveSchema(schemaIndex)}
                      className="h-8 w-8 p-0 text-error-600 hover:text-error-700 cursor-pointer"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </CardHeader>
              {expandedSchemas.has(schemaIndex) && (
                <CardContent className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Label className="text-sm font-semibold">Fields</Label>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => handleAddField(schemaIndex)}
                      className="h-8 text-xs cursor-pointer"
                    >
                      <Plus className="h-3 w-3 mr-1" />
                      Add Field
                    </Button>
                  </div>
                  {schema.fields.length === 0 ? (
                    <div className="text-center py-6 text-sm text-secondary-500 border border-dashed rounded">
                      No fields defined. Click "Add Field" to get started.
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {schema.fields.map((field, fieldIndex) => (
                        <div key={fieldIndex}>
                          {renderFieldEditor(field, schemaIndex, fieldIndex, null, 0)}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              )}
            </Card>
          ))}
        </div>
      )}

      {showPreview && renderSchemaPreview()}
    </div>
  )
}

