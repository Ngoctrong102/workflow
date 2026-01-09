import { apiClient } from "./api-client"
import type { Trigger } from "@/types/trigger"

export interface CreateApiTriggerRequest {
  workflowId: string
  path: string
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
  authentication?: {
    type: "none" | "api_key" | "bearer"
    key?: string
  }
}

export interface CreateScheduleTriggerRequest {
  workflowId: string
  cronExpression: string
  timezone: string
  startDate?: string
  endDate?: string
  data?: Record<string, unknown>
}

export interface CreateFileTriggerRequest {
  workflowId: string
  formats: string[]
  mapping?: Record<string, string>
  destination?: string
}

export interface CreateEventTriggerRequest {
  workflowId: string
  source: "kafka" | "rabbitmq"
  topic?: string
  queue?: string
  filters?: Record<string, unknown>
}

export interface UpdateTriggerRequest {
  id: string
  config: Record<string, unknown>
  status?: "active" | "inactive"
}

export interface ListTriggersParams {
  workflowId?: string
  type?: string
  status?: string
}

export interface TriggerRegistryItem {
  id: string
  name: string
  type: string
  description?: string
  configTemplate?: Record<string, unknown>
  metadata?: {
    icon?: string
    color?: string
    version?: string
  }
}

export interface TriggerRegistryResponse {
  triggers: TriggerRegistryItem[]
}

export const triggerService = {
  /**
   * Get all triggers from registry
   */
  getRegistry: async (): Promise<TriggerRegistryResponse> => {
    const response = await apiClient.get<TriggerRegistryResponse>("/triggers/registry")
    return response.data
  },

  /**
   * Get trigger from registry by ID
   */
  getRegistryById: async (id: string): Promise<TriggerRegistryItem> => {
    const response = await apiClient.get<TriggerRegistryItem>(`/triggers/registry/${id}`)
    return response.data
  },

  /**
   * Get triggers from registry by type
   */
  getRegistryByType: async (type: string): Promise<TriggerRegistryResponse> => {
    const response = await apiClient.get<TriggerRegistryResponse>(`/triggers/registry/type/${type}`)
    return response.data
  },
  /**
   * List triggers for a workflow
   */
  list: async (workflowId: string): Promise<Trigger[]> => {
    const response = await apiClient.get<Trigger[]>(`/workflows/${workflowId}/triggers`)
    return response.data
  },

  /**
   * Get trigger by ID
   */
  get: async (id: string): Promise<Trigger> => {
    const response = await apiClient.get<Trigger>(`/triggers/${id}`)
    return response.data
  },

  /**
   * Create API trigger
   */
  createApi: async (request: CreateApiTriggerRequest): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(
      `/workflows/${request.workflowId}/triggers/api`,
      {
        path: request.path,
        method: request.method,
        authentication: request.authentication,
      }
    )
    return response.data
  },

  /**
   * Create schedule trigger
   */
  createSchedule: async (request: CreateScheduleTriggerRequest): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(
      `/workflows/${request.workflowId}/triggers/schedule`,
      {
        cron_expression: request.cronExpression,
        timezone: request.timezone,
        start_date: request.startDate,
        end_date: request.endDate,
        data: request.data,
      }
    )
    return response.data
  },

  /**
   * Create file trigger
   */
  createFile: async (request: CreateFileTriggerRequest): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(
      `/workflows/${request.workflowId}/triggers/file`,
      {
        formats: request.formats,
        mapping: request.mapping,
        destination: request.destination,
      }
    )
    return response.data
  },

  /**
   * Create event trigger
   */
  createEvent: async (request: CreateEventTriggerRequest): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(
      `/workflows/${request.workflowId}/triggers/event`,
      {
        source: request.source,
        topic: request.topic,
        queue: request.queue,
        filters: request.filters,
      }
    )
    return response.data
  },

  /**
   * Update trigger
   */
  update: async (id: string, request: Omit<UpdateTriggerRequest, "id">): Promise<Trigger> => {
    const response = await apiClient.put<Trigger>(`/triggers/${id}`, request)
    return response.data
  },

  /**
   * Delete trigger
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/triggers/${id}`)
  },

  /**
   * Activate trigger
   */
  activate: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/activate`)
    return response.data
  },

  /**
   * Deactivate trigger
   */
  deactivate: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/deactivate`)
    return response.data
  },

  /**
   * Initialize trigger instance
   */
  initialize: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/initialize`)
    return response.data
  },

  /**
   * Start trigger instance
   */
  start: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/start`)
    return response.data
  },

  /**
   * Pause trigger instance
   */
  pause: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/pause`)
    return response.data
  },

  /**
   * Resume trigger instance
   */
  resume: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/resume`)
    return response.data
  },

  /**
   * Stop trigger instance
   */
  stop: async (id: string): Promise<Trigger> => {
    const response = await apiClient.post<Trigger>(`/triggers/${id}/stop`)
    return response.data
  },
}

