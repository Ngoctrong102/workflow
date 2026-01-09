import { useMutation } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { executionService } from "@/services/execution-service"
import { exportToCSV, exportToJSON } from "@/utils/export"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"
import type { Execution } from "@/types/execution"

export type ExportFormat = "csv" | "json"

export function useExportExecution() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({ id, format = "json" }: { id: string; format?: ExportFormat }) => {
      const execution = await executionService.get(id)
      
      if (format === "csv") {
        const csvData = [{
          id: execution.id,
          workflow_id: execution.workflow_id,
          workflow_name: execution.workflow_name || "",
          status: execution.status,
          started_at: execution.started_at,
          completed_at: execution.completed_at || "",
          duration: execution.duration || 0,
          nodes_executed: execution.nodes_executed,
          notifications_sent: execution.notifications_sent,
          error: execution.error || "",
        }]
        
        exportToCSV(csvData, [
          { key: "id", label: "ID" },
          { key: "workflow_id", label: "Workflow ID" },
          { key: "workflow_name", label: "Workflow Name" },
          { key: "status", label: "Status" },
          { key: "started_at", label: "Started At" },
          { key: "completed_at", label: "Completed At" },
          { key: "duration", label: "Duration (ms)" },
          { key: "nodes_executed", label: "Nodes Executed" },
          { key: "notifications_sent", label: "Notifications Sent" },
          { key: "error", label: "Error" },
        ], `execution-${id}-${Date.now()}.csv`)
      } else {
        exportToJSON([execution], `execution-${id}-${Date.now()}.json`)
      }
      
      return execution
    },
    onSuccess: () => {
      toast({
        title: "Execution Exported",
        description: "Execution has been exported successfully",
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

export function useBulkExportExecutions() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({ ids, format = "json" }: { ids: string[]; format?: ExportFormat }) => {
      const executions: Execution[] = []
      
      // Fetch all executions
      for (const id of ids) {
        try {
          const execution = await executionService.get(id)
          executions.push(execution)
        } catch (error) {
          console.error(`Failed to fetch execution ${id}:`, error)
        }
      }
      
      if (executions.length === 0) {
        throw new Error("No executions to export")
      }
      
      if (format === "csv") {
        const csvData = executions.map((execution) => ({
          id: execution.id,
          workflow_id: execution.workflow_id,
          workflow_name: execution.workflow_name || "",
          status: execution.status,
          started_at: execution.started_at,
          completed_at: execution.completed_at || "",
          duration: execution.duration || 0,
          nodes_executed: execution.nodes_executed,
          notifications_sent: execution.notifications_sent,
          error: execution.error || "",
        }))
        
        exportToCSV(csvData, [
          { key: "id", label: "ID" },
          { key: "workflow_id", label: "Workflow ID" },
          { key: "workflow_name", label: "Workflow Name" },
          { key: "status", label: "Status" },
          { key: "started_at", label: "Started At" },
          { key: "completed_at", label: "Completed At" },
          { key: "duration", label: "Duration (ms)" },
          { key: "nodes_executed", label: "Nodes Executed" },
          { key: "notifications_sent", label: "Notifications Sent" },
          { key: "error", label: "Error" },
        ], `executions-${Date.now()}.csv`)
      } else {
        exportToJSON(executions, `executions-${Date.now()}.json`)
      }
      
      return executions
    },
    onSuccess: (executions) => {
      toast({
        title: "Executions Exported",
        description: `${executions.length} execution${executions.length !== 1 ? "s" : ""} exported successfully`,
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

