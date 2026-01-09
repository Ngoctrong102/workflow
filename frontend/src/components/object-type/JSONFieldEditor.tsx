import { useState, useEffect, useMemo } from "react"
import CodeMirror from "@uiw/react-codemirror"
import { json } from "@codemirror/lang-json"
import { autocompletion, CompletionContext, completionKeymap } from "@codemirror/autocomplete"
import { keymap } from "@codemirror/view"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Code, CheckCircle2, AlertCircle, X, Lightbulb, Sparkles } from "lucide-react"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

interface JSONFieldEditorProps {
  fields: FieldDefinition[]
  onChange: (fields: FieldDefinition[]) => void
  onCancel?: () => void
}

const FIELD_TYPES = [
  "String",
  "Number",
  "Boolean",
  "Date",
  "DateTime",
  "Email",
  "Phone",
  "Url",
  "Json",
  "Array",
  "Object",
]

const FIELD_PROPERTIES = [
  "type",
  "required",
  "defaultValue",
  "displayName",
  "description",
  "validation",
]

const VALIDATION_PROPERTIES = [
  "minLength",
  "maxLength",
  "min",
  "max",
  "pattern",
  "enum",
  "minItems",
  "maxItems",
  "objectTypeId",
  "itemType",
]

/**
 * Convert FieldDefinition array to JSON format
 */
function fieldsToJSON(fields: FieldDefinition[]): string {
  const obj: Record<string, any> = {}
  
  fields.forEach((field) => {
    const config: any = {
      type: field.type.charAt(0).toUpperCase() + field.type.slice(1),
    }
    
    if (field.required) {
      config.required = true
    }
    
    if (field.defaultValue !== undefined) {
      config.defaultValue = field.defaultValue
    }
    
    if (field.displayName) {
      config.displayName = field.displayName
    }
    
    if (field.description) {
      config.description = field.description
    }
    
    if (field.validation && Object.keys(field.validation).length > 0) {
      config.validation = field.validation
    }
    
    obj[field.name] = config
  })
  
  return JSON.stringify(obj, null, 2)
}

/**
 * Parse JSON format to FieldDefinition array
 */
function jsonToFields(json: string): { fields: FieldDefinition[]; errors: string[] } {
  const errors: string[] = []
  
  try {
    const parsed = JSON.parse(json)
    
    if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
      return { fields: [], errors: ["JSON must be an object"] }
    }
    
    const fields: FieldDefinition[] = []
    
    Object.entries(parsed).forEach(([fieldName, config]: [string, any]) => {
      if (typeof config !== "object" || config === null || Array.isArray(config)) {
        errors.push(`Field "${fieldName}": config must be an object`)
        return
      }
      
      const field: FieldDefinition = {
        name: fieldName,
        type: (config.type || "string").toLowerCase(),
        required: config.required === true,
        defaultValue: config.defaultValue,
        displayName: config.displayName,
        description: config.description,
        validation: config.validation || {},
      }
      
      // Validate type
      if (!FIELD_TYPES.map(t => t.toLowerCase()).includes(field.type)) {
        errors.push(`Field "${fieldName}": invalid type "${field.type}"`)
      }
      
      fields.push(field)
    })
    
    return { fields, errors }
  } catch (error) {
    return {
      fields: [],
      errors: [`Invalid JSON: ${error instanceof Error ? error.message : String(error)}`],
    }
  }
}

/**
 * Custom autosuggest for JSON editor
 * Shows suggestions automatically as user types
 */
