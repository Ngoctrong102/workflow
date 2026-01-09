import { apiClient } from "./api-client"
import type {
  TemplateLibraryItem,
  TemplateLibraryListParams,
  TemplateLibraryListResponse,
  TemplateCategory,
} from "@/types/template-library"

export const templateLibraryService = {
  /**
   * List template library items
   */
  list: async (params?: TemplateLibraryListParams): Promise<TemplateLibraryListResponse> => {
    const response = await apiClient.get<TemplateLibraryListResponse>("/template-library", {
      params,
    })
    return response.data
  },

  /**
   * Get template library item details
   */
  get: async (id: string): Promise<TemplateLibraryItem> => {
    const response = await apiClient.get<TemplateLibraryItem>(`/template-library/${id}`)
    return response.data
  },

  /**
   * Install template from library
   */
  install: async (id: string): Promise<{ template_id: string; message: string }> => {
    const response = await apiClient.post<{ template_id: string; message: string }>(
      `/template-library/${id}/install`
    )
    return response.data
  },

  /**
   * Get template categories
   */
  getCategories: async (): Promise<TemplateCategory[]> => {
    const response = await apiClient.get<TemplateCategory[]>("/template-library/categories")
    return response.data
  },
}

