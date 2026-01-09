import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { workflowService, type WorkflowDefinition } from "@/services/workflow-service"
import { exportToJSON } from "@/utils/export"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useExportWorkflow() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: async (id: string) => {
      const workflowDef = await workflowService.export(id)
      exportToJSON([workflowDef], `workflow-${id}-${Date.now()}.json`)
      return workflowDef
    },
    onSuccess: () => {
      toast({
        title: "Workflow Exported",
        description: "Workflow has been exported successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Export Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useImportWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({
      workflow,
      options,
    }: {
      workflow: WorkflowDefinition
      options?: { overwrite?: boolean; skipConflicts?: boolean }
    }) => {
      return await workflowService.import(workflow, options)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      toast({
        title: "Workflow Imported",
        description: "Workflow has been imported successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Import Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

