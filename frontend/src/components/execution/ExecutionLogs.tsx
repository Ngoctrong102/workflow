import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { Download, AlertCircle, Info, AlertTriangle, Bug } from "lucide-react"
import { useExecutionLogs } from "@/hooks/use-executions"
import { formatDistanceToNow } from "date-fns"
import { exportToJSON, exportToCSV } from "@/utils/export"
import { cn } from "@/lib/utils"

interface ExecutionLogsProps {
  executionId: string
}

const logLevelConfig = {
  info: {
    label: "Info",
    color: "text-primary-600",
    bgColor: "bg-primary-50",
    icon: Info,
  },
  warning: {
    label: "Warning",
    color: "text-warning-600",
    bgColor: "bg-warning-50",
    icon: AlertTriangle,
  },
  error: {
    label: "Error",
    color: "text-error-600",
    bgColor: "bg-error-50",
    icon: AlertCircle,
  },
  debug: {
    label: "Debug",
    color: "text-secondary-600",
    bgColor: "bg-secondary-50",
    icon: Bug,
  },
}

export function ExecutionLogs({ executionId }: ExecutionLogsProps) {
  const [levelFilter, setLevelFilter] = useState<string>("all")
  const [nodeFilter, setNodeFilter] = useState<string>("all")
  
  // Use executionService directly to support query params
  const { data: logs, isLoading, error } = useExecutionLogs(executionId)

  const filteredLogs = logs?.filter((log) => {
    if (levelFilter !== "all" && log.level !== levelFilter) return false
    if (nodeFilter !== "all" && log.node_id !== nodeFilter) return false
    return true
  }) || []

  const uniqueNodes = Array.from(new Set(logs?.map((log) => log.node_id).filter(Boolean) || []))

  const handleExport = (format: "json" | "csv") => {
    if (!logs) return

    const exportData = filteredLogs.map((log) => ({
      timestamp: log.timestamp,
      level: log.level,
      node_id: log.node_id || "",
      message: log.message,
      data: JSON.stringify(log.data || {}),
    }))

    if (format === "json") {
      exportToJSON(exportData as Record<string, unknown>[], `execution-${executionId}-logs.json`)
    } else {
      exportToCSV(exportData, [
        { key: "timestamp", label: "Timestamp" },
        { key: "level", label: "Level" },
        { key: "node_id", label: "Node ID" },
        { key: "message", label: "Message" },
        { key: "data", label: "Data" },
      ], `execution-${executionId}-logs.csv`)
    }
  }

  if (isLoading) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
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
          <p className="text-error-600">Failed to load logs</p>
          <p className="text-sm mt-2 text-secondary-500">
            {error instanceof Error ? error.message : "Unknown error"}
          </p>
        </CardContent>
      </Card>
    )
  }

  if (!logs || logs.length === 0) {
    return (
      <Card>
        <CardContent className="py-12 text-center text-secondary-500">
          <p>No logs available</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Execution Logs</CardTitle>
            <CardDescription>{filteredLogs.length} log entries</CardDescription>
          </div>
          <div className="flex items-center space-x-2">
            <Button variant="outline" size="sm" onClick={() => handleExport("json")}>
              <Download className="h-4 w-4 mr-2" />
              Export JSON
            </Button>
            <Button variant="outline" size="sm" onClick={() => handleExport("csv")}>
              <Download className="h-4 w-4 mr-2" />
              Export CSV
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {/* Filters */}
        <div className="flex items-center space-x-4 mb-4">
          <Select value={levelFilter} onValueChange={setLevelFilter}>
            <SelectTrigger className="w-48">
              <SelectValue placeholder="Filter by level" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Levels</SelectItem>
              <SelectItem value="info">Info</SelectItem>
              <SelectItem value="warning">Warning</SelectItem>
              <SelectItem value="error">Error</SelectItem>
              <SelectItem value="debug">Debug</SelectItem>
            </SelectContent>
          </Select>
          {uniqueNodes.length > 0 && (
            <Select value={nodeFilter} onValueChange={setNodeFilter}>
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Filter by node" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Nodes</SelectItem>
                {uniqueNodes.map((nodeId) => (
                  <SelectItem key={nodeId} value={nodeId || ""}>
                    {nodeId || "Unknown"}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>

        {/* Logs */}
        <div className="space-y-2 max-h-96 overflow-y-auto">
          {filteredLogs.map((log) => {
            const level = logLevelConfig[log.level as keyof typeof logLevelConfig] || logLevelConfig.info
            const Icon = level.icon

            return (
              <div
                key={log.id}
                className={cn(
                  "p-3 rounded-lg border text-sm",
                  level.bgColor,
                  "border-secondary-200"
                )}
              >
                <div className="flex items-start space-x-2">
                  <Icon className={cn("h-4 w-4 mt-0.5 flex-shrink-0", level.color)} />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2 mb-1">
                      <Badge variant="outline" className={cn("text-xs", level.color)}>
                        {level.label}
                      </Badge>
                      {log.node_id && (
                        <Badge variant="secondary" className="text-xs">
                          {log.node_id}
                        </Badge>
                      )}
                      <span className="text-xs text-secondary-500">
                        {formatDistanceToNow(new Date(log.timestamp), { addSuffix: true })}
                      </span>
                    </div>
                    <div className="text-secondary-700">{log.message}</div>
                    {log.data && Object.keys(log.data).length > 0 && (
                      <pre className="mt-2 text-xs bg-secondary-100 p-2 rounded overflow-x-auto">
                        {JSON.stringify(log.data, null, 2)}
                      </pre>
                    )}
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

