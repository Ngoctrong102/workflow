import { apiClient } from "./api-client"
import type {
  Execution,
  ExecutionListParams,
  ExecutionListResponse,
  ExecutionDetails,
  NodeExecution,
  ExecutionLog,
} from "@/types/execution"

interface PagedResponse<T> {
  data: T[]
  total: number
  limit: number
  offset: number
  hasMore: boolean
}

export const executionService = {
  /**
   * List executions with filters
   */
  list: async (params?: ExecutionListParams): Promise<ExecutionListResponse> => {
    const queryParams = new URLSearchParams()
    if (params?.workflow_id) queryParams.append("workflowId", params.workflow_id)
    if (params?.status) queryParams.append("status", params.status)
    if (params?.start_date) queryParams.append("startDate", params.start_date)
    if (params?.end_date) queryParams.append("endDate", params.end_date)
    if (params?.limit) queryParams.append("limit", params.limit.toString())
    if (params?.offset) queryParams.append("offset", params.offset.toString())
    if (params?.search) queryParams.append("search", params.search)

    const queryString = queryParams.toString()
    const response = await apiClient.get<PagedResponse<Execution>>(`/executions${queryString ? `?${queryString}` : ""}`)
    
    // Backend returns PagedResponse, extract data and map to ExecutionListResponse
    const pagedData = response.data
    return {
      executions: pagedData.data || [],
      total: pagedData.total || 0,
      limit: pagedData.limit || 20,
      offset: pagedData.offset || 0,
    }
  },

  /**
   * Get execution details
   */
  get: async (executionId: string): Promise<ExecutionDetails> => {
    const response = await apiClient.get<ExecutionDetails>(`/executions/${executionId}/detail`)
    return response.data
  },

  /**
   * Get execution logs
   */
  getLogs: async (executionId: string, nodeId?: string, level?: string): Promise<ExecutionLog[]> => {
    const queryParams = new URLSearchParams()
    if (nodeId) queryParams.append("nodeId", nodeId)
    if (level) queryParams.append("level", level)

    const queryString = queryParams.toString()
    const response = await apiClient.get<ExecutionLog[]>(`/executions/${executionId}/logs${queryString ? `?${queryString}` : ""}`)
    return response.data
  },

  /**
   * Get node executions for an execution
   * Note: Node executions are included in execution details, but this method can be used if needed
   */
  getNodeExecutions: async (executionId: string): Promise<NodeExecution[]> => {
    // Node executions are included in execution details
    const details = await executionService.get(executionId)
    return details.node_executions || []
  },

  /**
   * Get execution context
   */
  getContext: async (executionId: string): Promise<Record<string, unknown>> => {
    const response = await apiClient.get<Record<string, unknown>>(`/executions/${executionId}/context`)
    return response.data
  },

  /**
   * Cancel a running execution
   */
  cancel: async (executionId: string): Promise<{ message: string }> => {
    // Backend expects CancelExecutionRequest body, but returns void (204 No Content)
    await apiClient.post(`/executions/${executionId}/cancel`, {})
    return { message: "Execution cancelled successfully" }
  },

  /**
   * Retry a failed execution
   */
  retry: async (executionId: string): Promise<Execution> => {
    // Backend expects RetryExecutionRequest body and returns ExecutionStatusResponse
    const response = await apiClient.post<Execution>(`/executions/${executionId}/retry`, {})
    return response.data
  },

  /**
   * Get execution for visualization
   */
  visualize: async (executionId: string): Promise<{
    execution: ExecutionDetails
    workflow: {
      id: string
      name: string
      definition: {
        nodes: unknown[]
        edges: unknown[]
      }
    }
    trigger: {
      type: string
      data: Record<string, unknown>
    }
    current_step: number
    total_steps: number
    nodes: Array<{
      id: string
      type: string
      status: "pending" | "running" | "completed" | "failed"
      execution?: NodeExecution
    }>
    context: Record<string, unknown>
  }> => {
    const response = await apiClient.get(`/executions/${executionId}/visualize`)
    return response.data
  },

  /**
   * Execute next step in visualization
   */
  executeStep: async (executionId: string, direction: "forward" | "backward" = "forward"): Promise<{
    step_number: number
    node_id: string
    node_type: string
    status: "running" | "completed" | "failed"
    execution?: NodeExecution
    context: Record<string, unknown>
    next_node?: string
    has_next: boolean
    has_previous: boolean
  }> => {
    const response = await apiClient.post(`/executions/${executionId}/visualize/step`, {
      direction,
    })
    return response.data
  },

  /**
   * Get execution state at specific step
   */
  getStepState: async (executionId: string, stepNumber: number): Promise<{
    step_number: number
    node_id: string
    node_type: string
    status: "pending" | "running" | "completed" | "failed"
    execution?: NodeExecution
    context: Record<string, unknown>
    has_next: boolean
    has_previous: boolean
  }> => {
    const response = await apiClient.get(`/executions/${executionId}/visualize/step/${stepNumber}`)
    return response.data
  },

  /**
   * Reset visualization to start
   */
  resetVisualization: async (executionId: string): Promise<void> => {
    await apiClient.post(`/executions/${executionId}/visualize/reset`, {})
  },

  /**
   * Get current context for visualization
   */
  getVisualizationContext: async (executionId: string): Promise<Record<string, unknown>> => {
    const response = await apiClient.get<Record<string, unknown>>(`/executions/${executionId}/visualize/context`)
    return response.data
  },

  /**
   * Bulk delete executions
   */
  bulkDelete: async (ids: string[]): Promise<{ deleted: number; errors: string[] }> => {
    const response = await apiClient.post<{
      total: number
      success: number
      failed: number
      results: Array<{
        id: string
        success: boolean
        message?: string
        error?: string
      }>
    }>("/executions/bulk-delete", ids)
    
    const errors = response.data.results
      .filter((r) => !r.success && r.error)
      .map((r) => r.error!)
    
    return {
      deleted: response.data.success,
      errors,
    }
  },
}
