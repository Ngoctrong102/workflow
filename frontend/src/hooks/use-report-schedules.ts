import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import {
  reportService,
  type ListReportSchedulesParams,
  type ListReportDataParams,
  type CreateReportScheduleRequest,
  type UpdateReportScheduleRequest,
} from "@/services/report-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useReportSchedules(params?: ListReportSchedulesParams) {
  return useQuery({
    queryKey: ["report-schedules", params],
    queryFn: () => reportService.listSchedules(params),
  })
}

export function useReportSchedule(id: string) {
  return useQuery({
    queryKey: ["report-schedule", id],
    queryFn: () => reportService.getSchedule(id),
    enabled: !!id,
  })
}

export function useCreateReportSchedule() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (schedule: CreateReportScheduleRequest) => reportService.createSchedule(schedule),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["report-schedules"] })
      toast({
        title: "Schedule Created",
        description: "Report schedule has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Schedule",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateReportSchedule() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, schedule }: { id: string; schedule: Omit<UpdateReportScheduleRequest, "id"> }) =>
      reportService.updateSchedule(id, schedule),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["report-schedules"] })
      queryClient.invalidateQueries({ queryKey: ["report-schedule", variables.id] })
      toast({
        title: "Schedule Updated",
        description: "Report schedule has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Schedule",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeleteReportSchedule() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => reportService.deleteSchedule(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["report-schedules"] })
      toast({
        title: "Schedule Deleted",
        description: "Report schedule has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Schedule",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useToggleReportSchedule() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      reportService.toggleSchedule(id, enabled),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["report-schedules"] })
      queryClient.invalidateQueries({ queryKey: ["report-schedule", variables.id] })
      toast({
        title: variables.enabled ? "Schedule Enabled" : "Schedule Disabled",
        description: `Report schedule has been ${variables.enabled ? "enabled" : "disabled"}`,
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Toggle Schedule",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useReportHistory(params?: ListReportDataParams) {
  return useQuery({
    queryKey: ["report-history", params],
    queryFn: () => reportService.listReportData(params),
  })
}
