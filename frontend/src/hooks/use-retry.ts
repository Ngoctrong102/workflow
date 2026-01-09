import { useState, useCallback } from "react"
import { isRetryableError, type ApiException } from "@/utils/error-handler"

interface UseRetryOptions {
  maxRetries?: number
  retryDelay?: number
  onRetry?: () => void | Promise<void>
}

export function useRetry(options: UseRetryOptions = {}) {
  const { maxRetries = 3, retryDelay = 1000, onRetry } = options
  const [retryCount, setRetryCount] = useState(0)
  const [isRetrying, setIsRetrying] = useState(false)

  const retry = useCallback(
    async (error: ApiException, operation: () => Promise<unknown>) => {
      if (!isRetryableError(error)) {
        return false
      }

      if (retryCount >= maxRetries) {
        setRetryCount(0)
        return false
      }

      setIsRetrying(true)
      setRetryCount((prev) => prev + 1)

      try {
        // Wait before retrying
        await new Promise((resolve) => setTimeout(resolve, retryDelay * retryCount))

        // Call custom retry handler if provided
        if (onRetry) {
          await onRetry()
        }

        // Execute the operation again
        await operation()

        // Reset on success
        setRetryCount(0)
        setIsRetrying(false)
        return true
      } catch (retryError) {
        setIsRetrying(false)
        // If this was the last retry, reset count
        if (retryCount >= maxRetries - 1) {
          setRetryCount(0)
        }
        throw retryError
      }
    },
    [retryCount, maxRetries, retryDelay, onRetry]
  )

  const reset = useCallback(() => {
    setRetryCount(0)
    setIsRetrying(false)
  }, [])

  return {
    retry,
    retryCount,
    isRetrying,
    reset,
    canRetry: (error: ApiException) => isRetryableError(error) && retryCount < maxRetries,
  }
}

