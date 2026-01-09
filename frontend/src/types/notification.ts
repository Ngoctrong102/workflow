export type NotificationStatus = "pending" | "sending" | "delivered" | "failed" | "bounced"

export interface Notification {
  id: string
  workflowId: string
  executionId: string
  channel: string
  recipient: string
  subject?: string
  body: string
  status: NotificationStatus
  sentAt?: string
  deliveredAt?: string
  openedAt?: string
  clickedAt?: string
  error?: string
  createdAt: string
  updatedAt: string
}

export interface SendNotificationRequest {
  workflowId: string
  channel: string
  recipient: string
  subject?: string
  body: string
  data?: Record<string, unknown>
}

export interface SendNotificationResponse {
  id: string
  status: NotificationStatus
  message: string
}

