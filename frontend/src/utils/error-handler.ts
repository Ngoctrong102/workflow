import type { AxiosError } from "axios"
import type { ApiErrorResponse } from "@/types/api"

export interface ApiException extends Error {
  code: string
  status?: number
  statusCode?: number
  details?: Record<string, unknown>
  requestId?: string
}

/**
 * Transform Axios errors into a consistent ApiException format
 */
export function handleApiError(error: unknown): ApiException {
  // Check if it's already an ApiException
  if (error && typeof error === "object" && "code" in error && ("statusCode" in error || "status" in error)) {
    return error as ApiException
  }

  if (error && typeof error === "object" && "response" in error) {
    const axiosError = error as AxiosError<ApiErrorResponse>

    // Handle API error response
    if (axiosError.response?.data?.error) {
      const errorData = axiosError.response.data.error
      const apiException = new Error(
        errorData.message || axiosError.response.statusText || "An error occurred"
      ) as ApiException

      apiException.code = errorData.code || `HTTP_${axiosError.response.status || 500}`
      apiException.status = axiosError.response.status
      apiException.statusCode = axiosError.response.status
      apiException.details = errorData.details || {}
      apiException.requestId = errorData.request_id

      return apiException
    }

    // Handle network errors
    if (axiosError.code === "ECONNABORTED") {
      const apiException = new Error("Request timeout. Please try again.") as ApiException
      apiException.code = "TIMEOUT"
      apiException.status = 0
      apiException.statusCode = 0
      return apiException
    }

    if (axiosError.code === "ERR_NETWORK") {
      const apiException = new Error("Network error. Please check your connection.") as ApiException
      apiException.code = "NETWORK_ERROR"
      apiException.status = 0
      apiException.statusCode = 0
      return apiException
    }

    // Handle HTTP errors without error response
    const apiException = new Error(axiosError.message || "An HTTP error occurred") as ApiException
    apiException.code = "HTTP_ERROR"
    apiException.status = axiosError.response?.status || 0
    apiException.statusCode = axiosError.response?.status || 0
    return apiException
  }

  // Handle unknown errors
  if (error instanceof Error) {
    const apiException = error as ApiException
    apiException.code = "UNKNOWN_ERROR"
    apiException.statusCode = 0
    apiException.status = 0
    return apiException
  }

  const apiException = new Error("An unexpected error occurred") as ApiException
  apiException.code = "UNKNOWN_ERROR"
  apiException.statusCode = 0
  apiException.status = 0
  return apiException
}

/**
 * Legacy function name for backward compatibility
 */
export function errorHandler(error: unknown): ApiException {
  return handleApiError(error)
}

/**
 * Get user-friendly error message from ApiException
 */
export function getUserFriendlyErrorMessage(error: ApiException): string {
  // Map error codes to user-friendly messages
  const errorMessages: Record<string, string> = {
    WORKFLOW_NOT_FOUND: "Workflow not found",
    WORKFLOW_INVALID_DEFINITION: "Invalid workflow configuration",
    WORKFLOW_ALREADY_EXISTS: "A workflow with this name already exists",
    TEMPLATE_NOT_FOUND: "Template not found",
    TEMPLATE_INVALID_VARIABLES: "Template contains invalid variables",
    CHANNEL_NOT_FOUND: "Channel not found",
    CHANNEL_CONNECTION_FAILED: "Failed to connect to channel",
    TRIGGER_NOT_FOUND: "Trigger not found",
    VALIDATION_ERROR: "Please check your input and try again",
    RATE_LIMIT_EXCEEDED: "Too many requests. Please try again later",
    INTERNAL_SERVER_ERROR: "An unexpected error occurred. Please try again",
    SERVICE_UNAVAILABLE: "Service temporarily unavailable. Please try again later",
    NETWORK_ERROR: "Network error. Please check your connection",
    TIMEOUT: "Request timeout. Please try again",
  }

  // Return mapped message or fallback to error message
  return errorMessages[error.code] || error.message || "An error occurred"
}

/**
 * Check if error is retryable
 */
export function isRetryableError(error: ApiException): boolean {
  const retryableStatusCodes = [500, 502, 503, 504]
  const retryableErrorCodes = [
    "INTERNAL_SERVER_ERROR",
    "SERVICE_UNAVAILABLE",
    "TIMEOUT",
    "NETWORK_ERROR",
  ]

  const status = error.statusCode || error.status || 0
  return (
    retryableStatusCodes.includes(status) ||
    retryableErrorCodes.includes(error.code)
  )
}

/**
 * Log error for debugging
 */
export function logError(error: ApiException, context?: string): void {
  const errorLog = {
    timestamp: new Date().toISOString(),
    context,
    code: error.code,
    message: error.message,
    status: error.statusCode || error.status,
    details: error.details,
    requestId: error.requestId,
    stack: error.stack,
  }

  console.error("Error logged:", errorLog)

  // In production, send to error reporting service
  // Example: sendToErrorReportingService(errorLog)
}
