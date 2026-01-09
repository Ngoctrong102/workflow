import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { channelService, type ListChannelsParams, type CreateChannelRequest, type UpdateChannelRequest } from "@/services/channel-service"
import type { ApiException } from "@/utils/error-handler"

export function useChannels(params?: ListChannelsParams) {
  return useQuery({
    queryKey: ["channels", params],
    queryFn: () => channelService.list(params),
  })
}

export function useChannel(id: string | undefined) {
  return useQuery({
    queryKey: ["channel", id],
    queryFn: () => channelService.get(id!),
    enabled: !!id,
  })
}

export function useCreateChannel() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (channel: CreateChannelRequest) => channelService.create(channel),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["channels"] })
      toast({
        title: "Channel Created",
        description: "Channel has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Channel",
        description: error.message || "An error occurred while creating the channel",
      })
    },
  })
}

export function useUpdateChannel() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation<
    Awaited<ReturnType<typeof channelService.update>>,
    ApiException,
    { id: string } & Omit<UpdateChannelRequest, "id">
  >({
    mutationFn: ({ id, ...channel }) =>
      channelService.update(id, channel),
    onSuccess: (_: unknown, variables: { id: string }) => {
      queryClient.invalidateQueries({ queryKey: ["channels"] })
      queryClient.invalidateQueries({ queryKey: ["channel", variables.id] })
      toast({
        title: "Channel Updated",
        description: "Channel has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Channel",
        description: error.message || "An error occurred while updating the channel",
      })
    },
  })
}

export function useDeleteChannel() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => channelService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["channels"] })
      toast({
        title: "Channel Deleted",
        description: "Channel has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Channel",
        description: error.message || "An error occurred while deleting the channel",
      })
    },
  })
}

export function useTestChannelConnection() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, config }: { id: string; config: Record<string, unknown> }) =>
      channelService.testConnection(id, config),
    onSuccess: (data) => {
      if (data.success) {
        toast({
          title: "Connection Test Successful",
          description: data.message || "Channel connection is working",
        })
      } else {
        toast({
          variant: "destructive",
          title: "Connection Test Failed",
          description: data.message || "Channel connection test failed",
        })
      }
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Connection Test Failed",
        description: error.message || "An error occurred while testing the connection",
      })
    },
  })
}

