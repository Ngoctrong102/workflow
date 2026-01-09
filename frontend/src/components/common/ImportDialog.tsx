import { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, CheckCircle2 } from "lucide-react"
import { parseImportFile, checkImportConflicts, type ImportResult } from "@/utils/import"

interface ImportDialogProps<T> {
  open: boolean
  onOpenChange: (open: boolean) => void
  onImport: (data: T, options?: { overwrite?: boolean; skipConflicts?: boolean }) => Promise<void>
  validator: (data: unknown) => data is T
  existingItems?: T[]
  getItemId?: (item: T) => string
  getItemName?: (item: T) => string
  title?: string
  description?: string
}

export function ImportDialog<T extends { id?: string; name: string }>({
  open,
  onOpenChange,
  onImport,
  validator,
  existingItems = [],
  getItemId: _getItemId = (item) => (item as { id?: string }).id || "",
  getItemName,
  title = "Import",
  description = "Upload a JSON file to import",
}: ImportDialogProps<T>) {
  const [, setFile] = useState<File | null>(null)
  const [importResult, setImportResult] = useState<ImportResult<T> | null>(null)
  const [conflict, setConflict] = useState<{
    hasConflict: boolean
    conflictType?: "id" | "name"
    existingItem?: T
  } | null>(null)
  const [isImporting, setIsImporting] = useState(false)
  const [overwrite, setOverwrite] = useState(false)
  const [skipConflicts, setSkipConflicts] = useState(false)

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0]
    if (!selectedFile) return

    setFile(selectedFile)
    setImportResult(null)
    setConflict(null)

    const result = await parseImportFile<T>(selectedFile, validator)
    setImportResult(result)

    if (result.success && result.data) {
      const conflictCheck = checkImportConflicts(result.data, existingItems)
      setConflict(conflictCheck)
    }
  }

  const handleImport = async () => {
    if (!importResult?.success || !importResult.data) return

    setIsImporting(true)
    try {
      await onImport(importResult.data, { overwrite, skipConflicts })
      setFile(null)
      setImportResult(null)
      setConflict(null)
      onOpenChange(false)
    } catch (error) {
      console.error("Import failed:", error)
    } finally {
      setIsImporting(false)
    }
  }

  const handleCancel = () => {
    setFile(null)
    setImportResult(null)
    setConflict(null)
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="file">Select JSON File</Label>
            <Input
              id="file"
              type="file"
              accept=".json"
              onChange={handleFileChange}
              disabled={isImporting}
            />
          </div>

          {importResult && (
            <>
              {importResult.success ? (
                <Alert>
                  <CheckCircle2 className="h-4 w-4" />
                  <AlertDescription>File parsed successfully</AlertDescription>
                </Alert>
              ) : (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    {importResult.errors?.join(", ") || "Failed to parse file"}
                  </AlertDescription>
                </Alert>
              )}

              {conflict?.hasConflict && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    Conflict detected: {conflict.conflictType === "id" ? "ID" : "Name"} already exists
                    {conflict.existingItem && getItemName && (
                      <div className="mt-2 text-sm">
                        Existing: {getItemName(conflict.existingItem)}
                      </div>
                    )}
                  </AlertDescription>
                </Alert>
              )}

              {conflict?.hasConflict && (
                <div className="space-y-2">
                  <Label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={overwrite}
                      onChange={(e) => setOverwrite(e.target.checked)}
                      disabled={isImporting}
                    />
                    <span>Overwrite existing item</span>
                  </Label>
                  <Label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={skipConflicts}
                      onChange={(e) => setSkipConflicts(e.target.checked)}
                      disabled={isImporting}
                    />
                    <span>Skip conflicts</span>
                  </Label>
                </div>
              )}
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleCancel} disabled={isImporting}>
            Cancel
          </Button>
          <Button
            onClick={handleImport}
            disabled={!importResult?.success || isImporting || (conflict?.hasConflict && !overwrite && !skipConflicts)}
          >
            {isImporting ? "Importing..." : "Import"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

