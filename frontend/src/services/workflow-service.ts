import { apiClient } from "./api-client"
import type { WorkflowDefinition } from "@/types/workflow"

export interface Workflow {
  id: string
  name: string
  description?: string
  status: "draft" | "active" | "inactive" | "paused" | "archived"
  version: number
  definition?: WorkflowDefinition
  tags?: string[]
  createdAt: string
  updatedAt: string
}

export interface CreateWorkflowRequest {
  name: string
  description?: string
  definition: WorkflowDefinition
  status?: "draft" | "active" | "inactive" | "paused" | "archived"
  tags?: string[]
}

export interface UpdateWorkflowRequest extends CreateWorkflowRequest {
  id: string
}

export interface ListWorkflowsParams {
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

export interface ListWorkflowsResponse extends PagedResponse<Workflow> {}

export interface ExecuteWorkflowRequest {
  data: Record<string, unknown>
}

export interface ExecuteWorkflowResponse {
  executionId: string
  status: "success" | "failed" | "running"
  nodeExecutions: Array<{
    nodeId: string
    nodeName: string
    status: "pending" | "running" | "success" | "failed"
    output?: Record<string, unknown>
    error?: string
    duration?: number
  }>
  error?: string
}

export const workflowService = {
  /**
   * List all workflows
   */
  list: async (params?: ListWorkflowsParams): Promise<ListWorkflowsResponse> => {
    const response = await apiClient.get<PagedResponse<Workflow>>("/workflows", {
      params,
    })
    return response.data
  },

  /**
   * Get workflow by ID
   */
  get: async (id: string): Promise<Workflow> => {
    const response = await apiClient.get<Workflow>(`/workflows/${id}`)
    return response.data
  },

  /**
   * Create new workflow
   */
  create: async (workflow: CreateWorkflowRequest): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>("/workflows", workflow)
    return response.data
  },

  /**
   * Update workflow
   */
  update: async (id: string, workflow: Omit<UpdateWorkflowRequest, "id">): Promise<Workflow> => {
    const response = await apiClient.put<Workflow>(`/workflows/${id}`, workflow)
    return response.data
  },

  /**
   * Delete workflow
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/workflows/${id}`)
  },

  /**
   * Execute workflow with test data
   */
  execute: async (id: string, data: ExecuteWorkflowRequest): Promise<ExecuteWorkflowResponse> => {
    const response = await apiClient.post<ExecuteWorkflowResponse>(
      `/workflows/${id}/execute`,
      data
    )
    return response.data
  },

  /**
   * Export workflow
   */
  export: async (id: string): Promise<WorkflowDefinition> => {
    const response = await apiClient.get<WorkflowDefinition>(`/workflows/${id}/export`)
    return response.data
  },

  /**
   * Import workflow
   */
  import: async (
    workflow: WorkflowDefinition,
    options?: { overwrite?: boolean; skipConflicts?: boolean }
  ): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>("/workflows/import", {
      workflow,
      ...options,
    })
    return response.data
  },

  /**
   * Bulk delete workflows
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
    }>("/workflows/bulk-delete", ids)
    
    // Map backend response to frontend expected format
    const errors = response.data.results
      .filter((r) => !r.success && r.error)
      .map((r) => r.error!)
    
    return {
      deleted: response.data.success,
      errors,
    }
  },

  /**
   * Bulk update workflow status
   */
  bulkUpdateStatus: async (
    ids: string[],
    status: "draft" | "active" | "inactive" | "paused" | "archived"
  ): Promise<{ updated: number; errors: string[] }> => {
    const response = await apiClient.post<{ updated: number; errors: string[] }>("/workflows/bulk-update-status", {
      ids,
      status,
    })
    return response.data
  },

  /**
   * Get workflow versions/history
   */
  getVersions: async (id: string): Promise<Workflow[]> => {
    const response = await apiClient.get<Workflow[]>(`/workflows/${id}/versions`)
    return response.data
  },

  /**
   * Get workflow by ID and version
   */
  getByVersion: async (id: string, version: number): Promise<Workflow> => {
    const response = await apiClient.get<Workflow>(`/workflows/${id}/versions/${version}`)
    return response.data
  },

  /**
   * Activate workflow
   */
  activate: async (id: string): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>(`/workflows/${id}/activate`)
    return response.data
  },

  /**
   * Deactivate workflow
   */
  deactivate: async (id: string): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>(`/workflows/${id}/deactivate`)
    return response.data
  },

  /**
   * Pause workflow
   */
  pause: async (id: string): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>(`/workflows/${id}/pause`)
    return response.data
  },

  /**
   * Resume workflow
   */
  resume: async (id: string): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>(`/workflows/${id}/resume`)
    return response.data
  },

  /**
   * Rollback workflow to a specific version
   */
  rollback: async (id: string, version: number): Promise<Workflow> => {
    const response = await apiClient.post<Workflow>(`/workflows/${id}/rollback`, { version })
    return response.data
  },
}

