import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { CheckCircle2, XCircle, Clock, Hourglass } from "lucide-react"
import { useNodeExecutions } from "@/hooks/use-executions"
import { formatDistanceToNow } from "date-fns"
import { cn } from "@/lib/utils"

interface ExecutionTimelineProps {
  executionId: string
}

const nodeStatusConfig = {
  running: {
    label: "Running",
    color: "text-primary-600",
    bgColor: "bg-primary-50",
    borderColor: "border-primary-200",
    icon: Clock,
  },
  waiting: {
    label: "Waiting",
    color: "text-warning-600",
    bgColor: "bg-warning-50",
    borderColor: "border-warning-200",
    icon: Hourglass,
  },
  completed: {
    label: "Completed",
    color: "text-success-600",
    bgColor: "bg-success-50",
    borderColor: "border-success-200",
    icon: CheckCircle2,
  },
  failed: {
    label: "Failed",
    color: "text-error-600",
    bgColor: "bg-error-50",
    borderColor: "border-error-200",
    icon: XCircle,
  },
}

export function ExecutionTimeline({ executionId }: ExecutionTimelineProps) {
  const { data: nodeExecutions, isLoading, error } = useNodeExecutions(executionId)

  const formatDuration = (ms?: number) => {
    if (!ms) return "-"
    if (ms < 1000) return `${ms}ms`
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`
    return `${(ms / 60000).toFixed(2)}m`
  }

  if (isLoading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-20 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardContent className="py-12 text-center">
          <p className="text-error-600">Failed to load timeline</p>
          <p className="text-sm mt-2 text-secondary-500">
            {error instanceof Error ? error.message : "Unknown error"}
          </p>
        </CardContent>
      </Card>
    )
  }

  if (!nodeExecutions || nodeExecutions.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center text-secondary-500">
          <p>No node executions found</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution Timeline</CardTitle>
        <CardDescription>Node execution order and status</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {nodeExecutions.map((nodeExec) => {
            const status = nodeStatusConfig[nodeExec.status as keyof typeof nodeStatusConfig] || nodeStatusConfig.completed
            const Icon = status.icon

            return (
              <div
                key={nodeExec.id}
                className={cn(
                  "p-4 rounded-lg border-2 transition-all",
                  status.bgColor,
                  status.borderColor
                )}
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3 flex-1">
                    <div className={cn("flex-shrink-0 mt-0.5", status.color)}>
                      <Icon className="h-5 w-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2 mb-1">
                        <h4 className="font-semibold text-sm">{nodeExec.node_label || nodeExec.node_id}</h4>
                        <Badge variant="outline" className={cn("text-xs", status.color)}>
                          {nodeExec.node_type}
                        </Badge>
                        <Badge variant="default" className={cn(status.bgColor, status.color, "border")}>
                          {status.label}
                        </Badge>
                      </div>
                      <div className="text-xs text-secondary-500 space-y-1">
                        <div>
                          Started: {formatDistanceToNow(new Date(nodeExec.started_at), { addSuffix: true })}
                        </div>
                        {nodeExec.completed_at && (
                          <div>
                            Completed: {formatDistanceToNow(new Date(nodeExec.completed_at), { addSuffix: true })}
                          </div>
                        )}
                        <div>Duration: {formatDuration(nodeExec.duration)}</div>
                      </div>
                      {nodeExec.wait_state && (
                        <div className="mt-2 p-2 bg-warning-50 border border-warning-200 rounded text-xs">
                          <div className="font-medium text-warning-800 mb-1">Waiting for Events</div>
                          <div className="text-warning-700">
                            <div>Correlation ID: {nodeExec.wait_state.correlationId}</div>
                            <div>
                              Waiting for: {nodeExec.wait_state.enabledEvents.length} event(s) | Received:{" "}
                              {nodeExec.wait_state.receivedEvents.length}
                            </div>
                          </div>
                        </div>
                      )}
                      {nodeExec.error && (
                        <div className="mt-2 p-2 bg-error-50 border border-error-200 rounded text-xs text-error-700">
                          {nodeExec.error}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}