function createAutosuggest(fields: FieldDefinition[]) {
  return autocompletion({
    activateOnTyping: true, // Auto-show suggestions when typing
    override: [
      (context: CompletionContext) => {
        const { state, pos } = context
        const line = state.doc.lineAt(pos)
        const lineText = line.text
        const beforeCursor = lineText.slice(0, pos - line.from)
        
        // Get word before cursor (including partial words)
        const wordMatch = beforeCursor.match(/([\w"]+)$/)
        const word = wordMatch ? wordMatch[1].replace(/"/g, "") : ""
        
        // Determine context
        const suggestions: Array<{ label: string; type: string; info?: string }> = []
        
        // Check if we're in a property name position (after field name quote)
        if (beforeCursor.match(/{\s*"[\w]*"$/)) {
          // After field name, suggest properties
          FIELD_PROPERTIES.forEach((prop) => {
            if (!word || prop.toLowerCase().startsWith(word.toLowerCase())) {
              suggestions.push({
                label: prop,
                type: "property",
                info: prop === "type" ? "Field type (required)" : `Field ${prop}`,
              })
            }
          })
        } else if (beforeCursor.match(/:\s*"[\w]*$/)) {
          // After type: ", suggest field types
          FIELD_TYPES.forEach((type) => {
            if (!word || type.toLowerCase().startsWith(word.toLowerCase())) {
              suggestions.push({
                label: type,
                type: "keyword",
                info: `${type} field type`,
              })
            }
          })
        } else if (beforeCursor.match(/validation:\s*{\s*"[\w]*$/)) {
          // After validation: { ", suggest validation properties
          VALIDATION_PROPERTIES.forEach((prop) => {
            if (!word || prop.toLowerCase().startsWith(word.toLowerCase())) {
              suggestions.push({
                label: prop,
                type: "property",
                info: `Validation ${prop}`,
              })
            }
          })
        } else if (beforeCursor.match(/{\s*"[\w]*$/)) {
          // At start of object, suggest existing field names
          fields.forEach((field) => {
            if (!word || field.name.toLowerCase().startsWith(word.toLowerCase())) {
              suggestions.push({
                label: field.name,
                type: "variable",
                info: field.displayName || field.name,
              })
            }
          })
        }
        
        if (suggestions.length > 0) {
          return {
            from: word ? pos - word.length : pos,
            options: suggestions.map((s) => ({
              label: s.label,
              type: s.type,
              info: s.info,
            })),
          }
        }
        
        return null
      },
    ],
  })
}

export function JSONFieldEditor({ fields, onChange, onCancel }: JSONFieldEditorProps) {
  const [jsonValue, setJsonValue] = useState(() => fieldsToJSON(fields))
  const [errors, setErrors] = useState<string[]>([])
  const [isValid, setIsValid] = useState(true)
  const [isFormatting, setIsFormatting] = useState(false)

  // Update JSON when fields change externally
  useEffect(() => {
    const currentJSON = fieldsToJSON(fields)
    if (currentJSON !== jsonValue) {
      setJsonValue(currentJSON)
      setErrors([])
      setIsValid(true)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fields])

  // Create autosuggest extension
  const autosuggestExtension = useMemo(
    () => createAutosuggest(fields),
    [fields]
  )

  // Handle JSON change
  const handleChange = (value: string) => {
    setJsonValue(value)
    
    // Parse and validate
    const { fields: parsedFields, errors: parseErrors } = jsonToFields(value)
    
    if (parseErrors.length > 0) {
      setErrors(parseErrors)
      setIsValid(false)
    } else {
      setErrors([])
      setIsValid(true)
      // Auto-update fields if valid
      if (parsedFields.length > 0) {
        onChange(parsedFields)
      }
    }
  }

  const handleFormat = () => {
    try {
      const parsed = JSON.parse(jsonValue)
      const formatted = JSON.stringify(parsed, null, 2)
      
      setIsFormatting(true)
      setJsonValue(formatted)
      
      setTimeout(() => {
        setIsFormatting(false)
      }, 300)
      
      // Validate after formatting
      const { fields: parsedFields, errors: parseErrors } = jsonToFields(formatted)
      
      if (parseErrors.length > 0) {
        setErrors(parseErrors)
        setIsValid(false)
      } else {
        setErrors([])
        setIsValid(true)
        if (parsedFields.length > 0) {
          onChange(parsedFields)
        }
      }
    } catch (error) {
      setErrors([`Invalid JSON: ${error instanceof Error ? error.message : String(error)}`])
      setIsValid(false)
      setIsFormatting(false)
    }
  }

  return (
    <Card className="border-secondary-200">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base flex items-center gap-2">
              <Code className="h-4 w-4" />
              JSON Editor
            </CardTitle>
            <CardDescription className="text-xs mt-1">
              Define fields in JSON format: <code className="text-xs">{"{ \"fieldName\": { \"type\": \"String\", \"required\": true } }"}</code>
            </CardDescription>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleFormat}
              className="cursor-pointer"
              title="Format JSON"
            >
              <Sparkles className="h-4 w-4 mr-1" />
              Format
            </Button>
            {onCancel && (
              <Button variant="ghost" size="sm" onClick={onCancel} className="cursor-pointer">
                <X className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Info Alert */}
        <Alert className="bg-primary-50 border-primary-200">
          <Lightbulb className="h-4 w-4 text-primary-600" />
          <AlertDescription className="text-xs text-primary-800">
            <strong>Format:</strong> Each field is a key with a config object. Properties: <code className="text-xs">type</code> (required), <code className="text-xs">required</code>, <code className="text-xs">defaultValue</code>, <code className="text-xs">displayName</code>, <code className="text-xs">description</code>, <code className="text-xs">validation</code>
          </AlertDescription>
        </Alert>

        {/* JSON Editor */}
        <div className="space-y-2">
          <div className="text-sm font-semibold">Field Definitions (JSON)</div>
          <div className="border border-secondary-200 rounded-lg overflow-hidden [&_.cm-editor]:outline-none [&_.cm-focused]:outline-none [&_.cm-scroller]:font-mono [&_.cm-scroller]:text-sm">
            <CodeMirror
              value={jsonValue}
              height="400px"
              extensions={[json(), autosuggestExtension, keymap.of(completionKeymap)]}
              onChange={handleChange}
              basicSetup={{
                lineNumbers: true,
                foldGutter: true,
                dropCursor: false,
                allowMultipleSelections: false,
                indentOnInput: true,
                bracketMatching: true,
                closeBrackets: true,
                autocompletion: true,
                highlightSelectionMatches: false,
                defaultKeymap: true,
                history: true,
                searchKeymap: true,
              }}
              theme="light"
            />
          </div>
          <div className="flex items-center justify-between">
            <p className="text-xs text-secondary-500">
              💡 Suggestions will appear automatically as you type. Use <kbd className="text-xs bg-secondary-100 px-1.5 py-0.5 rounded">↑↓</kbd> to navigate and <kbd className="text-xs bg-secondary-100 px-1.5 py-0.5 rounded">Enter</kbd> to select
            </p>
            {isFormatting && (
              <div className="flex items-center gap-1 text-xs text-primary-600">
                <Sparkles className="h-3 w-3 animate-pulse" />
                <span>Formatting...</span>
              </div>
            )}
          </div>
        </div>

        {/* Validation Status */}
        {isValid && jsonValue.trim() && (
          <Alert className="bg-success-50 border-success-200">
            <CheckCircle2 className="h-4 w-4 text-success-600" />
            <AlertDescription className="text-xs text-success-800">
              Valid JSON. Fields will be updated automatically.
            </AlertDescription>
          </Alert>
        )}

        {/* Errors */}
        {errors.length > 0 && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <div className="space-y-1">
                {errors.map((error, index) => (
                  <div key={index} className="text-xs">
                    {error}
                  </div>
                ))}
              </div>
            </AlertDescription>
          </Alert>
        )}
      </CardContent>
    </Card>
  )
}
