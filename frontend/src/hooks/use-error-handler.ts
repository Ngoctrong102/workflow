import { useCallback } from "react"
import { useToast } from "@/hooks/use-toast"
import { 
  handleApiError, 
  getUserFriendlyErrorMessage, 
  isRetryableError,
  logError,
  type ApiException 
} from "@/utils/error-handler"

interface UseErrorHandlerOptions {
  showToast?: boolean
  logError?: boolean
  context?: string
  onError?: (error: ApiException) => void
}

export function useErrorHandler(options: UseErrorHandlerOptions = {}) {
  const { toast } = useToast()
  const {
    showToast = true,
    logError: shouldLogError = true,
    context,
    onError,
  } = options

  const handleError = useCallback(
    (error: unknown, customContext?: string) => {
      const apiError = handleApiError(error)
      const errorContext = customContext || context

      // Log error if enabled
      if (shouldLogError) {
        logError(apiError, errorContext)
      }

      // Show toast notification if enabled
      if (showToast) {
        const message = getUserFriendlyErrorMessage(apiError)
        const isRetryable = isRetryableError(apiError)

        toast({
          variant: "destructive",
          title: "Error",
          description: message,
          action: isRetryable
            ? {
                altText: "Retry",
                onClick: () => {
                  // Retry logic can be passed via onError callback
                  if (onError) {
                    onError(apiError)
                  }
                },
              }
            : undefined,
        })
      }

      // Call custom error handler if provided
      if (onError) {
        onError(apiError)
      }

      return apiError
    },
    [toast, showToast, shouldLogError, context, onError]
  )

  return { handleError }
}

