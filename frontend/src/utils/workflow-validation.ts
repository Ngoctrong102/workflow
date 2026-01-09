import type { Node, Edge } from "reactflow"
import { NODE_DEFINITIONS } from "@/constants/workflow-nodes"
import { parseFieldReference, type FieldReference } from "./fieldReferenceUtils"
import type { FieldDefinition } from "./fieldTypeValidator"

export interface ValidationError {
  nodeId?: string
  message: string
  type: "error" | "warning"
}

export interface ValidationResult {
  isValid: boolean
  errors: ValidationError[]
}

export interface FieldReferenceValidationOptions {
  objectTypes?: Map<string, { name: string; fields: FieldDefinition[] }>
  validateTypes?: boolean
  allowOldFormat?: boolean
}

export function validateWorkflow(nodes: Node[], edges: Edge[]): ValidationResult {
  const errors: ValidationError[] = []

  // Check if workflow has exactly one trigger node
  const triggerNodes = nodes.filter((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    return nodeDef?.category === "trigger"
  })

  if (triggerNodes.length === 0) {
    errors.push({
      message: "Workflow must have exactly one trigger node",
      type: "error",
    })
  } else if (triggerNodes.length > 1) {
    errors.push({
      message: `Workflow can only have one trigger node, but found ${triggerNodes.length}`,
      type: "error",
    })
  }

  // Check if all nodes are connected (except trigger nodes)
  nodes.forEach((node) => {
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)
    if (!nodeDef) return

    // Trigger nodes don't need input connections
    if (nodeDef.category === "trigger") {
      return
    }

    // Check if node has input connection
    const hasInput = edges.some((edge) => edge.target === node.id)
    if (!hasInput) {
      errors.push({
        nodeId: node.id,
        message: `Node "${node.data.label || nodeDef.label}" is not connected`,
        type: "error",
      })
    }
  })

  // Check for circular connections
  const circularErrors = detectCircularConnections(nodes, edges)
  errors.push(...circularErrors)

  // Check node configurations
  nodes.forEach((node) => {
    const configErrors = validateNodeConfiguration(node)
    errors.push(...configErrors)
  })

  return {
    isValid: errors.filter((e) => e.type === "error").length === 0,
    errors,
  }
}

/**
 * Validate workflow with field reference validation
 */
export function validateWorkflowWithFieldReferences(
  nodes: Node[],
  edges: Edge[],
  options: FieldReferenceValidationOptions = {}
): ValidationResult {
  const baseValidation = validateWorkflow(nodes, edges)
  const errors = [...baseValidation.errors]

  // Add field reference validation
  nodes.forEach((node) => {
    const fieldRefErrors = validateNodeFieldReferences(node, options)
    errors.push(...fieldRefErrors)
  })

  return {
    isValid: errors.filter((e) => e.type === "error").length === 0,
    errors,
  }
}

function detectCircularConnections(nodes: Node[], edges: Edge[]): ValidationError[] {
  const errors: ValidationError[] = []
  const visited = new Set<string>()
  const recursionStack = new Set<string>()

  function hasCycle(nodeId: string): boolean {
    if (recursionStack.has(nodeId)) {
      return true
    }
    if (visited.has(nodeId)) {
      return false
    }

    visited.add(nodeId)
    recursionStack.add(nodeId)

    const outgoingEdges = edges.filter((edge) => edge.source === nodeId)
    for (const edge of outgoingEdges) {
      if (hasCycle(edge.target)) {
        errors.push({
          nodeId,
          message: "Circular connection detected",
          type: "error",
        })
        return true
      }
    }

    recursionStack.delete(nodeId)
    return false
  }

  nodes.forEach((node) => {
    if (!visited.has(node.id)) {
      hasCycle(node.id)
    }
  })

  return errors
}

