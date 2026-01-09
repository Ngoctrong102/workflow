export type ScheduleFrequency = "daily" | "weekly" | "monthly"
export type ReportFormat = "csv" | "pdf" | "excel"

export interface ReportSchedule {
  id: string
  name: string
  description?: string
  frequency: ScheduleFrequency
  format: ReportFormat
  recipients: string[] // Email addresses
  report_type: "analytics" | "delivery" | "error" | "workflow" | "custom"
  report_config?: Record<string, unknown> // Custom report configuration
  enabled: boolean
  last_run?: string
  next_run?: string
  created_at: string
  updated_at: string
}

export interface CreateReportScheduleRequest {
  name: string
  description?: string
  frequency: ScheduleFrequency
  format: ReportFormat
  recipients: string[]
  report_type: "analytics" | "delivery" | "error" | "workflow" | "custom"
  report_config?: Record<string, unknown>
  enabled?: boolean
}

export interface UpdateReportScheduleRequest extends Partial<CreateReportScheduleRequest> {}

export interface ListReportSchedulesResponse {
  schedules: ReportSchedule[]
  total: number
  limit: number
  offset: number
}

