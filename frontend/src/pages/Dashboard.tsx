import { lazy, Suspense, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { MetricCard } from "@/components/dashboard/MetricCard"
import { ActivityFeed } from "@/components/dashboard/ActivityFeed"
import { WorkflowList } from "@/components/dashboard/WorkflowList"
import { QuickActions } from "@/components/dashboard/QuickActions"
import { useWorkflows } from "@/hooks/use-workflows"
import { useExecutions } from "@/hooks/use-executions"
import { useRealtimeExecutions, type PollingInterval } from "@/hooks/use-realtime-executions"
import { AutoRefreshControl } from "@/components/common/AutoRefreshControl"
import { useDeliveryAnalytics } from "@/hooks/use-analytics"
import { Workflow, PlayCircle, Download } from "lucide-react"
import { format, subDays } from "date-fns"
import { Skeleton } from "@/components/ui/skeleton"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { exportDashboardDataAsCSV, exportChartAsImage } from "@/utils/chart-export"
import { exportToJSON } from "@/utils/export"

// Lazy load heavy chart components for code splitting
const ExecutionTrendChart = lazy(() => import("@/components/dashboard/ExecutionTrendChart").then(m => ({ default: m.ExecutionTrendChart })))
const DeliveryMetricsChart = lazy(() => import("@/components/dashboard/DeliveryMetricsChart").then(m => ({ default: m.DeliveryMetricsChart })))
const ChannelDistributionChart = lazy(() => import("@/components/dashboard/ChannelDistributionChart").then(m => ({ default: m.ChannelDistributionChart })))

export default function Dashboard() {
  const { t } = useTranslation()
  const [pollingInterval, setPollingInterval] = useState<PollingInterval>(60)
  const { data: workflowsData, isLoading: isLoadingWorkflows } = useWorkflows({ limit: 100 })
  const { data: executionsData, isLoading: isLoadingExecutions, refetch: refetchExecutions, isRefetching: isRefetchingExecutions } = useRealtimeExecutions(
    { limit: 10 },
    { pollingInterval }
  )
  
  const dateRange = {
    start: format(subDays(new Date(), 7), "yyyy-MM-dd"),
    end: format(new Date(), "yyyy-MM-dd"),
  }
  
  const { data: deliveryData, isLoading: isLoadingDelivery } = useDeliveryAnalytics({
    start: `${dateRange.start}T00:00:00Z`,
    end: `${dateRange.end}T23:59:59Z`,
  })

  const workflows = workflowsData?.workflows || []
  const totalWorkflows = workflowsData?.total || 0
  const activeWorkflows = workflows.filter((w) => w.status === "active").length
  const totalExecutions = executionsData?.total || deliveryData?.totalSent || 0
  const successExecutions = executionsData?.executions?.filter((e) => e.status === "COMPLETED").length || 0
  const successRate = totalExecutions > 0 ? (successExecutions / totalExecutions) * 100 : 0

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

  const mockTrendData = [
    { date: "2024-01-01", executions: 120, success: 118, failed: 2 },
    { date: "2024-01-02", executions: 145, success: 143, failed: 2 },
    { date: "2024-01-03", executions: 132, success: 130, failed: 2 },
    { date: "2024-01-04", executions: 158, success: 156, failed: 2 },
    { date: "2024-01-05", executions: 142, success: 140, failed: 2 },
    { date: "2024-01-06", executions: 165, success: 163, failed: 2 },
    { date: "2024-01-07", executions: 150, success: 148, failed: 2 },
  ]

  const mockActivities = [
    {
      id: "1",
      workflowName: "Welcome Email",
      workflowId: "wf-1",
      status: "success" as const,
      timestamp: new Date(Date.now() - 1000 * 60 * 5),
      executionId: "exec-1",
    },
    {
      id: "2",
      workflowName: "Order Confirmation",
      workflowId: "wf-2",
      status: "success" as const,
      timestamp: new Date(Date.now() - 1000 * 60 * 15),
      executionId: "exec-2",
    },
    {
      id: "3",
      workflowName: "Password Reset",
      workflowId: "wf-3",
      status: "failed" as const,
      timestamp: new Date(Date.now() - 1000 * 60 * 30),
      executionId: "exec-3",
    },
    {
      id: "4",
      workflowName: "Newsletter",
      workflowId: "wf-4",
      status: "running" as const,
      timestamp: new Date(Date.now() - 1000 * 60 * 45),
      executionId: "exec-4",
    },
  ]

  const isLoading = isLoadingWorkflows || isLoadingDelivery

  const executionTrendChartRef = useRef<HTMLDivElement>(null)
  const deliveryMetricsChartRef = useRef<HTMLDivElement>(null)
  const channelDistributionChartRef = useRef<HTMLDivElement>(null)

  const handleExportDashboard = (format: "csv" | "json") => {
    const dashboardData = [
      {
        metric: "Total Workflows",
        value: totalWorkflows,
        active: activeWorkflows,
      },
      {
        metric: "Total Executions",
        value: totalExecutions,
        success_rate: `${successRate.toFixed(1)}%`,
      },
    ]

    if (format === "csv") {
      exportDashboardDataAsCSV(dashboardData)
    } else {
      exportToJSON(dashboardData, `dashboard-${Date.now()}.json`)
    }
  }

  const handleExportChart = (chartRef: React.RefObject<HTMLDivElement>, chartName: string) => {
    if (chartRef.current) {
      exportChartAsImage(chartRef.current, `${chartName}-${Date.now()}.png`)
    }
  }

  return (
    <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-2xl font-bold">{t("dashboard.title")}</h1>
                  <p className="text-secondary-600 mt-1 text-sm" role="doc-subtitle">
                    {t("dashboard.overview")}
                  </p>
                </div>
        <div className="flex items-center space-x-2">
          <AutoRefreshControl
            pollingInterval={pollingInterval}
            onIntervalChange={setPollingInterval}
            onManualRefresh={() => refetchExecutions()}
            isRefreshing={isRefetchingExecutions}
          />
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm">
                <Download className="h-4 w-4 mr-2" />
                Export
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleExportDashboard("json")}>
                Export Data as JSON
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleExportDashboard("csv")}>
                Export Data as CSV
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
        {isLoading ? (
          <>
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </>
        ) : (
          <>
            <MetricCard
              title="Total Workflows"
              value={totalWorkflows}
              description={`${activeWorkflows} active`}
              href="/workflows"
              icon={<Workflow className="h-5 w-5" />}
            />
            <MetricCard
              title="Total Executions"
              value={totalExecutions.toLocaleString()}
              description={`${successRate.toFixed(1)}% success rate`}
              href="/executions"
              icon={<PlayCircle className="h-5 w-5" />}
            />
          </>
        )}
      </div>

      {/* Charts Row */}
      <div className="grid gap-3 md:grid-cols-2">
        <div ref={executionTrendChartRef}>
          <Suspense fallback={<Skeleton className="h-56" />}>
            <ExecutionTrendChart data={mockTrendData} />
          </Suspense>
        </div>
        {isLoadingDelivery ? (
          <Skeleton className="h-56" />
        ) : (
          <div ref={deliveryMetricsChartRef}>
            <Suspense fallback={<Skeleton className="h-56" />}>
              <DeliveryMetricsChart data={deliveryChartData} />
            </Suspense>
          </div>
        )}
      </div>

      {/* Recent Workflows and Executions */}
      <div className="grid gap-3 md:grid-cols-2">
        <WorkflowList
          workflows={workflows}
          isLoading={isLoadingWorkflows}
          limit={5}
        />
        <ActivityFeed
          activities={mockActivities}
          onActivityClick={(activity) => {
            console.log("Activity clicked:", activity)
          }}
        />
      </div>

      {/* Channel Distribution */}
      <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
        {isLoadingDelivery ? (
          <Skeleton className="h-56" />
        ) : (
          <div ref={channelDistributionChartRef}>
            <Suspense fallback={<Skeleton className="h-56" />}>
              <ChannelDistributionChart data={channelDistributionData} />
            </Suspense>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <QuickActions />
    </div>
  )
}
