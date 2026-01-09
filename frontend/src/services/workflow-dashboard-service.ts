import { apiClient } from "./api-client"
import type { WorkflowDashboardData } from "@/types/workflow-dashboard"

export interface WorkflowDashboardParams {
  workflowId: string
  start_date?: string
  end_date?: string
  timezone?: string
  status?: string
  triggerType?: string
  nodeId?: string
  channel?: string
}

export interface GetTrendsParams {
  workflowId: string
  start_date?: string
  end_date?: string
  granularity?: "hourly" | "daily" | "weekly" | "monthly"
}

export interface GetNodesParams {
  workflowId: string
  start_date?: string
  end_date?: string
}

export interface GetChannelsParams {
  workflowId: string
  start_date?: string
  end_date?: string
}

export interface GetExecutionsParams {
  workflowId: string
  status?: string
  limit?: number
  offset?: number
  start_date?: string
  end_date?: string
}

export interface GetErrorsParams {
  workflowId: string
  start_date?: string
  end_date?: string
  limit?: number
  offset?: number
}

export const workflowDashboardService = {
  /**
   * Get workflow dashboard overview
   */
  getDashboard: async (params: WorkflowDashboardParams): Promise<WorkflowDashboardData> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<WorkflowDashboardData>(
      `/workflows/${workflowId}/dashboard`,
      {
        params: {
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
          timezone: queryParams.timezone || "UTC",
        },
      }
    )
    return response.data
  },

  /**
   * Get workflow execution trends
   */
  getTrends: async (params: GetTrendsParams): Promise<{ data: Array<{ timestamp: string; total: number; successful: number; failed: number }> }> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<{ data: Array<{ timestamp: string; total: number; successful: number; failed: number }> }>(
      `/workflows/${workflowId}/dashboard/trends`,
      {
        params: {
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
          granularity: queryParams.granularity || "daily",
        },
      }
    )
    return response.data
  },

  /**
   * Get workflow node performance
   */
  getNodes: async (params: GetNodesParams): Promise<{ nodes: Array<{ node_id: string; node_name: string; node_type: string; execution_count: number; average_execution_time: number; success_rate: number }> }> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<{ nodes: Array<{ node_id: string; node_name: string; node_type: string; execution_count: number; average_execution_time: number; success_rate: number }> }>(
      `/workflows/${workflowId}/dashboard/nodes`,
      {
        params: {
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
        },
      }
    )
    return response.data
  },

  /**
   * Get workflow channel performance
   */
  getChannels: async (params: GetChannelsParams): Promise<{ channels: Array<{ channel: string; sent: number; delivered: number; delivery_rate: number }> }> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<{ channels: Array<{ channel: string; sent: number; delivered: number; delivery_rate: number }> }>(
      `/workflows/${workflowId}/dashboard/channels`,
      {
        params: {
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
        },
      }
    )
    return response.data
  },

  /**
   * Get workflow execution history
   */
  getExecutions: async (params: GetExecutionsParams): Promise<{ executions: Array<{ id: string; started_at: string; completed_at?: string; status: string; duration?: number }>; total: number }> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<{ executions: Array<{ id: string; started_at: string; completed_at?: string; status: string; duration?: number }>; total: number }>(
      `/workflows/${workflowId}/dashboard/executions`,
      {
        params: {
          status: queryParams.status,
          limit: queryParams.limit || 20,
          offset: queryParams.offset || 0,
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
        },
      }
    )
    return response.data
  },

  /**
   * Get workflow error analysis
   */
  getErrors: async (params: GetErrorsParams): Promise<{ summary: { total_errors: number; error_rate: number }; errors: Array<{ id: string; timestamp: string; error_type: string; error_message: string; execution_id: string }> }> => {
    const { workflowId, ...queryParams } = params
    const response = await apiClient.get<{ summary: { total_errors: number; error_rate: number }; errors: Array<{ id: string; timestamp: string; error_type: string; error_message: string; execution_id: string }> }>(
      `/workflows/${workflowId}/dashboard/errors`,
      {
        params: {
          start_date: queryParams.start_date,
          end_date: queryParams.end_date,
          limit: queryParams.limit || 50,
          offset: queryParams.offset || 0,
        },
      }
    )
    return response.data
  },
}
