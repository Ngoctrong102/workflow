import { useState, useMemo, useRef, useEffect } from "react"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Badge } from "@/components/ui/badge"
import { Check, ChevronsUpDown, Search } from "lucide-react"
import { cn } from "@/lib/utils"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

export interface FieldAutocompleteProps {
  objectTypeId: string | null
  value: string
  onChange: (fieldPath: string) => void
  objectTypes?: Map<string, { name: string; fields: FieldDefinition[] }>
  allowedTypes?: string[]
  placeholder?: string
  className?: string
}

export function FieldAutocomplete({
  objectTypeId,
  value,
  onChange,
  objectTypes,
  allowedTypes,
  placeholder = "Search fields...",
  className,
}: FieldAutocompleteProps) {
  const [open, setOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const inputRef = useRef<HTMLInputElement>(null)

  // Get all available fields from object type and nested object types
  const allFields = useMemo(() => {
    if (!objectTypeId || !objectTypes) return []

    const objectType = objectTypes.get(objectTypeId)
    if (!objectType) return []

    const fields: Array<{
      path: string
      field: FieldDefinition
      objectTypeId: string
      displayPath: string
    }> = []

    // Recursively collect fields from object type and nested types
    const collectFields = (
      currentObjectTypeId: string,
      currentPath: string[],
      visited: Set<string>
    ) => {
      if (visited.has(currentObjectTypeId)) return // Prevent circular references
      visited.add(currentObjectTypeId)

      const currentObjectType = objectTypes.get(currentObjectTypeId)
      if (!currentObjectType) return

      for (const field of currentObjectType.fields) {
        const fieldPath = [...currentPath, field.name].join('.')
        const displayPath = currentPath.length > 0 
          ? `${currentPath.join('.')}.${field.displayName || field.name}`
          : (field.displayName || field.name)

        fields.push({
          path: fieldPath,
          field,
          objectTypeId: currentObjectTypeId,
          displayPath,
        })

        // If field is an object type, recurse
        if (field.type === 'object' && field.validation?.objectTypeId) {
          collectFields(
            field.validation.objectTypeId,
            [...currentPath, field.name],
            visited
          )
        }

        // If field is an array of objects, recurse
        if (field.type === 'array' && field.validation?.itemObjectTypeId) {
          collectFields(
            field.validation.itemObjectTypeId,
            [...currentPath, field.name],
            visited
          )
        }
      }
    }

    collectFields(objectTypeId, [], new Set())

    // Filter by allowed types if specified
    if (allowedTypes && allowedTypes.length > 0) {
      return fields.filter((item) => allowedTypes.includes(item.field.type))
    }

    return fields
  }, [objectTypeId, objectTypes, allowedTypes])

  // Filter fields based on search query
  const filteredFields = useMemo(() => {
    if (!searchQuery) return allFields

    const query = searchQuery.toLowerCase()
    return allFields.filter((item) => {
      const fieldName = item.field.name.toLowerCase()
      const displayName = (item.field.displayName || "").toLowerCase()
      const path = item.path.toLowerCase()
      const description = (item.field.description || "").toLowerCase()

      return (
        fieldName.includes(query) ||
        displayName.includes(query) ||
        path.includes(query) ||
        description.includes(query)
      )
    })
  }, [allFields, searchQuery])

  // Handle field selection
  const handleSelect = (fieldPath: string) => {
    onChange(fieldPath)
    setOpen(false)
    setSearchQuery("")
  }

  // Handle keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setOpen(false)
      }
    }

    if (open) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  // Get current field display name
  const currentFieldDisplay = useMemo(() => {
    if (!value) return placeholder

    const field = allFields.find((f) => f.path === value)
    if (field) {
      return field.displayPath
    }

    return value
  }, [value, allFields, placeholder])

  if (!objectTypeId || allFields.length === 0) {
    return (
      <Input
        ref={inputRef}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className={className}
      />
    )
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className={cn("relative", className)}>
          <Input
            ref={inputRef}
            value={currentFieldDisplay}
            onChange={(e) => {
              setSearchQuery(e.target.value)
              if (!open) setOpen(true)
            }}
            onFocus={() => setOpen(true)}
            placeholder={placeholder}
            className="w-full"
          />
          <ChevronsUpDown className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-secondary-400 pointer-events-none" />
        </div>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0" align="start">
        <div className="flex flex-col max-h-[300px]">
          {/* Search Input */}
          <div className="flex items-center border-b px-3">
            <Search className="mr-2 h-4 w-4 shrink-0 opacity-50" />
            <Input
              placeholder="Search fields..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="border-0 focus-visible:ring-0 focus-visible:ring-offset-0"
            />
          </div>
          
          {/* Field List */}
          <div className="overflow-y-auto max-h-[250px]">
            {filteredFields.length === 0 ? (
              <div className="py-6 text-center text-sm text-secondary-500">
                No fields found.
              </div>
            ) : (
              <div className="p-1">
                {filteredFields.map((item) => {
                  const isSelected = item.path === value
                  return (
                    <div
                      key={item.path}
                      onClick={() => handleSelect(item.path)}
                      className={cn(
                        "relative flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none hover:bg-primary-50 hover:text-primary-950",
                        isSelected && "bg-primary-50 text-primary-950"
                      )}
                    >
                      <div className="flex items-center justify-between w-full">
                        <div className="flex flex-col space-y-1 flex-1">
                          <div className="flex items-center space-x-2">
                            <span className="font-medium">{item.field.displayName || item.field.name}</span>
                            <Badge variant="outline" className="text-xs">
                              {item.field.type}
                            </Badge>
                          </div>
                          <span className="text-xs text-secondary-500">{item.path}</span>
                          {item.field.description && (
                            <span className="text-xs text-secondary-400">{item.field.description}</span>
                          )}
                        </div>
                        {isSelected && <Check className="h-4 w-4 text-primary-600" />}
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}

