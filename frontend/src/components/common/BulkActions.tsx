import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu"
import { Progress } from "@/components/ui/progress"
import { Trash2, Archive, Play, Pause, MoreVertical, CheckSquare, Loader2, Download } from "lucide-react"
import { useConfirmDialog } from "./ConfirmDialog"

interface BulkActionsProps<T> {
  selectedItems: T[]
  onBulkDelete?: (items: T[]) => void
  onBulkUpdateStatus?: (items: T[], status: string) => void
  onBulkExport?: (format: "csv" | "json") => void
  getItemId: (item: T) => string
  getItemName?: (item: T) => string
}

export function BulkActions<T>({
  selectedItems,
  onBulkDelete,
  onBulkUpdateStatus,
  onBulkExport,
  getItemId: _getItemId,
  getItemName: _getItemName,
}: BulkActionsProps<T>) {
  const { confirm } = useConfirmDialog()
  const [isProcessing, setIsProcessing] = useState(false)
  const [progress, setProgress] = useState(0)
  const count = selectedItems.length

  if (count === 0) {
    return null
  }

  const handleBulkDelete = async () => {
    const confirmed = await confirm({
      title: "Delete Selected Items",
      description: `Are you sure you want to delete ${count} item${count > 1 ? "s" : ""}? This action cannot be undone.`,
      variant: "destructive",
      confirmText: "Delete",
    })

    if (confirmed && onBulkDelete) {
      setIsProcessing(true)
      setProgress(0)
      try {
        // Simulate progress
        const interval = setInterval(() => {
          setProgress((prev) => {
            if (prev >= 90) {
              clearInterval(interval)
              return 90
            }
            return prev + 10
          })
        }, 100)
        await onBulkDelete(selectedItems)
        setProgress(100)
        setTimeout(() => {
          setIsProcessing(false)
          setProgress(0)
        }, 500)
      } catch (error) {
        setIsProcessing(false)
        setProgress(0)
      }
    }
  }

  const handleBulkStatusUpdate = async (status: string) => {
    const statusLabel = status.charAt(0).toUpperCase() + status.slice(1)
    const confirmed = await confirm({
      title: `Update Status to ${statusLabel}`,
      description: `Are you sure you want to update ${count} item${count > 1 ? "s" : ""} to ${statusLabel}?`,
      confirmText: "Update",
    })

    if (confirmed && onBulkUpdateStatus) {
      setIsProcessing(true)
      setProgress(0)
      try {
        // Simulate progress
        const interval = setInterval(() => {
          setProgress((prev) => {
            if (prev >= 90) {
              clearInterval(interval)
              return 90
            }
            return prev + 10
          })
        }, 100)
        await onBulkUpdateStatus(selectedItems, status)
        setProgress(100)
        setTimeout(() => {
          setIsProcessing(false)
          setProgress(0)
        }, 500)
      } catch (error) {
        setIsProcessing(false)
        setProgress(0)
      }
    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center space-x-2 p-4 bg-primary-50 border border-primary-200 rounded-lg">
        <div className="flex items-center space-x-2 flex-1">
          {isProcessing ? (
            <Loader2 className="h-4 w-4 text-primary-600 animate-spin" />
          ) : (
            <CheckSquare className="h-4 w-4 text-primary-600" />
          )}
          <span className="text-sm font-medium text-primary-900">
            {count} item{count > 1 ? "s" : ""} selected
            {isProcessing && " (processing...)"}
          </span>
        </div>
        <div className="flex items-center space-x-2">
          {onBulkExport && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="sm" disabled={isProcessing}>
                  <Download className="h-4 w-4 mr-2" />
                  Export
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => onBulkExport("json")} disabled={isProcessing}>
                  Export as JSON
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => onBulkExport("csv")} disabled={isProcessing}>
                  Export as CSV
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
          {onBulkDelete && (
            <Button variant="danger" size="sm" onClick={handleBulkDelete} disabled={isProcessing}>
              {isProcessing ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Trash2 className="h-4 w-4 mr-2" />
              )}
              Delete
            </Button>
          )}
          {onBulkUpdateStatus && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="sm" disabled={isProcessing}>
                  <MoreVertical className="h-4 w-4 mr-2" />
                  Update Status
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => handleBulkStatusUpdate("active")} disabled={isProcessing}>
                  <Play className="h-4 w-4 mr-2" />
                  Set Active
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => handleBulkStatusUpdate("inactive")} disabled={isProcessing}>
                  <Pause className="h-4 w-4 mr-2" />
                  Set Inactive
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => handleBulkStatusUpdate("paused")} disabled={isProcessing}>
                  <Pause className="h-4 w-4 mr-2" />
                  Set Paused
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => handleBulkStatusUpdate("archived")} disabled={isProcessing}>
                  <Archive className="h-4 w-4 mr-2" />
                  Archive
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>
      {isProcessing && (
        <div className="px-4">
          <Progress value={progress} className="h-2" />
        </div>
      )}
    </div>
  )
}

