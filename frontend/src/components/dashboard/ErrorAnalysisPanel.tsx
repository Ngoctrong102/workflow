import { useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts"
import { AlertTriangle } from "lucide-react"
import type { ErrorAnalysis } from "@/types/workflow-dashboard"

interface ErrorAnalysisPanelProps {
  errorAnalysis?: ErrorAnalysis
  isLoading?: boolean
}

export function ErrorAnalysisPanel({ errorAnalysis, isLoading = false }: ErrorAnalysisPanelProps) {
  const errorsByType = useMemo(() => {
    if (!errorAnalysis?.errors) return []
    const grouped = errorAnalysis.errors.reduce((acc, error) => {
      acc[error.error_type] = (acc[error.error_type] || 0) + 1
      return acc
    }, {} as Record<string, number>)
    const totalErrors = errorAnalysis.errors.length
    return Object.entries(grouped).map(([type, count]) => ({
      type,
      count,
      percentage: count / totalErrors,
    }))
  }, [errorAnalysis?.errors])

  const errorsByNode = useMemo(() => {
    if (!errorAnalysis?.errors) return []
    const grouped = errorAnalysis.errors.reduce((acc, error) => {
      // Extract node info from execution_id or error_message if available
      const nodeId = error.execution_id || "unknown"
      if (!acc[nodeId]) {
        acc[nodeId] = {
          nodeId,
          nodeName: nodeId,
          count: 0,
        }
      }
      acc[nodeId].count++
      return acc
    }, {} as Record<string, { nodeId: string; nodeName: string; count: number }>)
    const totalErrors = errorAnalysis.errors.length
    return Object.values(grouped).map((node) => ({
      ...node,
      percentage: node.count / totalErrors,
    }))
  }, [errorAnalysis?.errors])

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Error Analysis</CardTitle>
          <CardDescription>Error metrics and distribution</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="h-32 bg-secondary-100 rounded animate-pulse" />
            <div className="h-64 bg-secondary-100 rounded animate-pulse" />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!errorAnalysis) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Error Analysis</CardTitle>
          <CardDescription>Error metrics and distribution</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-secondary-500">
            <AlertTriangle className="h-12 w-12 mx-auto mb-2 text-success-400" />
            <p className="text-sm">No errors found</p>
            <p className="text-xs mt-1">Great! No errors detected in the selected period</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      {/* Error Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Error Summary</CardTitle>
          <CardDescription>Overall error metrics</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-secondary-500">Total Errors</p>
              <p className="text-2xl font-bold">{errorAnalysis.summary.total_errors.toLocaleString()}</p>
            </div>
            <div>
              <p className="text-sm text-secondary-500">Error Rate</p>
              <p className="text-2xl font-bold">{(errorAnalysis.summary.error_rate * 100).toFixed(2)}%</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Error Timeline */}
      {errorAnalysis.errorTimeline && errorAnalysis.errorTimeline.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Error Timeline</CardTitle>
            <CardDescription>Errors over time</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={250}>
              <LineChart data={errorAnalysis.errorTimeline}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="count" stroke="#ef4444" name="Errors" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {/* Errors by Type */}
      {errorsByType.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Errors by Type</CardTitle>
            <CardDescription>Error distribution by error type</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Error Type</TableHead>
                  <TableHead>Count</TableHead>
                  <TableHead>Percentage</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {errorsByType.map((error, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-medium">{error.type}</TableCell>
                    <TableCell>{error.count.toLocaleString()}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{(error.percentage * 100).toFixed(1)}%</Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Errors by Node */}
      {errorsByNode.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Errors by Node</CardTitle>
            <CardDescription>Error distribution by workflow node</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Node</TableHead>
                  <TableHead>Count</TableHead>
                  <TableHead>Percentage</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {errorsByNode.map((error, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{error.nodeName}</p>
                        <p className="text-xs text-secondary-500">{error.nodeId}</p>
                      </div>
                    </TableCell>
                    <TableCell>{error.count.toLocaleString()}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{(error.percentage * 100).toFixed(1)}%</Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

