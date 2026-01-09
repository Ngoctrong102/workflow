import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { handleApiError, logError } from '@/utils/error-handler'

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
    },
    mutations: {
      retry: false,
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

