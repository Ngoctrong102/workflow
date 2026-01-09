export type PeriodType = "last_24h" | "last_7d" | "last_30d" | "last_90d" | "custom"
export type ReportFormat = "csv" | "excel" | "json"
export type ReportStatus = "active" | "inactive" | "paused"

export interface WorkflowReportConfig {
  id: string
  workflow_id: string
  name: string
  analyst_query: string
  period_type: PeriodType
  period_start_date?: string | null
  period_end_date?: string | null
  schedule_cron: string
  recipients: string[]
  format: ReportFormat
  timezone: string
  status: ReportStatus
  last_generated_at?: string | null
  next_generation_at?: string | null
  last_generation_status?: "success" | "failed" | null
  last_generation_error?: string | null
  generation_count: number
  created_at?: string
  updated_at?: string
}

export interface CreateWorkflowReportRequest {
  name: string
  analyst_query: string
  period_type: PeriodType
  period_start_date?: string | null
  period_end_date?: string | null
  schedule_cron: string
  recipients: string[]
  format: ReportFormat
  timezone: string
  status?: ReportStatus
}

export interface UpdateWorkflowReportRequest extends CreateWorkflowReportRequest {
  id: string
}

export interface WorkflowReport {
  id: string
  workflow_id: string
  period: {
    start: string
    end: string
  }
  generated_at: string
  format: ReportFormat
  file_size: number
  delivery_status?: "sent" | "failed"
  error?: string
}

export interface ReportPreview {
  query: string
  parameters: {
    workflow_id: string
    start_date: string
    end_date: string
  }
  results: Array<Record<string, unknown>>
  row_count: number
  execution_time_ms: number
}

