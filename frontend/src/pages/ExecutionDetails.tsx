import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Skeleton } from "@/components/ui/skeleton"
import {
  ArrowLeft,
  RefreshCw,
  XCircle,
  CheckCircle2,
  Clock,
  AlertCircle,
  Play,
  X,
  Eye,
} from "lucide-react"
import { useExecution, useCancelExecution, useRetryExecution } from "@/hooks/use-executions"
import { useConfirmDialog } from "@/components/common/ConfirmDialog"
import { formatDistanceToNow } from "date-fns"
import { ExecutionTimeline } from "@/components/execution/ExecutionTimeline"
import { ExecutionLogs } from "@/components/execution/ExecutionLogs"
import { ExecutionContext } from "@/components/execution/ExecutionContext"
import { WaitingStateCard } from "@/components/execution/WaitingStateCard"
import { Hourglass } from "lucide-react"

const statusConfig: Record<string, { label: string; color: string; icon: typeof CheckCircle2 }> = {
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

export default function ExecutionDetails() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: execution, isLoading, error, refetch } = useExecution(id)
  const cancelExecution = useCancelExecution()
  const retryExecution = useRetryExecution()
  const { confirm } = useConfirmDialog()

  const formatDuration = (ms?: number) => {
    if (!ms) return "-"
    if (ms < 1000) return `${ms}ms`
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`
    return `${(ms / 60000).toFixed(2)}m`
  }

  const handleCancel = async () => {
    if (!id) return

    const confirmed = await confirm({
      title: "Cancel Execution",
      description: "Are you sure you want to cancel this execution?",
      variant: "destructive",
      confirmText: "Cancel Execution",
    })

    if (confirmed) {
      await cancelExecution.mutateAsync(id)
      refetch()
    }
  }

  const handleRetry = async () => {
    if (!id) return

    const confirmed = await confirm({
      title: "Retry Execution",
      description: "This will create a new execution with the same context. Continue?",
      confirmText: "Retry",
    })

    if (confirmed) {
      const newExecution = await retryExecution.mutateAsync(id)
      navigate(`/executions/${newExecution.id}`)
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6 space-y-6">
        <Skeleton className="h-10 w-1/3" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (error || !execution) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-error-600">Failed to load execution</p>
            <p className="text-sm mt-2 text-secondary-500">
              {error instanceof Error ? error.message : "Unknown error"}
            </p>
            <Button variant="outline" className="mt-4" onClick={() => navigate("/executions")}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Executions
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const status = statusConfig[execution.status] || statusConfig.completed
  const StatusIcon = status.icon

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Button variant="ghost" size="sm" onClick={() => navigate("/executions")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">Execution Details</h1>
            <p className="text-secondary-600 mt-2">Execution ID: {execution.id}</p>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <Button variant="outline" onClick={() => navigate(`/executions/${id}/visualize`)}>
            <Eye className="h-4 w-4 mr-2" />
            Visualize
          </Button>
          <Button variant="outline" onClick={() => refetch()}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
          {(execution.status === "running" || execution.status === "waiting") && (
            <Button variant="destructive" onClick={handleCancel} disabled={cancelExecution.isPending}>
              <X className="h-4 w-4 mr-2" />
              Cancel
            </Button>
          )}
          {execution.status === "failed" && (
            <Button onClick={handleRetry} disabled={retryExecution.isPending}>
              <Play className="h-4 w-4 mr-2" />
              Retry
            </Button>
          )}
        </div>
      </div>

      {/* Execution Info */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center space-x-2">
                <StatusIcon className={`h-5 w-5 ${status.color.replace("bg-", "text-")}`} />
                <span>{status.label}</span>
              </CardTitle>
              <CardDescription className="mt-2">
                {execution.workflow_name || execution.workflow_id}
              </CardDescription>
            </div>
            <Badge variant="default" className={status.color}>
              {status.label}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <div className="text-sm text-secondary-500">Started</div>
              <div className="font-medium">
                {formatDistanceToNow(new Date(execution.started_at), { addSuffix: true })}
              </div>
              <div className="text-xs text-secondary-400">
                {new Date(execution.started_at).toLocaleString()}
              </div>
            </div>
            {execution.completed_at && (
              <div>
                <div className="text-sm text-secondary-500">Completed</div>
                <div className="font-medium">
                  {formatDistanceToNow(new Date(execution.completed_at), { addSuffix: true })}
                </div>
                <div className="text-xs text-secondary-400">
                  {new Date(execution.completed_at).toLocaleString()}
                </div>
              </div>
            )}
            <div>
              <div className="text-sm text-secondary-500">Duration</div>
              <div className="font-medium">{formatDuration(execution.duration)}</div>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Nodes Executed</div>
              <div className="font-medium">{execution.nodes_executed}</div>
            </div>
            <div>
              <div className="text-sm text-secondary-500">Notifications Sent</div>
              <div className="font-medium">{execution.notifications_sent}</div>
            </div>
          </div>
          {execution.error && (
            <div className="mt-4 p-4 bg-error-50 border border-error-200 rounded-lg">
              <div className="flex items-center space-x-2 text-error-600 font-semibold mb-2">
                <XCircle className="h-4 w-4" />
                <span>Error</span>
              </div>
              <pre className="text-sm text-error-700 whitespace-pre-wrap">{execution.error}</pre>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Waiting State Card */}
      {execution.waitState && execution.status === "waiting" && (
        <WaitingStateCard waitState={execution.waitState} />
      )}

      {/* Tabs */}
      <Tabs defaultValue="timeline" className="space-y-4">
        <TabsList>
          <TabsTrigger value="timeline">Timeline</TabsTrigger>
          <TabsTrigger value="context">Context</TabsTrigger>
          <TabsTrigger value="logs">Logs</TabsTrigger>
        </TabsList>
        <TabsContent value="timeline">
          <ExecutionTimeline executionId={execution.id} />
        </TabsContent>
        <TabsContent value="context">
          <ExecutionContext context={execution.context} />
        </TabsContent>
        <TabsContent value="logs">
          <ExecutionLogs executionId={execution.id} />
        </TabsContent>
      </Tabs>
    </div>
  )
}

