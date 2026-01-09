import { apiClient } from "./api-client"
import type { Notification, SendNotificationResponse } from "@/types/notification"
import type { SendNotificationRequest } from "@/types/notification"

export interface ListNotificationsParams {
  workflowId?: string
  executionId?: string
  channel?: string
  status?: string
  limit?: number
  offset?: number
}

export interface ListNotificationsResponse {
  notifications: Notification[]
  total: number
  limit: number
  offset: number
}

export const notificationService = {
  /**
   * Send notification
   */
  send: async (request: SendNotificationRequest): Promise<SendNotificationResponse> => {
    const response = await apiClient.post<SendNotificationResponse>("/notifications/send", request)
    return response.data
  },

  /**
   * Get notification by ID
   */
  get: async (id: string): Promise<Notification> => {
    const response = await apiClient.get<Notification>(`/notifications/${id}`)
    return response.data
  },

  /**
   * Get notification status
   */
  getStatus: async (id: string): Promise<{ status: Notification["status"] }> => {
    const response = await apiClient.get<{ status: Notification["status"] }>(
      `/notifications/${id}/status`
    )
    return response.data
  },

  /**
   * List notifications
   */
  list: async (params?: ListNotificationsParams): Promise<ListNotificationsResponse> => {
    const response = await apiClient.get<ListNotificationsResponse>("/notifications", {
      params,
    })
    return response.data
  },
}

