import { useQuery } from "@tanstack/react-query"
import { workflowDashboardService, type WorkflowDashboardParams } from "@/services/workflow-dashboard-service"

export function useWorkflowDashboard(params: WorkflowDashboardParams) {
  return useQuery({
    queryKey: ["workflow-dashboard", params],
    queryFn: () => workflowDashboardService.getDashboard(params),
    enabled: !!params.workflowId,
    refetchInterval: 30000, // Auto-refresh every 30 seconds
  })
}
