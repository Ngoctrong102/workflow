import { useState, useMemo } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { useForm, FormProvider, Controller } from "react-hook-form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ArrowLeft, Loader2, Save, Eye, Play } from "lucide-react"
import { useWorkflow } from "@/hooks/use-workflows"
import {
  useWorkflowReportConfig,
  useCreateWorkflowReportConfig,
  useUpdateWorkflowReportConfig,
  useGenerateReportPreview,
  useValidateQuery,
  useGenerateReport,
} from "@/hooks/use-workflow-report"
import { AnalystQueryEditor } from "@/components/report/AnalystQueryEditor"
import { CronExpressionInput } from "@/components/report/CronExpressionInput"
import { PeriodTypeSelector } from "@/components/report/PeriodTypeSelector"
import { RecipientsInput } from "@/components/report/RecipientsInput"
import { ReportPreview } from "@/components/report/ReportPreview"
import type { CreateWorkflowReportRequest, PeriodType } from "@/types/workflow-report"

export default function WorkflowReportConfig() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState("config")
  const [previewPeriodStart, setPreviewPeriodStart] = useState<string>()
  const [previewPeriodEnd, setPreviewPeriodEnd] = useState<string>()

  const { data: workflow } = useWorkflow(id)
  const { data: existingConfig, isLoading: isLoadingConfig } = useWorkflowReportConfig(id)
  const createConfig = useCreateWorkflowReportConfig()
  const updateConfig = useUpdateWorkflowReportConfig()
  const generatePreview = useGenerateReportPreview()
  const validateQuery = useValidateQuery()
  const generateReport = useGenerateReport()

  const methods = useForm<CreateWorkflowReportRequest & { period_start_date?: string; period_end_date?: string }>({
    defaultValues: existingConfig
      ? {
          name: existingConfig.name,
          analyst_query: existingConfig.analyst_query,
          period_type: existingConfig.period_type,
          period_start_date: existingConfig.period_start_date || undefined,
          period_end_date: existingConfig.period_end_date || undefined,
          schedule_cron: existingConfig.schedule_cron,
          recipients: existingConfig.recipients,
          format: existingConfig.format,
          timezone: existingConfig.timezone,
          status: existingConfig.status,
        }
      : {
          name: `${workflow?.name || "Workflow"} Report`,
          analyst_query: `SELECT COUNT(*) as total_executions, AVG(duration) as avg_duration FROM executions WHERE workflow_id = :workflow_id AND started_at BETWEEN :start_date AND :end_date`,
          period_type: "last_7d",
          period_start_date: undefined,
          period_end_date: undefined,
          schedule_cron: "0 9 * * *",
          recipients: [],
          format: "csv",
          timezone: "UTC",
          status: "active",
        },
  })

  const {
    register,
    handleSubmit,
    watch,
    control,
    formState: { errors },
  } = methods

  const periodType = watch("period_type")
  const analystQuery = watch("analyst_query")

  const onSubmit = async (data: CreateWorkflowReportRequest & { period_start_date?: string; period_end_date?: string }) => {
    if (!id) return

    const config: CreateWorkflowReportRequest = {
      name: data.name,
      analyst_query: data.analyst_query,
      period_type: data.period_type,
      period_start_date: data.period_type === "custom" ? (data.period_start_date || null) : null,
      period_end_date: data.period_type === "custom" ? (data.period_end_date || null) : null,
      schedule_cron: data.schedule_cron,
      recipients: data.recipients,
      format: data.format,
      timezone: data.timezone,
      status: data.status || "active",
    }

    if (existingConfig) {
      await updateConfig.mutateAsync({
        workflowId: id,
        config,
      })
    } else {
      await createConfig.mutateAsync({
        workflowId: id,
        config,
      })
    }
    navigate(`/workflows/${id}/dashboard`)
  }

  const handleValidateQuery = async (query: string) => {
    if (!id) return { valid: false, error: "Workflow ID is required" }
    return await validateQuery.mutateAsync({
      workflowId: id,
      analystQuery: query,
    })
  }

  const handleGeneratePreview = async () => {
    if (!id) return

    const periodStart = previewPeriodStart || new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
    const periodEnd = previewPeriodEnd || new Date().toISOString()

    await generatePreview.mutateAsync({
      workflowId: id,
      params: {
        analyst_query: analystQuery,
        period_start: periodStart,
        period_end: periodEnd,
      },
    })
  }

  const handleGenerateReport = async () => {
    if (!id) return

    const periodStart = previewPeriodStart || undefined
    const periodEnd = previewPeriodEnd || undefined

    await generateReport.mutateAsync({
      workflowId: id,
      params: periodStart && periodEnd ? { period_start: periodStart, period_end: periodEnd } : undefined,
    })
  }

  if (isLoadingConfig) {
    return (
      <div className="container mx-auto p-6">
        <div className="space-y-4">
          <div className="h-10 bg-secondary-200 rounded animate-pulse" />
          <div className="h-64 bg-secondary-200 rounded animate-pulse" />
        </div>
      </div>
    )
  }

  return (
    <FormProvider {...methods}>
      <div className="container mx-auto p-6 space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" onClick={() => navigate(`/workflows/${id}/dashboard`)}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Dashboard
            </Button>
            <div>
              <h1 className="text-3xl font-bold">
                {existingConfig ? "Edit Report Configuration" : "Configure Workflow Report"}
              </h1>
              <p className="text-sm text-secondary-500 mt-1">
                {workflow?.name || "Workflow"} - Automated report configuration
              </p>
            </div>
          </div>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList>
            <TabsTrigger value="config">Configuration</TabsTrigger>
            <TabsTrigger value="preview">Preview</TabsTrigger>
          </TabsList>

          <TabsContent value="config" className="space-y-6">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Basic Information */}
              <Card>
                <CardHeader>
                  <CardTitle>Basic Information</CardTitle>
                  <CardDescription>Configure the report name and format</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Report Name *</Label>
                    <Input
                      id="name"
                      {...register("name", {
                        required: "Report name is required",
                        maxLength: { value: 100, message: "Name must be less than 100 characters" },
                      })}
                      placeholder="Daily Workflow Report"
                    />
                    {errors.name && (
                      <p className="text-sm text-error-600">{errors.name.message as string}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="format">Report Format *</Label>
                    <Controller
                      name="format"
                      control={control}
                      rules={{ required: "Format is required" }}
                      render={({ field }) => (
                        <Select value={field.value} onValueChange={field.onChange}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select format" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="csv">CSV</SelectItem>
                            <SelectItem value="excel">Excel (XLSX)</SelectItem>
                            <SelectItem value="json">JSON</SelectItem>
                          </SelectContent>
                        </Select>
                      )}
                    />
                    {errors.format && (
                      <p className="text-sm text-error-600">{errors.format.message as string}</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Analyst Query */}
              <Card>
                <CardHeader>
                  <CardTitle>Analyst Query</CardTitle>
                  <CardDescription>SQL query to analyze execution data</CardDescription>
                </CardHeader>
                <CardContent>
                  <AnalystQueryEditor
                    value={analystQuery || ""}
                    onChange={(value) => methods.setValue("analyst_query", value)}
                    onValidate={handleValidateQuery}
                    error={errors.analyst_query?.message as string}
                  />
                </CardContent>
              </Card>

              {/* Period Configuration */}
              <Card>
                <CardHeader>
                  <CardTitle>Period Configuration</CardTitle>
                  <CardDescription>Configure the time period for report generation</CardDescription>
                </CardHeader>
                <CardContent>
                  <PeriodTypeSelector
                    control={control}
                    periodType={periodType as PeriodType}
                    periodStartDate={watch("period_start_date")}
                    periodEndDate={watch("period_end_date")}
                    error={errors.period_type?.message as string}
                  />
                </CardContent>
              </Card>

              {/* Schedule */}
              <Card>
                <CardHeader>
                  <CardTitle>Schedule</CardTitle>
                  <CardDescription>Configure when the report should be generated</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <CronExpressionInput
                    value={watch("schedule_cron") || ""}
                    onChange={(value) => methods.setValue("schedule_cron", value)}
                    error={errors.schedule_cron?.message as string}
                  />
                </CardContent>
              </Card>

              {/* Recipients */}
              <Card>
                <CardHeader>
                  <CardTitle>Recipients</CardTitle>
                  <CardDescription>Email addresses to receive the report</CardDescription>
                </CardHeader>
                <CardContent>
                  <RecipientsInput
                    value={watch("recipients") || []}
                    onChange={(recipients) => methods.setValue("recipients", recipients)}
                    error={errors.recipients?.message as string}
                  />
                </CardContent>
              </Card>

              {/* Timezone */}
              <Card>
                <CardHeader>
                  <CardTitle>Timezone</CardTitle>
                  <CardDescription>Timezone for schedule and date calculations</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <Label htmlFor="timezone">Timezone *</Label>
                    <Controller
                      name="timezone"
                      control={control}
                      rules={{ required: "Timezone is required" }}
                      render={({ field }) => (
                        <Select value={field.value} onValueChange={field.onChange}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select timezone" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="UTC">UTC</SelectItem>
                            <SelectItem value="America/New_York">America/New_York</SelectItem>
                            <SelectItem value="America/Los_Angeles">America/Los_Angeles</SelectItem>
                            <SelectItem value="Europe/London">Europe/London</SelectItem>
                            <SelectItem value="Europe/Paris">Europe/Paris</SelectItem>
                            <SelectItem value="Asia/Tokyo">Asia/Tokyo</SelectItem>
                            <SelectItem value="Asia/Shanghai">Asia/Shanghai</SelectItem>
                            <SelectItem value="Asia/Ho_Chi_Minh">Asia/Ho_Chi_Minh</SelectItem>
                          </SelectContent>
                        </Select>
                      )}
                    />
                    {errors.timezone && (
                      <p className="text-sm text-error-600">{errors.timezone.message as string}</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Actions */}
              <div className="flex items-center justify-end space-x-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate(`/workflows/${id}/dashboard`)}
                >
                  Cancel
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setActiveTab("preview")}
                >
                  <Eye className="h-4 w-4 mr-2" />
                  Preview
                </Button>
                <Button type="submit" disabled={createConfig.isPending || updateConfig.isPending}>
                  {(createConfig.isPending || updateConfig.isPending) ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <Save className="h-4 w-4 mr-2" />
                      {existingConfig ? "Update Configuration" : "Create Configuration"}
                    </>
                  )}
                </Button>
              </div>
            </form>
          </TabsContent>

          <TabsContent value="preview" className="space-y-6">
            {/* Preview Period Selection */}
            <Card>
              <CardHeader>
                <CardTitle>Preview Period</CardTitle>
                <CardDescription>Select date range for preview</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="preview-start">Start Date</Label>
                    <Input
                      id="preview-start"
                      type="datetime-local"
                      value={previewPeriodStart ? new Date(previewPeriodStart).toISOString().slice(0, 16) : ""}
                      onChange={(e) => setPreviewPeriodStart(e.target.value ? new Date(e.target.value).toISOString() : undefined)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="preview-end">End Date</Label>
                    <Input
                      id="preview-end"
                      type="datetime-local"
                      value={previewPeriodEnd ? new Date(previewPeriodEnd).toISOString().slice(0, 16) : ""}
                      onChange={(e) => setPreviewPeriodEnd(e.target.value ? new Date(e.target.value).toISOString() : undefined)}
                    />
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    onClick={handleGeneratePreview}
                    disabled={generatePreview.isPending}
                  >
                    {generatePreview.isPending ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Generating...
                      </>
                    ) : (
                      <>
                        <Eye className="h-4 w-4 mr-2" />
                        Generate Preview
                      </>
                    )}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleGenerateReport}
                    disabled={generateReport.isPending}
                  >
                    {generateReport.isPending ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Generating...
                      </>
                    ) : (
                      <>
                        <Play className="h-4 w-4 mr-2" />
                        Generate Report
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>

            <ReportPreview
              preview={generatePreview.data}
              isLoading={generatePreview.isPending}
              onGenerate={handleGeneratePreview}
            />
            <div className="flex items-center justify-end space-x-2">
              <Button variant="outline" onClick={() => setActiveTab("config")}>
                Back to Configuration
              </Button>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </FormProvider>
  )
}
