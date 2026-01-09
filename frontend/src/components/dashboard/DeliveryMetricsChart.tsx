import { memo, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts"

interface DeliveryMetricsChartProps {
  data: Array<{
    channel: string
    sent: number
    delivered: number
    failed: number
  }>
}

const COLORS = {
  sent: "#3B82F6", // Primary blue
  delivered: "#22C55E", // Success green
  failed: "#EF4444", // Error red
}

export const DeliveryMetricsChart = memo(function DeliveryMetricsChart({ data }: DeliveryMetricsChartProps) {
  // Memoize chart data to prevent unnecessary re-renders
  const chartData = useMemo(() => {
    if (!data || data.length === 0) return []
    return data
  }, [data])
  if (!data || data.length === 0) {
    return (
      <Card className="border-slate-200">
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base font-semibold text-slate-900">Delivery Metrics</CardTitle>
          <CardDescription className="text-xs text-slate-600">Notifications by channel</CardDescription>
        </CardHeader>
        <CardContent className="px-4 pb-3">
          <div className="flex items-center justify-center h-[240px] text-slate-500 text-sm">
            No data available
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-slate-200 shadow-sm">
      <CardHeader className="px-4 py-3 border-b border-slate-100">
        <CardTitle className="text-base font-semibold text-slate-900">Delivery Metrics</CardTitle>
        <CardDescription className="text-xs text-slate-600 mt-1">Notifications by channel</CardDescription>
      </CardHeader>
      <CardContent className="px-4 py-4">
        <ResponsiveContainer width="100%" height={280}>
          <BarChart 
            data={chartData}
            margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
            <XAxis
              dataKey="channel"
              stroke="#64748B"
              fontSize={12}
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#64748B" }}
            />
            <YAxis 
              stroke="#64748B" 
              fontSize={12} 
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#64748B" }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: "white",
                border: "1px solid #E2E8F0",
                borderRadius: "8px",
                boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
                padding: "8px 12px",
              }}
              cursor={{ fill: "rgba(59, 130, 246, 0.1)" }}
            />
            <Legend 
              wrapperStyle={{ paddingTop: "16px" }}
              iconType="circle"
              formatter={(value) => (
                <span className="text-xs text-slate-600 capitalize">{value}</span>
              )}
            />
            <Bar 
              dataKey="sent" 
              fill={COLORS.sent} 
              name="Sent"
              radius={[4, 4, 0, 0]}
            />
            <Bar 
              dataKey="delivered" 
              fill={COLORS.delivered} 
              name="Delivered"
              radius={[4, 4, 0, 0]}
            />
            <Bar 
              dataKey="failed" 
              fill={COLORS.failed} 
              name="Failed"
              radius={[4, 4, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
})
