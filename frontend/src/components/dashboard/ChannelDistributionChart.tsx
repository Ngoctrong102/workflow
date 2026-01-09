import { memo, useMemo } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
} from "recharts"

interface ChannelDistributionChartProps {
  data: Array<{
    name: string
    value: number
  }>
}

const COLORS = [
  "#3B82F6", // Primary blue
  "#22C55E", // Success green
  "#F59E0B", // Warning amber
  "#EF4444", // Error red
  "#8B5CF6", // Purple
  "#06B6D4", // Cyan
  "#F97316", // Orange
  "#EC4899", // Pink
]

const RADIAN = Math.PI / 180

const renderCustomLabel = ({
  cx,
  cy,
  midAngle,
  innerRadius,
  outerRadius,
  percent,
}: {
  cx: number
  cy: number
  midAngle: number
  innerRadius: number
  outerRadius: number
  percent: number
}) => {
  if (percent < 0.05) return null // Don't show label for slices < 5%

  const radius = innerRadius + (outerRadius - innerRadius) * 0.5
  const x = cx + radius * Math.cos(-midAngle * RADIAN)
  const y = cy + radius * Math.sin(-midAngle * RADIAN)

  return (
    <text
      x={x}
      y={y}
      fill="white"
      textAnchor={x > cx ? "start" : "end"}
      dominantBaseline="central"
      fontSize={12}
      fontWeight={600}
    >
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  )
}

export const ChannelDistributionChart = memo(function ChannelDistributionChart({ data }: ChannelDistributionChartProps) {
  // Memoize chart data to prevent unnecessary re-renders
  const chartData = useMemo(() => {
    if (!data || data.length === 0) return []
    return data
  }, [data])

  if (!data || data.length === 0) {
    return (
      <Card className="border-slate-200">
        <CardHeader className="px-4 py-3">
          <CardTitle className="text-base font-semibold text-slate-900">Channel Distribution</CardTitle>
          <CardDescription className="text-xs text-slate-600">Notifications by channel type</CardDescription>
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
        <CardTitle className="text-base font-semibold text-slate-900">Channel Distribution</CardTitle>
        <CardDescription className="text-xs text-slate-600 mt-1">Notifications by channel type</CardDescription>
      </CardHeader>
      <CardContent className="px-4 py-4">
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={renderCustomLabel as any}
              outerRadius={90}
              fill="#8884d8"
              dataKey="value"
            >
              {chartData.map((_, index) => (
                <Cell 
                  key={`cell-${index}`} 
                  fill={COLORS[index % COLORS.length]}
                  stroke="#fff"
                  strokeWidth={2}
                />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: "white",
                border: "1px solid #E2E8F0",
                borderRadius: "8px",
                boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
                padding: "8px 12px",
              }}
              formatter={(value: number | undefined) => {
                if (value === undefined) return ["0", "Count"]
                return [value.toLocaleString(), "Count"]
              }}
            />
            <Legend
              wrapperStyle={{ paddingTop: "16px" }}
              iconType="circle"
              formatter={(value) => (
                <span className="text-xs text-slate-600">{value}</span>
              )}
            />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
})
