import { useState } from "react"
import { FieldCard } from "./FieldCard"
import type { FieldDefinition } from "@/utils/fieldTypeValidator"

interface FieldListProps {
  fields: FieldDefinition[]
  onEdit: (field: FieldDefinition, index: number) => void
  onDelete: (index: number) => void
  onReorder?: (fromIndex: number, toIndex: number) => void
}

export function FieldList({ fields, onEdit, onDelete, onReorder }: FieldListProps) {
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null)

  const handleDragStart = (index: number) => {
    setDraggedIndex(index)
  }

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault()
    if (draggedIndex === null || draggedIndex === index) return

    if (onReorder) {
      onReorder(draggedIndex, index)
      setDraggedIndex(index)
    }
  }

  const handleDragEnd = () => {
    setDraggedIndex(null)
  }

  if (fields.length === 0) {
    return (
      <div className="text-center py-12 border-2 border-dashed border-secondary-200 rounded-lg bg-secondary-50/30">
        <div className="space-y-2">
          <p className="text-sm font-medium text-secondary-700">No fields defined</p>
          <p className="text-xs text-secondary-500">Add your first field to get started</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {fields.map((field, index) => (
        <FieldCard
          key={field.name || index}
          field={field}
          index={index}
          onEdit={() => onEdit(field, index)}
          onDelete={() => onDelete(index)}
          onDragStart={onReorder ? handleDragStart : undefined}
          onDragOver={onReorder ? handleDragOver : undefined}
          onDragEnd={onReorder ? handleDragEnd : undefined}
          isDragging={draggedIndex === index}
        />
      ))}
    </div>
  )
}

