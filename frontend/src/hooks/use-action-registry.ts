import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { actionRegistryService, type ActionRegistryItem } from '@/services/action-registry-service'
import type { ApiException } from '@/utils/error-handler'
import { getCachedData, setCachedData } from '@/utils/local-storage-cache'
import { useToast } from '@/hooks/use-toast'

export function useActionRegistry() {
  return useQuery({
    queryKey: ['action-registry'],
    queryFn: async () => {
      // Always fetch from API to get latest data
      // Cache is only used as fallback if API fails
      try {
        const data = await actionRegistryService.getAll()
        // Update cache with fresh data
        setCachedData('action-registry', data)
        return data
      } catch (error) {
        // Fallback to cache if API fails
        const cached = getCachedData('action-registry', 10 * 60 * 1000)
        if (cached) {
          return cached
        }
        throw error
      }
    },
    staleTime: 0, // Always consider data stale, refetch when needed
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
  // Debug logging in development
  if (import.meta.env.DEV && id) {
    console.log('[useActionRegistryById] Calling action API:', {
      id,
      url: `/actions/registry/${id}`,
      enabled: !!id,
    })
  }
  
  return useQuery({
    queryKey: ['action-registry', id],
    queryFn: () => {
      if (import.meta.env.DEV) {
        console.log('[useActionRegistryById] Executing query for:', id)
      }
      return actionRegistryService.getById(id!)
    },
    enabled: !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes - registry data changes infrequently
    gcTime: 30 * 60 * 1000, // 30 minutes
  })
}

export function useCreateActionRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (data: ActionRegistryItem) => actionRegistryService.create(data),
    onSuccess: async () => {
      // Clear localStorage cache first
      setCachedData('action-registry', null)
      // Invalidate and refetch immediately
      await queryClient.invalidateQueries({ queryKey: ['action-registry'] })
      await queryClient.refetchQueries({ queryKey: ['action-registry'] })
      // Also invalidate related queries
      await queryClient.invalidateQueries({ queryKey: ['action-registry', 'type'] })
      await queryClient.invalidateQueries({ queryKey: ['action-registry', 'custom'] })
      toast({
        title: "Success",
        description: "Action definition created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to create action definition",
      })
    },
  })
}

export function useUpdateActionRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<ActionRegistryItem> }) =>
      actionRegistryService.update(id, data),
    onSuccess: async (_, variables) => {
      // Clear localStorage cache first
      setCachedData('action-registry', null)
      // Invalidate and refetch immediately
      await queryClient.invalidateQueries({ queryKey: ['action-registry'] })
      await queryClient.refetchQueries({ queryKey: ['action-registry'] })
      await queryClient.invalidateQueries({ queryKey: ['action-registry', variables.id] })
      // Also invalidate related queries
      await queryClient.invalidateQueries({ queryKey: ['action-registry', 'type'] })
      await queryClient.invalidateQueries({ queryKey: ['action-registry', 'custom'] })
      toast({
        title: "Success",
        description: "Action definition updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to update action definition",
      })
    },
  })
}

export function useDeleteActionRegistry() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => actionRegistryService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['action-registry'] })
      // Clear cache
      setCachedData('action-registry', null)
      toast({
        title: "Success",
        description: "Action definition deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Error",
        description: error.message || "Failed to delete action definition",
      })
    },
  })
}

