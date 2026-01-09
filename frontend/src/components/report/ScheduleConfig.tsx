import { Controller, useFormContext } from "react-hook-form"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Clock, Calendar, Info } from "lucide-react"
import type { ReportScheduleType, ScheduleConfig } from "@/types/workflow-report"

interface ScheduleConfigProps {
  scheduleType: ReportScheduleType
  error?: string
}

const DAYS_OF_WEEK = [
  { value: 0, label: "Sunday" },
  { value: 1, label: "Monday" },
  { value: 2, label: "Tuesday" },
  { value: 3, label: "Wednesday" },
  { value: 4, label: "Thursday" },
  { value: 5, label: "Friday" },
  { value: 6, label: "Saturday" },
]

const TIMEZONES = [
  "UTC",
  "America/New_York",
  "America/Chicago",
  "America/Denver",
  "America/Los_Angeles",
  "Europe/London",
  "Europe/Paris",
  "Europe/Berlin",
  "Asia/Tokyo",
  "Asia/Shanghai",
  "Australia/Sydney",
]

export function ScheduleConfigComponent({ scheduleType, error }: ScheduleConfigProps) {
  const { control, watch, formState: { errors } } = useFormContext<{
    scheduleConfig: ScheduleConfig
  }>()

  const scheduleConfig = watch("scheduleConfig")

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label>Schedule Configuration *</Label>
        {scheduleType === "daily" && (
          <Card>
            <CardContent className="p-4 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="daily-time">Time *</Label>
                <Controller
                  name="scheduleConfig.time"
                  control={control}
                  rules={{ required: "Time is required" }}
                  render={({ field }) => (
                    <Input
                      id="daily-time"
                      type="time"
                      {...field}
                      className="w-full"
                    />
                  )}
                />
                {errors.scheduleConfig?.time && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.time.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="daily-timezone">Timezone *</Label>
                <Controller
                  name="scheduleConfig.timezone"
                  control={control}
                  rules={{ required: "Timezone is required" }}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select timezone" />
                      </SelectTrigger>
                      <SelectContent>
                        {TIMEZONES.map((tz) => (
                          <SelectItem key={tz} value={tz}>
                            {tz}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.scheduleConfig?.timezone && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.timezone.message as string}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {scheduleType === "weekly" && (
          <Card>
            <CardContent className="p-4 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="weekly-day">Day of Week *</Label>
                <Controller
                  name="scheduleConfig.dayOfWeek"
                  control={control}
                  rules={{ required: "Day of week is required" }}
                  render={({ field }) => (
                    <Select
                      value={field.value?.toString()}
                      onValueChange={(v) => field.onChange(parseInt(v))}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select day" />
                      </SelectTrigger>
                      <SelectContent>
                        {DAYS_OF_WEEK.map((day) => (
                          <SelectItem key={day.value} value={day.value.toString()}>
                            {day.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.scheduleConfig?.dayOfWeek && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.dayOfWeek.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="weekly-time">Time *</Label>
                <Controller
                  name="scheduleConfig.time"
                  control={control}
                  rules={{ required: "Time is required" }}
                  render={({ field }) => (
                    <Input
                      id="weekly-time"
                      type="time"
                      {...field}
                      className="w-full"
                    />
                  )}
                />
                {errors.scheduleConfig?.time && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.time.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="weekly-timezone">Timezone *</Label>
                <Controller
                  name="scheduleConfig.timezone"
                  control={control}
                  rules={{ required: "Timezone is required" }}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select timezone" />
                      </SelectTrigger>
                      <SelectContent>
                        {TIMEZONES.map((tz) => (
                          <SelectItem key={tz} value={tz}>
                            {tz}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.scheduleConfig?.timezone && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.timezone.message as string}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {scheduleType === "monthly" && (
          <Card>
            <CardContent className="p-4 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="monthly-day">Day of Month *</Label>
                <Controller
                  name="scheduleConfig.dayOfMonth"
                  control={control}
                  rules={{
                    required: "Day of month is required",
                    min: { value: 1, message: "Day must be between 1 and 31" },
                    max: { value: 31, message: "Day must be between 1 and 31" },
                  }}
                  render={({ field }) => (
                    <Input
                      id="monthly-day"
                      type="number"
                      min={1}
                      max={31}
                      {...field}
                      value={field.value || ""}
                      onChange={(e) => field.onChange(parseInt(e.target.value) || undefined)}
                      className="w-full"
                    />
                  )}
                />
                {errors.scheduleConfig?.dayOfMonth && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.dayOfMonth.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="monthly-time">Time *</Label>
                <Controller
                  name="scheduleConfig.time"
                  control={control}
                  rules={{ required: "Time is required" }}
                  render={({ field }) => (
                    <Input
                      id="monthly-time"
                      type="time"
                      {...field}
                      className="w-full"
                    />
                  )}
                />
                {errors.scheduleConfig?.time && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.time.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="monthly-timezone">Timezone *</Label>
                <Controller
                  name="scheduleConfig.timezone"
                  control={control}
                  rules={{ required: "Timezone is required" }}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select timezone" />
                      </SelectTrigger>
                      <SelectContent>
                        {TIMEZONES.map((tz) => (
                          <SelectItem key={tz} value={tz}>
                            {tz}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.scheduleConfig?.timezone && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.timezone.message as string}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {scheduleType === "custom" && (
          <Card>
            <CardContent className="p-4 space-y-4">
              <Alert>
                <Info className="h-4 w-4" />
                <AlertDescription>
                  Enter a valid cron expression. Format: minute hour day month weekday
                  <br />
                  Example: "0 9 * * 1-5" (9 AM every weekday)
                </AlertDescription>
              </Alert>
              <div className="space-y-2">
                <Label htmlFor="custom-cron">Cron Expression *</Label>
                <Controller
                  name="scheduleConfig.cronExpression"
                  control={control}
                  rules={{
                    required: "Cron expression is required",
                    validate: (value) => {
                      if (!value) return "Cron expression is required"
                      // Basic cron validation (5 fields)
                      const parts = value.trim().split(/\s+/)
                      if (parts.length !== 5) {
                        return "Cron expression must have 5 fields: minute hour day month weekday"
                      }
                      return true
                    },
                  }}
                  render={({ field }) => (
                    <Input
                      id="custom-cron"
                      {...field}
                      placeholder="0 9 * * 1-5"
                      className="font-mono"
                    />
                  )}
                />
                {errors.scheduleConfig?.cronExpression && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.cronExpression.message as string}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="custom-timezone">Timezone *</Label>
                <Controller
                  name="scheduleConfig.timezone"
                  control={control}
                  rules={{ required: "Timezone is required" }}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select timezone" />
                      </SelectTrigger>
                      <SelectContent>
                        {TIMEZONES.map((tz) => (
                          <SelectItem key={tz} value={tz}>
                            {tz}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.scheduleConfig?.timezone && (
                  <p className="text-sm text-error-600">
                    {errors.scheduleConfig.timezone.message as string}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>
        )}
      </div>
      {error && <p className="text-sm text-error-600">{error}</p>}
    </div>
  )
}

