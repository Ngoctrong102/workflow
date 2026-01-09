import { useQuery } from "@tanstack/react-query"
import { workflowDashboardService, type WorkflowDashboardParams } from "@/services/workflow-dashboard-service"

export type PollingInterval = 30 | 60 | 300 | false // 30s, 1m, 5m, or disabled

/**
 * Hook to get workflow dashboard data with configurable auto-refresh
 */
export function useRealtimeWorkflowDashboard(
  params: WorkflowDashboardParams,
  options?: { pollingInterval?: PollingInterval }
) {
  const pollingInterval = options?.pollingInterval ?? 30

  return useQuery({
    queryKey: ["workflow-dashboard", params],
    queryFn: () => workflowDashboardService.getDashboard(params),
    enabled: !!params.workflowId,
    refetchInterval: pollingInterval === false ? false : pollingInterval * 1000,
  })
}

