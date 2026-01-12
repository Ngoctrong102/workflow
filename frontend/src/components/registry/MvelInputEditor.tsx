import React, { useState, useRef, useEffect, useMemo } from "react"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Info } from "lucide-react"
import { cn } from "@/lib/utils"
import type { SchemaDefinition, FieldDefinition } from "./SchemaEditor"

interface MvelInputEditorProps {
  value: string
  onChange: (value: string) => void
  inputSchema?: SchemaDefinition[]
  errors?: string
  placeholder?: string
  label?: string
  description?: string
  required?: boolean
  multiline?: boolean
  rows?: number
  type?: "input" | "textarea"
}

interface ContextVariable {
  value: string
  label: string
  description: string
  category: "input-schema" | "builtin"
  fieldPath?: string
}

/**
 * Extract all field paths from input schema
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

export function MvelInputEditor({
  value,
  onChange,
  inputSchema = [],
  errors,
  placeholder,
  label,
  description,
  required = false,
  multiline = false,
  rows = 3,
  type = "input",
}: MvelInputEditorProps) {
  const [showAutocomplete, setShowAutocomplete] = useState(false)
  const [autocompletePosition, setAutocompletePosition] = useState({ top: 0, left: 0 })
  const [cursorPosition, setCursorPosition] = useState(0)
  const inputRef = useRef<HTMLInputElement | HTMLTextAreaElement>(null)
  const [searchQuery, setSearchQuery] = useState("")

  // Build context variables list from input schema
  const contextVariables = useMemo<ContextVariable[]>(() => {
    const vars: ContextVariable[] = []

    // 1. Input schema fields: @{fieldName} or @{schemaId.fieldName}
    inputSchema.forEach((schema) => {
      if (schema.fields && schema.fields.length > 0) {
        schema.fields.forEach((field) => {
          const fieldPath = schema.schemaId && schema.schemaId !== "input-schema" 
            ? `${schema.schemaId}.${field.name}`
            : field.name
          
          vars.push({
            value: `@{${fieldPath}}`,
            label: fieldPath,
            description: field.description || `Input field: ${field.name} (${field.type})`,
            category: "input-schema",
            fieldPath: fieldPath,
          })

          // Handle nested fields
          if (field.fields && field.fields.length > 0) {
            const nestedPaths = extractFieldPaths([{ ...schema, fields: field.fields }], fieldPath)
            nestedPaths.forEach((nestedPath) => {
              vars.push({
                value: `@{${nestedPath}}`,
                label: nestedPath,
                description: `Nested input field: ${nestedPath}`,
                category: "input-schema",
                fieldPath: nestedPath,
              })
            })
          }
        })
      }
    })

    // 2. Built-in functions
    vars.push(
      {
        value: "@{_now()}",
        label: "_now()",
        description: "Current timestamp",
        category: "builtin",
      },
      {
        value: "@{_uuid()}",
        label: "_uuid()",
        description: "Generate UUID",
        category: "builtin",
      },
      {
        value: "@{_random()}",
        label: "_random()",
        description: "Random number",
        category: "builtin",
      }
    )

    return vars
  }, [inputSchema])

  // Filter variables based on search query
  const filteredVariables = useMemo(() => {
    if (!searchQuery.trim()) return contextVariables

    const query = searchQuery.toLowerCase()
    return contextVariables.filter((v) =>
      v.label.toLowerCase().includes(query) ||
      v.description.toLowerCase().includes(query)
    )
  }, [contextVariables, searchQuery])

  // Handle input change and detect @{ trigger
  const handleChange = (newValue: string) => {
    onChange(newValue)
  }

  // Handle key up to detect cursor position and @{ trigger
  const handleKeyUp = (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const input = inputRef.current
    if (!input) return

    const currentValue = input instanceof HTMLInputElement || input instanceof HTMLTextAreaElement
      ? input.value
      : value

    let cursorPos = 0
    if (input instanceof HTMLInputElement) {
      cursorPos = input.selectionStart || 0
    } else if (input instanceof HTMLTextAreaElement) {
      cursorPos = input.selectionStart || 0
    }
    setCursorPosition(cursorPos)

    // Check if user is typing @{
    const textBeforeCursor = currentValue.substring(0, cursorPos)
    const lastAtIndex = textBeforeCursor.lastIndexOf("@")
    
    if (lastAtIndex !== -1 && currentValue[lastAtIndex + 1] === "{") {
      // Show autocomplete
      const rect = input.getBoundingClientRect()
      setAutocompletePosition({
        top: rect.bottom + window.scrollY + 4,
        left: rect.left + window.scrollX,
      })
      setShowAutocomplete(true)
      
      // Extract search query after @{
      const queryStart = lastAtIndex + 2
      const queryEnd = currentValue.indexOf("}", queryStart)
      if (queryEnd === -1) {
        setSearchQuery(currentValue.substring(queryStart, cursorPos))
      } else {
        setShowAutocomplete(false)
        setSearchQuery("")
      }
    } else {
      setShowAutocomplete(false)
      setSearchQuery("")
    }
  }

  // Handle autocomplete selection
  const handleSelectVariable = (variable: ContextVariable) => {
    const input = inputRef.current
    if (!input) return

    const textBeforeCursor = value.substring(0, cursorPosition)
    const lastAtIndex = textBeforeCursor.lastIndexOf("@")
    
    if (lastAtIndex !== -1) {
      const before = value.substring(0, lastAtIndex)
      const after = value.substring(cursorPosition)
      const newValue = `${before}${variable.value}${after}`
      onChange(newValue)
      
      // Set cursor position after inserted value
      setTimeout(() => {
        if (input instanceof HTMLInputElement || input instanceof HTMLTextAreaElement) {
          const newPosition = lastAtIndex + variable.value.length
          input.setSelectionRange(newPosition, newPosition)
          input.focus()
        }
      }, 0)
    }
    
    setShowAutocomplete(false)
    setSearchQuery("")
  }

  // Group variables by category
  const groupedVariables = useMemo(() => {
    const groups: Record<string, ContextVariable[]> = {}
    filteredVariables.forEach((v) => {
      if (!groups[v.category]) {
        groups[v.category] = []
      }
      groups[v.category].push(v)
    })
    return groups
  }, [filteredVariables])

  const InputComponent = type === "textarea" || multiline ? Textarea : Input

  return (
    <div className="space-y-2">
      {label && (
        <Label>
          {label}
          {required && <span className="text-error-600 ml-1">*</span>}
        </Label>
      )}
      
      <div className="relative">
        <InputComponent
          ref={inputRef as any}
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          onKeyUp={handleKeyUp}
          onClick={(e) => {
            const input = e.currentTarget
            if (input instanceof HTMLInputElement) {
              setCursorPosition(input.selectionStart || 0)
            } else if (input instanceof HTMLTextAreaElement) {
              setCursorPosition(input.selectionStart || 0)
            }
          }}
          onKeyDown={(e) => {
            if (e.key === "Escape") {
              setShowAutocomplete(false)
            }
          }}
          placeholder={placeholder}
          className={cn(
            errors && "border-error-500 focus-visible:ring-error-500",
            type === "textarea" || multiline ? "min-h-[80px]" : ""
          )}
          rows={type === "textarea" || multiline ? rows : undefined}
        />
        
        {showAutocomplete && filteredVariables.length > 0 && (
          <div
            className="absolute z-50 w-[400px] mt-1 bg-white border border-secondary-200 rounded-md shadow-lg"
            style={{
              position: "fixed",
              top: `${autocompletePosition.top}px`,
              left: `${autocompletePosition.left}px`,
            }}
          >
            <Command>
              <CommandInput placeholder="Search variables..." value={searchQuery} onValueChange={setSearchQuery} />
              <CommandList className="max-h-[300px]">
                <CommandEmpty>No variables found.</CommandEmpty>
                {Object.entries(groupedVariables).map(([category, vars]) => (
                  <CommandGroup key={category} heading={category === "input-schema" ? "Input Schema Fields" : "Built-in Functions"}>
                    {vars.map((variable) => (
                      <CommandItem
                        key={variable.value}
                        value={variable.value}
                        onSelect={() => handleSelectVariable(variable)}
                        className="cursor-pointer"
                      >
                        <div className="flex flex-col flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <code className="text-sm font-mono text-primary-600">{variable.label}</code>
                          </div>
                          <p className="text-xs text-secondary-500 truncate">{variable.description}</p>
                        </div>
                      </CommandItem>
                    ))}
                  </CommandGroup>
                ))}
              </CommandList>
            </Command>
          </div>
        )}
      </div>

      {errors && (
        <p className="text-sm text-error-600">{errors}</p>
      )}

      {description && (
        <p className="text-xs text-secondary-500">{description}</p>
      )}

      {inputSchema.length > 0 && (
        <div className="mt-2 p-2 bg-secondary-50 rounded-md border border-secondary-200">
          <p className="text-xs font-medium text-secondary-900 mb-1">Available Input Fields:</p>
          <ul className="text-xs text-secondary-600 space-y-1 list-disc list-inside">
            {inputSchema.flatMap((schema) =>
              schema.fields?.map((field) => (
                <li key={field.name}>
                  <code className="px-1 py-0.5 bg-white rounded">@{field.name}</code> - {field.description || field.name}
                </li>
              )) || []
            )}
          </ul>
        </div>
      )}
    </div>
  )
}

