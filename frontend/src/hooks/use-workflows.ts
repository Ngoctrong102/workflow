import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { workflowService, type ListWorkflowsParams, type CreateWorkflowRequest, type UpdateWorkflowRequest, type ExecuteWorkflowRequest } from "@/services/workflow-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useWorkflows(params?: ListWorkflowsParams) {
  return useQuery({
    queryKey: ["workflows", params],
    queryFn: () => workflowService.list(params),
    staleTime: 2 * 60 * 1000, // 2 minutes - workflows list changes frequently
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useWorkflow(id: string | undefined) {
  return useQuery({
    queryKey: ["workflow", id],
    queryFn: () => workflowService.get(id!),
    enabled: !!id,
    staleTime: 1 * 60 * 1000, // 1 minute - individual workflow may change
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useCreateWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (workflow: CreateWorkflowRequest) => workflowService.create(workflow),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      toast({
        title: "Workflow Created",
        description: "Workflow has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<
    Awaited<ReturnType<typeof workflowService.update>>,
    ApiException,
    { id: string } & Omit<UpdateWorkflowRequest, "id">
  >({
    mutationFn: ({ id, ...workflow }) =>
      workflowService.update(id, workflow),
    onSuccess: (_: unknown, variables: { id: string }) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", variables.id] })
      toast({
        title: "Workflow Updated",
        description: "Workflow has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeleteWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => workflowService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      toast({
        title: "Workflow Deleted",
        description: "Workflow has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useExecuteWorkflow() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ExecuteWorkflowRequest }) =>
      workflowService.execute(id, data),
    onSuccess: () => {
      toast({
        title: "Workflow Executed",
        description: "Workflow execution started successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Execute Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useWorkflowVersions(id: string | undefined) {
  return useQuery({
    queryKey: ["workflow-versions", id],
    queryFn: () => workflowService.getVersions(id!),
    enabled: !!id,
  })
}

export function useWorkflowByVersion(id: string | undefined, version: number | undefined) {
  return useQuery({
    queryKey: ["workflow", id, "version", version],
    queryFn: () => workflowService.getByVersion(id!, version!),
    enabled: !!id && !!version,
  })
}

export function useActivateWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => workflowService.activate(id),
    onSuccess: (_: unknown, id: string) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", id] })
      toast({
        title: "Workflow Activated",
        description: "Workflow has been activated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Activate Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeactivateWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => workflowService.deactivate(id),
    onSuccess: (_: unknown, id: string) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", id] })
      toast({
        title: "Workflow Deactivated",
        description: "Workflow has been deactivated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Deactivate Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function usePauseWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => workflowService.pause(id),
    onSuccess: (_: unknown, id: string) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", id] })
      toast({
        title: "Workflow Paused",
        description: "Workflow has been paused successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Pause Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useResumeWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => workflowService.resume(id),
    onSuccess: (_: unknown, id: string) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", id] })
      toast({
        title: "Workflow Resumed",
        description: "Workflow has been resumed successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Resume Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useRollbackWorkflow() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      workflowService.rollback(id, version),
    onSuccess: (_: unknown, { id }) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] })
      queryClient.invalidateQueries({ queryKey: ["workflow", id] })
      queryClient.invalidateQueries({ queryKey: ["workflow-versions", id] })
      toast({
        title: "Workflow Rolled Back",
        description: "Workflow has been rolled back successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Rollback Workflow",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

