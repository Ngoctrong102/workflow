import { useState, useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Search,
  Eye,
  RefreshCw,
  XCircle,
  CheckCircle2,
  Clock,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Hourglass,
  BarChart3,
  Download,
  Trash2,
  Loader2,
} from "lucide-react"
import { useExecutions } from "@/hooks/use-executions"
import { useRealtimeExecutions, type PollingInterval } from "@/hooks/use-realtime-executions"
import { AutoRefreshControl } from "@/components/common/AutoRefreshControl"
import { useWorkflows } from "@/hooks/use-workflows"
import { useExportExecution, useBulkExportExecutions } from "@/hooks/use-execution-export"
import { useBulkDeleteExecutions } from "@/hooks/use-bulk-operations"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { ExecutionBulkActions } from "@/components/common/ExecutionBulkActions"
import { formatDistanceToNow } from "date-fns"
import { Checkbox } from "@/components/ui/checkbox"
import type { ExecutionStatus } from "@/types/execution"

const statusConfig: Record<ExecutionStatus, { label: string; color: string; icon: typeof CheckCircle2 }> = {
  running: {
    label: "Running",
    color: "bg-primary-600",
    icon: Clock,
  },
  waiting: {
    label: "Waiting",
    color: "bg-warning-600",
    icon: Hourglass,
  },
  completed: {
    label: "Completed",
    color: "bg-success-600",
    icon: CheckCircle2,
  },
  failed: {
    label: "Failed",
    color: "bg-error-600",
    icon: XCircle,
  },
  cancelled: {
    label: "Cancelled",
    color: "bg-secondary-600",
    icon: AlertCircle,
  },
}

