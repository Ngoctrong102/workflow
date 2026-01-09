import { apiClient } from "./api-client"
import type { Template, TemplateChannel } from "@/types/template"

export interface CreateTemplateRequest {
  name: string
  description?: string
  channel: TemplateChannel
  subject?: string
  body: string
  variables?: string[]
  status?: "active" | "inactive" | "draft"
}

export interface UpdateTemplateRequest extends CreateTemplateRequest {
  id: string
}

export interface ListTemplatesParams {
  channel?: TemplateChannel
  status?: string
  limit?: number
  offset?: number
  search?: string
}

export interface PagedResponse<T> {
  data: T[]
  total: number
  limit: number
  offset: number
  hasMore: boolean
}

export interface ListTemplatesResponse extends PagedResponse<Template> {}

export const templateService = {
  /**
   * List all templates
   */
  list: async (params?: ListTemplatesParams): Promise<ListTemplatesResponse> => {
    const response = await apiClient.get<PagedResponse<Template>>("/templates", {
      params,
    })
    return response.data
  },

  /**
   * Get template by ID
   */
  get: async (id: string): Promise<Template> => {
    const response = await apiClient.get<Template>(`/templates/${id}`)
    return response.data
  },

  /**
   * Create new template
   */
  create: async (template: CreateTemplateRequest): Promise<Template> => {
    const response = await apiClient.post<Template>("/templates", template)
    return response.data
  },

  /**
   * Update template
   */
  update: async (id: string, template: Omit<UpdateTemplateRequest, "id">): Promise<Template> => {
    const response = await apiClient.put<Template>(`/templates/${id}`, template)
    return response.data
  },

  /**
   * Delete template
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/templates/${id}`)
  },

  /**
   * Export template
   */
  export: async (id: string): Promise<Template> => {
    const response = await apiClient.get<Template>(`/templates/${id}/export`)
    return response.data
  },

  /**
   * Import template
   */
  import: async (
    template: CreateTemplateRequest,
    options?: { overwrite?: boolean; skipConflicts?: boolean }
  ): Promise<Template> => {
    const response = await apiClient.post<Template>("/templates/import", {
      template,
      ...options,
    })
    return response.data
  },

  /**
   * Bulk delete templates
   */
  bulkDelete: async (ids: string[]): Promise<{ deleted: number; errors: string[] }> => {
    const response = await apiClient.post<{
      total: number
      success: number
      failed: number
      results: Array<{
        id: string
        name?: string
        success: boolean
        message?: string
        error?: string
      }>
      summary?: Record<string, unknown>
    }>("/templates/bulk-delete", ids)
    
    // Map backend response to frontend expected format
    const errors = response.data.results
      .filter((r) => !r.success && r.error)
      .map((r) => r.error!)
    
    return {
      deleted: response.data.success,
      errors,
    }
  },
}

