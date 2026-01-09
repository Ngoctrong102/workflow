import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { FileText, Loader2, Clock, Database } from "lucide-react"
import { format } from "date-fns"
import type { ReportPreview as ReportPreviewType } from "@/types/workflow-report"

interface ReportPreviewProps {
  preview?: ReportPreviewType
  isLoading?: boolean
  onGenerate?: () => void
}

export function ReportPreview({ preview, isLoading = false, onGenerate }: ReportPreviewProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Query Preview</CardTitle>
          <CardDescription>Preview query results before scheduling</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-32 w-full" />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!preview) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Query Preview</CardTitle>
          <CardDescription>Preview query results before scheduling</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8">
            <FileText className="h-12 w-12 mx-auto mb-4 text-secondary-400" />
            <p className="text-sm text-secondary-500 mb-4">
              Generate a preview to see the query results
            </p>
            {onGenerate && (
              <Button onClick={onGenerate}>
                <Loader2 className="h-4 w-4 mr-2" />
                Generate Preview
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    )
  }

  const columns = preview.results.length > 0 ? Object.keys(preview.results[0]) : []

  return (
    <Card>
      <CardHeader>
        <CardTitle>Query Preview</CardTitle>
        <CardDescription>Preview query results before scheduling</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Query Info */}
        <div className="border-b pb-4 space-y-2">
          <div className="flex items-center gap-2 text-sm">
            <Database className="h-4 w-4 text-secondary-400" />
            <span className="font-mono text-xs bg-secondary-100 px-2 py-1 rounded">
              {preview.query}
            </span>
          </div>
          <div className="flex items-center gap-4 text-sm text-secondary-600">
            <div className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              <span>Execution time: {preview.execution_time_ms}ms</span>
            </div>
            <Badge variant="outline">{preview.row_count} rows</Badge>
          </div>
        </div>

        {/* Query Parameters */}
        <div className="border-b pb-4">
          <h4 className="text-sm font-medium mb-2">Query Parameters:</h4>
          <div className="space-y-1 text-xs">
            <div className="flex justify-between">
              <span className="text-secondary-600">workflow_id:</span>
              <span className="font-mono">{preview.parameters.workflow_id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-secondary-600">start_date:</span>
              <span className="font-mono">
                {format(new Date(preview.parameters.start_date), "yyyy-MM-dd HH:mm:ss")}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-secondary-600">end_date:</span>
              <span className="font-mono">
                {format(new Date(preview.parameters.end_date), "yyyy-MM-dd HH:mm:ss")}
              </span>
            </div>
          </div>
        </div>

        {/* Query Results Table */}
        {preview.results.length > 0 ? (
          <div className="border rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    {columns.map((col) => (
                      <TableHead key={col} className="font-medium">
                        {col.replace(/_/g, " ")}
                      </TableHead>
                    ))}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {preview.results.slice(0, 100).map((row, idx) => (
                    <TableRow key={idx}>
                      {columns.map((col) => (
                        <TableCell key={col} className="font-mono text-xs">
                          {typeof row[col] === "number"
                            ? row[col].toLocaleString()
                            : typeof row[col] === "string"
                            ? row[col]
                            : row[col] === null
                            ? "null"
                            : JSON.stringify(row[col])}
                        </TableCell>
                      ))}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            {preview.results.length > 100 && (
              <div className="px-4 py-2 bg-secondary-50 text-xs text-secondary-600 text-center border-t">
                Showing first 100 rows of {preview.row_count} total rows
              </div>
            )}
          </div>
        ) : (
          <div className="text-center py-8 text-secondary-500 text-sm">
            No results returned from query
          </div>
        )}
      </CardContent>
    </Card>
  )
}

