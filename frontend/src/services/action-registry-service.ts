import { apiClient } from "./api-client"

export interface ActionRegistryItem {
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

export interface ActionRegistryResponse {
  actions: ActionRegistryItem[]
}

export interface GetActionsByTypeParams {
  type?: string
}

export const actionRegistryService = {
  /**
   * Get all actions from registry
   */
  getAll: async (): Promise<ActionRegistryResponse> => {
    const response = await apiClient.get<ActionRegistryResponse>("/actions/registry")
    return response.data
  },

  /**
   * Get action by ID
   */
  getById: async (id: string): Promise<ActionRegistryItem> => {
    const response = await apiClient.get<ActionRegistryItem>(`/actions/registry/${id}`)
    return response.data
  },

  /**
   * Get actions by type
   */
  getByType: async (params?: GetActionsByTypeParams): Promise<ActionRegistryResponse> => {
    const response = await apiClient.get<ActionRegistryResponse>("/actions/registry/type", {
      params,
    })
    return response.data
  },

  /**
   * Get custom actions
   */
  getCustom: async (): Promise<ActionRegistryResponse> => {
    const response = await apiClient.get<ActionRegistryResponse>("/actions/registry/custom")
    return response.data
  },

  /**
   * Create action definition in registry
   */
  create: async (data: ActionRegistryItem): Promise<ActionRegistryItem> => {
    const response = await apiClient.post<ActionRegistryItem>("/actions/registry", data)
    return response.data
  },

  /**
   * Update action definition in registry
   */
  update: async (id: string, data: Partial<ActionRegistryItem>): Promise<ActionRegistryItem> => {
    const response = await apiClient.put<ActionRegistryItem>(`/actions/registry/${id}`, data)
    return response.data
  },

  /**
   * Delete action definition from registry
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/actions/registry/${id}`)
  },
}

