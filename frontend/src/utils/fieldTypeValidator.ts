/**
 * Field Type Validator
 * 
 * Utilities for validating field types and field values against field definitions.
 */

export type FieldType =
  | 'string'
  | 'number'
  | 'boolean'
  | 'date'
  | 'datetime'
  | 'email'
  | 'phone'
  | 'url'
  | 'json'
  | 'array'
  | 'object'

export interface FieldDefinition {
  name: string
  displayName?: string
  type: FieldType
  required?: boolean
  defaultValue?: unknown
  validation?: {
    minLength?: number
    maxLength?: number
    min?: number
    max?: number
    pattern?: string
    enum?: string[]
    minItems?: number
    maxItems?: number
    objectTypeId?: string // For object/array types
    itemType?: FieldType // For array types
    itemObjectTypeId?: string // For array of objects
  }
  description?: string
  examples?: string[]
}

export interface ValidationResult {
  valid: boolean
  error?: string
}

/**
 * Get field type from object type definition
 * 
 * @param objectTypeId - Object type ID
 * @param fieldPath - Field path (may be nested)
 * @param objectTypes - Map of object type definitions (optional, for future use)
 * @returns Field type or null if not found
 */
export function getFieldType(
  objectTypeId: string | null,
  fieldPath: string,
  objectTypes?: Map<string, { fields: FieldDefinition[] }>
): FieldType | null {
  if (!objectTypeId || !fieldPath) return null

  // If object types are provided, look up the field
  if (objectTypes) {
    const objectType = objectTypes.get(objectTypeId)
    if (!objectType) return null

    const parts = fieldPath.split('.')
    let currentFields = objectType.fields
    let currentField: FieldDefinition | undefined

    for (const part of parts) {
      currentField = currentFields.find((f) => f.name === part)
      if (!currentField) return null

      if (currentField.type === 'object' && currentField.validation?.objectTypeId) {
        const nestedObjectType = objectTypes.get(currentField.validation.objectTypeId)
        if (nestedObjectType) {
          currentFields = nestedObjectType.fields
        } else {
          return null
        }
      } else if (currentField.type === 'array' && currentField.validation?.itemObjectTypeId) {
        const arrayItemObjectType = objectTypes.get(currentField.validation.itemObjectTypeId)
        if (arrayItemObjectType) {
          currentFields = arrayItemObjectType.fields
        } else {
          return currentField.type
        }
      } else {
        return currentField.type
      }
    }

    return currentField?.type || null
  }

  // Fallback: try to infer from field path
  const lowerPath = fieldPath.toLowerCase()
  if (lowerPath.includes('email')) return 'email'
  if (lowerPath.includes('phone') || lowerPath.includes('mobile')) return 'phone'
  if (lowerPath.includes('url') || lowerPath.includes('link')) return 'url'
  if (lowerPath.includes('date')) return 'date'
  if (lowerPath.includes('time')) return 'datetime'
  if (lowerPath.includes('count') || lowerPath.includes('total') || lowerPath.includes('amount')) return 'number'
  if (lowerPath.includes('is') || lowerPath.includes('has') || lowerPath.includes('active')) return 'boolean'

  return 'string'
}

/**
 * Validate field type matches expected type
 * 
 * @param field - Field definition
 * @param expectedType - Expected field type
 * @returns Validation result
 */
export function validateFieldType(field: FieldDefinition | null, expectedType: FieldType): ValidationResult {
  if (!field) {
    return {
      valid: false,
      error: 'Field definition not found',
    }
  }

  // Type compatibility mapping
  const compatibleTypes: Record<FieldType, FieldType[]> = {
    string: ['string', 'email', 'phone', 'url'],
    number: ['number'],
    boolean: ['boolean'],
    date: ['date', 'datetime'],
    datetime: ['datetime', 'date'],
    email: ['email', 'string'],
    phone: ['phone', 'string'],
    url: ['url', 'string'],
    json: ['json', 'object', 'array'],
    array: ['array', 'json'],
    object: ['object', 'json'],
  }

  const compatible = compatibleTypes[field.type] || []
  if (compatible.includes(expectedType) || field.type === expectedType) {
    return { valid: true }
  }

  return {
    valid: false,
    error: `Field type "${field.type}" is not compatible with expected type "${expectedType}"`,
  }
}

/**
 * Validate field value against field definition
 * 
 * @param value - Value to validate
 * @param fieldDefinition - Field definition
 * @returns Validation result
 */
