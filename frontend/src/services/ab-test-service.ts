import { apiClient } from "./api-client"
import type {
  ABTest,
  ABTestListParams,
  ABTestListResponse,
  ABTestResults,
} from "@/types/ab-test"

interface PagedResponse<T> {
  data: T[]
  total: number
  limit: number
  offset: number
  hasMore: boolean
}

export const abTestService = {
  /**
   * List A/B tests with filters
   */
  list: async (params?: ABTestListParams): Promise<ABTestListResponse> => {
    const response = await apiClient.get<PagedResponse<ABTest>>("/ab-tests", {
      params: {
        workflow_id: params?.workflow_id,
        status: params?.status,
        limit: params?.limit,
        offset: params?.offset,
        search: params?.search,
      },
    })
    
    // Backend returns PagedResponse, extract data and map to ABTestListResponse
    const pagedData = response.data
    return {
      tests: pagedData.data || [],
      total: pagedData.total || 0,
      limit: pagedData.limit || 20,
      offset: pagedData.offset || 0,
    }
  },

  /**
   * Get A/B test details
   */
  get: async (testId: string): Promise<ABTest> => {
    const response = await apiClient.get<ABTest>(`/ab-tests/${testId}`)
    return response.data
  },

  /**
   * Create A/B test
   */
  create: async (test: Omit<ABTest, "id" | "created_at" | "updated_at">): Promise<ABTest> => {
    const response = await apiClient.post<ABTest>("/ab-tests", test)
    return response.data
  },

  /**
   * Update A/B test
   */
  update: async (testId: string, test: Partial<ABTest>): Promise<ABTest> => {
    const response = await apiClient.put<ABTest>(`/ab-tests/${testId}`, test)
    return response.data
  },

  /**
   * Delete A/B test
   */
  delete: async (testId: string): Promise<{ message: string }> => {
    const response = await apiClient.delete<{ message: string }>(`/ab-tests/${testId}`)
    return response.data
  },

  /**
   * Get A/B test results
   */
  getResults: async (testId: string): Promise<ABTestResults> => {
    const response = await apiClient.get<ABTestResults>(`/ab-tests/${testId}/results`)
    return response.data
  },

  /**
   * Start A/B test
   */
  start: async (testId: string): Promise<ABTest> => {
    const response = await apiClient.post<ABTest>(`/ab-tests/${testId}/start`)
    return response.data
  },

  /**
   * Pause A/B test
   */
  pause: async (testId: string): Promise<ABTest> => {
    const response = await apiClient.post<ABTest>(`/ab-tests/${testId}/pause`)
    return response.data
  },

  /**
   * Stop A/B test
   */
  stop: async (testId: string): Promise<ABTest> => {
    const response = await apiClient.post<ABTest>(`/ab-tests/${testId}/stop`)
    return response.data
  },
}