function validateNodeConfiguration(node: Node): ValidationError[] {
  const errors: ValidationError[] = []
  const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)

  if (!nodeDef) {
    return errors
  }

  // Check if node has required label
  if (!node.data.label || node.data.label.trim() === "") {
    errors.push({
      nodeId: node.id,
      message: `Node "${nodeDef.label}" must have a label`,
      type: "error",
    })
  }

  // Node-specific validation
  const config = node.data.config || {}

  switch (node.data.type) {
    case "api-trigger":
      if (!config.path) {
        errors.push({
          nodeId: node.id,
          message: "API Trigger must have a path",
          type: "error",
        })
      }
      break

    case "schedule-trigger":
      if (!config.cron) {
        errors.push({
          nodeId: node.id,
          message: "Schedule Trigger must have a cron expression",
          type: "error",
        })
      }
      break

    case "send-email":
    case "send-sms":
    case "send-push":
    case "send-in-app":
      if (!config.recipients) {
        errors.push({
          nodeId: node.id,
          message: `${nodeDef.label} must have recipients`,
          type: "error",
        })
      }
      break

    case "send-slack":
      if (!config.channel || !config.message) {
        errors.push({
          nodeId: node.id,
          message: "Slack message must have channel and message",
          type: "error",
        })
      }
      break

    case "send-discord":
      if (!config.channelId || !config.content) {
        errors.push({
          nodeId: node.id,
          message: "Discord message must have channel ID and content",
          type: "error",
        })
      }
      break

    case "send-teams":
      if (!config.title || !config.text) {
        errors.push({
          nodeId: node.id,
          message: "Teams message must have title and text",
          type: "error",
        })
      }
      break

    case "send-webhook":
      if (!config.url) {
        errors.push({
          nodeId: node.id,
          message: "Webhook must have a URL",
          type: "error",
        })
      }
      break

    case "condition":
      if (!config.field || !config.operator) {
        errors.push({
          nodeId: node.id,
          message: "Condition must have field and operator",
          type: "error",
        })
      }
      break

    case "switch":
      if (!config.field) {
        errors.push({
          nodeId: node.id,
          message: "Switch must have a field to evaluate",
          type: "error",
        })
      }
      break

    case "loop":
      if (!config.arrayField || !config.itemVariable) {
        errors.push({
          nodeId: node.id,
          message: "Loop must have array field and item variable name",
          type: "error",
        })
      }
      break

    case "merge":
      const inputCount = Number(config.inputCount) || 2
      if (inputCount < 2 || inputCount > 10) {
        errors.push({
          nodeId: node.id,
          message: "Merge must have between 2 and 10 inputs",
          type: "error",
        })
      }
      break

    case "transform":
      if (!config.sourceField || !config.targetField) {
        errors.push({
          nodeId: node.id,
          message: "Transform must have source and target fields",
          type: "error",
        })
      }
      break

    case "map":
      if (!config.mapping) {
        errors.push({
          nodeId: node.id,
          message: "Map must have field mappings",
          type: "error",
        })
      } else {
        try {
          JSON.parse(config.mapping as string)
        } catch {
          errors.push({
            nodeId: node.id,
            message: "Map mappings must be valid JSON",
            type: "error",
          })
        }
      }
      break

    case "filter":
      if (!config.arrayField || !config.field || !config.operator) {
        errors.push({
          nodeId: node.id,
          message: "Filter must have array field, filter field, and operator",
          type: "error",
        })
      }
      break

    case "read-file":
      if (!config.fileFormat || !config.outputField) {
        errors.push({
          nodeId: node.id,
          message: "Read File must have file format and output field",
          type: "error",
        })
      }
      break

    case "file-trigger":
      if (!config.acceptedFormats) {
        errors.push({
          nodeId: node.id,
          message: "File Trigger must have accepted file formats",
          type: "error",
        })
      }
      break

    case "event-trigger":
      if (!config.eventType || !config.topic) {
        errors.push({
          nodeId: node.id,
          message: "Event Trigger must have event type and topic/queue name",
          type: "error",
        })
      }
      break

    case "delay":
      if (!config.duration || Number(config.duration) < 1) {
        errors.push({
          nodeId: node.id,
          message: "Delay must have a valid duration",
          type: "error",
        })
      }
      break

    case "ab-test":
      if (!config.test_id) {
        errors.push({
          nodeId: node.id,
          message: "A/B Test must have a test ID",
          type: "error",
        })
      }
      break
  }

  return errors
}

/**
 * Validate field references in a node
 */
