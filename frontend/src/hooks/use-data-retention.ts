import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import { reportService, type DataRetentionSettings } from "@/services/report-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useDataRetentionSettings() {
  return useQuery({
    queryKey: ["data-retention-settings"],
    queryFn: () => reportService.getDataRetentionSettings(),
  })
}

export function useUpdateDataRetentionSettings() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (settings: Partial<DataRetentionSettings>) =>
      reportService.updateDataRetentionSettings(settings),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["data-retention-settings"] })
      toast({
        title: "Settings Updated",
        description: "Data retention settings have been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Settings",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useTriggerDataCleanup() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: () => reportService.triggerCleanup(),
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["data-retention-settings"] })
      toast({
        title: "Cleanup Completed",
        description: `${result.deleted} records deleted successfully`,
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Cleanup Failed",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

