import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import type { ChannelPerformanceData } from "@/types/workflow-dashboard"

interface ChannelPerformanceChartProps {
  data: ChannelPerformanceData[]
  isLoading?: boolean
}

export function ChannelPerformanceChart({ data, isLoading = false }: ChannelPerformanceChartProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Channel Performance</CardTitle>
          <CardDescription>Notification delivery by channel</CardDescription>
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
          <CardTitle>Channel Performance</CardTitle>
          <CardDescription>Notification delivery by channel</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center">
            <p className="text-sm text-secondary-500">No channel performance data available</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  // Flatten channels from all data items
  const chartData = data.flatMap((item) =>
    item.channels.map((channel) => ({
      channel: channel.channel,
      sent: channel.sent,
      delivered: channel.delivered,
      failed: channel.sent - channel.delivered,
      deliveryRate: channel.delivery_rate,
    }))
  )

  return (
    <Card>
      <CardHeader>
        <CardTitle>Channel Performance</CardTitle>
        <CardDescription>Notification delivery by channel</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="channel" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="sent" fill="#3b82f6" name="Sent" />
            <Bar dataKey="delivered" fill="#22c55e" name="Delivered" />
            <Bar dataKey="failed" fill="#ef4444" name="Failed" />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}

