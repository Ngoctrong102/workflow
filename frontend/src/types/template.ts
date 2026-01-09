export type TemplateChannel = "email" | "sms" | "push" | "in-app" | "slack" | "discord" | "teams" | "webhook"

export interface Template {
  id: string
  name: string
  description?: string
  channel: TemplateChannel
  subject?: string // For email
  body: string
  variables?: string[] // List of variables used in template
  status?: "active" | "inactive" | "draft"
  version?: number
  createdAt?: string
  updatedAt?: string
}

export interface TemplateVariable {
  name: string
  description?: string
  type: "user" | "workflow" | "custom" | "system"
  example?: string
}

export interface TemplatePreviewData {
  [key: string]: unknown
}

