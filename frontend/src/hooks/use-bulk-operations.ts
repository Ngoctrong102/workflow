import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { workflowService } from "@/services/workflow-service"
import { executionService } from "@/services/execution-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useBulkDeleteWorkflows() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: async (ids: string[]) => {
      return await workflowService.bulkDelete(ids)
    },
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      if (result.errors && result.errors.length > 0) {
        toast({
          variant: "destructive",
          title: "Partial Success",
          description: `${result.deleted} workflows deleted. ${result.errors.length} failed.`,
        })
      } else {
        toast({
          title: "Workflows Deleted",
          description: `${result.deleted} workflow${result.deleted !== 1 ? "s" : ""} deleted successfully`,
        })
      }
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Bulk Delete Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useBulkUpdateWorkflowStatus() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: async ({
      ids,
      status,
    }: {
      ids: string[]
      status: "draft" | "active" | "inactive" | "paused" | "archived"
    }) => {
      return await workflowService.bulkUpdateStatus(ids, status)
    },
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      if (result.errors && result.errors.length > 0) {
        toast({
          variant: "destructive",
          title: "Partial Success",
          description: `${result.updated} workflows updated. ${result.errors.length} failed.`,
        })
      } else {
        toast({
          title: "Status Updated",
          description: `${result.updated} workflow${result.updated !== 1 ? "s" : ""} updated successfully`,
        })
      }
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Bulk Update Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useBulkDeleteExecutions() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: async (ids: string[]) => {
      return await executionService.bulkDelete(ids)
    },
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["executions"] })
      if (result.errors && result.errors.length > 0) {
        toast({
          variant: "destructive",
          title: "Partial Success",
          description: `${result.deleted} executions deleted. ${result.errors.length} failed.`,
        })
      } else {
        toast({
          title: "Executions Deleted",
          description: `${result.deleted} execution${result.deleted !== 1 ? "s" : ""} deleted successfully`,
        })
      }
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Bulk Delete Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

