import { useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { Sparkles, RefreshCw } from "lucide-react"
import type { SchemaDefinition, FieldDefinition } from "./SchemaEditor"
import { generateDefaultOutputMapping } from "@/utils/generate-config-template-schema"
import type { ActionType } from "./types"

interface OutputMappingEditorProps {
  actionType: ActionType
  outputSchema: SchemaDefinition[]
  outputMapping: Record<string, string>
  onChange: (outputMapping: Record<string, string>) => void
}

/**
 * Extract all field paths from output schema
 */
function extractFieldPaths(schema: SchemaDefinition[], prefix = ""): string[] {
  const paths: string[] = []

  schema.forEach((s) => {
    if (s.fields && s.fields.length > 0) {
      s.fields.forEach((field) => {
        const fieldPath = prefix ? `${prefix}.${field.name}` : field.name
        paths.push(fieldPath)

        // Handle nested fields
        if (field.fields && field.fields.length > 0) {
          paths.push(...extractFieldPaths([{ ...s, fields: field.fields }], fieldPath))
        }
      })
    }
  })

  return paths
}

/**
 * Get field definition by path
 */
function getFieldByPath(schema: SchemaDefinition[], path: string): FieldDefinition | null {
  const parts = path.split(".")
  let currentFields: FieldDefinition[] | undefined = schema.flatMap((s) => s.fields || [])

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i]
    const field = currentFields?.find((f) => f.name === part)

    if (!field) {
      return null
    }

    if (i === parts.length - 1) {
      return field
    }

    currentFields = field.fields
  }

  return null
}

export function OutputMappingEditor({
  actionType,
  outputSchema,
  outputMapping,
  onChange,
}: OutputMappingEditorProps) {
  const fieldPaths = useMemo(() => {
    if (!outputSchema || outputSchema.length === 0) {
      return []
    }
    return extractFieldPaths(outputSchema)
  }, [outputSchema])

  const handleMappingChange = (fieldPath: string, expression: string) => {
    onChange({
      ...outputMapping,
      [fieldPath]: expression,
    })
  }

  const handleAutoGenerate = () => {
    const generated = generateDefaultOutputMapping(actionType, outputSchema)
    onChange(generated)
  }

  if (!outputSchema || outputSchema.length === 0) {
    return (
      <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
        <p>Define Output Schema first to configure output mapping</p>
      </div>
    )
  }

  if (fieldPaths.length === 0) {
    return (
      <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
        <p>Add fields to Output Schema to configure output mapping</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-primary-600" />
          <p className="text-sm text-secondary-600">
            Map raw response to output schema using MVEL expressions
          </p>
        </div>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleAutoGenerate}
          className="cursor-pointer"
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Auto-generate
        </Button>
      </div>

      <div className="space-y-3">
        {fieldPaths.map((fieldPath) => {
          const field = getFieldByPath(outputSchema, fieldPath)
          const currentExpression = outputMapping[fieldPath] || ""

          return (
            <div key={fieldPath} className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor={`mapping-${fieldPath}`} className="text-sm font-medium">
                  {fieldPath}
                  {field?.required && <span className="text-error-600 ml-1">*</span>}
                </Label>
                {field && (
                  <Badge variant="outline" className="text-xs">
                    {field.type}
                  </Badge>
                )}
              </div>
              <Textarea
                id={`mapping-${fieldPath}`}
                value={currentExpression}
                onChange={(e) => handleMappingChange(fieldPath, e.target.value)}
                placeholder={`@{_response.${fieldPath.split(".").pop()}}`}
                rows={2}
                className="font-mono text-sm"
              />
              {field?.description && (
                <p className="text-xs text-secondary-500">{field.description}</p>
              )}
              <p className="text-xs text-secondary-500">
                MVEL expression to map from raw response. Use <code className="px-1 py-0.5 bg-secondary-100 rounded">@{_response.field}</code> to
                reference response data.
              </p>
            </div>
          )
        })}
      </div>

      <div className="mt-4 p-3 bg-secondary-50 rounded-md border border-secondary-200">
        <p className="text-xs font-medium text-secondary-900 mb-1">Context Variables:</p>
        <ul className="text-xs text-secondary-600 space-y-1 list-disc list-inside">
          <li>
            <code className="px-1 py-0.5 bg-white rounded">@{_response}</code> - Raw action response
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">@{_response.statusCode}</code> - HTTP status code (API Call)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">@{_response.body}</code> - Response body (API Call)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">@{_response.result}</code> - Function result (Function)
          </li>
          <li>
            <code className="px-1 py-0.5 bg-white rounded">@{_response.topic}</code> - Kafka topic (Publish Event)
          </li>
        </ul>
      </div>
    </div>
  )
}

