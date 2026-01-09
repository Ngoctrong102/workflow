import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useToast } from "@/hooks/use-toast"
import {
  workflowReportService,
  type CreateWorkflowReportRequest,
  type ListWorkflowReportsParams,
} from "@/services/workflow-report-service"
import { getUserFriendlyErrorMessage, type ApiException } from "@/utils/error-handler"

export function useWorkflowReportConfig(workflowId: string | undefined) {
  return useQuery({
    queryKey: ["workflow-report-config", workflowId],
    queryFn: () => workflowReportService.getConfig(workflowId!),
    enabled: !!workflowId,
  })
}

export function useCreateWorkflowReportConfig() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      config,
    }: {
      workflowId: string
      config: CreateWorkflowReportRequest
    }) => workflowReportService.createConfig(workflowId, config),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["workflow-report-config", variables.workflowId] })
      queryClient.invalidateQueries({ queryKey: ["workflow-reports", variables.workflowId] })
      toast({
        title: "Report Configuration Created",
        description: "Workflow report configuration has been created successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Create Configuration",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateWorkflowReportConfig() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      config,
    }: {
      workflowId: string
      config: Omit<CreateWorkflowReportRequest, "workflowId">
    }) => workflowReportService.updateConfig(workflowId, config),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["workflow-report-config", variables.workflowId] })
      toast({
        title: "Report Configuration Updated",
        description: "Workflow report configuration has been updated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Configuration",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeleteWorkflowReportConfig() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (workflowId: string) => workflowReportService.deleteConfig(workflowId),
    onSuccess: (_, workflowId) => {
      queryClient.invalidateQueries({ queryKey: ["workflow-report-config", workflowId] })
      queryClient.invalidateQueries({ queryKey: ["workflow-reports", workflowId] })
      toast({
        title: "Report Configuration Deleted",
        description: "Workflow report configuration has been deleted successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Delete Configuration",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateWorkflowReportStatus() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      status,
    }: {
      workflowId: string
      status: "active" | "inactive" | "paused"
    }) => workflowReportService.updateStatus(workflowId, status),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["workflow-report-config", variables.workflowId] })
      toast({
        title: "Report Status Updated",
        description: `Report has been ${variables.status === "active" ? "activated" : variables.status === "paused" ? "paused" : "deactivated"}`,
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Update Status",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useValidateQuery() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      analystQuery,
    }: {
      workflowId: string
      analystQuery: string
    }) => workflowReportService.validateQuery(workflowId, analystQuery),
    onSuccess: (data) => {
      if (data.valid) {
        toast({
          title: "Query Valid",
          description: "The analyst query is valid and ready to use",
        })
      } else {
        toast({
          variant: "destructive",
          title: "Query Invalid",
          description: data.error || "The analyst query has syntax errors",
        })
      }
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Validate Query",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useGenerateReportPreview() {
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      params,
    }: {
      workflowId: string
      params?: {
        analyst_query?: string
        period_start?: string
        period_end?: string
      }
    }) => workflowReportService.generatePreview(workflowId, params),
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Generate Preview",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useGenerateReport() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({
      workflowId,
      params,
    }: {
      workflowId: string
      params?: {
        period_start?: string
        period_end?: string
      }
    }) => workflowReportService.generateReport(workflowId, params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["workflow-reports", variables.workflowId] })
      toast({
        title: "Report Generated",
        description: "Workflow report has been generated successfully",
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: "destructive",
        title: "Failed to Generate Report",
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useWorkflowReports(params: ListWorkflowReportsParams) {
  return useQuery({
    queryKey: ["workflow-reports", params],
    queryFn: () => workflowReportService.listReports(params),
    enabled: !!params.workflowId,
  })
}

