import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Edit, Trash2, GripVertical, Info } from "lucide-react"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

interface FieldCardProps {
  field: FieldDefinition
  index: number
  onEdit: () => void
  onDelete: () => void
  onDragStart?: (index: number) => void
  onDragOver?: (e: React.DragEvent, index: number) => void
  onDragEnd?: () => void
  isDragging?: boolean
}

export function FieldCard({
  field,
  index,
  onEdit,
  onDelete,
  onDragStart,
  onDragOver,
  onDragEnd,
  isDragging,
}: FieldCardProps) {
  const getFieldTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      string: "bg-blue-50 text-blue-700 border-blue-200",
      number: "bg-green-50 text-green-700 border-green-200",
      boolean: "bg-purple-50 text-purple-700 border-purple-200",
      date: "bg-orange-50 text-orange-700 border-orange-200",
      datetime: "bg-orange-50 text-orange-700 border-orange-200",
      email: "bg-cyan-50 text-cyan-700 border-cyan-200",
      phone: "bg-teal-50 text-teal-700 border-teal-200",
      url: "bg-indigo-50 text-indigo-700 border-indigo-200",
      json: "bg-gray-50 text-gray-700 border-gray-200",
      array: "bg-pink-50 text-pink-700 border-pink-200",
      object: "bg-yellow-50 text-yellow-700 border-yellow-200",
    }
    return colors[type] || "bg-gray-50 text-gray-700 border-gray-200"
  }

  return (
    <div
      draggable={!!onDragStart}
      onDragStart={() => onDragStart?.(index)}
      onDragOver={(e) => onDragOver?.(e, index)}
      onDragEnd={onDragEnd}
      className={`
        group relative bg-white border-2 rounded-lg p-4 transition-all duration-200
        ${isDragging ? "opacity-50 border-primary-300" : "border-secondary-200 hover:border-secondary-300"}
        hover:shadow-md cursor-pointer
      `}
    >
      {/* Drag Handle */}
      {onDragStart && (
        <div className="absolute left-2 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity">
          <GripVertical className="h-4 w-4 text-secondary-400 cursor-move" />
        </div>
      )}

      <div className="flex items-start justify-between gap-4">
        {/* Field Info */}
        <div className="flex-1 min-w-0 space-y-2">
          <div className="flex items-center gap-2 flex-wrap">
            <h4 className="font-semibold text-sm text-secondary-900 truncate">
              {field.displayName || field.name}
            </h4>
            <Badge
              variant="outline"
              className={`text-xs font-medium ${getFieldTypeColor(field.type)}`}
            >
              {field.type}
            </Badge>
            {field.required && (
              <Badge variant="outline" className="text-xs border-error-300 text-error-700 bg-error-50">
                Required
              </Badge>
            )}
          </div>

          {/* Field Name (Technical) */}
          <div className="flex items-center gap-2">
            <code className="text-xs text-secondary-500 font-mono bg-secondary-50 px-2 py-0.5 rounded">
              {field.name}
            </code>
            {field.description && (
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Info className="h-3.5 w-3.5 text-secondary-400 cursor-help" />
                  </TooltipTrigger>
                  <TooltipContent className="max-w-xs">
                    <p className="text-xs">{field.description}</p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            )}
          </div>

          {/* Field Details */}
          <div className="flex flex-wrap gap-2 text-xs text-secondary-600">
            {field.defaultValue !== undefined && (
              <span>
                Default: <code className="text-secondary-700">{String(field.defaultValue)}</code>
              </span>
            )}
            {field.validation?.minLength && (
              <span>Min length: {field.validation.minLength}</span>
            )}
            {field.validation?.maxLength && (
              <span>Max length: {field.validation.maxLength}</span>
            )}
            {field.validation?.min !== undefined && (
              <span>Min: {field.validation.min}</span>
            )}
            {field.validation?.max !== undefined && (
              <span>Max: {field.validation.max}</span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <Button
            variant="ghost"
            size="sm"
            onClick={(e) => {
              e.stopPropagation()
              onEdit()
            }}
            className="h-8 w-8 p-0"
          >
            <Edit className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={(e) => {
              e.stopPropagation()
              onDelete()
            }}
            className="h-8 w-8 p-0 text-error-600 hover:text-error-700 hover:bg-error-50"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  )
}

