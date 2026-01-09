import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Download, Trash2, X } from "lucide-react"

interface ExecutionBulkActionsProps {
  selectedCount: number
  onClearSelection: () => void
  onBulkExport?: (format: "csv" | "json") => void
  onBulkDelete?: () => void
}

export function ExecutionBulkActions({
  selectedCount,
  onClearSelection,
  onBulkExport,
  onBulkDelete,
}: ExecutionBulkActionsProps) {
  if (selectedCount === 0) {
    return null
  }

  return (
    <div className="flex items-center justify-between p-4 bg-primary-50 border border-primary-200 rounded-lg mb-4">
      <div className="flex items-center space-x-2">
        <span className="text-sm font-medium text-primary-900">
          {selectedCount} execution{selectedCount !== 1 ? "s" : ""} selected
        </span>
        <Button variant="ghost" size="sm" onClick={onClearSelection}>
          <X className="h-4 w-4" />
        </Button>
      </div>
      <div className="flex items-center space-x-2">
        {onBulkExport && (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm">
                <Download className="h-4 w-4 mr-2" />
                Export
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => onBulkExport("json")}>
                Export as JSON
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onBulkExport("csv")}>
                Export as CSV
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}
        {onBulkDelete && (
          <Button variant="danger" size="sm" onClick={onBulkDelete}>
            <Trash2 className="h-4 w-4 mr-2" />
            Delete
          </Button>
        )}
      </div>
    </div>
  )
}

