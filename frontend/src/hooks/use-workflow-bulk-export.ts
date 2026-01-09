import { useMutation } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { workflowService } from "@/services/workflow-service"
import { exportToJSON, exportToCSV } from "@/utils/export"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"
import type { WorkflowDefinition } from "@/types/workflow"

export type ExportFormat = "csv" | "json"

export function useBulkExportWorkflows() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({ ids, format = "json" }: { ids: string[]; format?: ExportFormat }) => {
      const workflows: WorkflowDefinition[] = []
      
      // Fetch all workflows
      for (const id of ids) {
        try {
          const workflowDef = await workflowService.export(id)
          workflows.push(workflowDef)
        } catch (error) {
          console.error(`Failed to fetch workflow ${id}:`, error)
        }
      }
      
      if (workflows.length === 0) {
        throw new Error("No workflows to export")
      }
      
      if (format === "csv") {
        const csvData = workflows.map((workflow) => ({
          id: workflow.id || "",
          name: workflow.name || "",
          description: workflow.description || "",
          status: workflow.status || "",
          nodes_count: workflow.nodes?.length || 0,
          edges_count: workflow.edges?.length || 0,
        }))
        
        exportToCSV(csvData, [
          { key: "id", label: "ID" },
          { key: "name", label: "Name" },
          { key: "description", label: "Description" },
          { key: "status", label: "Status" },
          { key: "nodes_count", label: "Nodes Count" },
          { key: "edges_count", label: "Edges Count" },
        ], `workflows-${Date.now()}.csv`)
      } else {
        exportToJSON(workflows, `workflows-${Date.now()}.json`)
      }
      
      return workflows
    },
    onSuccess: (workflows) => {
      toast({
        title: "Workflows Exported",
        description: `${workflows.length} workflow${workflows.length !== 1 ? "s" : ""} exported successfully`,
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Bulk Export Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

