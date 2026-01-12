// Backend NodeType Enum - matches backend enum exactly
// Using const object instead of enum for erasableSyntaxOnly compatibility
// Backend enum only has 3 values: TRIGGER, LOGIC, ACTION
// Subtypes (e.g., api-call, scheduler, condition) are stored in node.data.config.subtype
// or node.data.config.registryId/triggerConfigId, not as separate enum values
export const NodeTypeEnum = {
  TRIGGER: "TRIGGER",
  LOGIC: "LOGIC",
  ACTION: "ACTION",
} as const

export type NodeTypeEnum = typeof NodeTypeEnum[keyof typeof NodeTypeEnum]

// Helper functions for NodeTypeEnum
export const NodeTypeEnumHelpers = {
  /**
   * Check if a string value is a valid node type enum
   */
  isValid: (value: string | null | undefined): boolean => {
    if (!value) return false
    return value === NodeTypeEnum.TRIGGER || 
           value === NodeTypeEnum.LOGIC || 
           value === NodeTypeEnum.ACTION
  },
}

// Node identification:
// - Trigger nodes: identified by triggerConfigId in node.data.config
// - Action nodes: identified by registryId in node.data.config
// - Logic nodes: identified by subtype in node.data.config (when re-implemented)

export interface WorkflowNode {
  id: string
  type: NodeTypeEnum // Only TRIGGER, LOGIC, or ACTION
  position: { x: number; y: number }
  data: {
    label: string
    config?: Record<string, unknown> // Contains triggerConfigId, registryId, subtype, etc.
  }
}

export interface WorkflowEdge {
  id: string
  source: string
  target: string
  sourceHandle?: string
  targetHandle?: string
}

export interface WorkflowDefinition {
  id?: string
  name: string
  description?: string
  status?: "draft" | "active" | "inactive" | "paused" | "archived"
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
}

export interface NodeDefinition {
  type: NodeTypeEnum // Only TRIGGER, LOGIC, or ACTION
  label: string
  description: string
  icon: string
  color: string
  inputs: number
  outputs: number
  registryId?: string // Registry ID for actions/triggers from registry
  configTemplate?: Record<string, unknown> // Config template from registry
}

