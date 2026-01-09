import { useQuery, useQueryClient } from "@tanstack/react-query"
import { executionService } from "@/services/execution-service"
import type { ExecutionListParams, ExecutionListResponse } from "@/types/execution"
import type { ApiException } from "@/utils/error-handler"

export type PollingInterval = 30 | 60 | 300 | false // 30s, 1m, 5m, or disabled

/**
 * Hook to list executions with configurable polling
 */
export function useRealtimeExecutions(
  params?: ExecutionListParams,
  options?: { pollingInterval?: PollingInterval }
) {
  const pollingInterval = options?.pollingInterval ?? 30

  return useQuery<ExecutionListResponse, ApiException>({
    queryKey: ["executions", params],
    queryFn: () => executionService.list(params),
    staleTime: 30 * 1000, // 30 seconds
    gcTime: 2 * 60 * 1000, // 2 minutes
    refetchInterval: pollingInterval === false ? false : pollingInterval * 1000,
  })
}

/**
 * Hook to get execution details with smart polling
 * Polls more frequently for active executions
 */
export function useRealtimeExecution(
  executionId: string | undefined,
  options?: { pollingInterval?: PollingInterval }
) {
  const queryClient = useQueryClient()
  const pollingInterval = options?.pollingInterval ?? 30

  return useQuery({
    queryKey: ["execution", executionId],
    queryFn: () => {
      if (!executionId) throw new Error("Execution ID is required")
      return executionService.get(executionId)
    },
    enabled: !!executionId,
    refetchInterval: (query) => {
      const data = query.state.data
      if (!data) return pollingInterval === false ? false : pollingInterval * 1000

      // Poll more frequently for active executions
      if (data.status === "waiting") {
        return 3000 // 3 seconds
      }
      if (data.status === "running") {
        return 5000 // 5 seconds
      }

      // Poll less frequently for completed executions
      if (data.status === "completed" || data.status === "failed" || data.status === "cancelled") {
        return pollingInterval === false ? false : pollingInterval * 1000
      }

      return false
    },
  })
}

