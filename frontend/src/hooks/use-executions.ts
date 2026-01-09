import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { UseQueryOptions, UseMutationOptions } from '@tanstack/react-query'
import { executionService } from "@/services/execution-service"
import { useToast } from "@/hooks/use-toast"
import type { ApiException } from "@/utils/error-handler"
import type {
  Execution,
  ExecutionListParams,
  ExecutionListResponse,
  ExecutionDetails,
  NodeExecution,
  ExecutionLog,
} from "@/types/execution"

/**
 * Hook to list executions
 */
export function useExecutions(params?: ExecutionListParams) {
  return useQuery<ExecutionListResponse, ApiException>({
    queryKey: ["executions", params],
    queryFn: () => executionService.list(params),
    staleTime: 30 * 1000, // 30 seconds - executions change frequently
    gcTime: 2 * 60 * 1000, // 2 minutes
  })
}

/**
 * Hook to get execution details with polling for waiting/running executions
 */
export function useExecution(executionId: string | undefined, options?: { refetchInterval?: number | false }) {
  return useQuery<ExecutionDetails, ApiException>({
    queryKey: ["execution", executionId],
    queryFn: () => {
      if (!executionId) throw new Error("Execution ID is required")
      return executionService.get(executionId)
    },
    enabled: !!executionId,
    refetchInterval: (query) => {
      // Poll every 2-5 seconds when waiting, every 10 seconds when running
      const data = query.state.data
      if (!data) return false
      
      if (data.status === "waiting") {
        return options?.refetchInterval ?? 3000 // 3 seconds
      }
      if (data.status === "running") {
        return options?.refetchInterval ?? 10000 // 10 seconds
      }
      
      // Stop polling when completed/failed/cancelled
      return false
    },
  })
}

/**
 * Hook to get execution logs
 */
export function useExecutionLogs(executionId: string | undefined) {
  return useQuery<ExecutionLog[], ApiException>({
    queryKey: ["execution-logs", executionId],
    queryFn: () => {
      if (!executionId) throw new Error("Execution ID is required")
      return executionService.getLogs(executionId)
    },
    enabled: !!executionId,
  })
}

/**
 * Hook to get node executions
 */
export function useNodeExecutions(executionId: string | undefined) {
  return useQuery<NodeExecution[], ApiException>({
    queryKey: ["node-executions", executionId],
    queryFn: () => {
      if (!executionId) throw new Error("Execution ID is required")
      return executionService.getNodeExecutions(executionId)
    },
    enabled: !!executionId,
  })
}

/**
 * Hook to get execution context
 */
export function useExecutionContext(executionId: string | undefined) {
  return useQuery<Record<string, unknown>, ApiException>({
    queryKey: ["execution-context", executionId],
    queryFn: () => {
      if (!executionId) throw new Error("Execution ID is required")
      return executionService.getContext(executionId)
    },
    enabled: !!executionId,
  })
}

/**
 * Hook to cancel execution
 */
export function useCancelExecution() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<{ message: string }, ApiException, string>({
    mutationFn: (executionId: string) => executionService.cancel(executionId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["executions"] })
      void queryClient.invalidateQueries({ queryKey: ["execution"] })
      toast({
        title: "Execution Cancelled",
        description: "The execution has been cancelled successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Cancel Execution",
        description: error.message || "An error occurred while cancelling the execution",
      })
    },
  })
}

/**
 * Hook to retry execution
 */
export function useRetryExecution() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<Execution, ApiException, string>({
    mutationFn: (executionId: string) => executionService.retry(executionId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["executions"] })
      void queryClient.invalidateQueries({ queryKey: ["execution"] })
      toast({
        title: "Execution Retried",
        description: "The execution has been retried successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Retry Execution",
        description: error.message || "An error occurred while retrying the execution",
      })
    },
  })
}

