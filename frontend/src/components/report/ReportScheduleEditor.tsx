import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"
import { useForm, Controller, useFieldArray } from "react-hook-form"
import { Plus, Trash2 } from "lucide-react"
import type {
  ReportSchedule,
  CreateReportScheduleRequest,
} from "@/types/report-schedule"

interface ReportScheduleEditorProps {
  schedule?: ReportSchedule
  onSave: (data: CreateReportScheduleRequest) => void
  onCancel: () => void
}

export function ReportScheduleEditor({ schedule, onSave, onCancel }: ReportScheduleEditorProps) {
  const { register, handleSubmit, control, formState: { errors } } = useForm<CreateReportScheduleRequest>({
    defaultValues: {
      name: schedule?.name || "",
      description: schedule?.description || "",
      frequency: schedule?.frequency || "daily",
      format: schedule?.format || "csv",
      recipients: schedule?.recipients || [""],
      report_type: schedule?.report_type || "analytics",
      enabled: schedule?.enabled ?? true,
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: "recipients" as const,
  })

  const addRecipient = () => {
    append("")
  }

  const removeRecipient = (index: number) => {
    remove(index)
  }

  const onSubmit = (data: CreateReportScheduleRequest) => {
    // Filter out empty recipients
    const recipients = data.recipients.filter((email) => email.trim() !== "")
    if (recipients.length === 0) {
      return
    }
    onSave({ ...data, recipients })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Report Schedule Details</CardTitle>
          <CardDescription>Configure the report schedule settings.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Schedule Name *</Label>
            <Input
              id="name"
              {...register("name", { required: "Schedule name is required" })}
              placeholder="e.g., Weekly Analytics Report"
            />
            {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register("description")}
              placeholder="Describe this report schedule"
              rows={3}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="frequency">Frequency *</Label>
              <Controller
                name="frequency"
                control={control}
                rules={{ required: "Frequency is required" }}
                render={({ field }) => (
                  <Select onValueChange={field.onChange} value={field.value}>
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
              {errors.frequency && <p className="text-sm text-red-500">{errors.frequency.message}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="format">Report Format *</Label>
              <Controller
                name="format"
                control={control}
                rules={{ required: "Format is required" }}
                render={({ field }) => (
                  <Select onValueChange={field.onChange} value={field.value}>
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
              {errors.format && <p className="text-sm text-red-500">{errors.format.message}</p>}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="report_type">Report Type *</Label>
            <Controller
              name="report_type"
              control={control}
              rules={{ required: "Report type is required" }}
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select report type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="analytics">Analytics</SelectItem>
                    <SelectItem value="delivery">Delivery Metrics</SelectItem>
                    <SelectItem value="error">Error Metrics</SelectItem>
                    <SelectItem value="workflow">Workflow Performance</SelectItem>
                    <SelectItem value="custom">Custom Report</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.report_type && <p className="text-sm text-red-500">{errors.report_type.message}</p>}
          </div>

          <div className="space-y-2">
            <Label>Recipients *</Label>
            <div className="space-y-2">
              {fields.map((field, index) => (
                <div key={field.id} className="flex items-center space-x-2">
                  <Input
                    {...register(`recipients.${index}`, {
                      required: index === 0 ? "At least one recipient is required" : false,
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: "Invalid email address",
                      },
                    })}
                    type="email"
                    placeholder="recipient@example.com"
                    className="flex-1"
                  />
                  {fields.length > 1 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeRecipient(index)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              ))}
              {errors.recipients && (
                <p className="text-sm text-red-500">
                  {errors.recipients[0]?.message || "At least one valid recipient is required"}
                </p>
              )}
              <Button type="button" variant="outline" size="sm" onClick={addRecipient}>
                <Plus className="h-4 w-4 mr-2" />
                Add Recipient
              </Button>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Controller
              name="enabled"
              control={control}
              render={({ field }) => (
                <Switch
                  checked={field.value}
                  onCheckedChange={field.onChange}
                  id="enabled"
                />
              )}
            />
            <Label htmlFor="enabled" className="cursor-pointer">
              Enable schedule
            </Label>
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end space-x-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          {schedule ? "Update Schedule" : "Create Schedule"}
        </Button>
      </div>
    </form>
  )
}

