// API Error Types
export interface ApiError {
  code: string
  message: string
  details?: Record<string, unknown>
  request_id?: string
}

export interface ApiErrorResponse {
  error: ApiError
}

// Pagination Types
export interface PaginationParams {
  limit?: number
  offset?: number
}

export interface PaginationResponse {
  total: number
  limit: number
  offset: number
  has_more: boolean
}

export interface PaginatedResponse<T> {
  data: T[]
  pagination: PaginationResponse
}

// Request/Response Types
export interface ApiResponse<T> {
  data: T
}

// Common Query Parameters
export interface ListQueryParams extends PaginationParams {
  status?: string
  channel?: string
  start_date?: string
  end_date?: string
  q?: string
  type?: string
}

