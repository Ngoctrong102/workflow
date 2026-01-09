import React, { useState, useRef, useEffect, useMemo, useCallback } from "react"
import { Textarea } from "@/components/ui/textarea"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import { Check, Zap, Layers, Database, AlertCircle, CheckCircle2 } from "lucide-react"
import { cn } from "@/lib/utils"
import type { Node } from "reactflow"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"
import { isTriggerNodeType } from "@/utils/node-type-utils"

export interface TemplateSuggestion {
  value: string
  label: string
  description?: string
  type: "trigger" | "node" | "variable" | "metadata"
  nodeId?: string
  fieldPath?: string
  fieldType?: string
}

export interface TemplateInputProps {
  value: string
  onChange: (value: string) => void
  nodes: Node[]
  currentNodeId: string
  triggerObjectTypeId?: string | null
  objectTypes?: Map<string, { name: string; fields: FieldDefinition[] }>
  placeholder?: string
  rows?: number
  className?: string
  label?: string
  description?: string
  required?: boolean
}

interface ExpressionMatch {
  start: number
  end: number
  expression: string
  isValid: boolean
  error?: string
}

// Validate template expression
function validateExpression(expression: string, suggestions: TemplateSuggestion[]): { isValid: boolean; error?: string } {
  if (!expression.trim()) {
    return { isValid: false, error: "Empty expression" }
  }

  // Check if expression matches any suggestion
  const matched = suggestions.find((s) => s.value === expression.trim())
  if (matched) {
    return { isValid: true }
  }

  // Check if it's a valid field path format
  // Allow: _nodeOutputs.nodeId.fieldPath, variableName, _metadata.fieldName
  const validPattern = /^(_nodeOutputs\.[a-zA-Z0-9]+(\.[a-zA-Z0-9_]+)*|[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z0-9_]+)*|_metadata\.[a-zA-Z0-9_]+)$/
  if (validPattern.test(expression.trim())) {
    return { isValid: true }
  }

  return { isValid: false, error: "Invalid expression format" }
}

// Parse template to find all {{...}} expressions
function parseTemplate(template: string): ExpressionMatch[] {
  const matches: ExpressionMatch[] = []
  const regex = /\{\{([^}]+)\}\}/g
  let match

  while ((match = regex.exec(template)) !== null) {
    const start = match.index
    const end = match.index + match[0].length
    const expression = match[1].trim()
    
    matches.push({
      start,
      end,
      expression,
      isValid: false, // Will be validated later
    })
  }

  return matches
}

