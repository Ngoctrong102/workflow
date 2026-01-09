export type ReportFrequency = "daily" | "weekly" | "monthly"
export type ReportFormat = "csv" | "pdf" | "excel"

export interface ReportSchedule {
  id: string
  name: string
  description?: string
  frequency: ReportFrequency
  format: ReportFormat
  recipients: string[]
  enabled: boolean
  lastRun?: string
  nextRun?: string
  createdAt: string
  updatedAt: string
}

export interface CreateReportScheduleRequest {
  name: string
  description?: string
  frequency: ReportFrequency
  format: ReportFormat
  recipients: string[]
  enabled?: boolean
}

export interface UpdateReportScheduleRequest extends CreateReportScheduleRequest {
  id: string
}

export interface ReportData {
  id: string
  scheduleId: string
  generatedAt: string
  format: ReportFormat
  fileUrl?: string
  status: "pending" | "generating" | "completed" | "failed"
  error?: string
  deliveryStatus?: {
    totalRecipients: number
    delivered: number
    failed: number
    pending: number
    lastAttempt?: string
    errors?: Array<{
      recipient: string
      error: string
      attemptedAt: string
    }>
  }
}

export interface DataRetentionSettings {
  enabled: boolean
  retentionDays: number
  lastCleanup?: string
  nextCleanup?: string
  totalRecordsDeleted?: number
}

