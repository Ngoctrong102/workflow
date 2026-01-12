import { useState, useMemo, useCallback } from "react"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Check, ChevronDown, Database, Zap, Layers, Search } from "lucide-react"
import { cn } from "@/lib/utils"
import type { Node } from "reactflow"
import type { FieldDefinition, SchemaDefinition } from "@/components/registry/SchemaEditor"
import { isTriggerNodeType, isTriggerNode, getNodeCategory } from "@/utils/node-type-utils"
import { NodeTypeEnum } from "@/types/workflow"

export interface ContextFieldOption {
  value: string
  label: string
  description?: string
  type: "trigger" | "node" | "variable" | "metadata"
  nodeId?: string
  fieldPath?: string
  fieldType?: string
}

export interface ContextFieldSelectorProps {
  value: string
  onChange: (value: string) => void
  nodes: Node[]
  currentNodeId: string
  label?: string
  description?: string
  placeholder?: string
  required?: boolean
  allowedTypes?: string[]
  className?: string
}

export function ContextFieldSelector({
  value,
  onChange,
  nodes,
  currentNodeId,
  label = "Field",
  description,
  placeholder = "Select or enter field path",
  required = false,
  allowedTypes,
  className,
}: ContextFieldSelectorProps) {
  const [open, setOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")

  // Get nodes that execute before current node (based on edges)
  const previousNodes = useMemo(() => {
    // For now, return all nodes except current node
    // In future, can be improved to only show nodes that are actually connected before current node
    return nodes.filter((node) => node.id !== currentNodeId)
  }, [nodes, currentNodeId])

  // Get trigger nodes (nodes with category "trigger")
  // Use getNodeCategory to correctly detect trigger nodes (checks both nodeType and triggerConfigId)
  const triggerNodes = useMemo(() => {
    return nodes.filter((node) => {
      const nodeType = node.data?.type as string
      const nodeConfig = (node.data as any)?.config || {}
      // Use getNodeCategory to correctly detect trigger nodes
      const nodeCategory = getNodeCategory(nodeType, nodeConfig)
      return nodeCategory === NodeTypeEnum.TRIGGER
    })
  }, [nodes])

  // Build context field options
  const contextOptions = useMemo(() => {
    const options: ContextFieldOption[] = []

    // 1. Trigger data fields - accessed via _nodeOutputs.{triggerNodeId}
    // For trigger nodes, schema is stored in node.data.config.schemas
    // Check both top-level config and nested config structure
    // Also check configTemplate if schemas not found in config
    triggerNodes.forEach((triggerNode) => {
      const triggerNodeLabel = triggerNode.data?.label || triggerNode.id
      const nodeConfig = (triggerNode.data?.config as any) || {}
      // Check both top-level and nested config for schemas
      let triggerSchemas = (nodeConfig.schemas || (nodeConfig.config as any)?.schemas || []) as SchemaDefinition[]
      
      // If schemas not found in config, try to get from configTemplate
      if (!triggerSchemas || triggerSchemas.length === 0) {
        const configTemplate = nodeConfig.configTemplate || (nodeConfig.config as any)?.configTemplate
        if (configTemplate && (configTemplate as any).schemas) {
          triggerSchemas = (configTemplate as any).schemas as SchemaDefinition[]
        }
      }
      
      // Debug logging
      if (import.meta.env.DEV) {
        console.log('[ContextFieldSelector] Trigger node schema check:', {
          nodeId: triggerNode.id,
          nodeLabel: triggerNodeLabel,
          hasSchemasInConfig: !!(nodeConfig.schemas || (nodeConfig.config as any)?.schemas),
          hasSchemasInTemplate: !!(nodeConfig.configTemplate || (nodeConfig.config as any)?.configTemplate)?.schemas,
          schemasCount: triggerSchemas?.length || 0,
          schemas: triggerSchemas,
        })
      }
      
      if (triggerSchemas && triggerSchemas.length > 0) {
        triggerSchemas.forEach((schema) => {
          if (schema.fields && schema.fields.length > 0) {
            schema.fields.forEach((field) => {
              const fieldPath = `_nodeOutputs.${triggerNode.id}.${field.name}`
              options.push({
                value: fieldPath,
                label: `${triggerNodeLabel}: ${field.name}`,
                description: `Trigger data from ${triggerNodeLabel} (ID: ${triggerNode.id}) - ${fieldPath}`,
                type: "trigger",
                nodeId: triggerNode.id,
                fieldPath: field.name,
                fieldType: field.type,
              })
            })
          }
        })
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

    // 2. Node outputs (_nodeOutputs.{nodeId}.{fieldPath}) - non-trigger nodes
    // For action/logic nodes, outputSchema is stored in node.data.config.outputSchema
    const nonTriggerNodes = previousNodes.filter((node) => {
      const nodeType = node.data?.type as string
      return !isTriggerNodeType(nodeType)
    })
    
    nonTriggerNodes.forEach((node) => {
      const nodeLabel = node.data?.label || node.id
      
      // Get output schema from node config
      // For action nodes from registry, outputSchema is stored in node.data.config.outputSchema
      const outputSchema = (node.data?.config as any)?.outputSchema as SchemaDefinition[] || []
      
      if (outputSchema && outputSchema.length > 0) {
        outputSchema.forEach((schema) => {
          if (schema.fields && schema.fields.length > 0) {
            schema.fields.forEach((field) => {
              const fieldPath = `_nodeOutputs.${node.id}.${field.name}`
              options.push({
                value: fieldPath,
                label: `${nodeLabel}: ${field.name}`,
                description: `From ${nodeLabel} node (ID: ${node.id}) - ${fieldPath}`,
                type: "node",
                nodeId: node.id,
                fieldPath: field.name,
                fieldType: field.type,
              })
            })
          }
        })
      } else {
        // Generic node output (without specific fields)
        options.push({
          value: `_nodeOutputs.${node.id}`,
          label: `${nodeLabel} (all fields)`,
          description: `All outputs from ${nodeLabel} node (ID: ${node.id}) - _nodeOutputs.${node.id}`,
          type: "node",
          nodeId: node.id,
        })
      }
    })

    // 3. Variables (global variables)
    options.push({
      value: "variables",
      label: "Variables",
      description: "Global workflow variables",
      type: "variable",
    })

    // 4. Metadata
    options.push({
      value: "_metadata",
      label: "Metadata",
      description: "Execution metadata (executionId, workflowId, etc.)",
      type: "metadata",
    })

    return options
  }, [triggerNodes, previousNodes])

  // Filter options based on search query and allowed types
  const filteredOptions = useMemo(() => {
    let filtered = contextOptions

    // Filter by search query
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      filtered = filtered.filter(
        (option) =>
          option.label.toLowerCase().includes(query) ||
          option.value.toLowerCase().includes(query) ||
          option.description?.toLowerCase().includes(query)
      )
    }

    // Filter by allowed types
    if (allowedTypes && allowedTypes.length > 0) {
      filtered = filtered.filter((option) => {
        if (!option.fieldType) return true
        return allowedTypes.includes(option.fieldType)
      })
    }

    return filtered
  }, [contextOptions, searchQuery, allowedTypes])

  // Group options by type
  const groupedOptions = useMemo(() => {
    const groups: Record<string, ContextFieldOption[]> = {
      trigger: [],
      node: [],
      variable: [],
      metadata: [],
    }

    filteredOptions.forEach((option) => {
      if (groups[option.type]) {
        groups[option.type].push(option)
      }
    })

    return groups
  }, [filteredOptions])

  const selectedOption = useMemo(() => {
    return contextOptions.find((opt) => opt.value === value)
  }, [contextOptions, value])

  const handleSelect = useCallback(
    (optionValue: string) => {
      onChange(optionValue)
      setOpen(false)
      setSearchQuery("")
    },
    [onChange]
  )

  const getTypeIcon = (type: ContextFieldOption["type"]) => {
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

  const getTypeColor = (type: ContextFieldOption["type"]) => {
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

  return (
    <div className={cn("space-y-2", className)}>
      {label && (
        <Label htmlFor="context-field-selector" className="text-sm font-medium">
          {label}
          {required && <span className="text-error-600 ml-1">*</span>}
        </Label>
      )}

      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            role="combobox"
            aria-expanded={open}
            className="w-full justify-between h-auto min-h-[2.5rem] py-2 px-3 text-left font-normal"
          >
            <div className="flex items-center gap-2 flex-1 min-w-0">
              {selectedOption ? (
                <>
                  <div className={cn("p-1 rounded", getTypeColor(selectedOption.type))}>
                    {getTypeIcon(selectedOption.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium truncate">{selectedOption.label}</div>
                    {selectedOption.description && (
                      <div className="text-xs text-secondary-500 truncate">{selectedOption.description}</div>
                    )}
                  </div>
                </>
              ) : (
                <span className="text-secondary-500">{placeholder}</span>
              )}
            </div>
            <ChevronDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-[400px] p-0 z-[10000]" align="start">
          <Command>
            <div className="flex items-center border-b px-3">
              <Search className="mr-2 h-4 w-4 shrink-0 opacity-50" />
              <CommandInput
                placeholder="Search fields..."
                value={searchQuery}
                onValueChange={setSearchQuery}
                className="border-0 focus:ring-0"
              />
            </div>
            <CommandList>
              <CommandEmpty>No fields found.</CommandEmpty>

              {/* Trigger Data */}
              {groupedOptions.trigger.length > 0 && (
                <CommandGroup heading="Trigger Data">
                  {groupedOptions.trigger.map((option) => (
                    <CommandItem
                      key={option.value}
                      value={option.value}
                      onSelect={() => handleSelect(option.value)}
                      className="cursor-pointer"
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
                        {option.fieldType && (
                          <Badge variant="outline" className="text-xs">
                            {option.fieldType}
                          </Badge>
                        )}
                        <Check
                          className={cn(
                            "ml-2 h-4 w-4",
                            value === option.value ? "opacity-100" : "opacity-0"
                          )}
                        />
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              )}

              {/* Node Outputs */}
              {groupedOptions.node.length > 0 && (
                <CommandGroup heading="Node Outputs">
                  {groupedOptions.node.map((option) => (
                    <CommandItem
                      key={option.value}
                      value={option.value}
                      onSelect={() => handleSelect(option.value)}
                      className="cursor-pointer"
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
                        {option.fieldType && (
                          <Badge variant="outline" className="text-xs">
                            {option.fieldType}
                          </Badge>
                        )}
                        <Check
                          className={cn(
                            "ml-2 h-4 w-4",
                            value === option.value ? "opacity-100" : "opacity-0"
                          )}
                        />
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              )}

              {/* Variables */}
              {groupedOptions.variable.length > 0 && (
                <CommandGroup heading="Variables">
                  {groupedOptions.variable.map((option) => (
                    <CommandItem
                      key={option.value}
                      value={option.value}
                      onSelect={() => handleSelect(option.value)}
                      className="cursor-pointer"
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
                        <Check
                          className={cn(
                            "ml-2 h-4 w-4",
                            value === option.value ? "opacity-100" : "opacity-0"
                          )}
                        />
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              )}

              {/* Metadata */}
              {groupedOptions.metadata.length > 0 && (
                <CommandGroup heading="Metadata">
                  {groupedOptions.metadata.map((option) => (
                    <CommandItem
                      key={option.value}
                      value={option.value}
                      onSelect={() => handleSelect(option.value)}
                      className="cursor-pointer"
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
                        <Check
                          className={cn(
                            "ml-2 h-4 w-4",
                            value === option.value ? "opacity-100" : "opacity-0"
                          )}
                        />
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              )}
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>

      {description && (
        <p className="text-xs text-secondary-500">{description}</p>
      )}

      {/* Manual entry input (always visible for flexibility) */}
      <div className="mt-2">
        <Input
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className="text-sm"
        />
        <p className="text-xs text-secondary-500 mt-1">
          You can also type the field path directly (e.g., <code className="text-xs bg-secondary-100 px-1 py-0.5 rounded">_nodeOutputs.nodeId.fieldName</code>)
        </p>
      </div>
    </div>
  )
}

