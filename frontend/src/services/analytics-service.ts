import { apiClient } from "./api-client"
import type {
  WorkflowAnalytics,
  DeliveryAnalytics,
  ChannelAnalytics,
  ErrorAnalytics,
  AnalyticsDateRange,
} from "@/types/analytics"

export interface GetWorkflowAnalyticsParams extends AnalyticsDateRange {
  workflowId: string
}

export interface GetDeliveryAnalyticsParams extends AnalyticsDateRange {}

export interface GetChannelAnalyticsParams extends AnalyticsDateRange {
  channelId: string
}

export interface GetErrorAnalyticsParams extends AnalyticsDateRange {}

export const analyticsService = {
  /**
   * Get workflow analytics
   */
  getWorkflowAnalytics: async (
    params: GetWorkflowAnalyticsParams
  ): Promise<WorkflowAnalytics> => {
    const response = await apiClient.get<WorkflowAnalytics>(
      `/analytics/workflows/${params.workflowId}`,
      {
        params: {
          start_date: params.start,
          end_date: params.end,
          granularity: params.granularity || "daily",
        },
      }
    )
    return response.data
  },

  /**
   * Get delivery analytics
   */
  getDeliveryAnalytics: async (params: GetDeliveryAnalyticsParams): Promise<DeliveryAnalytics> => {
    const response = await apiClient.get<DeliveryAnalytics>("/analytics/deliveries", {
      params: {
        start_date: params.start,
        end_date: params.end,
        granularity: params.granularity || "daily",
      },
    })
    return response.data
  },

  /**
   * Get channel analytics
   */
  getChannelAnalytics: async (params: GetChannelAnalyticsParams): Promise<ChannelAnalytics> => {
    const response = await apiClient.get<ChannelAnalytics>(
      `/analytics/channels/${params.channelId}`,
      {
        params: {
          start_date: params.start,
          end_date: params.end,
          granularity: params.granularity || "daily",
        },
      }
    )
    return response.data
  },

  /**
   * Get error analytics
   */
  getErrorAnalytics: async (params: GetErrorAnalyticsParams): Promise<ErrorAnalytics> => {
    const response = await apiClient.get<ErrorAnalytics>("/analytics/errors", {
      params: {
        start_date: params.start,
        end_date: params.end,
        granularity: params.granularity || "daily",
      },
    })
    return response.data
  },
}

