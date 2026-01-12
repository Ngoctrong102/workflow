import { useState, useEffect, useMemo, useRef } from "react"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { ContextFieldSelector } from "./ContextFieldSelector"
import type { Node } from "reactflow"

export type FieldSourceType = "previous-node" | "variables" | "static"

export interface FieldMapping {
  source: string // e.g., "_nodeOutputs.nodeId.fieldName" or "_triggerData.fieldName" or "_variables.varName" or static value
  type: string
}

export interface FieldSourceSelectorProps {
  value: FieldMapping | string | null | undefined
  onChange: (value: FieldMapping | string) => void
  fieldType: string
  required?: boolean
  label: string
  description?: string
  nodes: Node[] // Previous nodes for "From Previous Node" (includes trigger nodes)
  currentNodeId?: string
  variables?: string[] // Available variables
  className?: string
}

export function FieldSourceSelector({
  value,
  onChange,
  fieldType,
  required = false,
  label,
  description,
  nodes,
  currentNodeId,
  variables = [],
  className,
}: FieldSourceSelectorProps) {
  const [sourceType, setSourceType] = useState<FieldSourceType>("static")
  const [staticValue, setStaticValue] = useState<string>("")
  const isInternalChange = useRef(false)
  const sourceTypeRef = useRef<FieldSourceType>("static")
  
  // Keep ref in sync with state
  useEffect(() => {
    sourceTypeRef.current = sourceType
  }, [sourceType])

  // Parse initial value to determine source type
  // This effect should only update sourceType when value comes from outside (not from our own onChange)
  useEffect(() => {
    // Skip if this change was triggered internally
    if (isInternalChange.current) {
      isInternalChange.current = false
      return
    }

    // If value is null/undefined, check current sourceType
    // Don't reset to static if user is in the middle of selecting a field from previous node or variables
    if (!value) {
      // Only reset to static if we're already in static mode
      // This prevents resetting when user is selecting "From Previous Node" but hasn't chosen a field yet
      if (sourceTypeRef.current === "static") {
        setStaticValue("")
      }
      // Don't change sourceType if user is in non-static mode (previous-node or variables)
      // This allows user to continue selecting a field even if parent resets value to null
      return
    }

    const valueStr = typeof value === "string" ? value : value.source

    // If source is empty string and we have a FieldMapping object, 
    // it means user just selected a source type - don't override
    if (typeof value === "object" && value.source === "") {
      // Keep current sourceType - don't reset
      return
    }

    // Parse the actual value
    if (valueStr && valueStr.startsWith("_nodeOutputs.")) {
      setSourceType("previous-node")
    } else if (valueStr && valueStr.startsWith("_variables.")) {
      setSourceType("variables")
    } else if (valueStr) {
      // Only set to static if we have an actual string value
      setSourceType("static")
      setStaticValue(valueStr)
    }
    // If valueStr is empty, don't change sourceType - keep current state
  }, [value])

  // Debug: Log sourceType changes
  useEffect(() => {
    if (import.meta.env.DEV) {
      console.log('[FieldSourceSelector] sourceType state changed:', {
        sourceType,
        hasValue: !!value,
        valueType: typeof value,
        valueSource: typeof value === "object" && value ? value.source : value,
      })
    }
  }, [sourceType, value])

  // Get previous nodes (nodes that can be referenced)
  const previousNodes = useMemo(() => {
    // Filter out current node - show "From Previous Node" option if there are any previous nodes
    // The ContextFieldSelector will handle showing available fields from those nodes
    const filtered = nodes.filter((node) => {
      if (node.id === currentNodeId) return false
      
      // Include all previous nodes - schema availability will be checked in ContextFieldSelector
      // This allows the option to be visible even if schemas aren't loaded yet
      return true
    })
    
    // Debug logging
    if (import.meta.env.DEV) {
      console.log('[FieldSourceSelector] Previous nodes calculation:', {
        totalNodes: nodes.length,
        currentNodeId,
        previousNodesCount: filtered.length,
        previousNodeIds: filtered.map(n => n.id),
      })
    }
    
    return filtered
  }, [nodes, currentNodeId])

  const handleSourceTypeChange = (newSourceType: string) => {
    const typedSourceType = newSourceType as FieldSourceType
    
    // Debug logging before change
    if (import.meta.env.DEV) {
      console.log('[FieldSourceSelector] Source type change requested:', {
        from: sourceType,
        to: typedSourceType,
        previousNodes: previousNodes.length,
        currentNodeId,
      })
    }
    
    setSourceType(typedSourceType)
    
    // Mark this as an internal change to prevent useEffect from overriding
    isInternalChange.current = true
    
    // Reset value when switching source types
    if (typedSourceType === "static") {
      setStaticValue("")
      onChange("")
    } else {
      onChange({ source: "", type: fieldType })
    }
    
    // Debug logging after change
    if (import.meta.env.DEV) {
      console.log('[FieldSourceSelector] Source type changed successfully:', {
        newSourceType: typedSourceType,
        isInternalChange: isInternalChange.current,
      })
    }
  }

  const handleStaticValueChange = (newValue: string) => {
    setStaticValue(newValue)
    onChange(newValue)
  }

  const handleContextFieldChange = (fieldPath: string) => {
    onChange({
      source: fieldPath,
      type: fieldType,
    })
  }

  const handleVariableChange = (variableName: string) => {
    onChange({
      source: `_variables.${variableName}`,
      type: fieldType,
    })
  }

  // Render value display (only show for non-static sources or when static value is already set and not currently editing)
  const renderValueDisplay = () => {
    if (!value) return null

    const valueStr = typeof value === "string" ? value : value.source
    
    // Don't show badge for static values when currently in static mode (user is editing)
    if (sourceType === "static" && valueStr === staticValue) {
      return null
    }
    
    if (valueStr.startsWith("_nodeOutputs.")) {
      const parts = valueStr.split(".")
      const nodeId = parts[1]
      const fieldPath = parts.slice(2).join(".")
      const node = nodes.find((n) => n.id === nodeId)
      return (
        <Badge variant="secondary" className="mt-2">
          From: {node?.data?.label || nodeId} → {fieldPath}
        </Badge>
      )
    } else if (valueStr.startsWith("_variables.")) {
      const varName = valueStr.replace("_variables.", "")
      return (
        <Badge variant="secondary" className="mt-2">
          Variable: {varName}
        </Badge>
      )
    }
    // Don't show "Static:" badge for static values - the input field itself is enough
    return null
  }

  return (
    <div className={className}>
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor={`source-type-${label}`}>
            {label}
            {required && <span className="text-error-600 ml-1">*</span>}
          </Label>
          <Badge variant="outline" className="text-xs">
            {fieldType}
          </Badge>
        </div>
        
        {description && (
          <p className="text-xs text-secondary-500">{description}</p>
        )}

        <Select 
          value={sourceType} 
          onValueChange={handleSourceTypeChange}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select data source" />
          </SelectTrigger>
          <SelectContent>
            {previousNodes.length > 0 && (
              <SelectItem value="previous-node">From Previous Node</SelectItem>
            )}
            {variables.length > 0 && (
              <SelectItem value="variables">From Variables</SelectItem>
            )}
            <SelectItem value="static">Static Value</SelectItem>
          </SelectContent>
        </Select>

        {sourceType === "previous-node" && (
          <div className="mt-2">
            <ContextFieldSelector
              value={value ? (typeof value === "string" ? value : value.source) : ""}
              onChange={handleContextFieldChange}
              nodes={nodes}
              currentNodeId={currentNodeId || ""}
              label="Select Field"
              placeholder="Select field from previous node"
              required={required}
            />
          </div>
        )}

        {sourceType === "variables" && variables.length > 0 && (
          <div className="mt-2">
            <Select
              value={
                value
                  ? (typeof value === "string"
                      ? value.replace("_variables.", "")
                      : value.source.replace("_variables.", ""))
                  : ""
              }
              onValueChange={handleVariableChange}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select variable" />
              </SelectTrigger>
              <SelectContent>
                {variables.map((variable) => (
                  <SelectItem key={variable} value={variable}>
                    {variable}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}

        {sourceType === "static" && (
          <div className="mt-2">
            {fieldType === "string" || fieldType === "email" || fieldType === "phone" || fieldType === "url" ? (
              <Input
                value={staticValue}
                onChange={(e) => handleStaticValueChange(e.target.value)}
                placeholder={`Enter ${fieldType} value`}
                required={required}
              />
            ) : fieldType === "number" ? (
              <Input
                type="number"
                value={staticValue}
                onChange={(e) => handleStaticValueChange(e.target.value)}
                placeholder="Enter number value"
                required={required}
              />
            ) : fieldType === "boolean" ? (
              <Select value={staticValue} onValueChange={handleStaticValueChange}>
                <SelectTrigger>
                  <SelectValue placeholder="Select boolean value" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="true">True</SelectItem>
                  <SelectItem value="false">False</SelectItem>
                </SelectContent>
              </Select>
            ) : fieldType === "json" || fieldType === "object" ? (
              <Textarea
                value={staticValue}
                onChange={(e) => handleStaticValueChange(e.target.value)}
                placeholder='Enter JSON value, e.g., {"key": "value"}'
                rows={4}
                className="font-mono text-sm"
                required={required}
              />
            ) : (
              <Input
                value={staticValue}
                onChange={(e) => handleStaticValueChange(e.target.value)}
                placeholder={`Enter ${fieldType} value`}
                required={required}
              />
            )}
          </div>
        )}

        {renderValueDisplay()}
      </div>
    </div>
  )
}

