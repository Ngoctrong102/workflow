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
    const response = await apiClient.get<any>("/triggers/registry")
    // apiClient.get returns full axios response, data is in response.data
    const data = response.data || response
    // Map backend response format to TriggerRegistryResponse format
    const triggers = ((data.triggers || []) as any[]).map((item: any) => ({
      id: item.id,
      name: item.name,
      type: item.triggerType || item.type, // Backend returns "triggerType"
      description: item.description || "",
      configTemplate: item.config || {}, // Backend returns "config"
      metadata: item.metadata || {
        icon: "",
        color: "#0ea5e9",
        version: "1.0.0",
      },
    }))
    return { triggers }
  },

  /**
   * Get trigger from registry by ID
   */
  getRegistryById: async (id: string): Promise<TriggerRegistryItem> => {
    const response = await apiClient.get<any>(`/triggers/registry/${id}`)
    
    // apiClient.get returns full axios response, data is in response.data
    const data = response.data || response
    
    // Log response for debugging
    if (import.meta.env.DEV) {
      console.log('[TriggerService] getRegistryById response:', response)
      console.log('[TriggerService] getRegistryById data:', data)
      console.log('[TriggerService] getRegistryById data.config:', data.config)
      console.log('[TriggerService] getRegistryById data.config type:', typeof data.config)
      console.log('[TriggerService] getRegistryById data.config is null:', data.config === null)
      console.log('[TriggerService] getRegistryById data.config is undefined:', data.config === undefined)
    }
    
    // Map backend response format to TriggerRegistryItem format
    // Ensure configTemplate is always an object, never null or undefined
    let configTemplate: Record<string, unknown> = {}
    if (data.config) {
      // If config exists, ensure it's an object (not null, not undefined)
      if (typeof data.config === 'object' && data.config !== null) {
        configTemplate = data.config as Record<string, unknown>
      }
    }
    
    const mapped = {
      id: data.id,
      name: data.name,
      type: data.triggerType || data.type, // Backend returns "triggerType"
      description: data.description || "",
      configTemplate: configTemplate, // Always an object, never null or undefined
      metadata: data.metadata || {
        icon: "",
        color: "#0ea5e9",
        version: "1.0.0",
      },
    }
    
    // Log mapped result for debugging
    if (import.meta.env.DEV) {
      console.log('[TriggerService] getRegistryById mapped:', mapped)
      console.log('[TriggerService] getRegistryById mapped.configTemplate:', mapped.configTemplate)
      console.log('[TriggerService] getRegistryById mapped.configTemplate type:', typeof mapped.configTemplate)
      console.log('[TriggerService] getRegistryById mapped.configTemplate keys:', Object.keys(mapped.configTemplate))
    }
    
    return mapped
  },

  /**
   * Get triggers from registry by type
   */
  getRegistryByType: async (type: string): Promise<TriggerRegistryResponse> => {
    const response = await apiClient.get<any>(`/triggers/registry/type/${type}`)
    // apiClient.get returns full axios response, data is in response.data
    const data = response.data || response
    // Map backend response format to TriggerRegistryResponse format
    const triggers = ((data.triggers || []) as any[]).map((item: any) => ({
      id: item.id,
      name: item.name,
      type: item.triggerType || item.type, // Backend returns "triggerType"
      description: item.description || "",
      configTemplate: item.config || {}, // Backend returns "config"
      metadata: item.metadata || {
        icon: "",
        color: "#0ea5e9",
        version: "1.0.0",
      },
    }))
    return { triggers }
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
    // Convert authentication object to apiKey string for backend
    let apiKey: string | undefined
    if (request.authentication && request.authentication.type !== "none") {
      apiKey = request.authentication.key
    }
    
    const response = await apiClient.post<Trigger>(
      `/triggers/api`,
      {
        workflowId: request.workflowId,
        path: request.path,
        method: request.method,
        apiKey: apiKey,
      }
    )
    return response.data
  },

  /**
   * Create schedule trigger
   */
  createSchedule: async (request: CreateScheduleTriggerRequest): Promise<Trigger> => {
    // Convert date strings to ISO format for backend LocalDateTime
    // Backend expects ISO 8601 format: "2024-01-01T00:00:00" (without timezone)
    // datetime-local input returns "2024-01-01T00:00" format
    let startDate: string | undefined
    let endDate: string | undefined
    
    if (request.startDate) {
      // Convert "2024-01-01T00:00" to "2024-01-01T00:00:00"
      if (request.startDate.includes('T')) {
        // If it doesn't have seconds, add :00
        const parts = request.startDate.split('T')
        if (parts.length === 2 && !parts[1].includes(':')) {
          startDate = `${request.startDate}:00:00`
        } else if (parts[1].split(':').length === 2) {
          // Has hours:minutes but no seconds
          startDate = `${request.startDate}:00`
        } else {
          startDate = request.startDate
        }
      } else {
        startDate = `${request.startDate}T00:00:00`
      }
    }
    
    if (request.endDate) {
      if (request.endDate.includes('T')) {
        const parts = request.endDate.split('T')
        if (parts.length === 2 && !parts[1].includes(':')) {
          endDate = `${request.endDate}:00:00`
        } else if (parts[1].split(':').length === 2) {
          endDate = `${request.endDate}:00`
        } else {
          endDate = request.endDate
        }
      } else {
        endDate = `${request.endDate}T00:00:00`
      }
    }
    
    const response = await apiClient.post<Trigger>(
      `/triggers/schedule`,
      {
        workflowId: request.workflowId,
        cron_expression: request.cronExpression,
        timezone: request.timezone,
        start_date: startDate,
        end_date: endDate,
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
      `/triggers/file`,
      {
        workflowId: request.workflowId,
        fileFormats: request.formats,
        dataMapping: request.mapping,
        // Backend doesn't have destination field, it uses dataMapping instead
        // maxFileSize and processingMode have defaults in backend
      }
    )
    return response.data
  },

  /**
   * Create event trigger
   */
  createEvent: async (request: CreateEventTriggerRequest): Promise<Trigger> => {
    // Backend expects: queueType, topic, consumerGroup, brokers, offset, filter (singular)
    // Frontend sends: source, topic, queue, filters (plural)
    // Map frontend fields to backend fields
    const requestBody: Record<string, unknown> = {
      workflowId: request.workflowId,
      queueType: request.source, // Map source to queueType
      topic: request.topic,
      // Backend doesn't have queue field, it uses topic for both Kafka and RabbitMQ
      filter: request.filters, // Map filters (plural) to filter (singular)
    }
    
    // Add optional Kafka-specific fields if provided
    // Note: These fields are not in CreateEventTriggerRequest interface yet,
    // but they may be passed in the request object
    const requestAny = request as any
    if (requestAny.consumerGroup) {
      requestBody.consumerGroup = requestAny.consumerGroup
    }
    if (requestAny.brokers) {
      // Convert brokers string (newline-separated) or array to List<String>
      if (typeof requestAny.brokers === 'string') {
        requestBody.brokers = requestAny.brokers
          .split('\n')
          .map((s: string) => s.trim())
          .filter((s: string) => s.length > 0)
      } else if (Array.isArray(requestAny.brokers)) {
        requestBody.brokers = requestAny.brokers
      }
    }
    if (requestAny.offset) {
      requestBody.offset = requestAny.offset
    }
    
    const response = await apiClient.post<Trigger>(
      `/triggers/event`,
      requestBody
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

  /**
   * Create trigger definition in registry
   * @deprecated Use createTriggerConfig instead. This method maps to createTriggerConfig.
   */
  createRegistryDefinition: async (data: TriggerRegistryItem): Promise<TriggerRegistryItem> => {
    // Map TriggerRegistryItem to CreateTriggerConfigRequest format and call /triggers endpoint
    const response = await apiClient.post<any>("/triggers", {
      name: data.name,
      triggerType: data.type as "api-call" | "scheduler" | "event",
      status: "active",
      config: data.configTemplate || {},
    })
    
    // Map TriggerResponse back to TriggerRegistryItem format
    return {
      id: response.id,
      name: response.name || data.name,
      type: response.triggerType || data.type,
      description: data.description,
      configTemplate: response.config,
      metadata: data.metadata,
    }
  },

  /**
   * Update trigger definition in registry
   * @deprecated Use updateTriggerConfig instead. This method maps to updateTriggerConfig.
   */
  updateRegistryDefinition: async (id: string, data: Partial<TriggerRegistryItem>): Promise<TriggerRegistryItem> => {
    // Map TriggerRegistryItem to UpdateTriggerConfigRequest format and call /triggers/{id} endpoint
    const response = await apiClient.put<any>(`/triggers/${id}`, {
      name: data.name,
      config: data.configTemplate,
      // status is optional in update, keep existing if not provided
    })
    
    // Map TriggerResponse back to TriggerRegistryItem format
    return {
      id: response.id,
      name: response.name || data.name || "",
      type: response.triggerType || data.type || "",
      description: data.description,
      configTemplate: response.config,
      metadata: data.metadata,
    }
  },

  /**
   * Delete trigger definition from registry
   * @deprecated Use deleteTriggerConfig instead. This method maps to deleteTriggerConfig.
   */
  deleteRegistryDefinition: async (id: string): Promise<void> => {
    await apiClient.delete(`/triggers/${id}`)
  },

  /**
   * Create trigger config (independent, not workflow-specific)
   */
  createTriggerConfig: async (request: {
    name: string
    triggerType: "api-call" | "scheduler" | "event"
    status?: "active" | "inactive"
    config: Record<string, unknown>
  }): Promise<Trigger> => {
    // Log request details for debugging
    if (import.meta.env.DEV) {
      console.log('[TriggerService] Creating trigger config:', {
        url: '/triggers',
        method: 'POST',
        data: {
          name: request.name,
          triggerType: request.triggerType,
          status: request.status || "active",
          config: request.config,
        },
      })
    }
    
    const response = await apiClient.post<Trigger>("/triggers", {
      name: request.name,
      triggerType: request.triggerType,
      status: request.status || "active",
      config: request.config,
    })
    return response.data
  },

  /**
   * Update trigger config (independent, not workflow-specific)
   */
  updateTriggerConfig: async (id: string, request: {
    name?: string
    triggerType?: "api-call" | "scheduler" | "event"
    status?: "active" | "inactive"
    config?: Record<string, unknown>
  }): Promise<Trigger> => {
    const response = await apiClient.put<Trigger>(`/triggers/${id}`, request)
    return response.data
  },
}

