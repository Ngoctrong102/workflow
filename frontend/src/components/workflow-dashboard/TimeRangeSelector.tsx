import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { CalendarIcon } from "lucide-react"
import { format } from "date-fns"
import { useState } from "react"
import { cn } from "@/lib/utils"

export type TimeRangePreset = "24h" | "7d" | "30d" | "90d" | "custom"

export interface TimeRange {
  preset: TimeRangePreset
  startDate?: Date
  endDate?: Date
}

interface TimeRangeSelectorProps {
  value: TimeRange
  onChange: (range: TimeRange) => void
  className?: string
}

export function TimeRangeSelector({ value, onChange, className }: TimeRangeSelectorProps) {
  const [isCustomOpen, setIsCustomOpen] = useState(false)

  const handlePresetChange = (preset: TimeRangePreset) => {
    if (preset === "custom") {
      setIsCustomOpen(true)
      return
    }

    const endDate = new Date()
    const startDate = new Date()

    switch (preset) {
      case "24h":
        startDate.setHours(startDate.getHours() - 24)
        break
      case "7d":
        startDate.setDate(startDate.getDate() - 7)
        break
      case "30d":
        startDate.setDate(startDate.getDate() - 30)
        break
      case "90d":
        startDate.setDate(startDate.getDate() - 90)
        break
    }

    onChange({ preset, startDate, endDate })
  }

  const handleCustomDateChange = (startDate: Date | undefined, endDate: Date | undefined) => {
    if (startDate && endDate) {
      onChange({ preset: "custom", startDate, endDate })
      setIsCustomOpen(false)
    }
  }

  return (
    <div className={cn("flex items-center gap-2", className)}>
      <Select value={value.preset} onValueChange={handlePresetChange}>
        <SelectTrigger className="w-[140px]">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="24h">Last 24 hours</SelectItem>
          <SelectItem value="7d">Last 7 days</SelectItem>
          <SelectItem value="30d">Last 30 days</SelectItem>
          <SelectItem value="90d">Last 90 days</SelectItem>
          <SelectItem value="custom">Custom range</SelectItem>
        </SelectContent>
      </Select>

      {value.preset === "custom" && (
        <div className="flex items-center gap-2">
          <Input
            type="date"
            value={value.startDate ? format(value.startDate, "yyyy-MM-dd") : ""}
            onChange={(e) => {
              const startDate = e.target.value ? new Date(e.target.value) : undefined
              onChange({ ...value, startDate })
            }}
            className="w-[140px]"
          />
          <span className="text-secondary-500">to</span>
          <Input
            type="date"
            value={value.endDate ? format(value.endDate, "yyyy-MM-dd") : ""}
            onChange={(e) => {
              const endDate = e.target.value ? new Date(e.target.value) : undefined
              if (endDate && value.startDate) {
                onChange({ ...value, endDate })
              }
            }}
            className="w-[140px]"
          />
        </div>
      )}
    </div>
  )
}

