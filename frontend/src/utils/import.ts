/**
 * Import utilities for workflows and templates
 */

export interface ImportResult<T> {
  success: boolean
  data?: T
  errors?: string[]
  warnings?: string[]
}

/**
 * Parse JSON file and validate structure
 */
export function parseImportFile<T>(
  file: File,
  validator?: (data: unknown) => data is T
): Promise<ImportResult<T>> {
  return new Promise((resolve) => {
    const reader = new FileReader()

    reader.onload = (e) => {
      try {
        const text = e.target?.result as string
        const data = JSON.parse(text)

        if (validator && !validator(data)) {
          resolve({
            success: false,
            errors: ["Invalid file format"],
          })
          return
        }

        resolve({
          success: true,
          data: data as T,
        })
      } catch (error) {
        resolve({
          success: false,
          errors: [error instanceof Error ? error.message : "Failed to parse file"],
        })
      }
    }

    reader.onerror = () => {
      resolve({
        success: false,
        errors: ["Failed to read file"],
      })
    }

    reader.readAsText(file)
  })
}

/**
 * Validate workflow import data
 */
export function validateWorkflowImport(data: unknown): data is {
  name: string
  description?: string
  definition?: {
    nodes: unknown[]
    edges: unknown[]
  }
  status?: string
} {
  if (typeof data !== "object" || data === null) return false
  const obj = data as Record<string, unknown>
  return typeof obj.name === "string"
}

/**
 * Validate template import data
 */
export function validateTemplateImport(data: unknown): data is {
  name: string
  channel: string
  subject?: string
  body: string
  variables?: string[]
} {
  if (typeof data !== "object" || data === null) return false
  const obj = data as Record<string, unknown>
  return (
    typeof obj.name === "string" &&
    typeof obj.channel === "string" &&
    typeof obj.body === "string"
  )
}

/**
 * Check for conflicts in imported data
 */
export function checkImportConflicts<T extends { id?: string; name: string }>(
  imported: T,
  existing: T[]
): {
  hasConflict: boolean
  conflictType?: "id" | "name"
  existingItem?: T
} {
  // Check for ID conflict
  if (imported.id) {
    const idConflict = existing.find((item) => item.id === imported.id)
    if (idConflict) {
      return {
        hasConflict: true,
        conflictType: "id",
        existingItem: idConflict,
      }
    }
  }

  // Check for name conflict
  const nameConflict = existing.find((item) => item.name === imported.name)
  if (nameConflict) {
    return {
      hasConflict: true,
      conflictType: "name",
      existingItem: nameConflict,
    }
  }

  return { hasConflict: false }
}

