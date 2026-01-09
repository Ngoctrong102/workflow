import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { ExternalLink, ChevronLeft, ChevronRight } from "lucide-react"
import { format } from "date-fns"

interface Execution {
  id: string
  startedAt: string
  endedAt?: string
  duration?: number
  status: "success" | "failed" | "running" | "cancelled"
  notificationsSent?: number
}

interface ExecutionHistoryTableProps {
  executions?: Execution[]
  isLoading?: boolean
  onPageChange?: (page: number) => void
  currentPage?: number
  totalPages?: number
}

export function ExecutionHistoryTable({
  executions = [],
  isLoading = false,
  onPageChange,
  currentPage = 1,
  totalPages = 1,
}: ExecutionHistoryTableProps) {
  const navigate = useNavigate()

  const formatDuration = (seconds?: number) => {
    if (!seconds) return "-"
    if (seconds < 60) return `${seconds.toFixed(2)}s`
    if (seconds < 3600) return `${(seconds / 60).toFixed(2)}m`
    return `${(seconds / 3600).toFixed(2)}h`
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Execution History</CardTitle>
          <CardDescription>Recent workflow executions</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (executions.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Execution History</CardTitle>
          <CardDescription>Recent workflow executions</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-secondary-500">
            <p className="text-sm">No executions found</p>
            <p className="text-xs mt-1">This workflow hasn't been executed yet</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution History</CardTitle>
        <CardDescription>Recent workflow executions</CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Start Time</TableHead>
              <TableHead>End Time</TableHead>
              <TableHead>Duration</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Notifications</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {executions.map((execution) => (
              <TableRow
                key={execution.id}
                className="cursor-pointer hover:bg-secondary-50"
                onClick={() => navigate(`/executions/${execution.id}`)}
              >
                <TableCell className="font-mono text-xs">{execution.id.slice(0, 8)}...</TableCell>
                <TableCell>
                  {format(new Date(execution.startedAt), "MMM dd, yyyy HH:mm:ss")}
                </TableCell>
                <TableCell>
                  {execution.endedAt
                    ? format(new Date(execution.endedAt), "MMM dd, yyyy HH:mm:ss")
                    : "-"}
                </TableCell>
                <TableCell>{formatDuration(execution.duration)}</TableCell>
                <TableCell>
                  <Badge
                    variant={
                      execution.status === "success"
                        ? "default"
                        : execution.status === "failed"
                        ? "destructive"
                        : "secondary"
                    }
                  >
                    {execution.status}
                  </Badge>
                </TableCell>
                <TableCell>{execution.notificationsSent?.toLocaleString() || "-"}</TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation()
                      navigate(`/executions/${execution.id}`)
                    }}
                  >
                    <ExternalLink className="h-4 w-4" />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {totalPages > 1 && onPageChange && (
          <div className="flex items-center justify-between mt-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="h-4 w-4 mr-1" />
              Previous
            </Button>
            <span className="text-sm text-secondary-500">
              Page {currentPage} of {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
              <ChevronRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

