export type TriggerType = "api" | "schedule" | "file" | "event"

export interface Trigger {
  id: string
  workflowId: string
  type: TriggerType
  status: "active" | "inactive"
  config: Record<string, unknown>
  createdAt?: string
  updatedAt?: string
}

export interface ApiTriggerConfig {
  path: string
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
  authentication?: {
    type: "none" | "api_key" | "bearer"
    key?: string
  }
}

export interface ScheduleTriggerConfig {
  cronExpression: string
  timezone: string
  startDate?: string
  endDate?: string
  data?: Record<string, unknown>
}

export interface FileTriggerConfig {
  formats: string[] // ["csv", "json", "xlsx"]
  mapping?: Record<string, string>
  destination?: string
}

export interface EventTriggerConfig {
  source: "kafka" | "rabbitmq"
  topic?: string
  queue?: string
  filters?: Record<string, unknown>
}

