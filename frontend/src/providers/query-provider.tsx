import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { handleApiError, getUserFriendlyErrorMessage, logError, isRetryableError, type ApiException } from '@/utils/error-handler'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors
        if (error && typeof error === 'object' && 'statusCode' in error) {
          const statusCode = error.statusCode as number
          if (statusCode >= 400 && statusCode < 500) {
            return false
          }
        }
        // Retry up to 3 times for other errors
        return failureCount < 3
      },
      // Optimized caching: longer stale time for static data, shorter for dynamic data
      staleTime: 5 * 60 * 1000, // 5 minutes default
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      refetchOnMount: 'always', // Always refetch on mount for fresh data
      refetchOnReconnect: true,
      refetchInterval: false, // Disable automatic refetching
      onError: (error) => {
        // Global error handling for queries
        const apiError = handleApiError(error)
        logError(apiError, 'React Query Query Error')
        
        // Only log to console, don't show toast here
        // Individual hooks should handle toast notifications
        if (import.meta.env.DEV) {
          console.error('[Query Error]', {
            code: apiError.code,
            message: apiError.message,
            statusCode: apiError.statusCode,
          })
        }
      },
    },
    mutations: {
      retry: false,
      onError: (error) => {
        // Global error handling for mutations
        const apiError = handleApiError(error)
        logError(apiError, 'React Query Mutation Error')
        
        // Only log to console, don't show toast here
        // Individual hooks should handle toast notifications
        if (import.meta.env.DEV) {
          console.error('[Mutation Error]', {
            code: apiError.code,
            message: apiError.message,
            statusCode: apiError.statusCode,
          })
        }
      },
    },
  },
})

interface QueryProviderProps {
  children: ReactNode
}

export function QueryProvider({ children }: QueryProviderProps) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

