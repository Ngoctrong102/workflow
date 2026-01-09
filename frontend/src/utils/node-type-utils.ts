import { NodeTypeEnum, type WorkflowNodeType } from "@/types/workflow"
import type { WorkflowNode, WorkflowDefinition } from "@/types/workflow"

/**
 * Utility functions for working with node types and ensuring compatibility with backend enum
 */

/**
 * Validate that a node type string is valid according to backend enum
 * Backend uses enum NodeType for validation, but node.type in definition is still string (kebab-case)
 */
export function isValidNodeType(type: string): boolean {
  // Check if it's a valid WorkflowNodeType (specific types like "api-trigger", "send-email", etc.)
  // These are the actual node types used in workflow definitions
  const validTypes: WorkflowNodeType[] = [
    // Trigger types
    "api-trigger",
    "schedule-trigger",
    "file-trigger",
    "event-trigger",
    // Action types
    "send-email",
    "send-sms",
    "send-push",
    "send-in-app",
    "send-slack",
    "send-discord",
    "send-teams",
    "send-webhook",
    // Logic types
    "condition",
    "switch",
    "loop",
    "delay",
    "merge",
    "ab-test",
    "wait-events",
    // Data types
    "map",
    "filter",
    "transform",
    "read-file",
  ]
  
  return validTypes.includes(type as WorkflowNodeType)
}

/**
 * Get the category enum for a node type
 * Maps specific node types to their category enum (TRIGGER, ACTION, LOGIC, DATA, WAIT_EVENTS)
 */
export function getNodeCategoryEnum(type: WorkflowNodeType): NodeTypeEnum | null {
  // Trigger types
  if (type === "api-trigger") return NodeTypeEnum.API_TRIGGER
  if (type === "schedule-trigger") return NodeTypeEnum.SCHEDULE_TRIGGER
  if (type === "file-trigger") return NodeTypeEnum.FILE_TRIGGER
  if (type === "event-trigger") return NodeTypeEnum.EVENT_TRIGGER
  
  // Action types
  if (type.startsWith("send-")) return NodeTypeEnum.ACTION
  
  // Logic types
  if (["condition", "switch", "loop", "delay", "merge", "ab-test"].includes(type)) {
    return NodeTypeEnum.LOGIC
  }
  if (type === "wait-events") return NodeTypeEnum.WAIT_EVENTS
  
  // Data types
  if (["map", "filter", "transform", "read-file"].includes(type)) {
    return NodeTypeEnum.DATA
  }
  
  return null
}

/**
 * Convert specific node type (e.g., "send-webhook", "send-email") to backend enum value (e.g., "ACTION")
 * Backend expects node.type to be enum value, and subtype to be in node.data.config.subtype
 */
export function convertToBackendNodeType(type: WorkflowNodeType): string {
  // Trigger types - map to specific enum values
  if (type === "api-trigger") return "API_TRIGGER"
  if (type === "schedule-trigger") return "SCHEDULE_TRIGGER"
  if (type === "file-trigger") return "FILE_TRIGGER"
  if (type === "event-trigger") return "EVENT_TRIGGER"
  
  // Action types - all map to ACTION
  if (type.startsWith("send-")) return "ACTION"
  
  // Logic types - map to LOGIC
  if (["condition", "switch", "loop", "delay", "merge", "ab-test"].includes(type)) {
    return "LOGIC"
  }
  
  // Wait events - specific enum
  if (type === "wait-events") return "WAIT_EVENTS"
  
  // Data types - map to DATA
  if (["map", "filter", "transform", "read-file"].includes(type)) {
    return "DATA"
  }
  
  // Fallback - return as-is (should not happen if isValidNodeType is used)
  console.warn(`Unknown node type: ${type}, returning as-is`)
  return type.toUpperCase().replace(/-/g, "_")
}

/**
 * Validate and normalize a workflow node to ensure it's compatible with backend
 * Converts node.type from specific type (e.g., "send-webhook") to enum value (e.g., "ACTION")
 * and stores the original type in node.data.config.subtype
 */
