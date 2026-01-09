import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"
import { cn } from "@/lib/utils"
import type { WorkflowNodeType } from "@/types/workflow"
import { NODE_DEFINITIONS, NODE_ICONS } from "@/constants/workflow-nodes"
import { parseFieldReference, formatFieldReference, getFieldDisplayName } from "@/utils/fieldReferenceUtils"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { useContext } from "react"

// Import context directly (will be null if provider not available)
let FieldReferenceContext: React.Context<any> | null = null
try {
  const contextModule = require("@/providers/FieldReferenceContext")
  FieldReferenceContext = contextModule.FieldReferenceContext
} catch {
  // Context not available
}

interface CustomNodeData {
  label: string
  type: WorkflowNodeType
  config?: Record<string, unknown>
  invalid?: boolean
}

export const WorkflowNode = memo(function WorkflowNode({ data, selected, id }: NodeProps<CustomNodeData>) {
  const nodeDef = NODE_DEFINITIONS.find((n) => n.type === data.type)
  
  // Try to get field reference context (optional)
  let getFieldDefinition: (objectTypeId: string, fieldPath: string) => any = () => null
  if (FieldReferenceContext) {
    try {
      const context = useContext(FieldReferenceContext)
      if (context) {
        getFieldDefinition = context.getFieldDefinition || (() => null)
      }
    } catch {
      // Context not available
    }
  }

  if (!nodeDef) {
    return null
  }

  const IconComponent = NODE_ICONS[nodeDef.icon]

  // Get field reference display info
  const getFieldDisplayInfo = () => {
    const config = data.config || {}
    let fieldValue: string | null = null
    let fieldDisplayName: string | null = null
    let fieldTooltip: string | null = null

    switch (data.type) {
      case "condition":
        if (config.field) {
          fieldValue = typeof config.field === "string" ? config.field : formatFieldReference(config.field)
          const parsed = parseFieldReference(config.field)
          if (parsed?.objectTypeId) {
            fieldDisplayName = getFieldDisplayName(parsed.objectTypeId, parsed.fieldPath)
            const fieldDef = getFieldDefinition(parsed.objectTypeId, parsed.fieldPath)
            if (fieldDef) {
              fieldTooltip = fieldDef.description || `${parsed.objectTypeId}.${parsed.fieldPath} (${fieldDef.type})`
            }
          }
        }
        break
      case "transform":
        if (config.sourceField) {
          fieldValue = typeof config.sourceField === "string" ? config.sourceField : formatFieldReference(config.sourceField)
        }
        break
      case "filter":
        if (config.field) {
          fieldValue = typeof config.field === "string" ? config.field : formatFieldReference(config.field)
        }
        break
    }

    return { fieldValue, fieldDisplayName, fieldTooltip }
  }

  const { fieldValue, fieldDisplayName, fieldTooltip } = getFieldDisplayInfo()

  return (
    <div
      className={cn(
        "px-4 py-3 shadow-md rounded-lg border-2 min-w-[180px] bg-white",
        selected
          ? "border-primary-600 shadow-lg"
          : data.invalid
          ? "border-error-500 bg-error-50"
          : "border-secondary-300 hover:border-secondary-400"
      )}
    >
      {/* Input Handles */}
      {nodeDef.inputs > 0 && (
        <Handle
          type="target"
          position={Position.Top}
          className="w-3 h-3 bg-secondary-400"
        />
      )}

      {/* Node Content */}
      <div className="flex items-center space-x-3">
        {IconComponent && (
          <IconComponent
            className="flex-shrink-0"
            size={24}
            style={{ color: nodeDef.color }}
          />
        )}
        <div className="flex-1 min-w-0">
          <div className="text-[10px] font-bold mb-1 font-mono" style={{ color: nodeDef.color }}>
            {id}
          </div>
          <div className="font-semibold text-sm text-secondary-900">
            {data.label || nodeDef.label}
          </div>
          <div className="text-xs text-secondary-500 mt-0.5">
            {nodeDef.description}
          </div>
          {fieldValue && (
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div className="text-xs text-primary-600 mt-1 truncate">
                    {fieldDisplayName || fieldValue}
                  </div>
                </TooltipTrigger>
                {fieldTooltip && (
                  <TooltipContent>
                    <p>{fieldTooltip}</p>
                    {fieldValue !== fieldDisplayName && <p className="text-xs mt-1 opacity-75">{fieldValue}</p>}
                  </TooltipContent>
                )}
              </Tooltip>
            </TooltipProvider>
          )}
        </div>
      </div>

      {/* Output Handles */}
      {nodeDef.outputs === 1 && (
        <Handle
          type="source"
          position={Position.Bottom}
          className="w-3 h-3 bg-secondary-400"
        />
      )}
      {nodeDef.outputs === 2 && (
        <div className="relative">
          <Handle
            type="source"
            position={Position.Bottom}
            id="true"
            className="w-3 h-3 bg-success-500"
            style={{ left: '30%' }}
          />
          <Handle
            type="source"
            position={Position.Bottom}
            id="false"
            className="w-3 h-3 bg-error-500"
            style={{ right: '30%' }}
          />
          <div className="absolute -bottom-5 left-0 right-0 flex justify-between px-2 pointer-events-none">
            <span className="text-xs text-success-700 font-medium bg-white px-1 rounded">
              {data.config?.trueLabel || "True"}
            </span>
            <span className="text-xs text-error-700 font-medium bg-white px-1 rounded">
              {data.config?.falseLabel || "False"}
            </span>
          </div>
        </div>
      )}
      {nodeDef.outputs > 2 && (
        <div className="flex justify-center space-x-2 mt-2">
          {Array.from({ length: nodeDef.outputs }).map((_, i) => (
            <Handle
              key={i}
              type="source"
              position={Position.Bottom}
              id={`output-${i}`}
              className="w-3 h-3 bg-secondary-400"
              style={{ left: `${(i + 1) * (100 / (nodeDef.outputs + 1))}%` }}
            />
          ))}
        </div>
      )}
    </div>
  )
})

