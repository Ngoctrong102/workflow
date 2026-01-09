import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, Database, Trash2, Calendar, AlertCircle } from "lucide-react"
import {
  useDataRetentionSettings,
  useUpdateDataRetentionSettings,
  useTriggerDataCleanup,
} from "@/hooks/use-data-retention"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { format } from "date-fns"

export function DataRetentionPanel() {
  const [retentionDays, setRetentionDays] = useState<number>(90)
  const [enabled, setEnabled] = useState(false)
  const { data, isLoading, error } = useDataRetentionSettings()
  const updateSettings = useUpdateDataRetentionSettings()
  const triggerCleanup = useTriggerDataCleanup()
  const { confirm } = useConfirmDialog()

  // Sync with fetched data
  useEffect(() => {
    if (data) {
      setRetentionDays(data.retentionDays)
      setEnabled(data.enabled)
    }
  }, [data])

  const handleSave = async () => {
    await updateSettings.mutateAsync({
      enabled,
      retentionDays,
    })
  }

  const handleCleanup = async () => {
    const confirmed = await confirm({
      title: "Trigger Data Cleanup",
      description: `This will permanently delete analytics data older than ${retentionDays} days. This action cannot be undone.`,
      variant: "destructive",
      confirmText: "Cleanup",
    })

    if (confirmed) {
      await triggerCleanup.mutateAsync()
    }
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Data Retention</CardTitle>
          <CardDescription>Manage data retention and cleanup settings</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-20 w-full" />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Data Retention</CardTitle>
          <CardDescription>Manage data retention and cleanup settings</CardDescription>
        </CardHeader>
        <CardContent>
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              Failed to load data retention settings: {error instanceof Error ? error.message : "Unknown error"}
            </AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    )
  }

  const settings = data || {
    enabled: false,
    retentionDays: 90,
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Data Retention</CardTitle>
        <CardDescription>Manage data retention and cleanup settings</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="retention-enabled"
              checked={enabled}
              onCheckedChange={(checked) => setEnabled(checked as boolean)}
            />
            <Label htmlFor="retention-enabled" className="cursor-pointer">
              Enable automatic data cleanup
            </Label>
          </div>

          {enabled && (
            <div className="space-y-2 pl-6">
              <Label htmlFor="retention-days">Retention Period (days)</Label>
              <Input
                id="retention-days"
                type="number"
                min={1}
                max={365}
                value={retentionDays}
                onChange={(e) => setRetentionDays(parseInt(e.target.value) || 90)}
              />
              <p className="text-xs text-secondary-500">
                Data older than this period will be automatically deleted
              </p>
            </div>
          )}

          <Button
            onClick={handleSave}
            disabled={updateSettings.isPending}
            className="w-full"
          >
            {updateSettings.isPending ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Saving...
              </>
            ) : (
              "Save Settings"
            )}
          </Button>
        </div>

        <div className="border-t pt-4 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-sm font-medium">Cleanup Status</h4>
              <p className="text-xs text-secondary-500 mt-1">
                Last cleanup information
              </p>
            </div>
            <Badge variant={settings.enabled ? "default" : "secondary"}>
              {settings.enabled ? "Active" : "Inactive"}
            </Badge>
          </div>

          {settings.lastCleanup && (
            <div className="flex items-center gap-2 text-sm text-secondary-600">
              <Calendar className="h-4 w-4" />
              <span>
                Last cleanup: {format(new Date(settings.lastCleanup), "MMM d, yyyy 'at' h:mm a")}
              </span>
            </div>
          )}

          {settings.totalRecordsDeleted !== undefined && (
            <div className="flex items-center gap-2 text-sm text-secondary-600">
              <Database className="h-4 w-4" />
              <span>
                Total records deleted: {settings.totalRecordsDeleted.toLocaleString()}
              </span>
            </div>
          )}

          {settings.nextCleanup && (
            <div className="flex items-center gap-2 text-sm text-secondary-600">
              <Calendar className="h-4 w-4" />
              <span>
                Next cleanup: {format(new Date(settings.nextCleanup), "MMM d, yyyy 'at' h:mm a")}
              </span>
            </div>
          )}

          <Button
            variant="outline"
            onClick={handleCleanup}
            disabled={triggerCleanup.isPending || !enabled}
            className="w-full"
          >
            {triggerCleanup.isPending ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Cleaning up...
              </>
            ) : (
              <>
                <Trash2 className="h-4 w-4 mr-2" />
                Trigger Manual Cleanup
              </>
            )}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

