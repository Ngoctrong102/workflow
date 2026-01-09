import { createContext, useContext, useState, useEffect, useMemo, type ReactNode } from "react"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

export interface ObjectType {
  id: string
  name: string
  fields: FieldDefinition[]
}

interface FieldReferenceContextValue {
  objectTypes: Map<string, ObjectType>
  isLoading: boolean
  error: Error | null
  refresh: () => Promise<void>
  getObjectType: (id: string) => ObjectType | undefined
  getFieldDefinition: (objectTypeId: string, fieldPath: string) => FieldDefinition | null
}

const FieldReferenceContext = createContext<FieldReferenceContextValue | null>(null)

interface FieldReferenceProviderProps {
  children: ReactNode
  // Optional: provide object types directly (for testing or static data)
  initialObjectTypes?: ObjectType[]
}

/**
 * Field Reference Context Provider
 * 
 * Provides object types and field definitions to components.
 * Caches object types for performance and refreshes on updates.
 */
export function FieldReferenceProvider({
  children,
  initialObjectTypes,
}: FieldReferenceProviderProps) {
  const [objectTypes, setObjectTypes] = useState<Map<string, ObjectType>>(new Map())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  // Initialize with provided object types
  useEffect(() => {
    if (initialObjectTypes && initialObjectTypes.length > 0) {
      const typesMap = new Map<string, ObjectType>()
      initialObjectTypes.forEach((type) => {
        typesMap.set(type.id, type)
      })
      setObjectTypes(typesMap)
    }
  }, [initialObjectTypes])

  // Load object types from API (when API is available)
  const loadObjectTypes = async () => {
    setIsLoading(true)
    setError(null)

    try {
      // TODO: Replace with actual API call when object type API is available
      // const response = await objectTypeService.list()
      // const typesMap = new Map<string, ObjectType>()
      // response.data.forEach((type) => {
      //   typesMap.set(type.id, {
      //     id: type.id,
      //     name: type.name,
      //     fields: type.fields,
      //   })
      // })
      // setObjectTypes(typesMap)

      // For now, use empty map or initial types
      if (initialObjectTypes && initialObjectTypes.length > 0) {
        const typesMap = new Map<string, ObjectType>()
        initialObjectTypes.forEach((type) => {
          typesMap.set(type.id, type)
        })
        setObjectTypes(typesMap)
      }
    } catch (err) {
      setError(err instanceof Error ? err : new Error("Failed to load object types"))
      console.error("Failed to load object types:", err)
    } finally {
      setIsLoading(false)
    }
  }

  // Refresh object types
  const refresh = async () => {
    await loadObjectTypes()
  }

  // Get object type by ID
  const getObjectType = (id: string): ObjectType | undefined => {
    return objectTypes.get(id)
  }

  // Get field definition by object type ID and field path
  const getFieldDefinition = (
    objectTypeId: string,
    fieldPath: string
  ): FieldDefinition | null => {
    const objectType = objectTypes.get(objectTypeId)
    if (!objectType) return null

    const parts = fieldPath.split(".").filter(Boolean)
    if (parts.length === 0) return null

    let currentFields = objectType.fields
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

  const value: FieldReferenceContextValue = useMemo(
    () => ({
      objectTypes,
      isLoading,
      error,
      refresh,
      getObjectType,
      getFieldDefinition,
    }),
    [objectTypes, isLoading, error]
  )

  // Load object types on mount
  useEffect(() => {
    if (!initialObjectTypes || initialObjectTypes.length === 0) {
      loadObjectTypes()
    }
  }, []) // Only run on mount

  return (
    <FieldReferenceContext.Provider value={value}>
      {children}
    </FieldReferenceContext.Provider>
  )
}

/**
 * Hook to use Field Reference Context
 */
export function useFieldReference(): FieldReferenceContextValue {
  const context = useContext(FieldReferenceContext)
  if (!context) {
    throw new Error("useFieldReference must be used within FieldReferenceProvider")
  }
  return context
}

