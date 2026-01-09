import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Download, TrendingUp, Calendar } from "lucide-react"
import { exportToCSV, exportToExcel, exportToPDF } from "@/utils/export"
import type { DeliveryAnalytics, ErrorAnalytics } from "@/types/analytics"

interface AdvancedAnalyticsPanelProps {
  deliveryData?: DeliveryAnalytics[]
  errorData?: ErrorAnalytics[]
  onDateRangeChange?: (range: { start: string; end: string }) => void
  onComparisonToggle?: (enabled: boolean) => void
}

export function AdvancedAnalyticsPanel({
  deliveryData = [],
  errorData: _errorData = [],
  onDateRangeChange,
  onComparisonToggle,
}: AdvancedAnalyticsPanelProps) {
  const [comparisonMode, setComparisonMode] = useState(false)
  const [exportFormat, setExportFormat] = useState<"csv" | "excel" | "pdf">("csv")
  const [datePreset, setDatePreset] = useState<string>("custom")

  const handleDatePresetChange = (preset: string) => {
    setDatePreset(preset)
    const today = new Date()
    let start: Date
    let end: Date = today

    switch (preset) {
      case "today":
        start = today
        break
      case "yesterday":
        start = new Date(today)
        start.setDate(start.getDate() - 1)
        end = new Date(start)
        break
      case "last7days":
        start = new Date(today)
        start.setDate(start.getDate() - 7)
        break
      case "last30days":
        start = new Date(today)
        start.setDate(start.getDate() - 30)
        break
      case "last90days":
        start = new Date(today)
        start.setDate(start.getDate() - 90)
        break
      case "thisMonth":
        start = new Date(today.getFullYear(), today.getMonth(), 1)
        break
      case "lastMonth":
        start = new Date(today.getFullYear(), today.getMonth() - 1, 1)
        end = new Date(today.getFullYear(), today.getMonth(), 0)
        break
      default:
        return
    }

    if (onDateRangeChange) {
      onDateRangeChange({
        start: start.toISOString().split("T")[0],
        end: end.toISOString().split("T")[0],
      })
    }
  }

  const handleExport = () => {
    const columns = [
      { key: "date", label: "Date" },
      { key: "sent", label: "Sent" },
      { key: "delivered", label: "Delivered" },
      { key: "failed", label: "Failed" },
    ]

    const data = deliveryData.map((item) => ({
      date: item.period.start,
      sent: item.totalSent,
      delivered: item.delivered,
      failed: item.failed,
    }))

    const filename = `analytics-${new Date().toISOString().split("T")[0]}`

    switch (exportFormat) {
      case "csv":
        exportToCSV(data, columns, `${filename}.csv`)
        break
      case "excel":
        exportToExcel(data, columns, `${filename}.xlsx`)
        break
      case "pdf":
        exportToPDF(data, columns, `${filename}.pdf`)
        break
    }
  }

  const handleComparisonToggle = (enabled: boolean) => {
    setComparisonMode(enabled)
    if (onComparisonToggle) {
      onComparisonToggle(enabled)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Advanced Analytics</CardTitle>
        <CardDescription>Customize your analytics view and export reports</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="date-range" className="space-y-4">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="date-range">
              <Calendar className="h-4 w-4 mr-2" />
              Date Range
            </TabsTrigger>
            <TabsTrigger value="comparison">
              <TrendingUp className="h-4 w-4 mr-2" />
              Comparison
            </TabsTrigger>
            <TabsTrigger value="export">
              <Download className="h-4 w-4 mr-2" />
              Export
            </TabsTrigger>
          </TabsList>

          <TabsContent value="date-range" className="space-y-4">
            <div className="space-y-2">
              <Label>Quick Date Presets</Label>
              <Select value={datePreset} onValueChange={handleDatePresetChange}>
                <SelectTrigger>
                  <SelectValue placeholder="Select date range" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="today">Today</SelectItem>
                  <SelectItem value="yesterday">Yesterday</SelectItem>
                  <SelectItem value="last7days">Last 7 Days</SelectItem>
                  <SelectItem value="last30days">Last 30 Days</SelectItem>
                  <SelectItem value="last90days">Last 90 Days</SelectItem>
                  <SelectItem value="thisMonth">This Month</SelectItem>
                  <SelectItem value="lastMonth">Last Month</SelectItem>
                  <SelectItem value="custom">Custom Range</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </TabsContent>

          <TabsContent value="comparison" className="space-y-4">
            <div className="space-y-2">
              <Label>Period Comparison</Label>
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="comparison-mode"
                  checked={comparisonMode}
                  onChange={(e) => handleComparisonToggle(e.target.checked)}
                  className="h-4 w-4 rounded border-secondary-300"
                />
                <Label htmlFor="comparison-mode" className="cursor-pointer">
                  Enable comparison mode to compare with previous period
                </Label>
              </div>
              {comparisonMode && (
                <p className="text-xs text-secondary-500">
                  Compare current period metrics with the previous period of the same length
                </p>
              )}
            </div>
          </TabsContent>

          <TabsContent value="export" className="space-y-4">
            <div className="space-y-2">
              <Label>Export Format</Label>
              <Select value={exportFormat} onValueChange={(v) => setExportFormat(v as "csv" | "excel" | "pdf")}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="csv">CSV</SelectItem>
                  <SelectItem value="excel">Excel (XLSX)</SelectItem>
                  <SelectItem value="pdf">PDF</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button onClick={handleExport} className="w-full">
              <Download className="h-4 w-4 mr-2" />
              Export Analytics Data
            </Button>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  )
}

