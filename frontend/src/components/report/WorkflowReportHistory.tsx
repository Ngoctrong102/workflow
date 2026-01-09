import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { Download, ChevronLeft, ChevronRight, FileText } from "lucide-react"
import { format } from "date-fns"
import { useWorkflowReports } from "@/hooks/use-workflow-report"
import { workflowReportService } from "@/services/workflow-report-service"
import type { ListWorkflowReportsParams } from "@/services/workflow-report-service"

interface WorkflowReportHistoryProps {
  workflowId: string
}

export function WorkflowReportHistory({ workflowId }: WorkflowReportHistoryProps) {
  const [page, setPage] = useState(1)
  const limit = 20

  const params: ListWorkflowReportsParams = {
    workflowId,
    limit,
    offset: (page - 1) * limit,
  }

  const { data, isLoading, error } = useWorkflowReports(params)

  const reports = data?.data || []
  const total = data?.total || 0
  const totalPages = Math.ceil(total / limit)

  const handleDownload = async (reportId: string) => {
    try {
      const blob = await workflowReportService.downloadReport(workflowId, reportId)
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = `report-${reportId}.${blob.type.includes("pdf") ? "pdf" : blob.type.includes("excel") ? "xlsx" : "csv"}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error("Failed to download report:", error)
    }
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Report History</CardTitle>
          <CardDescription>Generated reports for this workflow</CardDescription>
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

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Report History</CardTitle>
          <CardDescription>Generated reports for this workflow</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-error-600">
            <p className="text-sm">Failed to load report history</p>
            <p className="text-xs mt-1">{error instanceof Error ? error.message : "Unknown error"}</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (reports.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Report History</CardTitle>
          <CardDescription>Generated reports for this workflow</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-secondary-500">
            <FileText className="h-12 w-12 mx-auto mb-2 text-secondary-400" />
            <p className="text-sm">No reports generated yet</p>
            <p className="text-xs mt-1">Reports will appear here once they are generated</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Report History</CardTitle>
        <CardDescription>Generated reports for this workflow</CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Period</TableHead>
              <TableHead>Generated At</TableHead>
              <TableHead>Format</TableHead>
              <TableHead>File Size</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {reports.map((report) => (
              <TableRow key={report.id}>
                <TableCell>
                  <div className="text-sm">
                    {format(new Date(report.period.start), "MMM dd, yyyy")} -{" "}
                    {format(new Date(report.period.end), "MMM dd, yyyy")}
                  </div>
                </TableCell>
                <TableCell>
                  {format(new Date(report.generated_at), "MMM dd, yyyy HH:mm:ss")}
                </TableCell>
                <TableCell>
                  <Badge variant="outline" className="uppercase">
                    {report.format}
                  </Badge>
                </TableCell>
                <TableCell>{formatFileSize(report.file_size)}</TableCell>
                <TableCell>
                  {report.delivery_status && (
                    <Badge
                      variant={
                        report.delivery_status === "sent"
                          ? "default"
                          : report.delivery_status === "failed"
                          ? "destructive"
                          : "secondary"
                      }
                    >
                      {report.delivery_status}
                    </Badge>
                  )}
                </TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDownload(report.id)}
                  >
                    <Download className="h-4 w-4 mr-1" />
                    Download
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(page - 1)}
              disabled={page === 1}
            >
              <ChevronLeft className="h-4 w-4 mr-1" />
              Previous
            </Button>
            <span className="text-sm text-secondary-500">
              Page {page} of {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(page + 1)}
              disabled={page === totalPages}
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

