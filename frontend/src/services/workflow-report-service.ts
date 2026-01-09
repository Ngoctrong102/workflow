import { apiClient } from "./api-client"
import type {
  WorkflowReportConfig,
  CreateWorkflowReportRequest,
  UpdateWorkflowReportRequest,
  WorkflowReport,
  ReportPreview,
} from "@/types/workflow-report"
import type { PagedResponse } from "./workflow-service"

export interface ListWorkflowReportsParams {
  workflowId: string
  limit?: number
  offset?: number
  startDate?: string
  endDate?: string
}

export interface ListWorkflowReportsResponse extends PagedResponse<WorkflowReport> {}

export const workflowReportService = {
  /**
   * Get report configuration for a workflow
   */
  getConfig: async (workflowId: string): Promise<WorkflowReportConfig | null> => {
    const response = await apiClient.get<WorkflowReportConfig | null>(
      `/workflows/${workflowId}/report`
    )
    return response.data
  },

  /**
   * Create report configuration
   */
  createConfig: async (
    workflowId: string,
    config: CreateWorkflowReportRequest
  ): Promise<WorkflowReportConfig> => {
    const response = await apiClient.post<WorkflowReportConfig>(
      `/workflows/${workflowId}/report`,
      config
    )
    return response.data
  },

  /**
   * Update report configuration
   */
  updateConfig: async (
    workflowId: string,
    config: Omit<UpdateWorkflowReportRequest, "id" | "workflowId">
  ): Promise<WorkflowReportConfig> => {
    const response = await apiClient.put<WorkflowReportConfig>(
      `/workflows/${workflowId}/report`,
      config
    )
    return response.data
  },

  /**
   * Delete report configuration
   */
  deleteConfig: async (workflowId: string): Promise<void> => {
    await apiClient.delete(`/workflows/${workflowId}/report`)
  },

  /**
   * Update report status
   */
  updateStatus: async (
    workflowId: string,
    status: "active" | "inactive" | "paused"
  ): Promise<WorkflowReportConfig> => {
    const response = await apiClient.patch<WorkflowReportConfig>(
      `/workflows/${workflowId}/report/status`,
      { status }
    )
    return response.data
  },

  /**
   * Validate analyst query
   */
  validateQuery: async (
    workflowId: string,
    analystQuery: string
  ): Promise<{ valid: boolean; error: string | null }> => {
    const response = await apiClient.post<{ valid: boolean; error: string | null }>(
      `/workflows/${workflowId}/report/validate`,
      { analyst_query: analystQuery }
    )
    return response.data
  },

  /**
   * Generate report preview
   */
  generatePreview: async (
    workflowId: string,
    params?: {
      analyst_query?: string
      period_start?: string
      period_end?: string
    }
  ): Promise<ReportPreview> => {
    const response = await apiClient.post<ReportPreview>(
      `/workflows/${workflowId}/report/preview`,
      params
    )
    return response.data
  },

  /**
   * Generate report manually
   */
  generateReport: async (
    workflowId: string,
    params?: {
      period_start?: string
      period_end?: string
    }
  ): Promise<WorkflowReport> => {
    const response = await apiClient.post<WorkflowReport>(
      `/workflows/${workflowId}/report/generate`,
      params
    )
    return response.data
  },

  /**
   * List generated reports
   */
  listReports: async (params: ListWorkflowReportsParams): Promise<ListWorkflowReportsResponse> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<PagedResponse<WorkflowReport>>(
      `/workflows/${workflowId}/report/history`,
      {
        params: queryParams,
      }
    )
    return response.data
  },

  /**
   * Download report file
   */
  downloadReport: async (workflowId: string, reportId: string): Promise<Blob> => {
    const response = await apiClient.get(
      `/workflows/${workflowId}/report/history/${reportId}/download`,
      {
        responseType: "blob",
      }
    )
    return response.data
  },
}

