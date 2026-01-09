import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Label } from "@/components/ui/label"
import { Edit2, Trash2, ChevronRight, ChevronDown, Code, Eye } from "lucide-react"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

interface ObjectStructureViewProps {
  fields: FieldDefinition[]
  onAddField: () => void
  onEditField: (field: FieldDefinition, index: number) => void
  onDeleteField: (index: number) => void
  objectName?: string
}

export function ObjectStructureView({
  fields,
  onAddField: _onAddField,
  onEditField,
  onDeleteField,
  objectName = "object",
}: ObjectStructureViewProps) {
  const [expandedFields, setExpandedFields] = useState<Set<number>>(new Set())
  const [viewMode, setViewMode] = useState<"visual" | "json">("visual")

  const toggleField = (index: number) => {
    const newExpanded = new Set(expandedFields)
    if (newExpanded.has(index)) {
      newExpanded.delete(index)
    } else {
      newExpanded.add(index)
    }
    setExpandedFields(newExpanded)
  }

  const getFieldTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      string: "bg-blue-50 text-blue-700 border-blue-200",
      number: "bg-green-50 text-green-700 border-green-200",
      boolean: "bg-purple-50 text-purple-700 border-purple-200",
      date: "bg-orange-50 text-orange-700 border-orange-200",
      datetime: "bg-orange-50 text-orange-700 border-orange-200",
      email: "bg-cyan-50 text-cyan-700 border-cyan-200",
      phone: "bg-teal-50 text-teal-700 border-teal-200",
      url: "bg-indigo-50 text-indigo-700 border-indigo-200",
      json: "bg-gray-50 text-gray-700 border-gray-200",
      array: "bg-pink-50 text-pink-700 border-pink-200",
      object: "bg-yellow-50 text-yellow-700 border-yellow-200",
    }
    return colors[type] || "bg-gray-50 text-gray-700 border-gray-200"
  }

  const getFieldValuePreview = (field: FieldDefinition): string => {
    if (field.defaultValue !== undefined) {
      if (typeof field.defaultValue === "string") {
        return `"${field.defaultValue}"`
      }
      return String(field.defaultValue)
    }

    switch (field.type) {
      case "string":
      case "email":
      case "phone":
      case "url":
        return `"string"`
      case "number":
        return "0"
      case "boolean":
        return "false"
      case "date":
        return `"2024-01-01"`
      case "datetime":
        return `"2024-01-01T00:00:00Z"`
      case "array":
        return "[]"
      case "object":
        return "{}"
      case "json":
        return "null"
      default:
        return "null"
    }
  }

  const _generateJSON = () => {
    const obj: Record<string, unknown> = {}
    fields.forEach((field) => {
      if (field.defaultValue !== undefined) {
        obj[field.name] = field.defaultValue
      } else {
        switch (field.type) {
          case "string":
          case "email":
          case "phone":
          case "url":
            obj[field.name] = `"example"`
            break
          case "number":
            obj[field.name] = 0
            break
          case "boolean":
            obj[field.name] = false
            break
          case "array":
            obj[field.name] = []
            break
          case "object":
            obj[field.name] = {}
            break
          default:
            obj[field.name] = null
        }
      }
    })
    return JSON.stringify(obj, null, 2)
  }

  if (viewMode === "json") {
    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Code className="h-4 w-4 text-secondary-600" />
            <Label className="text-sm font-semibold">JSON Structure</Label>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setViewMode("visual")}
            className="cursor-pointer"
          >
            <Eye className="h-4 w-4 mr-1" />
            Visual View
          </Button>
        </div>
        <div className="bg-secondary-900 rounded-lg p-4 overflow-x-auto">
          <pre className="text-xs text-secondary-100 font-mono">
            {`{\n${fields.map((field, i) => {
              const value = getFieldValuePreview(field)
              const comma = i < fields.length - 1 ? "," : ""
              return `  "${field.name}": ${value}${comma}`
            }).join("\n")}\n}`}
          </pre>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-1 h-5 bg-primary-500 rounded-full" />
          <Label className="text-sm font-semibold text-secondary-900">
            {objectName.charAt(0).toUpperCase() + objectName.slice(1)} Structure
          </Label>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setViewMode("json")}
            className="cursor-pointer"
          >
            <Code className="h-4 w-4 mr-1" />
            JSON
          </Button>
        </div>
      </div>

      {/* Object Structure */}
      <div className="bg-white border-2 border-secondary-200 rounded-lg overflow-hidden shadow-sm">
        {/* Opening Brace */}
        <div className="px-4 py-2.5 bg-gradient-to-r from-secondary-50 to-secondary-100/50 border-b border-secondary-200 flex items-center gap-2">
          <span className="text-secondary-700 font-mono text-base font-semibold">{`{`}</span>
          <span className="text-xs text-secondary-600 font-medium">
            {fields.length} {fields.length === 1 ? "property" : "properties"}
          </span>
        </div>

        {/* Fields */}
        <div className="divide-y divide-secondary-100">
          {fields.length === 0 ? (
            <div className="px-4 py-8 text-center text-secondary-400">
              <p className="text-sm">No fields defined</p>
              <p className="text-xs mt-1">Click "Add Field" to get started</p>
            </div>
          ) : (
            fields.map((field, index) => {
              const isExpanded = expandedFields.has(index)
              const isRequired = field.required
              const hasDetails = field.description || field.validation || field.defaultValue !== undefined

              return (
                <div
                  key={field.name || index}
                  className="group hover:bg-secondary-50/50 transition-colors"
                >
                  {/* Field Header */}
                  <div className="px-4 py-3.5 flex items-center gap-3 group/item">
                    {/* Expand/Collapse */}
                    {hasDetails && (
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-6 w-6 p-0 cursor-pointer hover:bg-secondary-100 transition-colors"
                        onClick={() => toggleField(index)}
                      >
                        {isExpanded ? (
                          <ChevronDown className="h-4 w-4 text-secondary-500" />
                        ) : (
                          <ChevronRight className="h-4 w-4 text-secondary-500" />
                        )}
                      </Button>
                    )}
                    {!hasDetails && <div className="w-6" />}

                    {/* Field Name with JSON-like styling */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <code className="text-sm font-semibold text-primary-700 font-mono">
                          "{field.name}"
                        </code>
                        <span className="text-secondary-400 font-mono text-sm">:</span>
                        {isRequired && (
                          <Badge variant="outline" className="text-xs border-error-300 text-error-700 bg-error-50">
                            required
                          </Badge>
                        )}
                        <Badge
                          variant="outline"
                          className={`text-xs font-medium ${getFieldTypeColor(field.type)}`}
                        >
                          {field.type}
                        </Badge>
                        {field.displayName && field.displayName !== field.name && (
                          <span className="text-xs text-secondary-500 italic">
                            // {field.displayName}
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Field Value Preview */}
                    <div className="flex items-center gap-2">
                      <code className="text-sm text-secondary-700 font-mono bg-secondary-100 px-2.5 py-1 rounded border border-secondary-200">
                        {getFieldValuePreview(field)}
                      </code>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-1 opacity-0 group-hover/item:opacity-100 transition-opacity">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onEditField(field, index)}
                        className="h-7 w-7 p-0 cursor-pointer hover:bg-secondary-100 transition-colors"
                      >
                        <Edit2 className="h-3.5 w-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onDeleteField(index)}
                        className="h-7 w-7 p-0 text-error-600 hover:text-error-700 hover:bg-error-50 cursor-pointer transition-colors"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  </div>

                  {/* Field Details (Expanded) */}
                  {isExpanded && hasDetails && (
                    <div className="px-4 pb-3 pl-11 bg-secondary-50/30 border-t border-secondary-100">
                      <div className="space-y-2 pt-2">
                        {field.description && (
                          <div className="text-xs text-secondary-600">
                            <span className="font-medium">Description:</span> {field.description}
                          </div>
                        )}
                        {field.defaultValue !== undefined && (
                          <div className="text-xs text-secondary-600">
                            <span className="font-medium">Default:</span>{" "}
                            <code className="bg-secondary-100 px-1 py-0.5 rounded">
                              {String(field.defaultValue)}
                            </code>
                          </div>
                        )}
                        {field.validation && (
                          <div className="text-xs text-secondary-600 space-y-1">
                            <span className="font-medium">Validation:</span>
                            <div className="pl-2 space-y-0.5">
                              {field.validation.minLength !== undefined && (
                                <div>Min length: {field.validation.minLength}</div>
                              )}
                              {field.validation.maxLength !== undefined && (
                                <div>Max length: {field.validation.maxLength}</div>
                              )}
                              {field.validation.min !== undefined && (
                                <div>Min: {field.validation.min}</div>
                              )}
                              {field.validation.max !== undefined && (
                                <div>Max: {field.validation.max}</div>
                              )}
                              {field.validation.pattern && (
                                <div>
                                  Pattern: <code className="bg-secondary-100 px-1 py-0.5 rounded text-xs">{field.validation.pattern}</code>
                                </div>
                              )}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              )
            })
          )}
        </div>

        {/* Closing Brace */}
        <div className="px-4 py-2.5 bg-gradient-to-r from-secondary-50 to-secondary-100/50 border-t border-secondary-200">
          <span className="text-secondary-700 font-mono text-base font-semibold">{`}`}</span>
        </div>
      </div>
    </div>
  )
}

