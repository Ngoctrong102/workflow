import { useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Clock } from "lucide-react"
import { format, subDays, subHours } from "date-fns"

export type TimeRangePreset = "24h" | "7d" | "30d" | "90d" | "custom"

export interface TimeRange {
  start: string
  end: string
  preset: TimeRangePreset
}

interface TimeRangeSelectorProps {
  value: TimeRange
  onChange: (range: TimeRange) => void
  className?: string
}

export function TimeRangeSelector({ value, onChange, className }: TimeRangeSelectorProps) {
  const [preset, setPreset] = useState<TimeRangePreset>(value.preset || "7d")

  const handlePresetChange = (newPreset: TimeRangePreset) => {
    setPreset(newPreset)
    const now = new Date()
    let start: Date
    let end: Date = now

    switch (newPreset) {
      case "24h":
        start = subHours(now, 24)
        break
      case "7d":
        start = subDays(now, 7)
        break
      case "30d":
        start = subDays(now, 30)
        break
      case "90d":
        start = subDays(now, 90)
        break
      case "custom":
        // Keep current range for custom
        onChange({ ...value, preset: "custom" })
        return
      default:
        start = subDays(now, 7)
    }

    onChange({
      start: format(start, "yyyy-MM-dd"),
      end: format(end, "yyyy-MM-dd"),
      preset: newPreset,
    })
  }

  const handleDateChange = (field: "start" | "end", dateValue: string) => {
    onChange({
      ...value,
      [field]: dateValue,
      preset: "custom",
    })
    setPreset("custom")
  }

  return (
    <Card className={className}>
      <CardContent className="p-4">
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-secondary-500" />
            <Label className="text-sm font-medium">Time Range</Label>
          </div>
          <Select value={preset} onValueChange={handlePresetChange}>
            <SelectTrigger className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="24h">Last 24 Hours</SelectItem>
              <SelectItem value="7d">Last 7 Days</SelectItem>
              <SelectItem value="30d">Last 30 Days</SelectItem>
              <SelectItem value="90d">Last 90 Days</SelectItem>
              <SelectItem value="custom">Custom Range</SelectItem>
            </SelectContent>
          </Select>
          {preset === "custom" && (
            <div className="grid grid-cols-2 gap-2">
              <div className="space-y-1">
                <Label htmlFor="start-date" className="text-xs">
                  Start Date
                </Label>
                <input
                  id="start-date"
                  type="date"
                  value={value.start}
                  onChange={(e) => handleDateChange("start", e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-secondary-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="end-date" className="text-xs">
                  End Date
                </Label>
                <input
                  id="end-date"
                  type="date"
                  value={value.end}
                  onChange={(e) => handleDateChange("end", e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-secondary-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                />
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

