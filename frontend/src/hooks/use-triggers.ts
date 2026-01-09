import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import {
  triggerService,
  type CreateApiTriggerRequest,
  type CreateScheduleTriggerRequest,
  type CreateFileTriggerRequest,
  type CreateEventTriggerRequest,
  type UpdateTriggerRequest,
} from "@/services/trigger-service"
import type { ApiException } from "@/utils/error-handler"

export function useTriggers(workflowId: string | undefined) {
  return useQuery({
    queryKey: ["triggers", workflowId],
    queryFn: () => triggerService.list(workflowId!),
    enabled: !!workflowId,
  })
}

export function useTrigger(id: string | undefined) {
  return useQuery({
    queryKey: ["trigger", id],
    queryFn: () => triggerService.get(id!),
    enabled: !!id,
  })
}

export function useCreateApiTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (request: CreateApiTriggerRequest) => triggerService.createApi(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["triggers", variables.workflowId] })
      toast({
        title: "API Trigger Created",
        description: "API trigger has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create API Trigger",
        description: error.message || "An error occurred while creating the trigger",
      })
    },
  })
}

export function useCreateScheduleTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (request: CreateScheduleTriggerRequest) =>
      triggerService.createSchedule(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["triggers", variables.workflowId] })
      toast({
        title: "Schedule Trigger Created",
        description: "Schedule trigger has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Schedule Trigger",
        description: error.message || "An error occurred while creating the trigger",
      })
    },
  })
}

export function useCreateFileTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (request: CreateFileTriggerRequest) => triggerService.createFile(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["triggers", variables.workflowId] })
      toast({
        title: "File Trigger Created",
        description: "File trigger has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create File Trigger",
        description: error.message || "An error occurred while creating the trigger",
      })
    },
  })
}

export function useCreateEventTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (request: CreateEventTriggerRequest) => triggerService.createEvent(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["triggers", variables.workflowId] })
      toast({
        title: "Event Trigger Created",
        description: "Event trigger has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Event Trigger",
        description: error.message || "An error occurred while creating the trigger",
      })
    },
  })
}

export function useUpdateTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<
    Awaited<ReturnType<typeof triggerService.update>>,
    ApiException,
    UpdateTriggerRequest
  >({
    mutationFn: ({ id, ...request }) => triggerService.update(id, request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Updated",
        description: "Trigger has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Trigger",
        description: error.message || "An error occurred while updating the trigger",
      })
    },
  })
}

export function useDeleteTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      toast({
        title: "Trigger Deleted",
        description: "Trigger has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Trigger",
        description: error.message || "An error occurred while deleting the trigger",
      })
    },
  })
}

export function useActivateTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.activate(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Activated",
        description: "Trigger has been activated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Activate Trigger",
        description: error.message || "An error occurred while activating the trigger",
      })
    },
  })
}

export function useDeactivateTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.deactivate(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Deactivated",
        description: "Trigger has been deactivated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Deactivate Trigger",
        description: error.message || "An error occurred while deactivating the trigger",
      })
    },
  })
}

export function useInitializeTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.initialize(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Initialized",
        description: "Trigger instance has been initialized successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Initialize Trigger",
        description: error.message || "An error occurred while initializing the trigger",
      })
    },
  })
}

export function useStartTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.start(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Started",
        description: "Trigger instance has been started successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Start Trigger",
        description: error.message || "An error occurred while starting the trigger",
      })
    },
  })
}

export function usePauseTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.pause(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Paused",
        description: "Trigger instance has been paused successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Pause Trigger",
        description: error.message || "An error occurred while pausing the trigger",
      })
    },
  })
}

export function useResumeTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.resume(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Resumed",
        description: "Trigger instance has been resumed successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Resume Trigger",
        description: error.message || "An error occurred while resuming the trigger",
      })
    },
  })
}

export function useStopTrigger() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.stop(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["triggers"] })
      queryClient.invalidateQueries({ queryKey: ["trigger", data.id] })
      toast({
        title: "Trigger Stopped",
        description: "Trigger instance has been stopped successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Stop Trigger",
        description: error.message || "An error occurred while stopping the trigger",
      })
    },
  })
}