export function TemplateInput({
  value,
  onChange,
  nodes,
  currentNodeId,
  triggerObjectTypeId,
  objectTypes,
  placeholder = "Enter text with {{variables}}...",
  rows = 4,
  className,
  label,
  description,
  required = false,
}: TemplateInputProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const [cursorPosition, setCursorPosition] = useState(0)
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [suggestionQuery, setSuggestionQuery] = useState("")
  const [expressionMatches, setExpressionMatches] = useState<ExpressionMatch[]>([])

  // Get previous nodes (all nodes except current)
  const previousNodes = useMemo(() => {
    return nodes.filter((node) => node.id !== currentNodeId)
  }, [nodes, currentNodeId])

  // Get trigger nodes (nodes with category "trigger")
  const triggerNodes = useMemo(() => {
    return nodes.filter((node) => {
      const nodeType = node.data?.type as string
      return isTriggerNodeType(nodeType)
    })
  }, [nodes])

  // Build suggestions
  const suggestions = useMemo(() => {
    const options: TemplateSuggestion[] = []

    // 1. Trigger data fields - accessed via _nodeOutputs.{triggerNodeId}
    triggerNodes.forEach((triggerNode) => {
      const triggerNodeLabel = triggerNode.data?.label || triggerNode.id
      const triggerNodeObjectTypeId = triggerNode.data?.config?.objectTypeId
      
      if (triggerNodeObjectTypeId && objectTypes) {
        const triggerObjectType = objectTypes.get(triggerNodeObjectTypeId)
        if (triggerObjectType) {
          triggerObjectType.fields.forEach((field) => {
            const fieldPath = `_nodeOutputs.${triggerNode.id}.${field.name}`
            options.push({
              value: fieldPath,
              label: `${triggerNodeLabel}: ${field.displayName || field.name}`,
              description: `Trigger data from ${triggerNodeLabel} (ID: ${triggerNode.id}) - ${fieldPath}`,
              type: "trigger",
              nodeId: triggerNode.id,
              fieldPath: field.name,
              fieldType: field.type,
            })
          })
        }
      } else {
        // Generic trigger output (without specific fields)
        options.push({
          value: `_nodeOutputs.${triggerNode.id}`,
          label: `${triggerNodeLabel} (all fields)`,
          description: `All trigger data from ${triggerNodeLabel} (ID: ${triggerNode.id}) - _nodeOutputs.${triggerNode.id}`,
          type: "trigger",
          nodeId: triggerNode.id,
        })
      }
    })

    // 2. Node outputs (non-trigger nodes)
    // Filter out trigger nodes to avoid duplicates (they're already in trigger data section)
    const nonTriggerNodes = previousNodes.filter((node) => {
      const nodeType = node.data?.type as string
      return !isTriggerNodeType(nodeType)
    })
    
    nonTriggerNodes.forEach((node) => {
      const nodeLabel = node.data?.label || node.id
      const nodeObjectTypeId = node.data?.config?.objectTypeId
      
      if (nodeObjectTypeId && objectTypes) {
        const nodeObjectType = objectTypes.get(nodeObjectTypeId)
        if (nodeObjectType) {
          nodeObjectType.fields.forEach((field) => {
            const fieldPath = `_nodeOutputs.${node.id}.${field.name}`
            options.push({
              value: fieldPath,
              label: `${nodeLabel}: ${field.displayName || field.name}`,
              description: `From ${nodeLabel} node (ID: ${node.id}) - ${fieldPath}`,
              type: "node",
              nodeId: node.id,
              fieldPath: field.name,
              fieldType: field.type,
            })
          })
        }
      } else {
        // Generic node output
        options.push({
          value: `_nodeOutputs.${node.id}`,
          label: `${nodeLabel} (all fields)`,
          description: `All outputs from ${nodeLabel} node (ID: ${node.id}) - _nodeOutputs.${node.id}`,
          type: "node",
          nodeId: node.id,
        })
      }
    })

    // 3. Variables
    options.push({
      value: "variables",
      label: "Variables",
      description: "Global workflow variables",
      type: "variable",
    })

    // 4. Metadata
    options.push({
      value: "_metadata.executionId",
      label: "Execution ID",
      description: "Current execution ID",
      type: "metadata",
    })
    options.push({
      value: "_metadata.workflowId",
      label: "Workflow ID",
      description: "Current workflow ID",
      type: "metadata",
    })

    return options
  }, [triggerNodes, objectTypes, previousNodes])

  // Filter suggestions based on query
  const filteredSuggestions = useMemo(() => {
    if (!suggestionQuery) return suggestions

    const query = suggestionQuery.toLowerCase()
    return suggestions.filter(
      (option) =>
        option.label.toLowerCase().includes(query) ||
        option.value.toLowerCase().includes(query) ||
        option.description?.toLowerCase().includes(query)
    )
  }, [suggestions, suggestionQuery])

  // Group suggestions
  const groupedSuggestions = useMemo(() => {
    const groups: Record<string, TemplateSuggestion[]> = {
      trigger: [],
      node: [],
      variable: [],
      metadata: [],
    }

    filteredSuggestions.forEach((option) => {
      if (groups[option.type]) {
        groups[option.type].push(option)
      }
    })

    return groups
  }, [filteredSuggestions])

  // Validate all expressions in template
  useEffect(() => {
    const matches = parseTemplate(value)
    const validatedMatches = matches.map((match) => {
      const validation = validateExpression(match.expression, suggestions)
      return {
        ...match,
        isValid: validation.isValid,
        error: validation.error,
      }
    })
    setExpressionMatches(validatedMatches)
  }, [value, suggestions])

  // Handle text change
  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      const newValue = e.target.value
      const cursorPos = e.target.selectionStart || 0
      
      onChange(newValue)
      setCursorPosition(cursorPos)

      // Check if we're typing {{ to show suggestions
      const textBeforeCursor = newValue.substring(0, cursorPos)
      const lastTwoChars = textBeforeCursor.slice(-2)
      
      if (lastTwoChars === "{{") {
        setShowSuggestions(true)
        setSuggestionQuery("")
      } else if (showSuggestions) {
        // Extract current expression being typed
        const lastOpenBrace = textBeforeCursor.lastIndexOf("{{")
        if (lastOpenBrace !== -1) {
          const currentExpression = textBeforeCursor.substring(lastOpenBrace + 2)
          setSuggestionQuery(currentExpression)
        } else {
          setShowSuggestions(false)
        }
      }
    },
    [onChange, showSuggestions]
  )

  // Handle suggestion selection
  const handleSelectSuggestion = useCallback(
    (suggestion: TemplateSuggestion) => {
      console.log("handleSelectSuggestion called with:", suggestion)
      const textarea = textareaRef.current
      if (!textarea) {
        console.warn("Textarea ref not available")
        return
      }

      const currentValue = value || ""
      const currentCursorPos = cursorPosition ?? currentValue.length
      const textBeforeCursor = currentValue.substring(0, currentCursorPos)
      const textAfterCursor = currentValue.substring(currentCursorPos)
      
      // Find the last {{ before cursor
      const lastOpenBrace = textBeforeCursor.lastIndexOf("{{")
      if (lastOpenBrace === -1) {
        console.warn("No opening {{ found")
        return
      }

      // Replace the expression with selected suggestion
      const beforeExpression = currentValue.substring(0, lastOpenBrace + 2)
      // Check if there's already a closing }} after cursor
      const closingBraceIndex = textAfterCursor.indexOf("}}")
      let afterExpression: string
      if (closingBraceIndex !== -1) {
        // Remove existing }} and everything between
        afterExpression = textAfterCursor.substring(closingBraceIndex + 2)
      } else {
        // Check if there's a single } that needs to be replaced
        const singleBraceIndex = textAfterCursor.indexOf("}")
        if (singleBraceIndex !== -1) {
          // Replace single } with }}
          afterExpression = textAfterCursor.substring(singleBraceIndex + 1)
        } else {
          // No closing brace, add }}
          afterExpression = textAfterCursor
        }
      }

      const newValue = `${beforeExpression}${suggestion.value}}${afterExpression}`
      console.log("Setting new value:", newValue)
      onChange(newValue)
      
      setShowSuggestions(false)
      setSuggestionQuery("")

      // Set cursor position after the inserted expression
      setTimeout(() => {
        if (textarea) {
          const newCursorPos = beforeExpression.length + suggestion.value.length + 2
          textarea.setSelectionRange(newCursorPos, newCursorPos)
          textarea.focus()
        }
      }, 0)
    },
    [value, cursorPosition, onChange]
  )

  // Get type icon
  const getTypeIcon = (type: TemplateSuggestion["type"]) => {
    switch (type) {
      case "trigger":
        return <Zap className="h-4 w-4" />
      case "node":
        return <Layers className="h-4 w-4" />
      case "variable":
        return <Database className="h-4 w-4" />
      case "metadata":
        return <Database className="h-4 w-4" />
      default:
        return null
    }
  }

  // Get type color
  const getTypeColor = (type: TemplateSuggestion["type"]) => {
    switch (type) {
      case "trigger":
        return "bg-primary-100 text-primary-700 border-primary-200"
      case "node":
        return "bg-secondary-100 text-secondary-700 border-secondary-200"
      case "variable":
        return "bg-accent-100 text-accent-700 border-accent-200"
      case "metadata":
        return "bg-muted-100 text-muted-700 border-muted-200"
      default:
        return ""
    }
  }

  // Render syntax-highlighted text (for overlay)
  const renderHighlightedText = useMemo(() => {
    if (!value || expressionMatches.length === 0) return value

    let highlighted = value
    let offset = 0

    // Replace expressions with highlighted versions (backwards to preserve indices)
    const sortedMatches = [...expressionMatches].sort((a, b) => b.start - a.start)
    
    sortedMatches.forEach((match) => {
      const before = highlighted.substring(0, match.start + offset)
      const expression = highlighted.substring(match.start + offset, match.end + offset)
      const after = highlighted.substring(match.end + offset)
      
      // Create highlighted version
      const highlightClass = match.isValid
        ? "bg-success-100 text-success-700 border border-success-300"
        : "bg-error-100 text-error-700 border border-error-300"
      
      // We'll use CSS to highlight, so just return the text
      // The actual highlighting will be done via a separate overlay div
      highlighted = before + expression + after
    })

    return highlighted
  }, [value, expressionMatches])

  return (
    <div className={cn("space-y-2", className)}>
      {label && (
        <label className="text-sm font-medium">
          {label}
          {required && <span className="text-error-600 ml-1">*</span>}
        </label>
      )}

      <div className="relative" style={{ isolation: "isolate" }}>
        <Textarea
          ref={textareaRef}
          value={value}
          onChange={handleChange}
          onKeyDown={(e) => {
            // Close suggestions on Escape
            if (e.key === "Escape") {
              setShowSuggestions(false)
            }
            // Close suggestions when typing }}
            if (e.key === "}" && showSuggestions) {
              const textBeforeCursor = value.substring(0, cursorPosition)
              if (textBeforeCursor.endsWith("}")) {
                setShowSuggestions(false)
              }
            }
          }}
          placeholder={placeholder}
          rows={rows}
          className={cn(
            "font-mono text-sm relative z-0",
            "focus-visible:ring-2 focus-visible:ring-primary-500"
          )}
        />

        {/* Syntax highlighting overlay - highlights expressions */}
        {value && expressionMatches.length > 0 && (
          <div
            className="absolute inset-0 pointer-events-none px-3 py-2 font-mono text-sm whitespace-pre-wrap break-words overflow-hidden border border-transparent"
            style={{
              color: "transparent",
              caretColor: "transparent",
              zIndex: 1,
              pointerEvents: "none",
            }}
          >
            {(() => {
              // Build highlighted text by replacing expressions
              const result: React.ReactNode[] = []
              let lastIndex = 0

              expressionMatches.forEach((match, index) => {
                // Add text before expression
                if (match.start > lastIndex) {
                  result.push(
                    <span key={`text-${index}`}>
                      {value.substring(lastIndex, match.start)}
                    </span>
                  )
                }

                // Add highlighted expression
                result.push(
                  <span
                    key={`expr-${index}`}
                    className={cn(
                      "px-0.5 rounded",
                      match.isValid
                        ? "bg-success-100 text-success-700 border border-success-300"
                        : "bg-error-100 text-error-700 border border-error-300"
                    )}
                  >
                    {value.substring(match.start, match.end)}
                  </span>
                )

                lastIndex = match.end
              })

              // Add remaining text
              if (lastIndex < value.length) {
                result.push(
                  <span key="text-end">
                    {value.substring(lastIndex)}
                  </span>
                )
              }

              return result.length > 0 ? result : <span>{value}</span>
            })()}
          </div>
        )}

        {/* Suggestions Popover */}
        {showSuggestions && (
          <Popover open={showSuggestions} onOpenChange={setShowSuggestions} modal={false}>
            <PopoverTrigger asChild>
              <div className="absolute" style={{ left: 0, top: 0, width: 0, height: 0, pointerEvents: "none" }} />
            </PopoverTrigger>
            <PopoverContent
              className="w-[400px] p-0"
              align="start"
              side="bottom"
              sideOffset={4}
              onOpenAutoFocus={(e) => e.preventDefault()}
              onInteractOutside={(e) => {
                // Don't close when clicking on textarea
                const target = e.target as HTMLElement
                if (target?.closest('textarea') || target?.closest('[role="textbox"]')) {
                  e.preventDefault()
                }
              }}
              style={{ zIndex: 10000 }}
              onClick={(e) => {
                // Allow clicks to propagate
                e.stopPropagation()
              }}
            >
              <Command>
                <CommandInput
                  placeholder="Search fields..."
                  value={suggestionQuery}
                  onValueChange={setSuggestionQuery}
                  className="border-0"
                />
                <CommandList>
                  <CommandEmpty>No fields found.</CommandEmpty>

                  {/* Trigger Data */}
                  {groupedSuggestions.trigger.length > 0 && (
                    <CommandGroup heading="Trigger Data">
                      {groupedSuggestions.trigger.map((option) => (
                        <CommandItem
                          key={option.value}
                          value={option.value}
                          onSelect={() => {
                            console.log("onSelect triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          className="cursor-pointer"
                          style={{ pointerEvents: "auto" }}
                          onPointerDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onPointerDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          onMouseDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onMouseDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                        >
                          <div 
                            className="flex items-center gap-2 flex-1"
                            onClick={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              console.log("div onClick triggered for:", option.value)
                              handleSelectSuggestion(option)
                            }}
                            onPointerDown={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              console.log("div onPointerDown triggered for:", option.value)
                              handleSelectSuggestion(option)
                            }}
                            style={{ pointerEvents: "auto", cursor: "pointer" }}
                          >
                            <div className={cn("p-1 rounded", getTypeColor(option.type))}>
                              {getTypeIcon(option.type)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium">{option.label}</div>
                              {option.description && (
                                <div className="text-xs text-secondary-500">{option.description}</div>
                              )}
                              {option.type === "node" && (
                                <div className="text-xs text-primary-600 font-mono mt-0.5">
                                  {option.value}
                                </div>
                              )}
                            </div>
                            {option.fieldType && (
                              <Badge variant="outline" className="text-xs">
                                {option.fieldType}
                              </Badge>
                            )}
                            <Check className="ml-2 h-4 w-4 opacity-0" />
                          </div>
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  )}

                  {/* Node Outputs */}
                  {groupedSuggestions.node.length > 0 && (
                    <CommandGroup heading="Node Outputs">
                      {groupedSuggestions.node.map((option) => (
                        <CommandItem
                          key={option.value}
                          value={option.value}
                          onSelect={() => {
                            console.log("onSelect triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          className="cursor-pointer"
                          style={{ pointerEvents: "auto" }}
                          onPointerDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onPointerDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          onMouseDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onMouseDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                        >
                          <div 
                            className="flex items-center gap-2 flex-1"
                            onClick={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              console.log("div onClick triggered for:", option.value)
                              handleSelectSuggestion(option)
                            }}
                            onPointerDown={(e) => {
                              e.preventDefault()
                              e.stopPropagation()
                              console.log("div onPointerDown triggered for:", option.value)
                              handleSelectSuggestion(option)
                            }}
                            style={{ pointerEvents: "auto", cursor: "pointer" }}
                          >
                            <div className={cn("p-1 rounded", getTypeColor(option.type))}>
                              {getTypeIcon(option.type)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium">{option.label}</div>
                              {option.description && (
                                <div className="text-xs text-secondary-500">{option.description}</div>
                              )}
                              {option.type === "node" && (
                                <div className="text-xs text-primary-600 font-mono mt-0.5">
                                  {option.value}
                                </div>
                              )}
                            </div>
                            {option.fieldType && (
                              <Badge variant="outline" className="text-xs">
                                {option.fieldType}
                              </Badge>
                            )}
                            <Check className="ml-2 h-4 w-4 opacity-0" />
                          </div>
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  )}

                  {/* Variables */}
                  {groupedSuggestions.variable.length > 0 && (
                    <CommandGroup heading="Variables">
                      {groupedSuggestions.variable.map((option) => (
                        <CommandItem
                          key={option.value}
                          value={option.value}
                          onSelect={() => {
                            console.log("onSelect triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          className="cursor-pointer"
                          style={{ pointerEvents: "auto" }}
                          onPointerDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onPointerDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          onMouseDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onMouseDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                        >
                          <div className="flex items-center gap-2 flex-1">
                            <div className={cn("p-1 rounded", getTypeColor(option.type))}>
                              {getTypeIcon(option.type)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium">{option.label}</div>
                              {option.description && (
                                <div className="text-xs text-secondary-500">{option.description}</div>
                              )}
                            </div>
                            <Check className="ml-2 h-4 w-4 opacity-0" />
                          </div>
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  )}

                  {/* Metadata */}
                  {groupedSuggestions.metadata.length > 0 && (
                    <CommandGroup heading="Metadata">
                      {groupedSuggestions.metadata.map((option) => (
                        <CommandItem
                          key={option.value}
                          value={option.value}
                          onSelect={() => {
                            console.log("onSelect triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          className="cursor-pointer"
                          style={{ pointerEvents: "auto" }}
                          onPointerDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onPointerDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                          onMouseDown={(e) => {
                            e.preventDefault()
                            e.stopPropagation()
                            console.log("onMouseDown triggered for:", option.value)
                            handleSelectSuggestion(option)
                          }}
                        >
                          <div className="flex items-center gap-2 flex-1">
                            <div className={cn("p-1 rounded", getTypeColor(option.type))}>
                              {getTypeIcon(option.type)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium">{option.label}</div>
                              {option.description && (
                                <div className="text-xs text-secondary-500">{option.description}</div>
                              )}
                            </div>
                            <Check className="ml-2 h-4 w-4 opacity-0" />
                          </div>
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  )}
                </CommandList>
              </Command>
            </PopoverContent>
          </Popover>
        )}
      </div>

      {/* Validation status */}
      {expressionMatches.length > 0 && (
        <div className="flex items-center gap-2 text-xs">
          {expressionMatches.every((m) => m.isValid) ? (
            <>
              <CheckCircle2 className="h-4 w-4 text-green-600" />
              <span className="text-green-600">All expressions are valid</span>
            </>
          ) : (
            <>
              <AlertCircle className="h-4 w-4 text-red-600" />
              <span className="text-red-600">
                {expressionMatches.filter((m) => !m.isValid).length} invalid expression(s)
              </span>
            </>
          )}
        </div>
      )}

      {description && (
        <p className="text-xs text-secondary-500">{description}</p>
      )}
    </div>
  )
}