export function normalizeWorkflowNode(node: WorkflowNode): WorkflowNode & { type: string } {
  // Validate node type
  if (!isValidNodeType(node.type)) {
    console.warn(`Invalid node type: ${node.type}. Keeping as-is but may cause backend validation errors.`)
    return node as WorkflowNode & { type: string }
  }
  
  // Convert to backend enum value
  const backendNodeType = convertToBackendNodeType(node.type)
  
  // Ensure config exists
  const config = node.data.config || {}
  
  // Store original type as subtype in config (backend uses this)
  // Convert kebab-case to snake_case for backend (e.g., "send-webhook" -> "send_webhook")
  const subtype = node.type.replace(/-/g, "_")
  
  // Only set subtype if it's different from the enum value (i.e., for specific types)
  // For example, "send-webhook" needs subtype, but "ACTION" enum doesn't need it
  const needsSubtype = node.type !== backendNodeType.toLowerCase().replace(/_/g, "-")
  
  return {
    ...node,
    type: backendNodeType, // Backend expects enum string value like "ACTION", "LOGIC", etc.
    data: {
      ...node.data,
      config: needsSubtype ? { ...config, subtype } : config,
    },
  } as WorkflowNode & { type: string }
}

/**
 * Validate and normalize a workflow definition to ensure all nodes are compatible with backend
 * Returns a definition where node.type is backend enum value (e.g., "ACTION") instead of specific type (e.g., "send-webhook")
 */
export function normalizeWorkflowDefinition(definition: WorkflowDefinition): WorkflowDefinition & {
  nodes: Array<WorkflowNode & { type: string }>
} {
  return {
    ...definition,
    nodes: definition.nodes.map(normalizeWorkflowNode),
  } as WorkflowDefinition & {
    nodes: Array<WorkflowNode & { type: string }>
  }
}

/**
 * Validate a workflow definition before sending to backend
 * Returns validation errors if any nodes have invalid types
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
 * Check if a node type is a trigger type
 */
export function isTriggerNodeType(type: string): boolean {
  return type === "api-trigger" || 
         type === "event-trigger" || 
         type === "schedule-trigger" || 
         type === "file-trigger"
}

/**
 * Check if a node type is an action type
 */
export function isActionNodeType(type: string): boolean {
  return type.startsWith("send-")
}

/**
 * Check if a node type is a logic type
 */
export function isLogicNodeType(type: string): boolean {
  return ["condition", "switch", "loop", "delay", "merge", "ab-test", "wait-events"].includes(type)
}

/**
 * Check if a node type is a data type
 */
export function isDataNodeType(type: string): boolean {
  return ["map", "filter", "transform", "read-file"].includes(type)
}

/**
 * Convert backend enum value + subtype back to frontend specific node type
 * Used when loading workflow from backend
 * Example: "ACTION" + subtype "send_webhook" -> "send-webhook"
 */
export function convertFromBackendNodeType(
  backendType: string,
  subtype?: string
): WorkflowNodeType {
  // If subtype exists, use it (convert snake_case to kebab-case)
  if (subtype) {
    const kebabCaseSubtype = subtype.replace(/_/g, "-") as WorkflowNodeType
    if (isValidNodeType(kebabCaseSubtype)) {
      return kebabCaseSubtype
    }
  }
  
  // Map enum values back to specific types
  const upperType = backendType.toUpperCase()
  
  if (upperType === "API_TRIGGER") return "api-trigger"
  if (upperType === "SCHEDULE_TRIGGER") return "schedule-trigger"
  if (upperType === "FILE_TRIGGER") return "file-trigger"
  if (upperType === "EVENT_TRIGGER") return "event-trigger"
  if (upperType === "WAIT_EVENTS") return "wait-events"
  
  // For ACTION, LOGIC, DATA - we need subtype to know the specific type
  // If no subtype, return a default (this shouldn't happen in practice)
  if (upperType === "ACTION") {
    console.warn(`ACTION node without subtype, defaulting to "send-email"`)
    return "send-email" // Default fallback
  }
  if (upperType === "LOGIC") {
    console.warn(`LOGIC node without subtype, defaulting to "condition"`)
    return "condition" // Default fallback
  }
  if (upperType === "DATA") {
    console.warn(`DATA node without subtype, defaulting to "map"`)
    return "map" // Default fallback
  }
  
  // Fallback - try to convert enum name to kebab-case
  return backendType.toLowerCase().replace(/_/g, "-") as WorkflowNodeType
}

/**
 * Denormalize a workflow node loaded from backend
 * Converts backend enum value (e.g., "ACTION") + subtype back to frontend specific type (e.g., "send-webhook")
 */
export function denormalizeWorkflowNode(node: WorkflowNode): WorkflowNode {
  const backendType = node.type
  const subtype = node.data.config?.subtype as string | undefined
  
  // Convert back to frontend specific type
  const frontendType = convertFromBackendNodeType(backendType, subtype)
  
  // Remove subtype from config if it exists (we don't need it in frontend)
  const config = { ...node.data.config }
  if (config.subtype) {
    delete config.subtype
  }
  
  return {
    ...node,
    type: frontendType,
    data: {
      ...node.data,
      config: Object.keys(config).length > 0 ? config : undefined,
    },
  }
}

