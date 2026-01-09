import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ChevronLeft } from "lucide-react"
import { TriggerEditor } from "@/components/trigger/TriggerEditor"
import { useTrigger } from "@/hooks/use-triggers"
import { useCreateApiTrigger, useCreateScheduleTrigger, useCreateFileTrigger, useCreateEventTrigger, useUpdateTrigger } from "@/hooks/use-triggers"
import { useToast } from "@/hooks/use-toast"
import { Skeleton } from "@/components/ui/skeleton"
import type { TriggerType } from "@/types/trigger"

export default function TriggerEditorPage() {
  const { id, workflowId, type } = useParams<{ id?: string; workflowId?: string; type?: TriggerType }>()
  const navigate = useNavigate()
  const { toast } = useToast()
  const isEditMode = id !== "new" && id !== undefined

  // Get trigger if editing
  const { data: trigger, isLoading: isLoadingTrigger } = useTrigger(id)

  const createApiTrigger = useCreateApiTrigger()
  const createScheduleTrigger = useCreateScheduleTrigger()
  const createFileTrigger = useCreateFileTrigger()
  const createEventTrigger = useCreateEventTrigger()
  const updateTrigger = useUpdateTrigger()

  const triggerType: TriggerType = (type || trigger?.type || "api") as TriggerType
  const actualWorkflowId = workflowId || trigger?.workflow_id || ""

  const handleSave = async (config: Record<string, unknown>) => {
    try {
      if (isEditMode && trigger) {
        // Update existing trigger
        await updateTrigger.mutateAsync({
          id: trigger.id,
          config,
        })
        toast({
          title: "Success",
          description: "Trigger updated successfully",
        })
        navigate(`/workflows/${trigger.workflowId}`)
      } else {
        // Create new trigger
        switch (triggerType) {
          case "api":
            const apiResult = await createApiTrigger.mutateAsync({
              workflowId: actualWorkflowId,
              path: config.path as string,
              method: config.method as "GET" | "POST" | "PUT" | "PATCH" | "DELETE",
              authentication: config.authentication as {
                type: "none" | "api_key" | "bearer"
                key?: string
              },
            })
            toast({
              title: "Success",
              description: "API trigger created successfully",
            })
            navigate(`/workflows/${actualWorkflowId}`)
            break
          case "schedule":
            await createScheduleTrigger.mutateAsync({
              workflowId: actualWorkflowId,
              cronExpression: config.cronExpression as string,
              timezone: config.timezone as string,
              startDate: config.startDate as string,
              endDate: config.endDate as string,
              data: config.data as Record<string, unknown>,
            })
            toast({
              title: "Success",
              description: "Schedule trigger created successfully",
            })
            navigate(`/workflows/${actualWorkflowId}`)
            break
          case "file":
            await createFileTrigger.mutateAsync({
              workflowId: actualWorkflowId,
              formats: config.formats as string[],
              mapping: config.mapping as Record<string, string>,
              destination: config.destination as string,
            })
            toast({
              title: "Success",
              description: "File trigger created successfully",
            })
            navigate(`/workflows/${actualWorkflowId}`)
            break
          case "event":
            await createEventTrigger.mutateAsync({
              workflowId: actualWorkflowId,
              source: config.source as "kafka" | "rabbitmq",
              topic: config.topic as string,
              queue: config.queue as string,
              filters: config.filters as Record<string, unknown>,
            })
            toast({
              title: "Success",
              description: "Event trigger created successfully",
            })
            navigate(`/workflows/${actualWorkflowId}`)
            break
        }
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to save trigger",
      })
    }
  }

  const handleCancel = () => {
    if (isEditMode && trigger) {
      navigate(`/workflows/${trigger.workflowId}`)
    } else if (workflowId) {
      navigate(`/workflows/${workflowId}`)
    } else {
      navigate("/triggers")
    }
  }

  if (isEditMode && isLoadingTrigger) {
    return (
      <div className="container mx-auto p-6 max-w-4xl">
        <Card>
          <CardHeader>
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-64 mt-2" />
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <div className="mb-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={handleCancel}
          className="mb-4"
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">
          {isEditMode ? "Edit Trigger" : "Create Trigger"}
        </h1>
        <p className="text-secondary-600">
          {isEditMode ? "Update trigger configuration" : "Configure a new workflow trigger"}
        </p>
      </div>

      <TriggerEditor
        workflowId={actualWorkflowId}
        triggerType={triggerType}
        trigger={trigger}
        onSave={handleSave}
        onCancel={handleCancel}
      />
    </div>
  )
}

