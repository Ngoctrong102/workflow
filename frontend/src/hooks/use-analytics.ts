import { useQuery } from "@tanstack/react-query"
import {
  analyticsService,
  type GetWorkflowAnalyticsParams,
  type GetDeliveryAnalyticsParams,
  type GetChannelAnalyticsParams,
  type GetErrorAnalyticsParams,
} from "@/services/analytics-service"

export function useWorkflowAnalytics(params: GetWorkflowAnalyticsParams | null) {
  return useQuery({
    queryKey: ["workflow-analytics", params],
    queryFn: () => analyticsService.getWorkflowAnalytics(params!),
    enabled: !!params,
    staleTime: 1 * 60 * 1000, // 1 minute - analytics data changes frequently
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useDeliveryAnalytics(params: GetDeliveryAnalyticsParams | null) {
  return useQuery({
    queryKey: ["delivery-analytics", params],
    queryFn: () => analyticsService.getDeliveryAnalytics(params!),
    enabled: !!params,
    staleTime: 1 * 60 * 1000, // 1 minute - analytics data changes frequently
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useChannelAnalytics(params: GetChannelAnalyticsParams | null) {
  return useQuery({
    queryKey: ["channel-analytics", params],
    queryFn: () => analyticsService.getChannelAnalytics(params!),
    enabled: !!params,
    staleTime: 1 * 60 * 1000, // 1 minute - analytics data changes frequently
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useErrorAnalytics(params: GetErrorAnalyticsParams | null) {
  return useQuery({
    queryKey: ["error-analytics", params],
    queryFn: () => analyticsService.getErrorAnalytics(params!),
    enabled: !!params,
    staleTime: 1 * 60 * 1000, // 1 minute - analytics data changes frequently
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

