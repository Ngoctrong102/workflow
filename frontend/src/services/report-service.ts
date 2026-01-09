import { apiClient } from "./api-client"
import type {
  ReportSchedule,
  CreateReportScheduleRequest,
  UpdateReportScheduleRequest,
  ReportData,
  DataRetentionSettings,
} from "@/types/report"
import type { PagedResponse } from "./workflow-service"

export interface ListReportSchedulesParams {
  limit?: number
  offset?: number
  enabled?: boolean
}

export interface ListReportSchedulesResponse extends PagedResponse<ReportSchedule> {}

export interface ListReportDataParams {
  scheduleId?: string
  start?: string
  end?: string
  status?: "pending" | "generating" | "completed" | "failed"
  limit?: number
  offset?: number
}

export interface ListReportDataResponse extends PagedResponse<ReportData> {}

export const reportService = {
  /**
   * List all report schedules
   */
  listSchedules: async (params?: ListReportSchedulesParams): Promise<ListReportSchedulesResponse> => {
    const response = await apiClient.get<PagedResponse<ReportSchedule>>("/reports/schedules", {
      params,
    })
    return response.data
  },

  /**
   * Get report schedule by ID
   */
  getSchedule: async (id: string): Promise<ReportSchedule> => {
    const response = await apiClient.get<ReportSchedule>(`/reports/schedules/${id}`)
    return response.data
  },

  /**
   * Create new report schedule
   */
  createSchedule: async (schedule: CreateReportScheduleRequest): Promise<ReportSchedule> => {
    const response = await apiClient.post<ReportSchedule>("/reports/schedules", schedule)
    return response.data
  },

  /**
   * Update report schedule
   */
  updateSchedule: async (
    id: string,
    schedule: Omit<UpdateReportScheduleRequest, "id">
  ): Promise<ReportSchedule> => {
    const response = await apiClient.put<ReportSchedule>(`/reports/schedules/${id}`, schedule)
    return response.data
  },

  /**
   * Delete report schedule
   */
  deleteSchedule: async (id: string): Promise<void> => {
    await apiClient.delete(`/reports/schedules/${id}`)
  },

  /**
   * Enable/disable report schedule
   */
  toggleSchedule: async (id: string, enabled: boolean): Promise<ReportSchedule> => {
    const response = await apiClient.patch<ReportSchedule>(`/reports/schedules/${id}/toggle`, {
      enabled,
    })
    return response.data
  },

  /**
   * List report data
   */
  listReportData: async (params?: ListReportDataParams): Promise<ListReportDataResponse> => {
    const response = await apiClient.get<PagedResponse<ReportData>>("/reports/data", {
      params,
    })
    return response.data
  },

  /**
   * Get data retention settings
   */
  getDataRetentionSettings: async (): Promise<DataRetentionSettings> => {
    const response = await apiClient.get<DataRetentionSettings>("/reports/data-retention")
    return response.data
  },

  /**
   * Update data retention settings
   */
  updateDataRetentionSettings: async (
    settings: Partial<DataRetentionSettings>
  ): Promise<DataRetentionSettings> => {
    const response = await apiClient.put<DataRetentionSettings>("/reports/data-retention", settings)
    return response.data
  },

  /**
   * Trigger manual data cleanup
   */
  triggerCleanup: async (): Promise<{ deleted: number; message: string }> => {
    const response = await apiClient.post<{ deleted: number; message: string }>(
      "/reports/data-retention/cleanup"
    )
    return response.data
  },
}

