import type { Node, Edge } from "reactflow"
import { NODE_DEFINITIONS } from "@/constants/workflow-nodes"
import { parseFieldReference, type FieldReference } from "./fieldReferenceUtils"
import type { FieldDefinition } from "./fieldTypeValidator"
import { getNodeCategory } from "./node-type-utils"
import { NodeTypeEnum } from "@/types/workflow"

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
    const nodeConfig = (node.data as any)?.config || {}
    const nodeCategory = getNodeCategory(node.data.type as string, nodeConfig)
    return nodeCategory === NodeTypeEnum.TRIGGER
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
    const nodeConfig = (node.data as any)?.config || {}
    const nodeCategory = getNodeCategory(node.data.type as string, nodeConfig)

    // Trigger nodes don't need input connections
    if (nodeCategory === NodeTypeEnum.TRIGGER) {
      return
    }

    // Get node definition for label (only for built-in nodes)
    const nodeDef = NODE_DEFINITIONS.find((n) => n.type === node.data.type)

    // Check if node has input connection
    const hasInput = edges.some((edge) => edge.target === node.id)
    if (!hasInput) {
      errors.push({
        nodeId: node.id,
        message: `Node "${node.data.label || nodeDef?.label || node.id}" is not connected`,
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
  // Get subtype from config (for trigger/action/logic subtypes)
  const subtype = config.subtype as string | undefined
  // Get node category to determine validation rules
  const nodeCategory = getNodeCategory(node.data.type as string, config)

  // Validate based on node category and subtype
  // For TRIGGER nodes, check subtype
  if (nodeCategory === NodeTypeEnum.TRIGGER) {
    switch (subtype) {
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
    }
  }
  
  // For LOGIC nodes, check subtype
  if (nodeCategory === NodeTypeEnum.LOGIC) {
    switch (subtype) {
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
  }
  
  // For ACTION nodes, validation is done through registry configTemplate
  // No specific validation needed here as actions are validated through their registry definitions

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
  // Get subtype from config (for trigger/action/logic subtypes)
  const subtype = config.subtype as string | undefined
  // Get node category to determine validation rules
  const nodeCategory = getNodeCategory(node.data.type as string, config)

  // Validate field references based on node category and subtype
  // For LOGIC nodes, check subtype
  if (nodeCategory === NodeTypeEnum.LOGIC) {
    switch (subtype) {
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
    }
  }
  
  // For ACTION nodes, field references are validated through registry configTemplate
  // No specific validation needed here as actions are validated through their registry definitions
  
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
  // Use getNodeCategory to determine node category (works for both built-in and registry nodes)
  const sourceConfig = (source.data as any)?.config || {}
  const targetConfig = (target.data as any)?.config || {}
  const sourceType = source.data.type as string
  const targetType = target.data.type as string
  
  const sourceCategory = getNodeCategory(sourceType, sourceConfig)
  const targetCategory = getNodeCategory(targetType, targetConfig)

  // Can't connect trigger to trigger
  if (sourceCategory === NodeTypeEnum.TRIGGER && targetCategory === NodeTypeEnum.TRIGGER) {
    return { isValid: false, message: "Cannot connect trigger to trigger" }
  }

  // Can't connect to trigger
  if (targetCategory === NodeTypeEnum.TRIGGER) {
    return { isValid: false, message: "Cannot connect to trigger node" }
  }

  // Check for duplicate connections
  const duplicateConnection = edges.some(
    (edge) => edge.source === source.id && edge.target === target.id
  )
  if (duplicateConnection) {
    return { isValid: false, message: "Connection already exists" }
  }

  // Get node definitions for input/output limits (only for built-in nodes)
  // Note: Registry nodes (triggers/actions) don't have entries in NODE_DEFINITIONS
  const targetDef = NODE_DEFINITIONS.find((n) => n.type === targetType)

  // Validate action nodes can only have single input
  // For registry action nodes, assume 1 input (default for actions)
  if (targetCategory === NodeTypeEnum.ACTION) {
    const existingInputs = edges.filter((edge) => edge.target === target.id).length
    const maxInputs = targetDef?.inputs ?? 1 // Default to 1 input for action nodes
    if (existingInputs >= maxInputs) {
      return {
        isValid: false,
        message: `Action nodes can only have ${maxInputs} input connection(s)`,
      }
    }
  }

  // Logic nodes can have multiple inputs/outputs - check if within limits
  if (targetCategory === NodeTypeEnum.LOGIC) {
    const existingInputs = edges.filter((edge) => edge.target === target.id).length
    const maxInputs = targetDef?.inputs ?? 1 // Default to 1 input for logic nodes
    if (existingInputs >= maxInputs) {
      return {
        isValid: false,
        message: `Logic node can only have ${maxInputs} input connection(s)`,
      }
    }
  }

  return { isValid: true }
}

