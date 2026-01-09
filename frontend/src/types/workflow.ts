// Backend NodeType Enum - matches backend enum
// Using const object instead of enum for erasableSyntaxOnly compatibility
export const NodeTypeEnum = {
  TRIGGER: "TRIGGER",
  API_TRIGGER: "API_TRIGGER",
  SCHEDULE_TRIGGER: "SCHEDULE_TRIGGER",
  FILE_TRIGGER: "FILE_TRIGGER",
  EVENT_TRIGGER: "EVENT_TRIGGER",
  ACTION: "ACTION",
  LOGIC: "LOGIC",
  DATA: "DATA",
  WAIT_EVENTS: "WAIT_EVENTS",
} as const

export type NodeTypeEnum = typeof NodeTypeEnum[keyof typeof NodeTypeEnum]

// Helper functions to convert between enum and string format (kebab-case)
// Backend uses toValue() to convert enum to kebab-case string
export const NodeTypeEnumHelpers = {
  /**
   * Convert enum to string value (kebab-case) - matches backend toValue()
   * Example: API_TRIGGER -> "api-trigger", WAIT_EVENTS -> "wait-events"
   */
  toValue: (enumValue: NodeTypeEnum): string => {
    return enumValue.toLowerCase().replace(/_/g, "-")
  },

  /**
   * Convert string value to enum - matches backend fromString()
   * Example: "api-trigger" -> API_TRIGGER, "wait-events" -> WAIT_EVENTS
   */
  fromString: (value: string | null | undefined): NodeTypeEnum | null => {
    if (!value) return null
    try {
      // Convert "api-trigger" -> "API_TRIGGER"
      const enumName = value.toUpperCase().replace(/-/g, "_")
      return NodeTypeEnum[enumName as keyof typeof NodeTypeEnum] || null
    } catch {
      return null
    }
  },

  /**
   * Check if a string value is a valid node type enum
   */
  isValid: (value: string | null | undefined): boolean => {
    return NodeTypeEnumHelpers.fromString(value) !== null
  },
}

// Workflow Node Types (category types)
export type NodeType = "trigger" | "action" | "logic" | "data"

export type TriggerNodeType =
  | "api-trigger"
  | "schedule-trigger"
  | "file-trigger"
  | "event-trigger"

export type ActionNodeType =
  | "send-email"
  | "send-sms"
  | "send-push"
  | "send-in-app"
  | "send-slack"
  | "send-discord"
  | "send-teams"
  | "send-webhook"

export type LogicNodeType = "condition" | "switch" | "loop" | "delay" | "merge" | "ab-test" | "wait-events"

export type DataNodeType = "map" | "filter" | "transform" | "read-file"

export type WorkflowNodeType =
  | TriggerNodeType
  | ActionNodeType
  | LogicNodeType
  | DataNodeType

export interface WorkflowNode {
  id: string
  type: WorkflowNodeType
  position: { x: number; y: number }
  data: {
    label: string
    config?: Record<string, unknown>
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
  type: WorkflowNodeType
  category: NodeType
  label: string
  description: string
  icon: string
  color: string
  inputs: number
  outputs: number
  registryId?: string // Registry ID for actions/triggers from registry
  configTemplate?: Record<string, unknown> // Config template from registry
}