export default function ExecutionList() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<ExecutionStatus | "all">("all")
  const [workflowFilter, setWorkflowFilter] = useState<string>("all")
  const [startDate, setStartDate] = useState<string>("")
  const [endDate, setEndDate] = useState<string>("")
  const [page, setPage] = useState(0)
  const [selectedExecutions, setSelectedExecutions] = useState<Set<string>>(new Set())
  const [pollingInterval, setPollingInterval] = useState<PollingInterval>(30)
  const limit = 20

  // Fetch workflows for filter dropdown
  const { data: workflowsData } = useWorkflows({ limit: 100 })

  const workflows = useMemo(() => workflowsData?.data || [], [workflowsData?.data])

  const { data, isLoading, error, refetch, isRefetching } = useRealtimeExecutions(
    {
      workflow_id: workflowFilter !== "all" ? workflowFilter : undefined,
      status: statusFilter !== "all" ? statusFilter : undefined,
      start_date: startDate || undefined,
      end_date: endDate || undefined,
      search: searchQuery || undefined,
      limit,
      offset: page * limit,
    },
    { pollingInterval }
  )

  const executions = data?.executions || []
  const total = data?.total || 0
  const totalPages = Math.ceil(total / limit)

  const exportExecution = useExportExecution()
  const bulkExportExecutions = useBulkExportExecutions()
  const bulkDeleteExecutions = useBulkDeleteExecutions()
  const { confirm } = useConfirmDialog()

  const formatDuration = (ms?: number) => {
    if (!ms) return "-"
    if (ms < 1000) return `${ms}ms`
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`
    return `${(ms / 60000).toFixed(2)}m`
  }

  const handleSelectAll = () => {
    if (selectedExecutions.size === executions.length) {
      setSelectedExecutions(new Set())
    } else {
      setSelectedExecutions(new Set(executions.map((e) => e.id)))
    }
  }

  const handleSelectExecution = (id: string) => {
    const newSelected = new Set(selectedExecutions)
    if (newSelected.has(id)) {
      newSelected.delete(id)
    } else {
      newSelected.add(id)
    }
    setSelectedExecutions(newSelected)
  }

  const handleExport = async (id: string, format: "csv" | "json" = "json") => {
    await exportExecution.mutateAsync({ id, format })
  }

  const handleBulkExport = async (format: "csv" | "json" = "json") => {
    const ids = Array.from(selectedExecutions)
    if (ids.length === 0) return
    await bulkExportExecutions.mutateAsync({ ids, format })
    setSelectedExecutions(new Set())
  }

  const handleBulkDelete = async () => {
    const ids = Array.from(selectedExecutions)
    if (ids.length === 0) return

    const confirmed = await confirm({
      title: "Delete Executions",
      description: `Are you sure you want to delete ${ids.length} execution${ids.length !== 1 ? "s" : ""}? This action cannot be undone.`,
      variant: "destructive",
      confirmText: "Delete",
    })

    if (confirmed) {
      await bulkDeleteExecutions.mutateAsync(ids)
      setSelectedExecutions(new Set())
    }
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
              {/* Header */}
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-3xl font-bold">{t("execution.title")}</h1>
                  <p className="text-secondary-600 mt-2" role="doc-subtitle">View and manage workflow executions</p>
                </div>
        <div className="flex items-center space-x-2">
          {selectedExecutions.size > 0 && (
            <>
              <Button
                variant="outline"
                onClick={() => handleBulkExport("json")}
                disabled={bulkExportExecutions.isPending}
              >
                {bulkExportExecutions.isPending ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Download className="h-4 w-4 mr-2" />
                )}
                Export ({selectedExecutions.size})
              </Button>
              <Button
                variant="destructive"
                onClick={handleBulkDelete}
                disabled={bulkDeleteExecutions.isPending}
              >
                {bulkDeleteExecutions.isPending ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Trash2 className="h-4 w-4 mr-2" />
                )}
                Delete ({selectedExecutions.size})
              </Button>
            </>
          )}
          <Button variant="outline" onClick={() => refetch()}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="space-y-4">
            <div className="flex items-center space-x-4">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  placeholder="Search executions..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value)
                    setPage(0)
                  }}
                  className="pl-9"
                />
              </div>
              <Select
                value={workflowFilter}
                onValueChange={(v) => {
                  setWorkflowFilter(v)
                  setPage(0)
                }}
              >
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Filter by workflow" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Workflows</SelectItem>
                  {workflows.map((workflow) => (
                    <SelectItem key={workflow.id} value={workflow.id}>
                      {workflow.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={statusFilter}
                onValueChange={(v) => {
                  setStatusFilter(v as ExecutionStatus | "all")
                  setPage(0)
                }}
              >
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="running">Running</SelectItem>
                  <SelectItem value="waiting">Waiting</SelectItem>
                  <SelectItem value="completed">Completed</SelectItem>
                  <SelectItem value="failed">Failed</SelectItem>
                  <SelectItem value="cancelled">Cancelled</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-center space-x-4">
              <Input
                type="date"
                placeholder="Start date"
                value={startDate}
                onChange={(e) => {
                  setStartDate(e.target.value)
                  setPage(0)
                }}
                className="w-48"
              />
              <Input
                type="date"
                placeholder="End date"
                value={endDate}
                onChange={(e) => {
                  setEndDate(e.target.value)
                  setPage(0)
                }}
                className="w-48"
              />
              {(startDate || endDate) && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setStartDate("")
                    setEndDate("")
                    setPage(0)
                  }}
                >
                  Clear Dates
                </Button>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Execution List */}
      <Card>
        <CardHeader>
          <CardTitle>Executions {total > 0 && `(${total})`}</CardTitle>
          <CardDescription>All workflow executions</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-center space-x-4">
                  <Skeleton className="h-12 w-full" />
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-12 text-error-600">
              <p>Failed to load executions</p>
              <p className="text-sm mt-2">{error instanceof Error ? error.message : "Unknown error"}</p>
            </div>
          ) : executions.length === 0 ? (
            <div className="text-center py-12 text-secondary-500">
              <p>No executions found</p>
            </div>
          ) : (
            <>
              {selectedExecutions.size > 0 && (
                <ExecutionBulkActions
                  selectedCount={selectedExecutions.size}
                  onClearSelection={() => setSelectedExecutions(new Set())}
                  onBulkExport={handleBulkExport}
                  onBulkDelete={handleBulkDelete}
                />
              )}
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-12">
                      <Checkbox
                        checked={selectedExecutions.size === executions.length && executions.length > 0}
                        onCheckedChange={handleSelectAll}
                      />
                    </TableHead>
                    <TableHead>ID</TableHead>
                    <TableHead>Workflow</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Started</TableHead>
                    <TableHead>Duration</TableHead>
                    <TableHead>Nodes</TableHead>
                    <TableHead>Notifications</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {executions.map((execution) => {
                    const status = statusConfig[execution.status]
                    const StatusIcon = status.icon

                    return (
                      <TableRow key={execution.id}>
                        <TableCell>
                          <Checkbox
                            checked={selectedExecutions.has(execution.id)}
                            onCheckedChange={() => handleSelectExecution(execution.id)}
                          />
                        </TableCell>
                        <TableCell>
                          <div className="font-mono text-sm">{execution.id.slice(0, 8)}...</div>
                        </TableCell>
                        <TableCell>
                          <div>
                            <div className="font-medium">{execution.workflow_name || execution.workflow_id}</div>
                            {execution.workflow_id && (
                              <div className="text-xs text-secondary-500">{execution.workflow_id}</div>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant="default" className={status.color}>
                            <StatusIcon className="h-3 w-3 mr-1" />
                            {status.label}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="text-sm">
                            {formatDistanceToNow(new Date(execution.started_at), { addSuffix: true })}
                          </div>
                          <div className="text-xs text-secondary-500">
                            {new Date(execution.started_at).toLocaleString()}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="text-sm">{formatDuration(execution.duration)}</div>
                        </TableCell>
                        <TableCell>
                          <div className="text-sm">{execution.nodes_executed}</div>
                        </TableCell>
                        <TableCell>
                          <div className="text-sm">{execution.notifications_sent}</div>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end space-x-1">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => navigate(`/executions/${execution.id}`)}
                              title="View details"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => navigate(`/executions/${execution.id}/visualize`)}
                              title="View visualization"
                            >
                              <BarChart3 className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleExport(execution.id, "json")}
                              disabled={exportExecution.isPending}
                              title="Export execution"
                            >
                              {exportExecution.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <Download className="h-4 w-4" />
                              )}
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t">
                  <div className="text-sm text-secondary-500">
                    Showing {page * limit + 1} to {Math.min((page + 1) * limit, total)} of {total} executions
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                      disabled={page === 0}
                    >
                      <ChevronLeft className="h-4 w-4" />
                      Previous
                    </Button>
                    <div className="text-sm">
                      Page {page + 1} of {totalPages}
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                      disabled={page >= totalPages - 1}
                    >
                      Next
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

