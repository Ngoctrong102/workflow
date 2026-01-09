import { useState, useEffect, useMemo } from "react"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { HelpTooltip } from "@/components/common/HelpTooltip"
import { AlertCircle, ChevronRight } from "lucide-react"
import { FieldPathBuilder } from "./FieldPathBuilder"
import { FieldAutocomplete } from "./FieldAutocomplete"
import { parseFieldReference, formatFieldReference, type FieldReference } from "@/utils/fieldReferenceUtils"
import { getFieldType, type FieldDefinition } from "@/utils/fieldTypeValidator"

export interface FieldSelectorProps {
  value: string | FieldReference | null | undefined
  onChange: (value: FieldReference | string) => void
  objectTypeId?: string
  fieldPath?: string
  required?: boolean
  allowedTypes?: string[]
  label?: string
  description?: string
  placeholder?: string
  showManualEntry?: boolean
  objectTypes?: Map<string, { name: string; fields: FieldDefinition[] }>
  className?: string
}

export function FieldSelector({
  value,
  onChange,
  objectTypeId: initialObjectTypeId,
  fieldPath: initialFieldPath,
  required = false,
  allowedTypes,
  label = "Field",
  description,
  placeholder = "Select or enter field",
  showManualEntry = true,
  objectTypes,
  className,
}: FieldSelectorProps) {
  const [selectedObjectTypeId, setSelectedObjectTypeId] = useState<string | null>(
    initialObjectTypeId || null
  )
  const [selectedFieldPath, setSelectedFieldPath] = useState<string>("")
  const [manualEntry, setManualEntry] = useState<string>("")
  const [useManualEntry, setUseManualEntry] = useState(false)
  const [showPathBuilderModal, setShowPathBuilderModal] = useState(false)

  // Parse initial value
  useEffect(() => {
    const parsed = parseFieldReference(value)
    if (parsed) {
      setSelectedObjectTypeId(parsed.objectTypeId || initialObjectTypeId || null)
      setSelectedFieldPath(parsed.fieldPath || initialFieldPath || "")
      if (typeof value === 'string' && !parsed.objectTypeId) {
        setManualEntry(value)
        setUseManualEntry(true)
      } else {
        setUseManualEntry(false)
      }
    } else {
      setSelectedObjectTypeId(initialObjectTypeId || null)
      setSelectedFieldPath(initialFieldPath || "")
      setManualEntry(typeof value === 'string' ? value : "")
      setUseManualEntry(typeof value === 'string' && !!value)
    }
  }, [value, initialObjectTypeId, initialFieldPath])

  // Available object types
  const availableObjectTypes = useMemo(() => {
    if (!objectTypes) return []
    return Array.from(objectTypes.entries()).map(([id, def]) => ({
      id,
      name: def.name,
    }))
  }, [objectTypes])

  // Available fields for selected object type
  const availableFields = useMemo(() => {
    if (!selectedObjectTypeId || !objectTypes) return []
    const objectType = objectTypes.get(selectedObjectTypeId)
    if (!objectType) return []

    let fields = objectType.fields

    // Filter by allowed types if specified
    if (allowedTypes && allowedTypes.length > 0) {
      fields = fields.filter((field) => allowedTypes.includes(field.type))
    }

    return fields
  }, [selectedObjectTypeId, objectTypes, allowedTypes])

  // Handle object type selection
  const handleObjectTypeChange = (newObjectTypeId: string) => {
    setSelectedObjectTypeId(newObjectTypeId)
    setSelectedFieldPath("")
    setUseManualEntry(false)
    
    // If object type is selected, use new format
    if (newObjectTypeId) {
      onChange({
        objectTypeId: newObjectTypeId,
        fieldPath: "",
      })
    }
  }

  // Handle field selection
  const handleFieldChange = (newFieldPath: string) => {
    setSelectedFieldPath(newFieldPath)
    setUseManualEntry(false)

    if (selectedObjectTypeId) {
      onChange({
        objectTypeId: selectedObjectTypeId,
        fieldPath: newFieldPath,
      })
    } else {
      // Fallback to string format if no object type
      onChange(newFieldPath)
    }
  }

  // Handle manual entry
  const handleManualEntryChange = (newValue: string) => {
    setManualEntry(newValue)
    setUseManualEntry(true)
    onChange(newValue)
  }

  // Handle path builder result
  const handlePathBuilderChange = (newPath: string) => {
    setSelectedFieldPath(newPath)
    setShowPathBuilder(false)
    setUseManualEntry(false)

    if (selectedObjectTypeId) {
      onChange({
        objectTypeId: selectedObjectTypeId,
        fieldPath: newPath,
      })
    } else {
      onChange(newPath)
    }
  }

  // Get field type for display
  const selectedFieldType = useMemo(() => {
    if (!selectedObjectTypeId || !selectedFieldPath || !objectTypes) return null
    return getFieldType(selectedObjectTypeId, selectedFieldPath, objectTypes)
  }, [selectedObjectTypeId, selectedFieldPath, objectTypes])

  // Get field definition for selected field
  const selectedFieldDef = useMemo(() => {
    if (!selectedObjectTypeId || !selectedFieldPath || !objectTypes) return null
    const objectType = objectTypes.get(selectedObjectTypeId)
    if (!objectType) return null

    const parts = selectedFieldPath.split('.')
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
          break
        }
      } else {
        break
      }
    }

    return currentField || null
  }, [selectedObjectTypeId, selectedFieldPath, objectTypes])

  const isNestedPath = selectedFieldPath.includes('.')

  return (
    <div className={`space-y-2 ${className || ""}`}>
      <div className="flex items-center space-x-2">
        <Label htmlFor="field-selector" className={required ? "after:content-['*'] after:ml-0.5 after:text-error-600" : ""}>
          {label}
        </Label>
        {description && <HelpTooltip content={description} />}
      </div>

      {/* Object Type Selector (if object types available) */}
      {availableObjectTypes.length > 0 && (
        <div className="space-y-2">
          <Select
            value={selectedObjectTypeId || ""}
            onValueChange={handleObjectTypeChange}
            disabled={useManualEntry}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select object type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">None (manual entry)</SelectItem>
              {availableObjectTypes.map((type) => (
                <SelectItem key={type.id} value={type.id}>
                  {type.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      )}

      {/* Field Selection Mode */}
      {!useManualEntry && selectedObjectTypeId && availableFields.length > 0 && (
        <div className="space-y-2">
          {/* Simple field selector for non-nested fields */}
          {!isNestedPath && (
            <div className="flex space-x-2">
              <Select value={selectedFieldPath} onValueChange={handleFieldChange}>
                <SelectTrigger>
                  <SelectValue placeholder="Select field" />
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
              {availableFields.some((f) => f.type === 'object') && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setShowPathBuilderModal(true)}
                  className="whitespace-nowrap"
                >
                  <ChevronRight className="h-4 w-4 mr-1" />
                  Nested
                </Button>
              )}
            </div>
          )}

          {/* Path builder for nested fields */}
          {isNestedPath && (
            <FieldPathBuilder
              objectTypeId={selectedObjectTypeId}
              fieldPath={selectedFieldPath}
              onChange={handlePathBuilderChange}
              objectTypes={objectTypes}
            />
          )}

          {/* Field Autocomplete (alternative to dropdown) */}
          <FieldAutocomplete
            objectTypeId={selectedObjectTypeId}
            value={selectedFieldPath}
            onChange={handleFieldChange}
            objectTypes={objectTypes}
            allowedTypes={allowedTypes}
            placeholder="Search fields..."
          />
        </div>
      )}

      {/* Manual Entry Mode */}
      {useManualEntry && (
        <div className="space-y-2">
          <Input
            value={manualEntry}
            onChange={(e) => handleManualEntryChange(e.target.value)}
            placeholder={placeholder}
          />
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => {
              setUseManualEntry(false)
              // Try to parse and set object type/field
              const parsed = parseFieldReference(manualEntry)
              if (parsed && parsed.objectTypeId) {
                setSelectedObjectTypeId(parsed.objectTypeId)
                setSelectedFieldPath(parsed.fieldPath)
              }
            }}
          >
            Use Field Selector
          </Button>
        </div>
      )}

      {/* Toggle between manual entry and selector */}
      {showManualEntry && !useManualEntry && (
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={() => setUseManualEntry(true)}
          className="text-xs"
        >
          Enter manually
        </Button>
      )}

      {/* Field Information Display */}
      {selectedFieldDef && (
        <div className="text-xs text-secondary-600 space-y-1">
          {selectedFieldDef.description && (
            <p>{selectedFieldDef.description}</p>
          )}
          {selectedFieldType && (
            <p className="font-medium">Type: {selectedFieldType}</p>
          )}
          {selectedFieldDef.examples && selectedFieldDef.examples.length > 0 && (
            <p>Examples: {selectedFieldDef.examples.join(", ")}</p>
          )}
        </div>
      )}

      {/* Validation Feedback */}
      {required && !value && (
        <div className="flex items-center space-x-1 text-sm text-error-600">
          <AlertCircle className="h-4 w-4" />
          <span>This field is required</span>
        </div>
      )}

      {/* Path Builder Modal/Dialog */}
      {showPathBuilderModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
            <h3 className="text-lg font-semibold mb-4">Build Nested Field Path</h3>
            <FieldPathBuilder
              objectTypeId={selectedObjectTypeId}
              fieldPath={selectedFieldPath}
              onChange={handlePathBuilderChange}
              objectTypes={objectTypes}
            />
            <div className="flex justify-end space-x-2 mt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowPathBuilderModal(false)}
              >
                Close
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

