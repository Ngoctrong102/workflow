import { useState, useEffect } from "react"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { TriggerEditor } from "./TriggerEditor"
import type { Trigger, TriggerType } from "@/types/trigger"
import {
  useCreateApiTrigger,
  useCreateScheduleTrigger,
  useCreateFileTrigger,
  useCreateEventTrigger,
  useUpdateTrigger,
  useTrigger,
} from "@/hooks/use-triggers"

interface TriggerDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  workflowId: string
  trigger?: Trigger
}

export function TriggerDialog({ open, onOpenChange, workflowId, trigger: triggerProp }: TriggerDialogProps) {
  const triggerId = triggerProp && "id" in triggerProp ? triggerProp.id : undefined
  const { data: triggerData } = useTrigger(triggerId)
  
  const actualTrigger = triggerData || (triggerProp && "id" in triggerProp ? undefined : triggerProp as Trigger | undefined)
  
  const [selectedType, setSelectedType] = useState<TriggerType>(
    (actualTrigger?.type as TriggerType) || "api"
  )

  useEffect(() => {
    if (actualTrigger) {
      setSelectedType(actualTrigger.type)
    }
  }, [actualTrigger])

  const createApiTrigger = useCreateApiTrigger()
  const createScheduleTrigger = useCreateScheduleTrigger()
  const createFileTrigger = useCreateFileTrigger()
  const createEventTrigger = useCreateEventTrigger()
  const updateTrigger = useUpdateTrigger()

  const handleSave = async (config: Record<string, unknown>) => {
    try {
      if (actualTrigger) {
        // Update existing trigger
        await updateTrigger.mutateAsync({
          id: actualTrigger.id,
          config,
        })
      } else {
        // Create new trigger
        switch (selectedType) {
          case "api":
            await createApiTrigger.mutateAsync({
              workflowId,
              path: config.path as string,
              method: config.method as "GET" | "POST" | "PUT" | "PATCH" | "DELETE",
              authentication: config.authentication as {
                type: "none" | "api_key" | "bearer"
                key?: string
              },
            })
            break
          case "schedule":
            await createScheduleTrigger.mutateAsync({
              workflowId,
              cronExpression: config.cronExpression as string,
              timezone: config.timezone as string,
              startDate: config.startDate as string,
              endDate: config.endDate as string,
              data: config.data as Record<string, unknown>,
            })
            break
          case "file":
            await createFileTrigger.mutateAsync({
              workflowId,
              formats: config.formats as string[],
              mapping: config.mapping as Record<string, string>,
              destination: config.destination as string,
            })
            break
          case "event":
            await createEventTrigger.mutateAsync({
              workflowId,
              source: config.source as "kafka" | "rabbitmq",
              topic: config.topic as string,
              queue: config.queue as string,
              filters: config.filters as Record<string, unknown>,
            })
            break
        }
      }
      onOpenChange(false)
    } catch (error) {
      // Error handling is done in the mutation hooks
      console.error("Failed to save trigger:", error)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {actualTrigger ? "Edit Trigger" : "Create Trigger"}
          </DialogTitle>
          <DialogDescription>
            Configure how this workflow will be triggered
          </DialogDescription>
        </DialogHeader>

        {!actualTrigger && (
          <div className="space-y-2">
            <label className="text-sm font-medium">Trigger Type</label>
            <Select value={selectedType} onValueChange={(v) => setSelectedType(v as TriggerType)}>
              <SelectTrigger>
                <SelectValue placeholder="Select trigger type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="api">API Trigger</SelectItem>
                <SelectItem value="schedule">Schedule Trigger</SelectItem>
                <SelectItem value="file">File Upload Trigger</SelectItem>
                <SelectItem value="event">Event Trigger</SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}

        <TriggerEditor
          workflowId={workflowId}
          triggerType={actualTrigger?.type || selectedType}
          trigger={actualTrigger}
          onSave={handleSave}
          onCancel={() => onOpenChange(false)}
        />
      </DialogContent>
    </Dialog>
  )
}

