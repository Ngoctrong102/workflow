/**
 * Field Reference Utilities
 * 
 * Utilities for parsing, formatting, and resolving field references.
 * Supports both old format (string) and new format (object with objectTypeId and fieldPath).
 */

export interface FieldReference {
  objectTypeId?: string
  fieldPath: string
  type?: string
}

export interface ParsedFieldReference {
  objectTypeId: string | null
  fieldPath: string
  isNested: boolean
  parts: string[]
}

/**
 * Parse a field reference from string or object format
 * 
 * @param value - Field reference as string (e.g., "user.email") or object ({objectTypeId: "user", fieldPath: "email"})
 * @returns Parsed field reference
 */
export function parseFieldReference(value: string | FieldReference | null | undefined): ParsedFieldReference | null {
  if (!value) return null

  // If it's already an object with fieldPath
  if (typeof value === 'object' && 'fieldPath' in value) {
    const fieldPath = value.fieldPath || ''
    const parts = fieldPath.split('.').filter(Boolean)
    return {
      objectTypeId: value.objectTypeId || null,
      fieldPath,
      isNested: parts.length > 1,
      parts,
    }
  }

  // If it's a string
  if (typeof value === 'string') {
    const parts = value.split('.').filter(Boolean)
    // Try to extract object type from first part if it looks like "objectType.field"
    const objectTypeId = parts.length > 1 ? parts[0] : null
    const fieldPath = objectTypeId ? parts.slice(1).join('.') : value

    return {
      objectTypeId,
      fieldPath,
      isNested: parts.length > 1,
      parts: fieldPath.split('.').filter(Boolean),
    }
  }

  return null
}

/**
 * Format a field reference for display
 * 
 * @param fieldRef - Field reference object
 * @returns Formatted string for display
 */
export function formatFieldReference(fieldRef: FieldReference | ParsedFieldReference | null | undefined): string {
  if (!fieldRef) return ''

  if ('objectTypeId' in fieldRef && fieldRef.objectTypeId) {
    return `${fieldRef.objectTypeId}.${fieldRef.fieldPath}`
  }

  if ('fieldPath' in fieldRef) {
    return fieldRef.fieldPath
  }

  return ''
}

/**
 * Resolve nested field path
 * 
 * @param objectTypeId - Object type ID
 * @param fieldPath - Field path (may be nested like "profile.email")
 * @returns Resolved field path parts
 */
export function resolveFieldPath(_objectTypeId: string | null, fieldPath: string): string[] {
  if (!fieldPath) return []

  return fieldPath.split('.').filter(Boolean)
}

/**
 * Get display name for a field
 * 
 * @param objectTypeId - Object type ID
 * @param fieldPath - Field path
 * @returns Display name (formatted field path)
 */
export function getFieldDisplayName(objectTypeId: string | null, fieldPath: string): string {
  if (!fieldPath) return ''

  // If we have object type, format as "ObjectType: field.path"
  if (objectTypeId) {
    const formattedObjectType = objectTypeId
      .split('-')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ')
    
    const formattedField = fieldPath
      .split('.')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' → ')

    return `${formattedObjectType}: ${formattedField}`
  }

  // Otherwise, just format the field path
  return fieldPath
    .split('.')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' → ')
}

/**
 * Check if a field reference is in the new format (object with objectTypeId)
 */
export function isNewFormat(value: string | FieldReference | null | undefined): boolean {
  if (!value) return false
  return typeof value === 'object' && 'objectTypeId' in value && 'fieldPath' in value
}

/**
 * Check if a field reference is in the old format (string)
 */
export function isOldFormat(value: string | FieldReference | null | undefined): boolean {
  if (!value) return false
  return typeof value === 'string'
}

/**
 * Convert old format to new format (if possible)
 * 
 * @param value - Old format string (e.g., "user.email")
 * @param defaultObjectTypeId - Default object type ID to use if not in path
 * @returns New format object or null if conversion not possible
 */
export function convertToNewFormat(
  value: string,
  defaultObjectTypeId?: string
): FieldReference | null {
  if (!value || typeof value !== 'string') return null

  const parsed = parseFieldReference(value)
  if (!parsed) return null

  return {
    objectTypeId: parsed.objectTypeId || defaultObjectTypeId,
    fieldPath: parsed.fieldPath,
  }
}

/**
 * Convert new format to old format (string)
 * 
 * @param value - New format object
 * @returns Old format string
 */
export function convertToOldFormat(value: FieldReference | null | undefined): string {
  if (!value) return ''
  if (value.objectTypeId && value.fieldPath) {
    return `${value.objectTypeId}.${value.fieldPath}`
  }
  return value.fieldPath || ''
}

