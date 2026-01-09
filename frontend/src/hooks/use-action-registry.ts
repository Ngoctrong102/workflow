import { useQuery } from '@tanstack/react-query'
import { actionRegistryService } from '@/services/action-registry-service'
import type { ApiException } from '@/utils/error-handler'
import { getCachedData, setCachedData } from '@/utils/local-storage-cache'

export function useActionRegistry() {
  return useQuery({
    queryKey: ['action-registry'],
    queryFn: async () => {
      // Check localStorage cache first
      const cached = getCachedData('action-registry', 10 * 60 * 1000) // 10 minutes
      if (cached) {
        return cached
      }
      
      // Fetch from API
      const data = await actionRegistryService.getAll()
      
      // Cache the result
      setCachedData('action-registry', data)
      
      return data
    },
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useActionRegistryByType(type?: string) {
  return useQuery({
    queryKey: ['action-registry', 'type', type],
    queryFn: () => actionRegistryService.getByType({ type }),
    enabled: !!type,
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useCustomActions() {
  return useQuery({
    queryKey: ['action-registry', 'custom'],
    queryFn: () => actionRegistryService.getCustom(),
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useActionRegistryById(id: string | undefined) {
  return useQuery({
    queryKey: ['action-registry', id],
    queryFn: () => actionRegistryService.getById(id!),
    enabled: !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

