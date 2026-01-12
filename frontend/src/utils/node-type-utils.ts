import { NodeTypeEnum, NodeTypeEnumHelpers } from "@/types/workflow"
import type { WorkflowNode, WorkflowDefinition } from "@/types/workflow"

/**
 * Utility functions for working with node types
 * Backend enum only has 3 values: TRIGGER, LOGIC, ACTION
 * Nodes are identified by:
 * - Trigger nodes: triggerConfigId in node.data.config
 * - Action nodes: registryId in node.data.config
 * - Logic nodes: subtype in node.data.config (when re-implemented)
 */

/**
 * Validate that a node type string is valid according to backend enum
 */
export function isValidNodeType(type: string): boolean {
  return NodeTypeEnumHelpers.isValid(type)
}

/**
 * Determine node category based on nodeType enum only
 * Backend enum has 3 values: TRIGGER, LOGIC, ACTION
 * This function only checks the nodeType enum value, ignoring config IDs
 */
export function getNodeCategory(
  nodeType: string,
  config?: Record<string, unknown>
): NodeTypeEnum {
  // First check config for triggerConfigId or registryId to determine category
  // This is more reliable than nodeType alone
  if (config) {
    const triggerConfigId = config.triggerConfigId as string | undefined
    const registryId = config.registryId as string | undefined
    
    // If triggerConfigId exists, it's a trigger node
    if (triggerConfigId) {
      return NodeTypeEnum.TRIGGER
    }
    
    // If registryId exists without triggerConfigId, it's an action node
    if (registryId) {
      return NodeTypeEnum.ACTION
    }
  }
  
  // Fallback to nodeType enum value
  if (nodeType === NodeTypeEnum.TRIGGER) {
    return NodeTypeEnum.TRIGGER
  }
  if (nodeType === NodeTypeEnum.ACTION) {
    return NodeTypeEnum.ACTION
  }
  if (nodeType === NodeTypeEnum.LOGIC) {
    return NodeTypeEnum.LOGIC
  }
  
  // Default fallback to ACTION if nodeType doesn't match any enum value
  return NodeTypeEnum.ACTION
}

/**
 * Check if a node is a trigger node
 */
export function isTriggerNode(
  nodeType: string,
  config?: Record<string, unknown>
): boolean {
  return getNodeCategory(nodeType, config) === NodeTypeEnum.TRIGGER
}

/**
 * Check if a node is an action node
 */
export function isActionNode(
  nodeType: string,
  config?: Record<string, unknown>
): boolean {
  return getNodeCategory(nodeType, config) === NodeTypeEnum.ACTION
}

/**
 * Check if a node is a logic node
 */
export function isLogicNode(
  nodeType: string,
  config?: Record<string, unknown>
): boolean {
  return getNodeCategory(nodeType, config) === NodeTypeEnum.LOGIC
}

/**
 * Check if a node type string is a trigger type (legacy - use isTriggerNode instead)
 * This is kept for backward compatibility with some components
 */
export function isTriggerNodeType(type: string): boolean {
  return type === NodeTypeEnum.TRIGGER
}

/**
 * Validate and normalize a workflow node to ensure it's compatible with backend
 * Ensures node.type is one of: TRIGGER, LOGIC, ACTION
 */
export function normalizeWorkflowNode(node: WorkflowNode): WorkflowNode {
  // Validate node type
  if (!isValidNodeType(node.type)) {
    console.warn(`Invalid node type: ${node.type}. Defaulting to ACTION.`)
    return {
      ...node,
      type: NodeTypeEnum.ACTION,
    }
  }
  
  // Ensure node.type is a valid enum value
  const nodeData = node.data as any
  const config = { ...(nodeData?.config || {}) }
  const triggerConfigId = config.triggerConfigId as string | undefined
  const registryId = config.registryId as string | undefined
  
  // Determine correct node type based on config
  let correctType: NodeTypeEnum
  if (triggerConfigId) {
    correctType = NodeTypeEnum.TRIGGER
  } else if (registryId) {
    correctType = NodeTypeEnum.ACTION
  } else {
    // Use node.type if it's valid, otherwise default to ACTION
    correctType = isValidNodeType(node.type) ? (node.type as NodeTypeEnum) : NodeTypeEnum.ACTION
  }
  
  // Ensure config exists (preserve existing config)
  const normalizedConfig = { ...config }
  
  // For nodes with registryId/triggerConfigId, ensure they're in config
  if (triggerConfigId) {
    normalizedConfig.triggerConfigId = triggerConfigId
  }
  if (registryId) {
    normalizedConfig.registryId = registryId
  }
  
  return {
    ...node,
    type: correctType,
    data: {
      ...nodeData,
      config: Object.keys(normalizedConfig).length > 0 ? normalizedConfig : undefined,
    },
  }
}

/**
 * Validate and normalize a workflow definition to ensure all nodes are compatible with backend
 */
export function normalizeWorkflowDefinition(definition: WorkflowDefinition): WorkflowDefinition {
  return {
    ...definition,
    nodes: definition.nodes.map(normalizeWorkflowNode),
  }
}

/**
 * Validate a workflow definition before sending to backend
 */
export function validateWorkflowDefinition(definition: WorkflowDefinition): {
  isValid: boolean
  errors: string[]
} {
  const errors: string[] = []
  
  definition.nodes.forEach((node, index) => {
    if (!isValidNodeType(node.type)) {
      errors.push(`Node ${index + 1} (${node.id}): Invalid node type "${node.type}"`)
    }
  })
  
  return {
    isValid: errors.length === 0,
    errors,
  }
}

/**
 * Convert backend enum value back to frontend (no conversion needed, they're the same)
 * Used when loading workflow from backend
 */
export function convertFromBackendNodeType(backendType: string): NodeTypeEnum {
  if (backendType === NodeTypeEnum.TRIGGER) return NodeTypeEnum.TRIGGER
  if (backendType === NodeTypeEnum.ACTION) return NodeTypeEnum.ACTION
  if (backendType === NodeTypeEnum.LOGIC) return NodeTypeEnum.LOGIC
  
  // Default to ACTION if unknown
  console.warn(`Unknown backend node type: ${backendType}, defaulting to ACTION`)
  return NodeTypeEnum.ACTION
}

/**
 * Denormalize a workflow node loaded from backend
 * Preserves triggerConfigId and registryId from config
 */
export function denormalizeWorkflowNode(node: WorkflowNode): WorkflowNode {
  const backendType = node.type
  
  // Convert back to frontend type (no conversion needed, they're the same)
  const frontendType = convertFromBackendNodeType(backendType)
  
  // Preserve config
  const config = { ...node.data.config }
  
  // Ensure config is preserved even if empty (for nodes with triggerConfigId/registryId)
  const hasRegistryFields = config.triggerConfigId || config.registryId
  
  return {
    ...node,
    type: frontendType,
    data: {
      ...node.data,
      config: (Object.keys(config).length > 0 || hasRegistryFields) ? config : undefined,
    },
  }
}
