import { useQuery, useMutation } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { notificationService, type ListNotificationsParams } from "@/services/notification-service"
import type { SendNotificationRequest } from "@/types/notification"
import type { ApiException } from "@/utils/error-handler"

export function useNotifications(params?: ListNotificationsParams) {
  return useQuery({
    queryKey: ["notifications", params],
    queryFn: () => notificationService.list(params),
  })
}

export function useNotification(id: string | undefined) {
  return useQuery({
    queryKey: ["notification", id],
    queryFn: () => notificationService.get(id!),
    enabled: !!id,
  })
}

export function useNotificationStatus(id: string | undefined) {
  return useQuery({
    queryKey: ["notification-status", id],
    queryFn: () => notificationService.getStatus(id!),
    enabled: !!id,
    refetchInterval: 5000, // Poll every 5 seconds for status updates
  })
}

export function useSendNotification() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: (request: SendNotificationRequest) => notificationService.send(request),
    onSuccess: () => {
      toast({
        title: "Notification Sent",
        description: "Notification has been sent successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Send Notification",
        description: error.message || "An error occurred while sending the notification",
      })
    },
  })
}

