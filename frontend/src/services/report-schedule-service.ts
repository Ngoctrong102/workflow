import { apiClient } from "./api-client"
import type {
  ReportSchedule,
  CreateReportScheduleRequest,
  UpdateReportScheduleRequest,
  ListReportSchedulesResponse,
} from "@/types/report-schedule"

export interface ListReportSchedulesParams {
  enabled?: boolean
  report_type?: string
  limit?: number
  offset?: number
  search?: string
}

export const reportScheduleService = {
  /**
   * List report schedules
   */
  list: async (params?: ListReportSchedulesParams): Promise<ListReportSchedulesResponse> => {
    const response = await apiClient.get<ListReportSchedulesResponse>("/report-schedules", {
      params,
    })
    return response.data
  },

  /**
   * Get report schedule by ID
   */
  get: async (id: string): Promise<ReportSchedule> => {
    const response = await apiClient.get<ReportSchedule>(`/report-schedules/${id}`)
    return response.data
  },

  /**
   * Create report schedule
   */
  create: async (data: CreateReportScheduleRequest): Promise<ReportSchedule> => {
    const response = await apiClient.post<ReportSchedule>("/report-schedules", data)
    return response.data
  },

  /**
   * Update report schedule
   */
  update: async (id: string, data: UpdateReportScheduleRequest): Promise<ReportSchedule> => {
    const response = await apiClient.put<ReportSchedule>(`/report-schedules/${id}`, data)
    return response.data
  },

  /**
   * Delete report schedule
   */
  delete: async (id: string): Promise<{ message: string }> => {
    const response = await apiClient.delete<{ message: string }>(`/report-schedules/${id}`)
    return response.data
  },

  /**
   * Enable/disable report schedule
   */
  toggle: async (id: string, enabled: boolean): Promise<ReportSchedule> => {
    const response = await apiClient.patch<ReportSchedule>(`/report-schedules/${id}/toggle`, {
      enabled,
    })
    return response.data
  },
}

