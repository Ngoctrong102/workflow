import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useForm, Controller } from "react-hook-form"
import type { Trigger, TriggerType } from "@/types/trigger"

interface TriggerEditorProps {
  workflowId?: string // Optional, for backward compatibility
  triggerType: TriggerType
  trigger?: Trigger
  onSave: (data: { name?: string; triggerType?: string; status?: string; config: Record<string, unknown> }) => void
  onCancel: () => void
  showNameAndStatus?: boolean // Show name and status fields for trigger configs
}

export function TriggerEditor({
  triggerType,
  trigger,
  onSave,
  onCancel,
  showNameAndStatus = false,
}: TriggerEditorProps) {
  const { register, handleSubmit, control, formState: { errors } } = useForm<Record<string, unknown>>({
    defaultValues: {
      name: (trigger as any)?.name || "",
      status: (trigger as any)?.status || "active",
      ...getDefaultConfig(triggerType),
      ...(trigger?.config || {}),
      timezone: (trigger?.config as { timezone?: string })?.timezone || "UTC",
    },
  })

  const onSubmit = (data: Record<string, unknown>) => {
    // Extract name and status if showNameAndStatus is true
    const { name, status, ...config } = data
    onSave({
      ...(showNameAndStatus ? { name: name as string, status: status as string } : {}),
      config,
    })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          {trigger ? "Edit Trigger" : "Create Trigger"} - {triggerType.toUpperCase()}
        </CardTitle>
        <CardDescription>Configure trigger settings</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Name and Status fields for trigger configs */}
          {showNameAndStatus && (
            <>
              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input
                  id="name"
                  {...register("name", { required: "Name is required" })}
                  placeholder="Enter trigger config name"
                />
                {errors.name && (
                  <p className="text-sm text-error-600">{errors.name.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Controller
                  name="status"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value as string} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="active">Active</SelectItem>
                        <SelectItem value="inactive">Inactive</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </>
          )}

          {triggerType === "api" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="path">API Path *</Label>
                <Input
                  id="path"
                  {...register("path", { required: "Path is required" })}
                  placeholder="/api/trigger/example"
                />
                {errors.path && (
                  <p className="text-sm text-error-600">{errors.path.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="method">HTTP Method *</Label>
                <Controller
                  name="method"
                  control={control}
                  rules={{ required: "Method is required" }}
                  render={({ field }) => (
                    <Select value={field.value as string} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select method" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="GET">GET</SelectItem>
                        <SelectItem value="POST">POST</SelectItem>
                        <SelectItem value="PUT">PUT</SelectItem>
                        <SelectItem value="PATCH">PATCH</SelectItem>
                        <SelectItem value="DELETE">DELETE</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.method && (
                  <p className="text-sm text-error-600">{errors.method.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="authType">Authentication</Label>
                <Controller
                  name="authentication.type"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value || "none"} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select auth type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">None</SelectItem>
                        <SelectItem value="api_key">API Key</SelectItem>
                        <SelectItem value="bearer">Bearer Token</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </>
          )}

          {triggerType === "schedule" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="cronExpression">Cron Expression *</Label>
                <Input
                  id="cronExpression"
                  {...register("cronExpression", { required: "Cron expression is required" })}
                  placeholder="0 9 * * *"
                />
                {errors.cronExpression && (
                  <p className="text-sm text-error-600">{errors.cronExpression.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Examples: 0 9 * * * (daily at 9 AM), 0 */6 * * * (every 6 hours)
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="timezone">Timezone *</Label>
                  <Input
                    id="timezone"
                    {...register("timezone", { required: "Timezone is required" })}
                    placeholder="UTC"
                    defaultValue="UTC"
                  />
                {errors.timezone && (
                  <p className="text-sm text-error-600">{errors.timezone.message as string}</p>
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="startDate">Start Date</Label>
                  <Input
                    id="startDate"
                    type="datetime-local"
                    {...register("startDate")}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="endDate">End Date</Label>
                  <Input
                    id="endDate"
                    type="datetime-local"
                    {...register("endDate")}
                  />
                </div>
              </div>
            </>
          )}

          {triggerType === "file" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="formats">File Formats *</Label>
                <Controller
                  name="formats"
                  control={control}
                  rules={{ required: "File formats are required" }}
                  render={({ field }) => (
                    <Select
                      value={Array.isArray(field.value) ? field.value.join(",") : ""}
                      onValueChange={(value) => field.onChange(value.split(","))}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select formats" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="csv">CSV</SelectItem>
                        <SelectItem value="json">JSON</SelectItem>
                        <SelectItem value="xlsx">Excel (XLSX)</SelectItem>
                        <SelectItem value="csv,json">CSV, JSON</SelectItem>
                        <SelectItem value="csv,json,xlsx">All Formats</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.formats && (
                  <p className="text-sm text-error-600">{errors.formats.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="mapping">Data Mapping (JSON)</Label>
                <Textarea
                  id="mapping"
                  {...register("mapping")}
                  placeholder='{"email": "email", "name": "name"}'
                  rows={4}
                  className="font-mono text-sm"
                />
                <p className="text-xs text-secondary-500">
                  Map file columns to workflow variables
                </p>
              </div>
            </>
          )}

          {triggerType === "event" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="source">Message Queue *</Label>
                <Controller
                  name="source"
                  control={control}
                  rules={{ required: "Source is required" }}
                  render={({ field }) => (
                    <Select value={field.value as string} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select source" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="kafka">Kafka</SelectItem>
                        <SelectItem value="rabbitmq">RabbitMQ</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.source && (
                  <p className="text-sm text-error-600">{errors.source.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="topic">Topic/Queue *</Label>
                <Input
                  id="topic"
                  {...register("topic", { required: "Topic/Queue is required" })}
                  placeholder="topic-name or queue-name"
                />
                {errors.topic && (
                  <p className="text-sm text-error-600">{errors.topic.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="filters">Event Filters (JSON)</Label>
                <Textarea
                  id="filters"
                  {...register("filters")}
                  placeholder='{"event_type": "user.created"}'
                  rows={4}
                  className="font-mono text-sm"
                />
                <p className="text-xs text-secondary-500">
                  Optional filters for event messages
                </p>
              </div>
            </>
          )}

          <div className="flex justify-end space-x-2 pt-4">
            <Button type="button" variant="outline" onClick={onCancel}>
              Cancel
            </Button>
            <Button type="submit">Save Trigger</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function getDefaultConfig(type: TriggerType): Record<string, unknown> {
  switch (type) {
    case "api":
      return {
        path: "",
        method: "POST",
        authentication: { type: "none" },
      }
    case "schedule":
      return {
        cronExpression: "",
        timezone: "UTC",
      }
    case "file":
      return {
        formats: [],
      }
    case "event":
      return {
        source: "kafka",
        topic: "",
      }
  }
}

