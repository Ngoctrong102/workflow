import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { UseQueryOptions, UseMutationOptions } from '@tanstack/react-query'
import { api } from '@/services/api-client'
import type { ApiException } from '@/utils/error-handler'

// Generic API query hook
export function useApiQuery<TData = unknown, TError = ApiException>(
  queryKey: readonly unknown[],
  queryFn: () => Promise<TData>,
  options?: Omit<UseQueryOptions<TData, TError>, 'queryKey' | 'queryFn'>
) {
  return useQuery<TData, TError>({
    queryKey,
    queryFn,
    ...options,
  })
}

// Generic API mutation hook  
export function useApiMutation<TData = unknown, TError = ApiException, TVariables = unknown>(
  options: UseMutationOptions<TData, TError, TVariables>
) {
  return useMutation<TData, TError, TVariables>(options)
}

// Helper to invalidate queries
export function useInvalidateQueries() {
  const queryClient = useQueryClient()

  return {
    invalidate: (queryKey: string[]) => {
      void queryClient.invalidateQueries({ queryKey })
    },
    invalidateAll: () => {
      void queryClient.invalidateQueries()
    },
  }
}