export function validateFieldValue(value: unknown, fieldDefinition: FieldDefinition): ValidationResult {
  // Check required
  if (fieldDefinition.required && (value === null || value === undefined || value === '')) {
    return {
      valid: false,
      error: `Field "${fieldDefinition.displayName || fieldDefinition.name}" is required`,
    }
  }

  // If value is empty and not required, it's valid
  if (value === null || value === undefined || value === '') {
    return { valid: true }
  }

  const { type, validation } = fieldDefinition

  // Type-specific validation
  switch (type) {
    case 'string':
    case 'email':
    case 'phone':
    case 'url':
      if (typeof value !== 'string') {
        return {
          valid: false,
          error: `Expected string, got ${typeof value}`,
        }
      }

      // Length validation
      if (validation) {
        if (validation.minLength !== undefined && value.length < validation.minLength) {
          return {
            valid: false,
            error: `Minimum length is ${validation.minLength} characters`,
          }
        }
        if (validation.maxLength !== undefined && value.length > validation.maxLength) {
          return {
            valid: false,
            error: `Maximum length is ${validation.maxLength} characters`,
          }
        }

        // Pattern validation
        if (validation.pattern) {
          const regex = new RegExp(validation.pattern)
          if (!regex.test(value)) {
            return {
              valid: false,
              error: 'Value does not match required pattern',
            }
          }
        }

        // Enum validation
        if (validation.enum && !validation.enum.includes(value)) {
          return {
            valid: false,
            error: `Value must be one of: ${validation.enum.join(', ')}`,
          }
        }
      }

      // Email-specific validation
      if (type === 'email') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) {
          return {
            valid: false,
            error: 'Invalid email format',
          }
        }
      }

      // URL-specific validation
      if (type === 'url') {
        try {
          new URL(value)
        } catch {
          return {
            valid: false,
            error: 'Invalid URL format',
          }
        }
      }

      break

    case 'number':
      const numValue = typeof value === 'string' ? parseFloat(value) : value
      if (typeof numValue !== 'number' || isNaN(numValue)) {
        return {
          valid: false,
          error: 'Expected number',
        }
      }

      if (validation) {
        if (validation.min !== undefined && numValue < validation.min) {
          return {
            valid: false,
            error: `Minimum value is ${validation.min}`,
          }
        }
        if (validation.max !== undefined && numValue > validation.max) {
          return {
            valid: false,
            error: `Maximum value is ${validation.max}`,
          }
        }
      }

      break

    case 'boolean':
      if (typeof value !== 'boolean' && value !== 'true' && value !== 'false') {
        return {
          valid: false,
          error: 'Expected boolean',
        }
      }
      break

    case 'date':
    case 'datetime':
      if (typeof value !== 'string') {
        return {
          valid: false,
          error: 'Expected date string',
        }
      }
      const date = new Date(value)
      if (isNaN(date.getTime())) {
        return {
          valid: false,
          error: 'Invalid date format',
        }
      }
      break

    case 'array':
      if (!Array.isArray(value)) {
        return {
          valid: false,
          error: 'Expected array',
        }
      }

      if (validation) {
        if (validation.minItems !== undefined && value.length < validation.minItems) {
          return {
            valid: false,
            error: `Minimum ${validation.minItems} items required`,
          }
        }
        if (validation.maxItems !== undefined && value.length > validation.maxItems) {
          return {
            valid: false,
            error: `Maximum ${validation.maxItems} items allowed`,
          }
        }
      }
      break

    case 'object':
    case 'json':
      if (typeof value !== 'object' || Array.isArray(value)) {
        return {
          valid: false,
          error: 'Expected object',
        }
      }
      break
  }

  return { valid: true }
}

/**
 * Check if two field types are compatible
 */
export function areTypesCompatible(type1: FieldType, type2: FieldType): boolean {
  if (type1 === type2) return true

  const compatibleTypes: Record<FieldType, FieldType[]> = {
    string: ['email', 'phone', 'url'],
    number: [],
    boolean: [],
    email: ['string'],
    phone: ['string'],
    url: ['string'],
    date: ['datetime'],
    datetime: ['date'],
    json: ['object', 'array'],
    object: ['json'],
    array: ['json'],
  }

  return compatibleTypes[type1]?.includes(type2) || compatibleTypes[type2]?.includes(type1) || false
}

