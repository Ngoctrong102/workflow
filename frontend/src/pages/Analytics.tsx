import { useState, lazy, Suspense, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { AnalyticsFilterPanel } from "@/components/analytics/AnalyticsFilterPanel"
import { AnalyticsTable, type Column } from "@/components/analytics/AnalyticsTable"
import { AdvancedAnalyticsPanel } from "@/components/analytics/AdvancedAnalyticsPanel"
import { ReportScheduleForm } from "@/components/report/ReportScheduleForm"
import { ScheduledReportsList } from "@/components/report/ScheduledReportsList"
import { DataRetentionPanel } from "@/components/report/DataRetentionPanel"
import { ReportHistory } from "@/components/report/ReportHistory"
import { useDeliveryAnalytics, useErrorAnalytics } from "@/hooks/use-analytics"
import { useCreateReportSchedule, useUpdateReportSchedule } from "@/hooks/use-report-schedules"
import { exportToCSV } from "@/utils/export"
import { format, subDays } from "date-fns"
import { Download, TrendingUp, TrendingDown, Send, CheckCircle2, XCircle, AlertTriangle, Activity, Calendar, Settings, FileText } from "lucide-react"
import type { DeliveryAnalytics, ErrorAnalytics } from "@/types/analytics"
import type { ReportSchedule, CreateReportScheduleRequest } from "@/types/report"
import { cn } from "@/lib/utils"

// Lazy load heavy chart components for code splitting
const DeliveryMetricsChart = lazy(() => import("@/components/dashboard/DeliveryMetricsChart").then(m => ({ default: m.DeliveryMetricsChart })))
const ChannelDistributionChart = lazy(() => import("@/components/dashboard/ChannelDistributionChart").then(m => ({ default: m.ChannelDistributionChart })))

interface MetricCardProps {
  label: string
  value: string | number
  trend?: number
  icon: React.ReactNode
  variant?: "default" | "success" | "error" | "warning"
  isLoading?: boolean
}

function MetricCard({ label, value, trend, icon, variant = "default", isLoading }: MetricCardProps) {
  const variantStyles = {
    default: "bg-white border-secondary-200",
    success: "bg-success-50 border-success-200",
    error: "bg-error-50 border-error-200",
    warning: "bg-warning-50 border-warning-200",
  }

  const valueStyles = {
    default: "text-slate-900",
    success: "text-success-700",
    error: "text-error-700",
    warning: "text-warning-700",
  }

  if (isLoading) {
    return (
      <Card className={cn("border transition-all duration-200 hover:shadow-md cursor-pointer", variantStyles[variant])}>
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            <div className="space-y-2 flex-1">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-8 w-32" />
            </div>
            <Skeleton className="h-10 w-10 rounded-lg" />
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={cn("border transition-all duration-200 hover:shadow-md cursor-pointer", variantStyles[variant])}>
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1 flex-1">
            <p className="text-sm font-medium text-slate-600">{label}</p>
            <div className="flex items-baseline gap-2">
              <p className={cn("text-2xl font-bold", valueStyles[variant])}>
                {typeof value === "number" ? value.toLocaleString() : value}
              </p>
              {trend !== undefined && (
                <div className={cn("flex items-center gap-1 text-xs font-medium", trend >= 0 ? "text-success-600" : "text-error-600")}>
                  {trend >= 0 ? (
                    <TrendingUp className="h-3 w-3" />
                  ) : (
                    <TrendingDown className="h-3 w-3" />
                  )}
                  <span>{Math.abs(trend).toFixed(1)}%</span>
                </div>
              )}
            </div>
          </div>
          <div className={cn("p-2 rounded-lg", variant === "default" ? "bg-slate-100" : variant === "success" ? "bg-success-100" : variant === "error" ? "bg-error-100" : "bg-warning-100")}>
            {icon}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

export default function Analytics() {
  const [activeTab, setActiveTab] = useState("analytics")
  const [editingSchedule, setEditingSchedule] = useState<ReportSchedule | null>(null)
  const [dateRange, setDateRange] = useState({
    start: format(subDays(new Date(), 7), "yyyy-MM-dd"),
    end: format(new Date(), "yyyy-MM-dd"),
  })
  const [selectedWorkflow, setSelectedWorkflow] = useState<string | undefined>()
  const [selectedChannel, setSelectedChannel] = useState<string | undefined>()
  const [selectedStatus, setSelectedStatus] = useState<string | undefined>()
  const [granularity, setGranularity] = useState<"hourly" | "daily" | "weekly" | "monthly">("daily")
  const [comparisonMode, setComparisonMode] = useState(false)

  const createSchedule = useCreateReportSchedule()
  const updateSchedule = useUpdateReportSchedule()

  const deliveryParams = {
    start: `${dateRange.start}T00:00:00Z`,
    end: `${dateRange.end}T23:59:59Z`,
    granularity,
  }

  const { data: deliveryData, isLoading: isLoadingDelivery } = useDeliveryAnalytics(deliveryParams)
  const { data: errorData, isLoading: isLoadingError } = useErrorAnalytics(deliveryParams)

  const handleDateRangeChange = (field: "start" | "end", value: string) => {
    setDateRange((prev) => ({ ...prev, [field]: value }))
  }

  const handleDateRangePresetChange = (range: { start: string; end: string }) => {
    setDateRange(range)
  }

  const handleScheduleSubmit = async (data: CreateReportScheduleRequest) => {
    if (editingSchedule) {
      await updateSchedule.mutateAsync({
        id: editingSchedule.id,
        schedule: data,
      })
    } else {
      await createSchedule.mutateAsync(data)
    }
    setEditingSchedule(null)
  }

  const handleScheduleEdit = (schedule: ReportSchedule) => {
    setEditingSchedule(schedule)
    setActiveTab("schedules")
  }

  const handleResetFilters = () => {
    setSelectedWorkflow(undefined)
    setSelectedChannel(undefined)
    setSelectedStatus(undefined)
    setGranularity("daily")
    setDateRange({
      start: format(subDays(new Date(), 7), "yyyy-MM-dd"),
      end: format(new Date(), "yyyy-MM-dd"),
    })
  }

  const handleExportDelivery = () => {
    if (!deliveryData) return

    const exportData = deliveryData.byChannel.map((item) => ({
      Channel: item.channel,
      Sent: item.sent,
      Delivered: item.delivered,
      Failed: item.failed,
      "Delivery Rate": `${item.deliveryRate.toFixed(2)}%`,
    }))

    const columns: Column<typeof exportData[0]>[] = [
      { key: "Channel", label: "Channel" },
      { key: "Sent", label: "Sent" },
      { key: "Delivered", label: "Delivered" },
      { key: "Failed", label: "Failed" },
      { key: "Delivery Rate", label: "Delivery Rate" },
    ]

    exportToCSV(exportData, columns, `delivery-analytics-${dateRange.start}-${dateRange.end}.csv`)
  }

  const handleExportErrors = () => {
    if (!errorData) return

    const exportData = [
      ...errorData.byType.map((item) => ({
        Type: "Error Type",
        Category: item.type,
        Count: item.count,
        Percentage: `${item.percentage.toFixed(2)}%`,
      })),
      ...errorData.byChannel.map((item) => ({
        Type: "Channel",
        Category: item.channel,
        Count: item.count,
        Percentage: `${item.percentage.toFixed(2)}%`,
      })),
    ]

    const columns: Column<typeof exportData[0]>[] = [
      { key: "Type", label: "Type" },
      { key: "Category", label: "Category" },
      { key: "Count", label: "Count" },
      { key: "Percentage", label: "Percentage" },
    ]

    exportToCSV(exportData, columns, `error-analytics-${dateRange.start}-${dateRange.end}.csv`)
  }

  // Memoize chart data to prevent unnecessary re-renders
  const deliveryChartData = useMemo(
    () =>
      deliveryData?.byChannel.map((item) => ({
        channel: item.channel,
        sent: item.sent,
        delivered: item.delivered,
        failed: item.failed,
      })) || [],
    [deliveryData?.byChannel]
  )

  const channelDistributionData = useMemo(
    () =>
      deliveryData?.byChannel.map((item) => ({
        name: item.channel,
        value: item.sent,
      })) || [],
    [deliveryData?.byChannel]
  )

  // Table columns for delivery analytics
  const deliveryColumns: Column<DeliveryAnalytics["byChannel"][0]>[] = [
    {
      key: "channel",
      label: "Channel",
      sortable: true,
    },
    {
      key: "sent",
      label: "Sent",
      sortable: true,
      render: (value) => Number(value).toLocaleString(),
    },
    {
      key: "delivered",
      label: "Delivered",
      sortable: true,
      render: (value) => Number(value).toLocaleString(),
    },
    {
      key: "failed",
      label: "Failed",
      sortable: true,
      render: (value) => Number(value).toLocaleString(),
    },
    {
      key: "deliveryRate",
      label: "Delivery Rate",
      sortable: true,
      render: (value) => `${Number(value).toFixed(2)}%`,
    },
  ]

  // Table columns for error analytics
  const errorTypeColumns: Column<ErrorAnalytics["byType"][0]>[] = [
    {
      key: "type",
      label: "Error Type",
      sortable: true,
    },
    {
      key: "count",
      label: "Count",
      sortable: true,
      render: (value) => Number(value).toLocaleString(),
    },
    {
      key: "percentage",
      label: "Percentage",
      sortable: true,
      render: (value) => `${Number(value).toFixed(2)}%`,
    },
  ]

  const errorChannelColumns: Column<ErrorAnalytics["byChannel"][0]>[] = [
    {
      key: "channel",
      label: "Channel",
      sortable: true,
    },
    {
      key: "count",
      label: "Count",
      sortable: true,
      render: (value) => Number(value).toLocaleString(),
    },
    {
      key: "percentage",
      label: "Percentage",
      sortable: true,
      render: (value) => `${Number(value).toFixed(2)}%`,
    },
  ]

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Analytics & Reports</h1>
          <p className="text-slate-600 mt-1.5">View analytics, schedule reports, and manage data retention</p>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="analytics">
            <Activity className="h-4 w-4 mr-2" />
            Analytics
          </TabsTrigger>
          <TabsTrigger value="schedules">
            <Calendar className="h-4 w-4 mr-2" />
            Report Schedules
          </TabsTrigger>
          <TabsTrigger value="report-history">
            <FileText className="h-4 w-4 mr-2" />
            Report History
          </TabsTrigger>
          <TabsTrigger value="data-retention">
            <Settings className="h-4 w-4 mr-2" />
            Data Retention
          </TabsTrigger>
        </TabsList>

        {/* Analytics Tab */}
        <TabsContent value="analytics" className="space-y-6">

          <div className="grid gap-6 lg:grid-cols-4">
            {/* Filter Panel */}
            <div className="lg:col-span-1 space-y-4">
              <AnalyticsFilterPanel
                dateRange={dateRange}
                onDateRangeChange={handleDateRangeChange}
                granularity={granularity}
                onGranularityChange={setGranularity}
                selectedWorkflow={selectedWorkflow}
                onWorkflowChange={setSelectedWorkflow}
                selectedChannel={selectedChannel}
                onChannelChange={setSelectedChannel}
                selectedStatus={selectedStatus}
                onStatusChange={setSelectedStatus}
                onReset={handleResetFilters}
              />
              <AdvancedAnalyticsPanel
                deliveryData={deliveryData?.byChannel}
                errorData={errorData?.byType}
                onDateRangeChange={handleDateRangePresetChange}
                onComparisonToggle={setComparisonMode}
              />
            </div>

            {/* Main Content */}
            <div className="lg:col-span-3 space-y-6">
          {/* Delivery Analytics Summary Metrics */}
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-slate-900">Delivery Analytics</h2>
              <p className="text-sm text-slate-600 mt-1">Notification delivery metrics and performance</p>
            </div>
            
            {isLoadingDelivery ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                {[...Array(4)].map((_, i) => (
                  <MetricCard key={i} label="" value="" icon={<Activity className="h-5 w-5" />} isLoading />
                ))}
              </div>
            ) : deliveryData ? (
              <>
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
                  <MetricCard
                    label="Total Sent"
                    value={deliveryData.totalSent}
                    icon={<Send className="h-5 w-5 text-slate-600" />}
                    variant="default"
                  />
                  <MetricCard
                    label="Delivered"
                    value={deliveryData.delivered}
                    trend={deliveryData.deliveryRate}
                    icon={<CheckCircle2 className="h-5 w-5 text-success-600" />}
                    variant="success"
                  />
                  <MetricCard
                    label="Failed"
                    value={deliveryData.failed}
                    icon={<XCircle className="h-5 w-5 text-error-600" />}
                    variant="error"
                  />
                  <MetricCard
                    label="Delivery Rate"
                    value={`${deliveryData.deliveryRate.toFixed(1)}%`}
                    icon={<Activity className="h-5 w-5 text-primary-600" />}
                    variant="default"
                  />
                </div>

                {/* Charts */}
                {deliveryChartData.length > 0 && (
                  <div className="grid gap-6 md:grid-cols-2 mb-6">
                    <Suspense fallback={<Skeleton className="h-56" />}>
                      <DeliveryMetricsChart data={deliveryChartData} />
                    </Suspense>
                    <Suspense fallback={<Skeleton className="h-56" />}>
                      <ChannelDistributionChart data={channelDistributionData} />
                    </Suspense>
                  </div>
                )}

                {/* Delivery Table */}
                {deliveryData.byChannel.length > 0 && (
                  <AnalyticsTable
                    title="Delivery by Channel"
                    description="Detailed delivery metrics per channel"
                    columns={deliveryColumns}
                    data={deliveryData.byChannel}
                    onExport={handleExportDelivery}
                    exportLabel="Export CSV"
                  />
                )}
              </>
            ) : (
              <Card className="border-dashed">
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <Activity className="h-12 w-12 text-slate-400 mb-4" />
                  <p className="text-slate-600 font-medium mb-1">No delivery data available</p>
                  <p className="text-sm text-slate-500 text-center max-w-sm">
                    Delivery analytics will appear here once notifications are sent through your workflows.
                  </p>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Error Analytics */}
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-slate-900">Error Analytics</h2>
              <p className="text-sm text-slate-600 mt-1">Error metrics and distribution analysis</p>
            </div>

            {isLoadingError ? (
              <div className="grid gap-4 md:grid-cols-2">
                {[...Array(2)].map((_, i) => (
                  <MetricCard key={i} label="" value="" icon={<AlertTriangle className="h-5 w-5" />} isLoading />
                ))}
              </div>
            ) : errorData ? (
              <>
                <div className="grid gap-4 md:grid-cols-2 mb-6">
                  <MetricCard
                    label="Total Errors"
                    value={errorData.totalErrors}
                    icon={<AlertTriangle className="h-5 w-5 text-error-600" />}
                    variant="error"
                  />
                  <MetricCard
                    label="Error Rate"
                    value={`${errorData.errorRate.toFixed(2)}%`}
                    icon={<Activity className="h-5 w-5 text-warning-600" />}
                    variant="warning"
                  />
                </div>

                {/* Error Tables */}
                {errorData.byType.length > 0 && (
                  <div className="mb-6">
                    <AnalyticsTable
                      title="Errors by Type"
                      description="Error distribution by error type"
                      columns={errorTypeColumns}
                      data={errorData.byType}
                      onExport={handleExportErrors}
                      exportLabel="Export CSV"
                    />
                  </div>
                )}
                {errorData.byChannel.length > 0 && (
                  <AnalyticsTable
                    title="Errors by Channel"
                    description="Error distribution by channel"
                    columns={errorChannelColumns}
                    data={errorData.byChannel}
                    onExport={handleExportErrors}
                    exportLabel="Export CSV"
                  />
                )}
              </>
            ) : (
              <Card className="border-dashed">
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <CheckCircle2 className="h-12 w-12 text-success-400 mb-4" />
                  <p className="text-slate-600 font-medium mb-1">No errors found</p>
                  <p className="text-sm text-slate-500 text-center max-w-sm">
                    Great! No errors detected in the selected time period. Your workflows are running smoothly.
                  </p>
                </CardContent>
              </Card>
            )}
            </div>
          </div>
            </div>
        </TabsContent>

        {/* Report Schedules Tab */}
        <TabsContent value="schedules" className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold">Report Scheduling</h2>
              <p className="text-slate-600 mt-1">Configure automated report generation and delivery</p>
            </div>
            <Button
              onClick={() => {
                setEditingSchedule(null)
                setActiveTab("schedules")
              }}
              size="sm"
            >
              <FileText className="h-4 w-4 mr-2" />
              New Schedule
            </Button>
          </div>

          {editingSchedule || (!editingSchedule && createSchedule.isPending) ? (
            <ReportScheduleForm
              schedule={editingSchedule || undefined}
              onSubmit={handleScheduleSubmit}
              onCancel={() => setEditingSchedule(null)}
              isLoading={createSchedule.isPending || updateSchedule.isPending}
            />
          ) : (
            <ScheduledReportsList onEdit={handleScheduleEdit} />
          )}
        </TabsContent>

        {/* Report History Tab */}
        <TabsContent value="report-history" className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold">Report History</h2>
            <p className="text-slate-600 mt-1">View generated reports and their status</p>
          </div>
          <ReportHistory />
        </TabsContent>

        {/* Report History Tab */}
        <TabsContent value="report-history" className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold">Report History</h2>
            <p className="text-slate-600 mt-1">View generated reports and their delivery status</p>
          </div>
          <ReportHistory />
        </TabsContent>

        {/* Data Retention Tab */}
        <TabsContent value="data-retention" className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold">Data Retention & Cleanup</h2>
            <p className="text-slate-600 mt-1">Manage data retention policies and cleanup settings</p>
          </div>
          <DataRetentionPanel />
        </TabsContent>
      </Tabs>
    </div>
  )
}
