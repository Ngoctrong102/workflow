import { useQuery } from '@tanstack/react-query'
import { triggerService } from '@/services/trigger-service'
import type { ApiException } from '@/utils/error-handler'
import { getCachedData, setCachedData } from '@/utils/local-storage-cache'

export function useTriggerRegistry() {
  return useQuery({
    queryKey: ['trigger-registry'],
    queryFn: async () => {
      // Check localStorage cache first
      const cached = getCachedData('trigger-registry', 10 * 60 * 1000) // 10 minutes
      if (cached) {
        return cached
      }
      
      // Fetch from API
      const data = await triggerService.getRegistry()
      
      // Cache the result
      setCachedData('trigger-registry', data)
      
      return data
    },
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useTriggerRegistryByType(type: string) {
  return useQuery({
    queryKey: ['trigger-registry', 'type', type],
    queryFn: () => triggerService.getRegistryByType(type),
    enabled: !!type,
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

