import { Controller } from "react-hook-form"
import type { Control } from "react-hook-form"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import type { PeriodType } from "@/types/workflow-report"

interface PeriodTypeSelectorProps {
  control: Control<any>
  periodType: PeriodType
  periodStartDate?: string | null
  periodEndDate?: string | null
  error?: string
}

export function PeriodTypeSelector({
  control,
  periodType,
  periodStartDate: _periodStartDate,
  periodEndDate: _periodEndDate,
  error,
}: PeriodTypeSelectorProps) {
  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="period-type">Period Type *</Label>
        <Controller
          name="period_type"
          control={control}
          rules={{ required: "Period type is required" }}
          render={({ field }) => (
            <Select value={field.value} onValueChange={field.onChange}>
              <SelectTrigger>
                <SelectValue placeholder="Select period type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="last_24h">Last 24 hours</SelectItem>
                <SelectItem value="last_7d">Last 7 days</SelectItem>
                <SelectItem value="last_30d">Last 30 days</SelectItem>
                <SelectItem value="last_90d">Last 90 days</SelectItem>
                <SelectItem value="custom">Custom period</SelectItem>
              </SelectContent>
            </Select>
          )}
        />
        {error && <p className="text-sm text-error-600">{error}</p>}
      </div>

      {periodType === "custom" && (
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="period-start-date">Start Date *</Label>
            <Controller
              name="period_start_date"
              control={control}
              rules={{
                required: periodType === "custom" ? "Start date is required for custom period" : false,
              }}
              render={({ field }) => (
                <Input
                  id="period-start-date"
                  type="datetime-local"
                  value={field.value ? new Date(field.value).toISOString().slice(0, 16) : ""}
                  onChange={(e) => {
                    const date = e.target.value ? new Date(e.target.value).toISOString() : null
                    field.onChange(date)
                  }}
                />
              )}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="period-end-date">End Date *</Label>
            <Controller
              name="period_end_date"
              control={control}
              rules={{
                required: periodType === "custom" ? "End date is required for custom period" : false,
              }}
              render={({ field }) => (
                <Input
                  id="period-end-date"
                  type="datetime-local"
                  value={field.value ? new Date(field.value).toISOString().slice(0, 16) : ""}
                  onChange={(e) => {
                    const date = e.target.value ? new Date(e.target.value).toISOString() : null
                    field.onChange(date)
                  }}
                />
              )}
            />
          </div>
        </div>
      )}
    </div>
  )
}

