import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"
import { cn } from "@/lib/utils"
import { NodeTypeEnum } from "@/types/workflow"
import { NODE_DEFINITIONS, NODE_ICONS } from "@/constants/workflow-nodes"
import { parseFieldReference, formatFieldReference, getFieldDisplayName } from "@/utils/fieldReferenceUtils"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { useContext } from "react"
import { useActionRegistryById } from "@/hooks/use-action-registry"
import { useTriggerRegistryById } from "@/hooks/use-trigger-registry"
import { getNodeCategory } from "@/utils/node-type-utils"

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
  type: NodeTypeEnum
  config?: Record<string, unknown>
  invalid?: boolean
}

interface CustomNodeDataWithRegistry extends CustomNodeData {
  registryId?: string
  configTemplate?: Record<string, unknown>
}

export const WorkflowNode = memo(function WorkflowNode({ data, selected, id }: NodeProps<CustomNodeDataWithRegistry>) {
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

  // Check if this is a trigger or action node with registryId/triggerConfigId
  const nodeConfig = (data as any)?.config || {}
  const registryId = (data as any)?.registryId || nodeConfig.registryId
  const triggerConfigId = nodeConfig.triggerConfigId
  const nodeType = data.type || ''
  
  // Use helper function to correctly detect node category
  // This follows backend enum NodeType (TRIGGER, LOGIC, ACTION)
  const nodeCategory = getNodeCategory(nodeType, nodeConfig)
  const isTrigger = nodeCategory === NodeTypeEnum.TRIGGER
  const isAction = nodeCategory === NodeTypeEnum.ACTION
  
  // For triggers, use triggerConfigId if available, otherwise use registryId
  // IMPORTANT: Only set triggerId for actual trigger nodes, NOT for action nodes
  // Action nodes have their own registry and should NOT call trigger API
  // CRITICAL: Only use registryId for triggerId if this is actually a trigger node
  // Do NOT use registryId for action nodes - they have their own API endpoint
  const triggerId = isTrigger ? (triggerConfigId || registryId) : undefined
  
  // Debug logging in development
  if (import.meta.env.DEV && registryId) {
    console.log('[WorkflowNode] Node registry lookup:', {
      nodeId: id,
      nodeType,
      registryId,
      triggerConfigId,
      isTrigger,
      isAction,
      triggerId,
      willCallTriggerAPI: !!triggerId,
      willCallActionAPI: isAction,
    })
  }
  
  // Load registry data if needed
  // Only call trigger API if this is actually a trigger node
  const { data: triggerRegistryItem } = useTriggerRegistryById(triggerId)
  // Only call action API if this is actually an action node
  const { data: actionRegistryItem } = useActionRegistryById(isAction ? registryId : undefined)
  
  // Find node definition from NODE_DEFINITIONS first
  let nodeDef = NODE_DEFINITIONS.find((n) => n.type === data.type)
  
  // If not found and this is a registry node, create a default nodeDef from registry data
  if (!nodeDef && (registryId || triggerConfigId)) {
    const registryItem = isTrigger ? triggerRegistryItem : actionRegistryItem
    if (registryItem) {
      nodeDef = {
        type: data.type,
        category: isTrigger ? 'trigger' : 'action',
        label: registryItem.name || data.label || data.type,
        description: registryItem.description || '',
        icon: (registryItem.metadata?.icon as string) || 'api-call',
        color: (registryItem.metadata?.color as string) || '#22c55e',
        inputs: isTrigger ? 0 : 1,
        outputs: 1,
      }
    }
  }
  
  // If still not found, create a minimal default nodeDef
  // CRITICAL: Use nodeCategory (isTrigger/isAction) to determine inputs
  if (!nodeDef) {
    nodeDef = {
      type: data.type,
      category: isTrigger ? 'trigger' : (isAction ? 'action' : 'logic'),
      label: data.label || data.type,
      description: '',
      icon: 'api-call',
      color: '#22c55e',
      inputs: isTrigger ? 0 : 1, // Trigger nodes have 0 inputs, others have 1
      outputs: 1,
    }
  }
  
  // Debug logging for nodeDef inputs
  if (import.meta.env.DEV) {
    console.log('[WorkflowNode] Node definition:', {
      nodeId: id,
      nodeType,
      nodeCategory,
      isTrigger,
      isAction,
      nodeDefInputs: nodeDef.inputs,
      nodeDefOutputs: nodeDef.outputs,
      hasTriggerConfigId: !!triggerConfigId,
      hasRegistryId: !!registryId,
    })
  }

  const IconComponent = NODE_ICONS[nodeDef.icon]

  // Get field reference display info
  const getFieldDisplayInfo = () => {
    const config = data.config || {}
    let fieldValue: string | null = null
    let fieldDisplayName: string | null = null
    let fieldTooltip: string | null = null

    // Check subtype in config instead of nodeType (legacy)
    const nodeSubtype = config?.subtype as string | undefined
    const nodeCategory = getNodeCategory(data.type, config)
    
    // Only handle logic nodes with condition subtype
    if (nodeCategory === NodeTypeEnum.LOGIC && nodeSubtype === "condition") {
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

