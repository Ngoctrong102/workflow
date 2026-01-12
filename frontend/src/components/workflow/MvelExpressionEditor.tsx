import React, { useState, useRef, useEffect, useMemo } from "react"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Info, ChevronRight } from "lucide-react"
import { cn } from "@/lib/utils"
import type { Node } from "reactflow"
import type { SchemaDefinition, FieldDefinition } from "@/components/registry/SchemaEditor"
import { isTriggerNode } from "@/utils/node-type-utils"

interface MvelExpressionEditorProps {
  value: string
  onChange: (value: string) => void
  field: FieldDefinition
  nodes: Node[]
  currentNodeId: string
  triggerSchema?: SchemaDefinition[]
  errors?: string
  placeholder?: string
  label?: string
  description?: string
  required?: boolean
  showMvelFormatHelp?: boolean // Whether to show MVEL Expression Format section
}

interface ContextVariable {
  value: string
  label: string
  description: string
  category: "previous-node" | "trigger" | "variables" | "builtin"
  nodeId?: string
  fieldPath?: string
}

export function MvelExpressionEditor({
  value,
  onChange,
  field,
  nodes,
  currentNodeId,
  triggerSchema = [],
  errors,
  placeholder,
  label,
  description,
  required = false,
  showMvelFormatHelp = true, // Default to true for backward compatibility
}: MvelExpressionEditorProps) {
  const [showAutocomplete, setShowAutocomplete] = useState(false)
  const [autocompletePosition, setAutocompletePosition] = useState({ top: 0, left: 0 })
  const [cursorPosition, setCursorPosition] = useState(0)
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const [searchQuery, setSearchQuery] = useState("")

  // Get previous nodes (nodes that execute before current node)
  const previousNodes = useMemo(() => {
    return nodes.filter((node) => node.id !== currentNodeId)
  }, [nodes, currentNodeId])

  // Get trigger nodes
  const triggerNodes = useMemo(() => {
    return nodes.filter((node) => {
      const nodeType = node.data?.type as string
      const nodeConfig = (node.data as any)?.config || {}
      // Use helper function to correctly detect trigger nodes
      return nodeType ? isTriggerNode(nodeType, nodeConfig) : false
    })
  }, [nodes])

  // Build context variables list
  const contextVariables = useMemo<ContextVariable[]>(() => {
    const vars: ContextVariable[] = []

    // 1. Previous node outputs: @{nodeId.field}
    previousNodes.forEach((node) => {
      const nodeLabel = node.data?.label || node.id
      const nodeType = node.data?.type as string
      
      // Get output schema from node config or registry
      const outputSchema = (node.data?.config as any)?.outputSchema || []
      
      if (outputSchema && outputSchema.length > 0) {
        outputSchema.forEach((schema: SchemaDefinition) => {
          if (schema.fields && schema.fields.length > 0) {
            schema.fields.forEach((field: FieldDefinition) => {
              vars.push({
                value: `@{${node.id}.${field.name}}`,
                label: `${nodeLabel}.${field.name}`,
                description: `Output from ${nodeLabel} node (${field.type})`,
                category: "previous-node",
                nodeId: node.id,
                fieldPath: field.name,
              })
            })
          }
        })
      } else {
        // Generic node output
        vars.push({
          value: `@{${node.id}}`,
          label: `${nodeLabel} (all fields)`,
          description: `All output from ${nodeLabel} node`,
          category: "previous-node",
          nodeId: node.id,
        })
      }
    })

    // 2. Trigger data: @{_trigger.field}
    triggerNodes.forEach((triggerNode) => {
      const triggerLabel = triggerNode.data?.label || triggerNode.id
      
      if (triggerSchema && triggerSchema.length > 0) {
        triggerSchema.forEach((schema) => {
          if (schema.fields && schema.fields.length > 0) {
            schema.fields.forEach((field) => {
              vars.push({
                value: `@{_trigger.${field.name}}`,
                label: `_trigger.${field.name}`,
                description: `Trigger data from ${triggerLabel} (${field.type})`,
                category: "trigger",
              })
            })
          }
        })
      } else {
        vars.push({
          value: `@{_trigger}`,
          label: `_trigger (all fields)`,
          description: `All trigger data from ${triggerLabel}`,
          category: "trigger",
        })
      }
    })

    // 3. Workflow variables: @{_vars.varName}
    vars.push({
      value: `@{_vars.varName}`,
      label: `_vars.varName`,
      description: `Workflow variable (replace varName with actual variable name)`,
      category: "variables",
    })

    // 4. Built-in functions
    vars.push(
      {
        value: `@{_now()}`,
        label: `_now()`,
        description: `Current timestamp in milliseconds`,
        category: "builtin",
      },
      {
        value: `@{_uuid()}`,
        label: `_uuid()`,
        description: `Generate UUID`,
        category: "builtin",
      },
      {
        value: `@{_date()}`,
        label: `_date()`,
        description: `Current date string`,
        category: "builtin",
      },
      {
        value: `@{_timestamp()}`,
        label: `_timestamp()`,
        description: `Current timestamp in seconds`,
        category: "builtin",
      }
    )

    return vars
  }, [previousNodes, triggerNodes, triggerSchema])

  // Filter context variables based on search query
  const filteredVariables = useMemo(() => {
    if (!searchQuery) return contextVariables

    const query = searchQuery.toLowerCase()
    return contextVariables.filter((v) => {
      return (
        v.label.toLowerCase().includes(query) ||
        v.description.toLowerCase().includes(query) ||
        v.value.toLowerCase().includes(query)
      )
    })
  }, [contextVariables, searchQuery])

  // Detect @{ typing and show autocomplete
  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newValue = e.target.value
    const cursorPos = e.target.selectionStart || 0
    setCursorPosition(cursorPos)
    onChange(newValue)

    // Check if user is typing @{
    const textBeforeCursor = newValue.substring(0, cursorPos)
    const lastAtIndex = textBeforeCursor.lastIndexOf("@{")
    
    if (lastAtIndex !== -1) {
      const textAfterAt = textBeforeCursor.substring(lastAtIndex + 2)
      const closingBraceIndex = textAfterAt.indexOf("}")
      
      // If no closing brace found, show autocomplete
      if (closingBraceIndex === -1) {
        setSearchQuery(textAfterAt)
        setShowAutocomplete(true)
        
        // Calculate position for autocomplete popover
        const textarea = textareaRef.current
        if (textarea) {
          const rect = textarea.getBoundingClientRect()
          const lineHeight = 20 // Approximate line height
          const lines = textBeforeCursor.split("\n")
          const currentLine = lines.length - 1
          const charInLine = lines[lines.length - 1].length
          
          // Approximate position (can be improved with better text measurement)
          setAutocompletePosition({
            top: rect.top + (currentLine * lineHeight) + lineHeight,
            left: rect.left + (charInLine * 8), // Approximate character width
          })
        }
      } else {
        setShowAutocomplete(false)
      }
    } else {
      setShowAutocomplete(false)
    }
  }

  // Insert selected variable into textarea
  const handleSelectVariable = (variable: ContextVariable) => {
    const textarea = textareaRef.current
    if (!textarea) return

    const textBeforeCursor = value.substring(0, cursorPosition)
    const textAfterCursor = value.substring(cursorPosition)
    
    // Find the @{ that started this autocomplete
    const lastAtIndex = textBeforeCursor.lastIndexOf("@{")
    if (lastAtIndex === -1) return

    // Replace text from @{ to cursor with the selected variable
    const beforeAt = value.substring(0, lastAtIndex)
    const afterCursor = value.substring(cursorPosition)
    const newValue = beforeAt + variable.value + afterCursor

    onChange(newValue)
    setShowAutocomplete(false)
    setSearchQuery("")

    // Set cursor position after inserted variable
    setTimeout(() => {
      const newCursorPos = beforeAt.length + variable.value.length
      textarea.setSelectionRange(newCursorPos, newCursorPos)
      textarea.focus()
    }, 0)
  }

  // Basic MVEL syntax validation (check balanced braces)
  const validateMvelSyntax = (text: string): string | null => {
    let openBraces = 0
    let inExpression = false

    for (let i = 0; i < text.length; i++) {
      if (text[i] === "@" && i + 1 < text.length && text[i + 1] === "{") {
        inExpression = true
        openBraces++
        i++ // Skip next character
      } else if (text[i] === "}" && inExpression) {
        openBraces--
        if (openBraces === 0) {
          inExpression = false
        }
      }
    }

    if (openBraces > 0) {
      return "Unclosed MVEL expression. Missing closing brace }"
    }
    if (openBraces < 0) {
      return "Extra closing brace }"
    }

    return null
  }

  const syntaxError = validateMvelSyntax(value)

  return (
    <div className="space-y-2">
      {label && (
        <div className="flex items-center gap-2">
          <Label className="text-sm">
            {label}
            {required && <span className="text-error-600 ml-1">*</span>}
          </Label>
          {description && (
            <Popover>
              <PopoverTrigger asChild>
                <button
                  type="button"
                  className="h-4 w-4 text-secondary-500 hover:text-secondary-900 cursor-pointer"
                >
                  <Info className="h-4 w-4" />
                </button>
              </PopoverTrigger>
              <PopoverContent className="w-80">
                <p className="text-sm">{description}</p>
              </PopoverContent>
            </Popover>
          )}
        </div>
      )}

      <div className="relative">
        <Textarea
          ref={textareaRef}
          value={value}
          onChange={handleInputChange}
          onKeyDown={(e) => {
            if (e.key === "Escape") {
              setShowAutocomplete(false)
            }
          }}
          placeholder={placeholder || "Enter static value or MVEL expression (e.g., @{userID})"}
          className={cn(
            "font-mono text-sm min-h-[80px]",
            (errors || syntaxError) && "border-error-500"
          )}
          rows={field.type === "json" ? 8 : 3}
        />

        {/* Autocomplete dropdown */}
        {showAutocomplete && filteredVariables.length > 0 && (
          <div
            className="absolute z-50 w-80 bg-white border border-secondary-200 rounded-md shadow-lg max-h-60 overflow-auto"
            style={{
              top: autocompletePosition.top,
              left: autocompletePosition.left,
            }}
          >
            <Command>
              <CommandInput
                placeholder="Search variables..."
                value={searchQuery}
                onValueChange={setSearchQuery}
              />
              <CommandList>
                <CommandEmpty>No variables found.</CommandEmpty>
                <CommandGroup heading="Previous Nodes">
                  {filteredVariables
                    .filter((v) => v.category === "previous-node")
                    .map((v) => (
                      <CommandItem
                        key={v.value}
                        onSelect={() => handleSelectVariable(v)}
                        className="cursor-pointer"
                      >
                        <div className="flex flex-col flex-1">
                          <div className="flex items-center gap-2">
                            <code className="text-xs font-mono">{v.value}</code>
                            <Badge variant="outline" className="text-xs">
                              {v.category}
                            </Badge>
                          </div>
                          <span className="text-xs text-secondary-500">{v.description}</span>
                        </div>
                      </CommandItem>
                    ))}
                </CommandGroup>
                <CommandGroup heading="Trigger Data">
                  {filteredVariables
                    .filter((v) => v.category === "trigger")
                    .map((v) => (
                      <CommandItem
                        key={v.value}
                        onSelect={() => handleSelectVariable(v)}
                        className="cursor-pointer"
                      >
                        <div className="flex flex-col flex-1">
                          <div className="flex items-center gap-2">
                            <code className="text-xs font-mono">{v.value}</code>
                            <Badge variant="outline" className="text-xs">
                              {v.category}
                            </Badge>
                          </div>
                          <span className="text-xs text-secondary-500">{v.description}</span>
                        </div>
                      </CommandItem>
                    ))}
                </CommandGroup>
                <CommandGroup heading="Variables">
                  {filteredVariables
                    .filter((v) => v.category === "variables")
                    .map((v) => (
                      <CommandItem
                        key={v.value}
                        onSelect={() => handleSelectVariable(v)}
                        className="cursor-pointer"
                      >
                        <div className="flex flex-col flex-1">
                          <div className="flex items-center gap-2">
                            <code className="text-xs font-mono">{v.value}</code>
                            <Badge variant="outline" className="text-xs">
                              {v.category}
                            </Badge>
                          </div>
                          <span className="text-xs text-secondary-500">{v.description}</span>
                        </div>
                      </CommandItem>
                    ))}
                </CommandGroup>
                <CommandGroup heading="Built-in Functions">
                  {filteredVariables
                    .filter((v) => v.category === "builtin")
                    .map((v) => (
                      <CommandItem
                        key={v.value}
                        onSelect={() => handleSelectVariable(v)}
                        className="cursor-pointer"
                      >
                        <div className="flex flex-col flex-1">
                          <div className="flex items-center gap-2">
                            <code className="text-xs font-mono">{v.value}</code>
                            <Badge variant="outline" className="text-xs">
                              {v.category}
                            </Badge>
                          </div>
                          <span className="text-xs text-secondary-500">{v.description}</span>
                        </div>
                      </CommandItem>
                    ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </div>
        )}
      </div>

      {(errors || syntaxError) && (
        <p className="text-sm text-error-600">{errors || syntaxError}</p>
      )}

      {description && !errors && !syntaxError && (
        <p className="text-xs text-secondary-500">{description}</p>
      )}

      {showMvelFormatHelp && (
        <div className="mt-2 p-2 bg-secondary-50 rounded-md border border-secondary-200">
          <p className="text-xs font-medium text-secondary-900 mb-1">MVEL Expression Format:</p>
          <ul className="text-xs text-secondary-600 space-y-1 list-disc list-inside">
            <li>
              <code className="px-1 py-0.5 bg-white rounded">{"@{nodeId.field}"}</code> - Reference previous node output
            </li>
            <li>
              <code className="px-1 py-0.5 bg-white rounded">{"@{_trigger.field}"}</code> - Reference trigger data
            </li>
            <li>
              <code className="px-1 py-0.5 bg-white rounded">{"@{_vars.varName}"}</code> - Reference workflow variable
            </li>
            <li>
              <code className="px-1 py-0.5 bg-white rounded">{"@{_now()}"}</code> - Built-in functions
            </li>
          </ul>
        </div>
      )}
    </div>
  )
}

