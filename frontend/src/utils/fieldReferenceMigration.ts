/**
 * Field Reference Migration Utilities
 * 
 * Utilities for detecting and migrating field references from old format to new format.
 */

import { parseFieldReference, type FieldReference } from "./fieldReferenceUtils"
import type { WorkflowDefinition, WorkflowNode } from "@/types/workflow"

export interface FieldReferenceSuggestion {
  fieldPath: string
  suggestedObjectTypeId: string
  confidence: number
  reason: string
}

export interface MigrationResult {
  migrated: boolean
  fieldPath: string
  oldValue: string | FieldReference
  newValue: FieldReference | null
  errors: string[]
}

/**
 * Detect field references in old format (string) in a workflow
 */
export function detectFieldReferences(workflow: WorkflowDefinition): Array<{
  nodeId: string
  nodeType: string
  fieldName: string
  fieldValue: string
}> {
  const oldFormatFields: Array<{
    nodeId: string
    nodeType: string
    fieldName: string
    fieldValue: string
  }> = []

  workflow.nodes.forEach((node) => {
    const config = node.data.config || {}

    // Check different node types for field references
    switch (node.type) {
      case "condition":
        if (config.field && typeof config.field === "string") {
          oldFormatFields.push({
            nodeId: node.id,
            nodeType: node.type,
            fieldName: "field",
            fieldValue: config.field,
          })
        }
        break

      case "transform":
        if (config.sourceField && typeof config.sourceField === "string") {
          oldFormatFields.push({
            nodeId: node.id,
            nodeType: node.type,
            fieldName: "sourceField",
            fieldValue: config.sourceField,
          })
        }
        if (config.targetField && typeof config.targetField === "string") {
          oldFormatFields.push({
            nodeId: node.id,
            nodeType: node.type,
            fieldName: "targetField",
            fieldValue: config.targetField,
          })
        }
        break

      case "map":
        if (config.mapping && typeof config.mapping === "object") {
          Object.entries(config.mapping).forEach(([key, value]) => {
            if (typeof value === "string") {
              oldFormatFields.push({
                nodeId: node.id,
                nodeType: node.type,
                fieldName: `mapping.${key}`,
                fieldValue: value,
              })
            }
          })
        }
        break

      case "filter":
        if (config.field && typeof config.field === "string") {
          oldFormatFields.push({
            nodeId: node.id,
            nodeType: node.type,
            fieldName: "field",
            fieldValue: config.field,
          })
        }
        break
    }
  })

  return oldFormatFields
}

/**
 * Suggest object types for a field path
 * 
 * @param fieldPath - Field path (e.g., "user.email", "order.total")
 * @param availableObjectTypes - Map of available object types
 * @returns Array of suggestions with confidence scores
 */
export function suggestObjectTypes(
  fieldPath: string,
  availableObjectTypes: Map<string, { name: string; fields: Array<{ name: string }> }>
): FieldReferenceSuggestion[] {
  if (!fieldPath) return []

  const suggestions: FieldReferenceSuggestion[] = []
  const parts = fieldPath.split(".").filter(Boolean)
  if (parts.length === 0) return []

  const firstPart = parts[0].toLowerCase()

  // Check each object type
  availableObjectTypes.forEach((objectType, objectTypeId) => {
    let confidence = 0
    let reason = ""

    // Check if first part matches object type name
    const objectTypeNameLower = objectType.name.toLowerCase()
    if (firstPart === objectTypeNameLower || firstPart === objectTypeId.toLowerCase()) {
      confidence += 50
      reason = `First part matches object type name`
    }

    // Check if field exists in object type
    const fieldName = parts.length > 1 ? parts[1] : parts[0]
    const hasField = objectType.fields.some((f) => f.name === fieldName)
    if (hasField) {
      confidence += 30
      reason += reason ? ", field exists in object type" : "Field exists in object type"
    }

    // Check if first part is a common prefix
    if (objectTypeId.toLowerCase().startsWith(firstPart) || firstPart.startsWith(objectTypeId.toLowerCase())) {
      confidence += 20
      reason += reason ? ", name similarity" : "Name similarity"
    }

    if (confidence > 0) {
      suggestions.push({
        fieldPath,
        suggestedObjectTypeId: objectTypeId,
        confidence,
        reason,
      })
    }
  })

  // Sort by confidence (highest first)
  return suggestions.sort((a, b) => b.confidence - a.confidence)
}

