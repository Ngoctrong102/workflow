import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ChevronLeft } from "lucide-react"
import { TriggerEditor } from "@/components/trigger/TriggerEditor"
import { useTrigger, useCreateTriggerConfig, useUpdateTriggerConfig } from "@/hooks/use-triggers"
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

  const createTriggerConfig = useCreateTriggerConfig()
  const updateTriggerConfig = useUpdateTriggerConfig()

  const triggerType: TriggerType = (type || trigger?.type || "api") as TriggerType

  // Map frontend trigger types to backend trigger types
  const mapTriggerTypeToBackend = (type: TriggerType): "api-call" | "scheduler" | "event" => {
    switch (type) {
      case "api":
        return "api-call"
      case "schedule":
        return "scheduler"
      case "event":
        return "event"
      default:
        return "api-call"
    }
  }

  const handleSave = async (data: { name?: string; triggerType?: string; status?: string; config: Record<string, unknown> }) => {
    try {
      if (isEditMode && trigger) {
        // Update existing trigger config
        await updateTriggerConfig.mutateAsync({
          id: trigger.id,
          data: {
            name: data.name,
            triggerType: data.triggerType as "api-call" | "scheduler" | "event" | undefined,
            status: data.status as "active" | "inactive" | undefined,
            config: data.config,
          },
        })
        navigate("/triggers")
      } else {
        // Create new trigger config
        await createTriggerConfig.mutateAsync({
          name: data.name || `Trigger ${triggerType}`,
          triggerType: mapTriggerTypeToBackend(triggerType),
          status: (data.status as "active" | "inactive") || "active",
          config: data.config,
        })
        navigate("/triggers")
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to save trigger config",
      })
    }
  }

  const handleCancel = () => {
    navigate("/triggers")
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
        triggerType={triggerType}
        trigger={trigger}
        onSave={handleSave}
        onCancel={handleCancel}
        showNameAndStatus={true}
      />
    </div>
  )
}

