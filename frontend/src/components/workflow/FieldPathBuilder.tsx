import { useState, useEffect, useMemo } from "react"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { X, Plus, ChevronRight } from "lucide-react"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

export interface FieldPathBuilderProps {
  objectTypeId: string | null
  fieldPath: string
  onChange: (fieldPath: string) => void
  objectTypes?: Map<string, { name: string; fields: FieldDefinition[] }>
  className?: string
}

interface PathSegment {
  fieldName: string
  fieldType: string
  isArray?: boolean
  arrayIndex?: number
}

export function FieldPathBuilder({
  objectTypeId,
  fieldPath,
  onChange,
  objectTypes,
  className,
}: FieldPathBuilderProps) {
  const [segments, setSegments] = useState<PathSegment[]>([])

  // Parse field path into segments
  useEffect(() => {
    if (!fieldPath) {
      setSegments([])
      return
    }

    const parts = fieldPath.split('.')
    const newSegments: PathSegment[] = []

    for (const part of parts) {
      // Check if it's an array access (e.g., "orders[0]")
      const arrayMatch = part.match(/^(.+)\[(\d+)\]$/)
      if (arrayMatch) {
        newSegments.push({
          fieldName: arrayMatch[1],
          fieldType: 'array',
          isArray: true,
          arrayIndex: parseInt(arrayMatch[2], 10),
        })
      } else {
        newSegments.push({
          fieldName: part,
          fieldType: 'string', // Will be resolved from object type
        })
      }
    }

    setSegments(newSegments)
  }, [fieldPath])

  // Get available fields for current path
  const getAvailableFields = (currentPath: string[]): FieldDefinition[] => {
    if (!objectTypeId || !objectTypes) return []

    const objectType = objectTypes.get(objectTypeId)
    if (!objectType) return []

    let currentFields = objectType.fields
    let currentField: FieldDefinition | undefined

    // Navigate through nested path
    for (const part of currentPath) {
      currentField = currentFields.find((f) => f.name === part)
      if (!currentField) return []

      if (currentField.type === 'object' && currentField.validation?.objectTypeId) {
        const nestedObjectType = objectTypes.get(currentField.validation.objectTypeId)
        if (nestedObjectType) {
          currentFields = nestedObjectType.fields
        } else {
          return []
        }
      } else if (currentField.type === 'array' && currentField.validation?.itemObjectTypeId) {
        const arrayItemObjectType = objectTypes.get(currentField.validation.itemObjectTypeId)
        if (arrayItemObjectType) {
          currentFields = arrayItemObjectType.fields
        } else {
          return []
        }
      } else {
        return []
      }
    }

    return currentFields
  }

  // Build field path from segments
  const buildFieldPath = (newSegments: PathSegment[]): string => {
    return newSegments
      .map((seg) => {
        if (seg.isArray && seg.arrayIndex !== undefined) {
          return `${seg.fieldName}[${seg.arrayIndex}]`
        }
        return seg.fieldName
      })
      .filter(Boolean)
      .join('.')
  }

  // Handle segment change
  const handleSegmentChange = (index: number, fieldName: string) => {
    const newSegments = [...segments]
    
    // Get available fields up to this point
    const pathUpToHere = newSegments.slice(0, index).map((s) => s.fieldName)
    const availableFields = getAvailableFields(pathUpToHere)
    const selectedField = availableFields.find((f) => f.name === fieldName)

    if (selectedField) {
      newSegments[index] = {
        fieldName,
        fieldType: selectedField.type,
        isArray: selectedField.type === 'array',
      }

      // Remove segments after this one (path has changed)
      newSegments.splice(index + 1)

      // If it's an object or array, we can add another segment
      if (selectedField.type === 'object' || selectedField.type === 'array') {
        // Don't auto-add, let user click "Add Field"
      }
    }

    setSegments(newSegments)
    onChange(buildFieldPath(newSegments))
  }

  // Handle array index change
  const handleArrayIndexChange = (index: number, arrayIndex: number) => {
    const newSegments = [...segments]
    if (newSegments[index]) {
      newSegments[index] = {
        ...newSegments[index],
        arrayIndex,
      }
      setSegments(newSegments)
      onChange(buildFieldPath(newSegments))
    }
  }

  // Add new segment
  const handleAddSegment = () => {
    const pathUpToHere = segments.map((s) => s.fieldName)
    const availableFields = getAvailableFields(pathUpToHere)

    if (availableFields.length > 0) {
      const newSegments = [
        ...segments,
        {
          fieldName: '',
          fieldType: 'string',
        },
      ]
      setSegments(newSegments)
    }
  }

  // Remove segment
  const handleRemoveSegment = (index: number) => {
    const newSegments = segments.slice(0, index)
    setSegments(newSegments)
    onChange(buildFieldPath(newSegments))
  }

  // Get available fields for a segment
  const getSegmentAvailableFields = (index: number): FieldDefinition[] => {
    if (index === 0) {
      // First segment - get fields from root object type
      if (!objectTypeId || !objectTypes) return []
      const objectType = objectTypes.get(objectTypeId)
      return objectType?.fields || []
    }

    // Get fields from parent path
    const pathUpToHere = segments.slice(0, index).map((s) => s.fieldName)
    return getAvailableFields(pathUpToHere)
  }

  // Check if can add more segments
  const canAddMore = useMemo(() => {
    if (segments.length === 0) {
      if (!objectTypeId || !objectTypes) return false
      const objectType = objectTypes.get(objectTypeId)
      return (objectType?.fields.length || 0) > 0
    }

    const lastSegment = segments[segments.length - 1]
    if (!lastSegment.fieldName) return false

    const pathUpToHere = segments.map((s) => s.fieldName)
    const availableFields = getAvailableFields(pathUpToHere)
    return availableFields.length > 0
  }, [segments, objectTypeId, objectTypes])

  return (
    <div className={`space-y-3 ${className || ""}`}>
      <div className="flex items-center justify-between">
        <Label>Field Path</Label>
        {canAddMore && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleAddSegment}
          >
            <Plus className="h-4 w-4 mr-1" />
            Add Field
          </Button>
        )}
      </div>

      {/* Breadcrumb display */}
      {segments.length > 0 && (
        <div className="flex items-center space-x-1 text-sm text-secondary-600 flex-wrap">
          {objectTypeId && (
            <>
              <span className="font-medium">{objectTypeId}</span>
              <ChevronRight className="h-3 w-3" />
            </>
          )}
          {segments.map((seg, index) => (
            <div key={index} className="flex items-center space-x-1">
              <Badge variant="outline">{seg.fieldName}</Badge>
              {index < segments.length - 1 && <ChevronRight className="h-3 w-3" />}
            </div>
          ))}
        </div>
      )}

      {/* Segment inputs */}
      <div className="space-y-2">
        {segments.length === 0 && (
          <div className="text-sm text-secondary-500">
            Click "Add Field" to start building the field path
          </div>
        )}

        {segments.map((segment, index) => {
          const availableFields = getSegmentAvailableFields(index)
          const selectedField = availableFields.find((f) => f.name === segment.fieldName)

          return (
            <div key={index} className="flex items-center space-x-2">
              <Select
                value={segment.fieldName || ""}
                onValueChange={(value) => handleSegmentChange(index, value)}
              >
                <SelectTrigger className="flex-1">
                  <SelectValue placeholder={`Select field ${index + 1}`} />
                </SelectTrigger>
                <SelectContent>
                  {availableFields.map((field) => (
                    <SelectItem key={field.name} value={field.name}>
                      <div className="flex items-center space-x-2">
                        <span>{field.displayName || field.name}</span>
                        <span className="text-xs text-secondary-500">({field.type})</span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              {/* Array index input */}
              {segment.isArray && (
                <Input
                  type="number"
                  min="0"
                  value={segment.arrayIndex ?? 0}
                  onChange={(e) => handleArrayIndexChange(index, parseInt(e.target.value, 10) || 0)}
                  className="w-20"
                  placeholder="0"
                />
              )}

              {/* Remove button */}
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => handleRemoveSegment(index)}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          )
        })}
      </div>

      {/* Current path display */}
      {segments.length > 0 && (
        <div className="text-xs text-secondary-500 p-2 bg-secondary-50 rounded">
          <span className="font-medium">Path: </span>
          <code className="text-xs">{buildFieldPath(segments)}</code>
        </div>
      )}
    </div>
  )
}

