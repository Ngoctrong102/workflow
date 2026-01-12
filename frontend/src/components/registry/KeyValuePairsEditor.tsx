import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { MvelInputEditor } from "./MvelInputEditor"
import { Plus, Trash2 } from "lucide-react"
import { cn } from "@/lib/utils"
import type { SchemaDefinition } from "./SchemaEditor"

export interface KeyValuePair {
  key: string
  value: string
}

interface KeyValuePairsEditorProps {
  pairs: Record<string, string>
  onChange: (pairs: Record<string, string>) => void
  keyPlaceholder?: string
  valuePlaceholder?: string
  emptyMessage?: string
  className?: string
  inputSchema?: SchemaDefinition[]
}

export function KeyValuePairsEditor({
  pairs,
  onChange,
  keyPlaceholder = "Header name",
  valuePlaceholder = "Header value (supports ${variable})",
  emptyMessage = "No key-value pairs. Click 'Add' to add one.",
  className,
  inputSchema = [],
}: KeyValuePairsEditorProps) {
  // Convert Record to array for editing
  const [pairsArray, setPairsArray] = useState<KeyValuePair[]>(() => {
    return Object.entries(pairs).map(([key, value]) => ({ key, value }))
  })

  const handleAdd = () => {
    const newPairs = [...pairsArray, { key: "", value: "" }]
    setPairsArray(newPairs)
    updatePairs(newPairs)
  }

  const handleRemove = (index: number) => {
    const newPairs = pairsArray.filter((_, i) => i !== index)
    setPairsArray(newPairs)
    updatePairs(newPairs)
  }

  const handleKeyChange = (index: number, key: string) => {
    const newPairs = [...pairsArray]
    newPairs[index].key = key
    setPairsArray(newPairs)
    updatePairs(newPairs)
  }

  const handleValueChange = (index: number, value: string) => {
    const newPairs = [...pairsArray]
    newPairs[index].value = value
    setPairsArray(newPairs)
    updatePairs(newPairs)
  }

  const updatePairs = (newPairs: KeyValuePair[]) => {
    // Convert array back to Record, filtering out empty keys
    const newRecord: Record<string, string> = {}
    const seenKeys = new Set<string>()
    const duplicateKeys: string[] = []

    newPairs.forEach((pair) => {
      if (pair.key.trim()) {
        if (seenKeys.has(pair.key.trim())) {
          duplicateKeys.push(pair.key.trim())
        } else {
          seenKeys.add(pair.key.trim())
          newRecord[pair.key.trim()] = pair.value || ""
        }
      }
    })

    onChange(newRecord)
  }

  // Check for duplicate keys
  const getDuplicateKeys = (): Set<string> => {
    const seen = new Set<string>()
    const duplicates = new Set<string>()
    pairsArray.forEach((pair) => {
      const key = pair.key.trim()
      if (key) {
        if (seen.has(key)) {
          duplicates.add(key)
        } else {
          seen.add(key)
        }
      }
    })
    return duplicates
  }

  const duplicateKeys = getDuplicateKeys()

  return (
    <div className={cn("space-y-3", className)}>
      {pairsArray.length === 0 ? (
        <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
          {emptyMessage}
        </div>
      ) : (
        <div className="space-y-3">
          {pairsArray.map((pair, index) => {
            const isDuplicate = duplicateKeys.has(pair.key.trim())
            return (
              <div key={index} className="flex gap-2 items-start">
                <div className="flex-1 space-y-1">
                  <Input
                    placeholder={keyPlaceholder}
                    value={pair.key}
                    onChange={(e) => handleKeyChange(index, e.target.value)}
                    className={cn(
                      isDuplicate && pair.key.trim() && "border-error-500 focus-visible:ring-error-500"
                    )}
                  />
                  {isDuplicate && pair.key.trim() && (
                    <p className="text-xs text-error-600">Duplicate key</p>
                  )}
                </div>
                <div className="flex-1">
                  <MvelInputEditor
                    value={pair.value}
                    onChange={(value) => handleValueChange(index, value)}
                    inputSchema={inputSchema}
                    placeholder={valuePlaceholder}
                    type="input"
                  />
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => handleRemove(index)}
                  className="mt-0 cursor-pointer text-error-600 hover:text-error-700 hover:bg-error-50"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            )
          })}
        </div>
      )}

      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={handleAdd}
        className="cursor-pointer"
      >
        <Plus className="h-4 w-4 mr-2" />
        Add Pair
      </Button>
    </div>
  )
}

