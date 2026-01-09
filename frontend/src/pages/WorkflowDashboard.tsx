import { useState, useMemo } from "react"
import { useParams, useNavigate, Link } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  ArrowLeft,
  Edit,
  Download,
  RefreshCw,
  ExternalLink,
  AlertCircle,
  CheckCircle2,
  Clock,
  XCircle,
  FileText,
} from "lucide-react"
import { useWorkflow } from "@/hooks/use-workflows"
import { useWorkflowDashboard } from "@/hooks/use-workflow-dashboard"
import { useRealtimeWorkflowDashboard, type PollingInterval } from "@/hooks/use-realtime-workflow-dashboard"
import { AutoRefreshControl } from "@/components/common/AutoRefreshControl"
import { MetricCard } from "@/components/workflow-dashboard/MetricCard"
import { TimeRangeSelector, type TimeRange } from "@/components/workflow-dashboard/TimeRangeSelector"
import { ExecutionTrendChart } from "@/components/dashboard/ExecutionTrendChart"
import { formatDistanceToNow } from "date-fns"
import { format } from "date-fns"

export default function WorkflowDashboard() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [timeRange, setTimeRange] = useState<TimeRange>({
    preset: "7d",
    startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    endDate: new Date(),
  })
  const [timezone, setTimezone] = useState<string>("UTC")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [pollingInterval, setPollingInterval] = useState<PollingInterval>(30)

  // Calculate date range for API calls
  const dateRange = useMemo(() => {
    if (!timeRange.startDate || !timeRange.endDate) return null
    return {
      start_date: timeRange.startDate.toISOString(),
      end_date: timeRange.endDate.toISOString(),
      timezone,
    }
  }, [timeRange, timezone])

  // Fetch workflow data
  const { data: workflow, isLoading: isLoadingWorkflow } = useWorkflow(id)

  // Fetch dashboard data
  const dashboardParams = useMemo(
    () =>
      id && dateRange
        ? {
            workflowId: id,
            ...dateRange,
          }
        : null,
    [id, dateRange]
  )

  const { data: dashboardData, isLoading: isLoadingDashboard, refetch, isRefetching } = useRealtimeWorkflowDashboard(
    dashboardParams!,
    { pollingInterval }
  )

  const overview = dashboardData?.overview
  const trends = dashboardData?.executionTrends
  const nodePerformance = dashboardData?.nodePerformance
  const channelPerformance = dashboardData?.channelPerformance
  const executionHistory = dashboardData?.executionTrends // Using executionTrends as history
  const errorAnalysis = dashboardData?.errorAnalysis

  const isLoading = isLoadingWorkflow || isLoadingDashboard

  const statusConfig: Record<string, { label: string; color: string; icon: typeof CheckCircle2 }> =
    {
      active: {
        label: "Active",
        color: "bg-success-600",
        icon: CheckCircle2,
      },
      inactive: {
        label: "Inactive",
        color: "bg-secondary-600",
        icon: Clock,
      },
      paused: {
        label: "Paused",
        color: "bg-warning-600",
        icon: AlertCircle,
      },
      archived: {
        label: "Archived",
        color: "bg-secondary-600",
        icon: XCircle,
      },
      draft: {
        label: "Draft",
        color: "bg-secondary-600",
        icon: Clock,
      },
    }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 space-y-6">
        <Skeleton className="h-10 w-1/3" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (!workflow || !overview) {
    return (
      <div className="container mx-auto p-6">
        <div className="text-center py-12">
          <p className="text-error-600">Failed to load workflow dashboard</p>
          <Button variant="outline" className="mt-4" onClick={() => navigate("/workflows")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Workflows
          </Button>
        </div>
      </div>
    )
  }

  const statusInfo = statusConfig[workflow.status] || statusConfig.inactive

  // Transform trends data for chart
  const chartData =
    trends?.data?.map((item) => ({
      date: format(new Date(item.timestamp), "MMM dd"),
      executions: item.total,
      success: item.successful,
      failed: item.failed,
    })) || []

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate("/workflows")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold">{overview?.workflow?.name || workflow?.name || "Workflow"}</h1>
              <Badge
                variant={workflow.status === "active" ? "default" : "secondary"}
                className="text-xs"
              >
                {statusInfo.label}
              </Badge>
            </div>
            <p className="text-sm text-secondary-500 mt-1">
              Last execution:{" "}
              {overview?.workflow?.last_execution
                ? formatDistanceToNow(new Date(overview.workflow.last_execution), {
                    addSuffix: true,
                  })
                : "Never"}
            </p>
          </div>
        </div>
        <AutoRefreshControl
          pollingInterval={pollingInterval}
          onIntervalChange={setPollingInterval}
          onManualRefresh={() => refetch()}
          isRefreshing={isRefetching}
        />
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => navigate(`/workflows/${id}/report`)}>
            <FileText className="h-4 w-4 mr-2" />
            Configure Report
          </Button>
          <Button variant="outline" size="sm" onClick={() => navigate(`/workflows/${id}`)}>
            <Edit className="h-4 w-4 mr-2" />
            Edit
          </Button>
          <Button variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            Export
          </Button>
          <Button variant="outline" size="sm">
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex items-center justify-between gap-4">
        <TimeRangeSelector value={timeRange} onChange={setTimeRange} />
        <div className="flex items-center gap-2">
          <Select value={timezone} onValueChange={setTimezone}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Timezone" />
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
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[140px]">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              <SelectItem value="completed">Completed</SelectItem>
              <SelectItem value="failed">Failed</SelectItem>
              <SelectItem value="running">Running</SelectItem>
              <SelectItem value="cancelled">Cancelled</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Metrics Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          label="Total Executions"
          value={(overview?.metrics?.total_executions || 0).toLocaleString()}
          trend={overview?.trends?.executions_change}
        />
        <MetricCard
          label="Success Rate"
          value={`${((overview?.metrics?.success_rate || 0) * 100).toFixed(1)}%`}
          trend={overview?.trends?.success_rate_change}
        />
        <MetricCard
          label="Avg Execution Time"
          value={`${(overview?.metrics?.average_execution_time || 0).toFixed(2)}s`}
          trend={overview?.trends?.execution_time_change}
        />
        <MetricCard
          label="Total Notifications"
          value={(overview?.metrics?.total_notifications_sent || 0).toLocaleString()}
        />
      </div>

      {/* Charts Section */}
      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="performance">Performance</TabsTrigger>
          <TabsTrigger value="errors">Errors</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <ExecutionTrendChart data={chartData} />

          {/* Channel Performance */}
          {channelPerformance && (
            <Card>
              <CardHeader>
                <CardTitle>Channel Performance</CardTitle>
                <CardDescription>Notification delivery by channel</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {channelPerformance?.channels?.map((channel) => (
                    <div key={channel.channel} className="flex items-center justify-between">
                      <div>
                        <p className="font-medium capitalize">{channel.channel}</p>
                        <p className="text-sm text-secondary-500">
                          {channel.delivered} / {channel.sent} delivered
                        </p>
                      </div>
                      <Badge variant="outline">
                        {(channel.delivery_rate * 100).toFixed(1)}%
                      </Badge>
                    </div>
                  )) || <p className="text-sm text-secondary-500 text-center py-4">No channel data available</p>}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Execution History */}
          {executionHistory && (
            <Card>
              <CardHeader>
                <CardTitle>Recent Executions</CardTitle>
                <CardDescription>Last 20 executions</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {executionHistory?.executions?.map((execution) => (
                    <div
                      key={execution.id}
                      className="flex items-center justify-between p-3 border rounded-lg hover:bg-secondary-50 cursor-pointer"
                      onClick={() => navigate(`/executions/${execution.id}`)}
                    >
                      <div>
                        <p className="text-sm font-medium">{execution.id}</p>
                        <p className="text-xs text-secondary-500">
                          {format(new Date(execution.started_at), "MMM dd, yyyy HH:mm")}
                        </p>
                      </div>
                      <div className="flex items-center gap-4">
                        <Badge
                          variant={
                            execution.status === "completed"
                              ? "default"
                              : execution.status === "failed"
                              ? "destructive"
                              : "secondary"
                          }
                        >
                          {execution.status}
                        </Badge>
                        <ExternalLink className="h-4 w-4 text-secondary-400" />
                      </div>
                    </div>
                  )) || <p className="text-sm text-secondary-500 text-center py-4">No executions found</p>}
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="performance" className="space-y-4">
          {nodePerformance && (
            <Card>
              <CardHeader>
                <CardTitle>Node Performance</CardTitle>
                <CardDescription>Performance metrics by node</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {nodePerformance?.nodes?.map((node) => (
                    <div key={node.node_id} className="flex items-center justify-between p-3 border rounded-lg">
                      <div>
                        <p className="font-medium">{node.node_name}</p>
                        <p className="text-sm text-secondary-500">{node.node_type}</p>
                      </div>
                      <div className="flex items-center gap-4 text-sm">
                        <div>
                          <p className="text-secondary-500">Executions</p>
                          <p className="font-medium">{node.execution_count}</p>
                        </div>
                        <div>
                          <p className="text-secondary-500">Avg Time</p>
                          <p className="font-medium">{node.average_execution_time.toFixed(2)}s</p>
                        </div>
                        <div>
                          <p className="text-secondary-500">Success Rate</p>
                          <p className="font-medium">{(node.success_rate * 100).toFixed(1)}%</p>
                        </div>
                      </div>
                    </div>
                  )) || <p className="text-sm text-secondary-500 text-center py-4">No node performance data available</p>}
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="errors" className="space-y-4">
          {errorAnalysis && (
            <>
              <Card>
                <CardHeader>
                  <CardTitle>Error Summary</CardTitle>
                  <CardDescription>Error analysis for selected period</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-secondary-500">Total Errors</p>
                      <p className="text-2xl font-bold">{errorAnalysis?.summary?.total_errors || 0}</p>
                    </div>
                    <div>
                      <p className="text-sm text-secondary-500">Error Rate</p>
                      <p className="text-2xl font-bold">
                        {((errorAnalysis?.summary?.error_rate || 0) * 100).toFixed(2)}%
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Error Timeline Chart */}
              {errorAnalysis?.errorTimeline && errorAnalysis.errorTimeline.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle>Error Timeline</CardTitle>
                    <CardDescription>Errors over time</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ExecutionTrendChart
                      data={errorAnalysis.errorTimeline.map((item) => ({
                        date: format(new Date(item.date), "MMM dd"),
                        executions: item.count,
                        success: 0,
                        failed: item.count,
                      }))}
                    />
                  </CardContent>
                </Card>
              )}

              <Card>
                <CardHeader>
                  <CardTitle>Error Details</CardTitle>
                  <CardDescription>Recent errors</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {errorAnalysis?.errors?.map((error) => (
                      <div
                        key={error.id}
                        className="p-3 border rounded-lg hover:bg-secondary-50 cursor-pointer"
                        onClick={() => navigate(`/executions/${error.execution_id}`)}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm font-medium">{error.error_type}</p>
                            <p className="text-xs text-secondary-500">{error.error_message}</p>
                            <p className="text-xs text-secondary-400 mt-1">
                              {format(new Date(error.timestamp), "MMM dd, yyyy HH:mm")}
                            </p>
                          </div>
                          <ExternalLink className="h-4 w-4 text-secondary-400" />
                        </div>
                      </div>
                    )) || (
                      <p className="text-sm text-secondary-500 text-center py-4">No errors found</p>
                    )}
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}

