import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus, Edit, Trash2, Power, PowerOff, Loader2, X } from "lucide-react"
import { useTriggers, useDeleteTrigger, useActivateTrigger, useDeactivateTrigger } from "@/hooks/use-triggers"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { Skeleton } from "@/components/ui/skeleton"
import type { Trigger } from "@/types/trigger"

interface TriggerListProps {
  workflowId: string
  onCreateTrigger?: (type: Trigger["type"]) => void
  onEditTrigger?: (trigger: Trigger) => void
  onClose?: () => void
}

const triggerTypeLabels: Record<Trigger["type"], string> = {
  api: "API",
  schedule: "Schedule",
  file: "File Upload",
  event: "Event",
}

export function TriggerList({ workflowId, onCreateTrigger, onEditTrigger, onClose }: TriggerListProps) {
  const { data: triggers, isLoading, error } = useTriggers(workflowId)
  const deleteTrigger = useDeleteTrigger()
  const activateTrigger = useActivateTrigger()
  const deactivateTrigger = useDeactivateTrigger()

  const { confirm } = useConfirmDialog()

  const handleDelete = async (id: string) => {
    const confirmed = await confirm({
      title: "Delete Trigger",
      description: "Are you sure you want to delete this trigger? This action cannot be undone.",
      variant: "destructive",
      confirmText: "Delete",
      cancelText: "Cancel",
    })

    if (confirmed) {
      await deleteTrigger.mutateAsync(id)
    }
  }

  const handleToggleStatus = async (trigger: Trigger) => {
    if (trigger.status === "active") {
      await deactivateTrigger.mutateAsync(trigger.id)
    } else {
      await activateTrigger.mutateAsync(trigger.id)
    }
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Triggers</CardTitle>
          <CardDescription>Workflow triggers</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[1, 2].map((i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Triggers</CardTitle>
          <CardDescription>Workflow triggers</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-error-600">
            <p>Failed to load triggers</p>
            <p className="text-sm mt-2">
              {error instanceof Error ? error.message : "Unknown error"}
            </p>
          </div>
        </CardContent>
      </Card>
    )
  }

  const triggerList = triggers || []

  return (
    <div>
      <div className="p-3 border-b border-secondary-200">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-semibold">Triggers</h3>
            <p className="text-xs text-secondary-500 mt-0.5">Workflow activation triggers</p>
          </div>
          <div className="flex items-center gap-2">
            {onCreateTrigger && (
              <Button size="sm" onClick={() => onCreateTrigger("api")}>
                <Plus className="h-4 w-4 mr-2" />
                Add Trigger
              </Button>
            )}
            {onClose && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onClose}
                className="h-8 w-8 p-0"
                title="Close triggers panel"
              >
                <X className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </div>
      <div className="p-3">
        {triggerList.length === 0 ? (
          <div className="text-center py-6 text-secondary-500">
            <p className="text-sm">No triggers configured</p>
            {onCreateTrigger && (
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => onCreateTrigger("api")}
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Trigger
              </Button>
            )}
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Configuration</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {triggerList.map((trigger) => (
                <TableRow key={trigger.id}>
                  <TableCell>
                    <Badge variant="outline">
                      {triggerTypeLabels[trigger.type]}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      {trigger.type === "api" && (
                        <span>
                          {(trigger.config as { path?: string; method?: string }).method || "POST"}{" "}
                          {(trigger.config as { path?: string }).path || "-"}
                        </span>
                      )}
                      {trigger.type === "schedule" && (
                        <span>
                          {(trigger.config as { cronExpression?: string }).cronExpression || "-"}
                        </span>
                      )}
                      {trigger.type === "file" && (
                        <span>
                          Formats:{" "}
                          {((trigger.config as { formats?: string[] }).formats || []).join(", ")}
                        </span>
                      )}
                      {trigger.type === "event" && (
                        <span>
                          {(trigger.config as { source?: string }).source || "-"}:{" "}
                          {(trigger.config as { topic?: string; queue?: string }).topic ||
                            (trigger.config as { topic?: string; queue?: string }).queue ||
                            "-"}
                        </span>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={trigger.status === "active" ? "default" : "secondary"}
                    >
                      {trigger.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end space-x-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleToggleStatus(trigger)}
                        disabled={
                          activateTrigger.isPending || deactivateTrigger.isPending
                        }
                      >
                        {activateTrigger.isPending || deactivateTrigger.isPending ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : trigger.status === "active" ? (
                          <PowerOff className="h-4 w-4" />
                        ) : (
                          <Power className="h-4 w-4" />
                        )}
                      </Button>
                      {onEditTrigger && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => onEditTrigger(trigger)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                      )}
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(trigger.id)}
                        disabled={deleteTrigger.isPending}
                      >
                        {deleteTrigger.isPending ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          <Trash2 className="h-4 w-4 text-error-600" />
                        )}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    </div>
  )
}

