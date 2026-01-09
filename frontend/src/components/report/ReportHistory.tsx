import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Download, Calendar, FileText, AlertCircle, CheckCircle2, Clock, XCircle } from "lucide-react"
import { useReportHistory } from "@/hooks/use-report-schedules"
import { format } from "date-fns"
import type { ReportData } from "@/types/report"

interface ReportHistoryProps {
  scheduleId?: string
}

export function ReportHistory({ scheduleId }: ReportHistoryProps) {
  const [dateRange, setDateRange] = useState({
    start: format(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), "yyyy-MM-dd"),
    end: format(new Date(), "yyyy-MM-dd"),
  })
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [offset, setOffset] = useState(0)
  const limit = 20

  // Reset offset when filters change
  const handleFilterChange = () => {
    setOffset(0)
  }

  const { data, isLoading, error } = useReportHistory({
    scheduleId,
    start: dateRange.start ? `${dateRange.start}T00:00:00Z` : undefined,
    end: dateRange.end ? `${dateRange.end}T23:59:59Z` : undefined,
    status: statusFilter !== "all" ? (statusFilter as "pending" | "generating" | "completed" | "failed") : undefined,
    limit,
    offset,
  })

  const reports = data?.data || []

  const getStatusIcon = (status: ReportData["status"]) => {
    switch (status) {
      case "completed":
        return <CheckCircle2 className="h-4 w-4 text-green-600" />
      case "failed":
        return <XCircle className="h-4 w-4 text-red-600" />
      case "generating":
        return <Clock className="h-4 w-4 text-yellow-600 animate-pulse" />
      case "pending":
        return <Clock className="h-4 w-4 text-gray-400" />
      default:
        return <AlertCircle className="h-4 w-4 text-gray-400" />
    }
  }

  const getStatusBadge = (status: ReportData["status"]) => {
    const variants: Record<ReportData["status"], "default" | "secondary" | "destructive"> = {
      completed: "default",
      failed: "destructive",
      generating: "secondary",
      pending: "secondary",
    }
    return (
      <Badge variant={variants[status] || "secondary"} className="capitalize">
        {status}
      </Badge>
    )
  }

  const handleDownload = (report: ReportData) => {
    if (report.fileUrl) {
      window.open(report.fileUrl, "_blank")
    }
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Report History</CardTitle>
          <CardDescription>View generated reports</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
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
        <CardHeader>
          <CardTitle>Report History</CardTitle>
          <CardDescription>View generated reports</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-red-600">
            <AlertCircle className="h-8 w-8 mx-auto mb-2" />
            <p className="text-sm">Failed to load report history</p>
            <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Report History</CardTitle>
        <CardDescription>View generated reports and their status</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Filters */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="space-y-2">
            <Label htmlFor="start-date">Start Date</Label>
            <Input
              id="start-date"
              type="date"
              value={dateRange.start}
              onChange={(e) => {
                setDateRange((prev) => ({ ...prev, start: e.target.value }))
                handleFilterChange()
              }}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="end-date">End Date</Label>
            <Input
              id="end-date"
              type="date"
              value={dateRange.end}
              onChange={(e) => {
                setDateRange((prev) => ({ ...prev, end: e.target.value }))
                handleFilterChange()
              }}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="status-filter">Status</Label>
            <Select
              value={statusFilter}
              onValueChange={(value) => {
                setStatusFilter(value)
                handleFilterChange()
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder="All statuses" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="failed">Failed</SelectItem>
                <SelectItem value="generating">Generating</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Report List */}
        {reports.length === 0 ? (
          <div className="text-center py-8 text-secondary-500">
            <FileText className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p className="text-sm">No reports found</p>
            <p className="text-xs mt-1">Reports will appear here once they are generated</p>
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Generated At</TableHead>
                <TableHead>Format</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Error</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {reports.map((report) => (
                <TableRow key={report.id}>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-secondary-400" />
                      <span className="text-sm">
                        {format(new Date(report.generatedAt), "MMM d, yyyy 'at' h:mm a")}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="uppercase">
                      {report.format}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getStatusIcon(report.status)}
                      {getStatusBadge(report.status)}
                    </div>
                  </TableCell>
                  <TableCell>
                    {report.error ? (
                      <div className="flex items-center gap-2 text-sm text-red-600 max-w-xs">
                        <AlertCircle className="h-4 w-4 flex-shrink-0" />
                        <span className="truncate">{report.error}</span>
                      </div>
                    ) : report.deliveryStatus ? (
                      <div className="flex flex-col gap-1 text-xs">
                        <div className="flex items-center gap-2">
                          <CheckCircle2 className="h-3 w-3 text-green-600" />
                          <span>{report.deliveryStatus.delivered}/{report.deliveryStatus.totalRecipients} delivered</span>
                        </div>
                        {report.deliveryStatus.failed > 0 && (
                          <div className="flex items-center gap-2 text-red-600">
                            <XCircle className="h-3 w-3" />
                            <span>{report.deliveryStatus.failed} failed</span>
                          </div>
                        )}
                      </div>
                    ) : (
                      <span className="text-sm text-secondary-400">-</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    {report.status === "completed" && report.fileUrl ? (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDownload(report)}
                        className="h-8"
                      >
                        <Download className="h-4 w-4 mr-2" />
                        Download
                      </Button>
                    ) : (
                      <span className="text-sm text-secondary-400">-</span>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}

        {/* Pagination */}
        {data && data.total > limit && (
          <div className="flex items-center justify-between text-sm text-secondary-600">
            <div>
              Showing {offset + 1} to {Math.min(offset + limit, data.total)} of {data.total} reports
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={offset === 0}
                onClick={() => setOffset(Math.max(0, offset - limit))}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={offset + limit >= data.total}
                onClick={() => setOffset(offset + limit)}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

