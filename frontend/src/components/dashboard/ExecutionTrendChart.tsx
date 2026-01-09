import { memo, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import type { ExecutionTrendData } from "@/types/workflow-dashboard"

interface ExecutionTrendChartProps {
  data: ExecutionTrendData[]
  isLoading?: boolean
}

export const ExecutionTrendChart = memo(function ExecutionTrendChart({ data, isLoading = false }: ExecutionTrendChartProps) {
  // Memoize chart data to prevent unnecessary re-renders
  const chartData = useMemo(() => {
    if (!data || data.length === 0) return []
    return data
  }, [data])
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Execution Trends</CardTitle>
          <CardDescription>Execution count over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center">
            <p className="text-sm text-secondary-500">Loading chart data...</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Execution Trends</CardTitle>
          <CardDescription>Execution count over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center">
            <p className="text-sm text-secondary-500">No execution data available</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution Trends</CardTitle>
        <CardDescription>Execution count over time</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="executions" stroke="#8884d8" name="Total" />
            <Line type="monotone" dataKey="success" stroke="#22c55e" name="Successful" />
            <Line type="monotone" dataKey="failed" stroke="#ef4444" name="Failed" />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
})
