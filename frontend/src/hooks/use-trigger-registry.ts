import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { triggerService, type TriggerRegistryItem } from '@/services/trigger-service'
import type { ApiException } from '@/utils/error-handler'
import { getCachedData, setCachedData } from '@/utils/local-storage-cache'
import { useToast } from '@/hooks/use-toast'

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

export function useTriggerRegistryById(id: string | undefined) {
  // Debug logging in development
  if (import.meta.env.DEV && id) {
    console.log('[useTriggerRegistryById] Calling trigger API:', {
      id,
      url: `/triggers/registry/${id}`,
      enabled: !!id,
    })
  }
  
  return useQuery({
    queryKey: ['trigger-registry', id],
    queryFn: () => {
      if (import.meta.env.DEV) {
        console.log('[useTriggerRegistryById] Executing query for:', id)
      }
      return triggerService.getRegistryById(id!)
    },
    enabled: !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useCreateTriggerRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (data: TriggerRegistryItem) => triggerService.createRegistryDefinition(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trigger-registry'] })
      // Clear cache
      setCachedData('trigger-registry', null)
      toast({
        title: "Success",
        description: "Trigger definition created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to create trigger definition",
      })
    },
  })
}

export function useUpdateTriggerRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<TriggerRegistryItem> }) =>
      triggerService.updateRegistryDefinition(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['trigger-registry'] })
      queryClient.invalidateQueries({ queryKey: ['trigger-registry', variables.id] })
      // Clear cache
      setCachedData('trigger-registry', null)
      toast({
        title: "Success",
        description: "Trigger definition updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to update trigger definition",
      })
    },
  })
}

export function useDeleteTriggerRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => triggerService.deleteRegistryDefinition(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trigger-registry'] })
      // Clear cache
      setCachedData('trigger-registry', null)
      toast({
        title: "Success",
        description: "Trigger definition deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to delete trigger definition",
      })
    },
  })
}

