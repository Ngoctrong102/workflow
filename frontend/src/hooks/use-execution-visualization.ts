import { useState, useCallback } from "react"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { executionService } from "@/services/execution-service"
import { useToast } from "@/hooks/use-toast"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"
import type { ExecutionVisualizationData, StepExecutionResponse, StepState } from "@/types/execution-visualization"

/**
 * Hook to load execution visualization data
 */
export function useExecutionVisualization(executionId: string | undefined) {
  return useQuery<ExecutionVisualizationData, ApiException>({
    queryKey: ["execution-visualization", executionId],
    queryFn: () => executionService.visualize(executionId!),
    enabled: !!executionId,
    staleTime: 1 * 60 * 1000, // 1 minute
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

/**
 * Hook to execute next/previous step in visualization
 */
export function useExecuteVisualizationStep() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<
    StepExecutionResponse,
    ApiException,
    { executionId: string; direction: "forward" | "backward" }
  >({
    mutationFn: ({ executionId, direction }) => executionService.executeStep(executionId, direction),
    onSuccess: (data, variables) => {
      // Invalidate visualization data to refresh
      queryClient.invalidateQueries({ queryKey: ["execution-visualization", variables.executionId] })
      queryClient.invalidateQueries({ queryKey: ["execution-visualization-step", variables.executionId] })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Execute Step",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

/**
 * Hook to get execution state at specific step
 */
export function useExecutionStepState(executionId: string | undefined, stepNumber: number | undefined) {
  return useQuery<StepState, ApiException>({
    queryKey: ["execution-visualization-step", executionId, stepNumber],
    queryFn: () => executionService.getStepState(executionId!, stepNumber!),
    enabled: !!executionId && stepNumber !== undefined,
    staleTime: 1 * 60 * 1000, // 1 minute
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

/**
 * Hook to reset visualization
 */
export function useResetVisualization() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<void, ApiException, string>({
    mutationFn: (executionId) => executionService.resetVisualization(executionId),
    onSuccess: (_, executionId) => {
      // Invalidate visualization data to refresh
      queryClient.invalidateQueries({ queryKey: ["execution-visualization", executionId] })
      queryClient.invalidateQueries({ queryKey: ["execution-visualization-step", executionId] })
      toast({
        title: "Visualization Reset",
        description: "Visualization has been reset to the start",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Reset Visualization",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

/**
 * Hook to get current visualization context
 */
export function useVisualizationContext(executionId: string | undefined) {
  return useQuery<Record<string, unknown>, ApiException>({
    queryKey: ["execution-visualization-context", executionId],
    queryFn: () => executionService.getVisualizationContext(executionId!),
    enabled: !!executionId,
    staleTime: 30 * 1000, // 30 seconds
    gcTime: 2 * 60 * 1000, // 2 minutes
  })
}

