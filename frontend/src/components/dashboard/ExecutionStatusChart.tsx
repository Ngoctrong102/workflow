import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"
import type { ExecutionStatusDistribution } from "@/types/workflow-dashboard"

interface ExecutionStatusChartProps {
  data: ExecutionStatusDistribution[]
  isLoading?: boolean
}

const COLORS = {
  success: "#22c55e",
  failed: "#ef4444",
  running: "#3b82f6",
  cancelled: "#6b7280",
}

export function ExecutionStatusChart({ data, isLoading = false }: ExecutionStatusChartProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Execution Status Distribution</CardTitle>
          <CardDescription>Status breakdown of executions</CardDescription>
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
          <CardTitle>Execution Status Distribution</CardTitle>
          <CardDescription>Status breakdown of executions</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center">
            <p className="text-sm text-secondary-500">No status data available</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  const chartData = data.map((item) => ({
    name: item.status.charAt(0).toUpperCase() + item.status.slice(1),
    value: item.count,
    percentage: item.percentage,
    color: COLORS[item.status] || "#6b7280",
  }))

  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution Status Distribution</CardTitle>
        <CardDescription>Status breakdown of executions</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, percent }: any) => `${name}: ${(percent * 100).toFixed(1)}%`}
              outerRadius={80}
              fill="#8884d8"
              dataKey="value"
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}