function validateNodeFieldReferences(
  node: Node,
  options: FieldReferenceValidationOptions
): ValidationError[] {
  const errors: ValidationError[] = []
  const { objectTypes, validateTypes = true, allowOldFormat = true } = options

  if (!objectTypes || objectTypes.size === 0) {
    // No object types available, skip validation
    return errors
  }

  const config = node.data.config || {}

  // Validate field references based on node type
  switch (node.data.type) {
    case "condition":
      if (config.field) {
        const fieldErrors = validateFieldReference(
          config.field,
          node.id,
          "Condition field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...fieldErrors)
      }
      break

    case "switch":
      if (config.field) {
        const fieldErrors = validateFieldReference(
          config.field,
          node.id,
          "Switch field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...fieldErrors)
      }
      break

    case "loop":
      if (config.arrayField) {
        const arrayFieldErrors = validateFieldReference(
          config.arrayField,
          node.id,
          "Loop array field",
          { objectTypes, validateTypes: true, allowOldFormat } // Require array type
        )
        errors.push(...arrayFieldErrors)
      }
      break

    case "transform":
      if (config.sourceField) {
        const sourceErrors = validateFieldReference(
          config.sourceField,
          node.id,
          "Transform source field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...sourceErrors)
      }
      if (config.targetField) {
        const targetErrors = validateFieldReference(
          config.targetField,
          node.id,
          "Transform target field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...targetErrors)
      }
      break

    case "map":
      if (config.mapping && typeof config.mapping === "object") {
        Object.entries(config.mapping).forEach(([key, value]) => {
          if (typeof value === "string" || (typeof value === "object" && value !== null)) {
            const mappingErrors = validateFieldReference(
              value,
              node.id,
              `Map field "${key}"`,
              { objectTypes, validateTypes, allowOldFormat }
            )
            errors.push(...mappingErrors)
          }
        })
      }
      break

    case "filter":
      if (config.field) {
        const fieldErrors = validateFieldReference(
          config.field,
          node.id,
          "Filter field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...fieldErrors)
      }
      if (config.arrayField) {
        const arrayFieldErrors = validateFieldReference(
          config.arrayField,
          node.id,
          "Filter array field",
          { objectTypes, validateTypes, allowOldFormat }
        )
        errors.push(...arrayFieldErrors)
      }
      break

    case "send-email":
    case "send-sms":
      // Recipient fields might be field references
      if (config.recipientField) {
        const recipientErrors = validateFieldReference(
          config.recipientField,
          node.id,
          "Recipient field",
          { objectTypes, validateTypes: false, allowOldFormat } // Don't validate type for recipients
        )
        errors.push(...recipientErrors)
      }
      break
  }

  return errors
}

/**
 * Validate a single field reference
 */
function validateFieldReference(
  fieldValue: string | FieldReference | unknown,
  nodeId: string,
  fieldLabel: string,
  options: {
    objectTypes: Map<string, { name: string; fields: FieldDefinition[] }>
    validateTypes: boolean
    allowOldFormat: boolean
  }
): ValidationError[] {
  const errors: ValidationError[] = []
  const { objectTypes, validateTypes, allowOldFormat } = options

  // Parse field reference
  const parsed = parseFieldReference(
    typeof fieldValue === "string"
      ? fieldValue
      : typeof fieldValue === "object" && fieldValue !== null && !Array.isArray(fieldValue)
      ? (fieldValue as FieldReference)
      : null
  )

  if (!parsed) {
    // Invalid format
    if (!allowOldFormat) {
      errors.push({
        nodeId,
        message: `${fieldLabel} has invalid format`,
        type: "error",
      })
    }
    return errors
  }

  // Check if using old format (string without objectTypeId)
  if (!parsed.objectTypeId && !allowOldFormat) {
    errors.push({
      nodeId,
      message: `${fieldLabel} should use new format with object type`,
      type: "warning",
    })
    return errors
  }

  // If old format and allowed, just warn
  if (!parsed.objectTypeId && allowOldFormat) {
    errors.push({
      nodeId,
      message: `${fieldLabel} uses old format. Consider migrating to new format for better validation.`,
      type: "warning",
    })
    return errors
  }

  // Validate object type exists
  if (parsed.objectTypeId) {
    const objectType = objectTypes.get(parsed.objectTypeId)
    if (!objectType) {
      errors.push({
        nodeId,
        message: `${fieldLabel}: Object type "${parsed.objectTypeId}" not found`,
        type: "error",
      })
      return errors
    }

    // Validate field path exists
    const fieldDef = resolveFieldPath(objectType.fields, parsed.fieldPath, objectTypes)
    if (!fieldDef) {
      errors.push({
        nodeId,
        message: `${fieldLabel}: Field path "${parsed.fieldPath}" not found in object type "${parsed.objectTypeId}"`,
        type: "error",
      })
      return errors
    }

    // Validate field type if needed
    if (validateTypes && fieldDef.type) {
      // Type validation can be added here if expected types are specified
      // For now, we just validate that the field exists
    }
  }

  return errors
}

/**
 * Resolve field definition from field path
 */
function resolveFieldPath(
  fields: FieldDefinition[],
  fieldPath: string,
  objectTypes: Map<string, { name: string; fields: FieldDefinition[] }>
): FieldDefinition | null {
  if (!fieldPath) return null

  const parts = fieldPath.split(".").filter(Boolean)
  if (parts.length === 0) return null

  let currentFields = fields
  let currentField: FieldDefinition | undefined

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i]

    // Check if it's an array access (e.g., "orders[0]")
    const arrayMatch = part.match(/^(.+)\[(\d+)\]$/)
    const fieldName = arrayMatch ? arrayMatch[1] : part

    currentField = currentFields.find((f) => f.name === fieldName)
    if (!currentField) return null

    // If this is the last part, return the field
    if (i === parts.length - 1) {
      return currentField
    }

    // Navigate to nested object type
    if (currentField.type === "object" && currentField.validation?.objectTypeId) {
      const nestedObjectType = objectTypes.get(currentField.validation.objectTypeId)
      if (nestedObjectType) {
        currentFields = nestedObjectType.fields
      } else {
        return null
      }
    } else if (currentField.type === "array" && currentField.validation?.itemObjectTypeId) {
      const arrayItemObjectType = objectTypes.get(currentField.validation.itemObjectTypeId)
      if (arrayItemObjectType) {
        currentFields = arrayItemObjectType.fields
      } else {
        return null
      }
    } else {
      // Can't navigate further
      return null
    }
  }

  return currentField || null
}

export function validateConnection(
  source: Node,
  target: Node,
  edges: Edge[]
): { isValid: boolean; message?: string } {
  const sourceDef = NODE_DEFINITIONS.find((n) => n.type === source.data.type)
  const targetDef = NODE_DEFINITIONS.find((n) => n.type === target.data.type)

  if (!sourceDef || !targetDef) {
    return { isValid: false, message: "Invalid node types" }
  }

  // Can't connect trigger to trigger
  if (sourceDef.category === "trigger" && targetDef.category === "trigger") {
    return { isValid: false, message: "Cannot connect trigger to trigger" }
  }

  // Can't connect to trigger
  if (targetDef.category === "trigger") {
    return { isValid: false, message: "Cannot connect to trigger node" }
  }

  // Check for duplicate connections
  const duplicateConnection = edges.some(
    (edge) => edge.source === source.id && edge.target === target.id
  )
  if (duplicateConnection) {
    return { isValid: false, message: "Connection already exists" }
  }

  // Validate action nodes can only have single input
  if (targetDef.category === "action") {
    const existingInputs = edges.filter((edge) => edge.target === target.id).length
    if (existingInputs >= targetDef.inputs) {
      return {
        isValid: false,
        message: `Action nodes can only have ${targetDef.inputs} input connection(s)`,
      }
    }
  }

  // Logic nodes can have multiple inputs/outputs - check if within limits
  if (targetDef.category === "logic") {
    const existingInputs = edges.filter((edge) => edge.target === target.id).length
    if (existingInputs >= targetDef.inputs) {
      return {
        isValid: false,
        message: `Logic node can only have ${targetDef.inputs} input connection(s)`,
      }
    }
  }

  return { isValid: true }
}