/**
 * Migrate a single field reference to new format
 * 
 * @param fieldPath - Old format field path (string)
 * @param objectTypeId - Object type ID to use
 * @returns New format field reference
 */
export function migrateFieldReference(
  fieldPath: string,
  objectTypeId: string
): FieldReference {
  const parsed = parseFieldReference(fieldPath)
  
  if (parsed && parsed.objectTypeId) {
    // Already in new format
    return {
      objectTypeId: parsed.objectTypeId,
      fieldPath: parsed.fieldPath,
    }
  }

  // Extract field path (remove object type prefix if present)
  let cleanFieldPath = fieldPath
  if (parsed && parsed.fieldPath) {
    cleanFieldPath = parsed.fieldPath
  }

  return {
    objectTypeId,
    fieldPath: cleanFieldPath,
  }
}

/**
 * Migrate an entire workflow to use new field reference format
 * 
 * @param workflow - Workflow definition
 * @param objectTypeMapping - Map of field paths to object type IDs
 * @returns Migration result with migrated workflow and errors
 */
export function bulkMigrateWorkflow(
  workflow: WorkflowDefinition,
  objectTypeMapping: Map<string, string> // fieldPath -> objectTypeId
): {
  workflow: WorkflowDefinition
  results: MigrationResult[]
  errors: string[]
} {
  const results: MigrationResult[] = []
  const errors: string[] = []
  const migratedNodes: WorkflowNode[] = []

  workflow.nodes.forEach((node) => {
    const config = { ...(node.data.config || {}) }
    let nodeMigrated = false

    // Migrate field references based on node type
    switch (node.type) {
      case "condition":
        if (config.field && typeof config.field === "string") {
          const objectTypeId = objectTypeMapping.get(config.field)
          if (objectTypeId) {
            const newValue = migrateFieldReference(config.field, objectTypeId)
            config.field = newValue
            nodeMigrated = true
            results.push({
              migrated: true,
              fieldPath: config.field as string,
              oldValue: config.field as string,
              newValue,
              errors: [],
            })
          } else {
            results.push({
              migrated: false,
              fieldPath: config.field as string,
              oldValue: config.field as string,
              newValue: null,
              errors: [`No object type mapping found for field: ${config.field}`],
            })
            errors.push(`Node ${node.id}: No object type mapping for field "${config.field}"`)
          }
        }
        break

      case "transform":
        if (config.sourceField && typeof config.sourceField === "string") {
          const objectTypeId = objectTypeMapping.get(config.sourceField)
          if (objectTypeId) {
            config.sourceField = migrateFieldReference(config.sourceField, objectTypeId)
            nodeMigrated = true
          }
        }
        if (config.targetField && typeof config.targetField === "string") {
          const objectTypeId = objectTypeMapping.get(config.targetField)
          if (objectTypeId) {
            config.targetField = migrateFieldReference(config.targetField, objectTypeId)
            nodeMigrated = true
          }
        }
        break

      case "map":
        if (config.mapping && typeof config.mapping === "object") {
          const migratedMapping: Record<string, FieldReference | string> = {}
          let mappingMigrated = false

          Object.entries(config.mapping).forEach(([key, value]) => {
            if (typeof value === "string") {
              const objectTypeId = objectTypeMapping.get(value)
              if (objectTypeId) {
                migratedMapping[key] = migrateFieldReference(value, objectTypeId)
                mappingMigrated = true
              } else {
                migratedMapping[key] = value
              }
            } else {
              migratedMapping[key] = value
            }
          })

          if (mappingMigrated) {
            config.mapping = migratedMapping
            nodeMigrated = true
          }
        }
        break

      case "filter":
        if (config.field && typeof config.field === "string") {
          const objectTypeId = objectTypeMapping.get(config.field)
          if (objectTypeId) {
            config.field = migrateFieldReference(config.field, objectTypeId)
            nodeMigrated = true
          }
        }
        break
    }

    // Create migrated node
    migratedNodes.push({
      ...node,
      data: {
        ...node.data,
        config: nodeMigrated ? config : node.data.config,
      },
    })
  })

  return {
    workflow: {
      ...workflow,
      nodes: migratedNodes,
    },
    results,
    errors,
  }
}

