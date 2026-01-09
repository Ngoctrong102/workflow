import { useState } from "react"
import { useForm, Controller } from "react-hook-form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Loader2 } from "lucide-react"
import type {
  ReportSchedule,
  CreateReportScheduleRequest,
  ReportFrequency,
  ReportFormat,
} from "@/types/report"

interface ReportScheduleFormProps {
  schedule?: ReportSchedule
  onSubmit: (data: CreateReportScheduleRequest) => Promise<void>
  onCancel?: () => void
  isLoading?: boolean
}

export function ReportScheduleForm({
  schedule,
  onSubmit,
  onCancel,
  isLoading = false,
}: ReportScheduleFormProps) {
  const [recipients, setRecipients] = useState<string>(
    schedule?.recipients.join(", ") || ""
  )

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<CreateReportScheduleRequest>({
    defaultValues: {
      name: schedule?.name || "",
      description: schedule?.description || "",
      frequency: schedule?.frequency || "daily",
      format: schedule?.format || "csv",
      recipients: schedule?.recipients || [],
      enabled: schedule?.enabled ?? true,
    },
  })

  const handleFormSubmit = async (data: CreateReportScheduleRequest) => {
    const recipientList = recipients
      .split(",")
      .map((email) => email.trim())
      .filter((email) => email.length > 0)

    if (recipientList.length === 0) {
      return
    }

    await onSubmit({
      ...data,
      recipients: recipientList,
    })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{schedule ? "Edit Report Schedule" : "Create Report Schedule"}</CardTitle>
        <CardDescription>
          Configure when and how reports should be generated and delivered
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Schedule Name *</Label>
            <Input
              id="name"
              {...register("name", { required: "Schedule name is required" })}
              placeholder="Daily Analytics Report"
            />
            {errors.name && (
              <p className="text-sm text-error-600">{errors.name.message as string}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register("description")}
              placeholder="Optional description for this schedule"
              rows={3}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="frequency">Frequency *</Label>
              <Controller
                name="frequency"
                control={control}
                rules={{ required: "Frequency is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select frequency" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="daily">Daily</SelectItem>
                      <SelectItem value="weekly">Weekly</SelectItem>
                      <SelectItem value="monthly">Monthly</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.frequency && (
                <p className="text-sm text-error-600">{errors.frequency.message as string}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="format">Report Format *</Label>
              <Controller
                name="format"
                control={control}
                rules={{ required: "Format is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select format" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="csv">CSV</SelectItem>
                      <SelectItem value="pdf">PDF</SelectItem>
                      <SelectItem value="excel">Excel</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.format && (
                <p className="text-sm text-error-600">{errors.format.message as string}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="recipients">Recipients (comma-separated emails) *</Label>
            <Textarea
              id="recipients"
              value={recipients}
              onChange={(e) => setRecipients(e.target.value)}
              placeholder="user1@example.com, user2@example.com"
              rows={3}
            />
            <p className="text-xs text-secondary-500">
              Enter email addresses separated by commas
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <Controller
              name="enabled"
              control={control}
              render={({ field }) => (
                <Checkbox
                  id="enabled"
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
            <Label htmlFor="enabled" className="cursor-pointer">
              Enable this schedule
            </Label>
          </div>

          <div className="flex items-center justify-end space-x-2 pt-4">
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading}>
                Cancel
              </Button>
            )}
            <Button type="submit" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  {schedule ? "Updating..." : "Creating..."}
                </>
              ) : (
                schedule ? "Update Schedule" : "Create Schedule"
              )}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

