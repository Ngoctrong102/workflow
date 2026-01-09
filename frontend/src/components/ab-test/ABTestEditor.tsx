import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useForm, Controller, useFieldArray } from "react-hook-form"
import { Plus, Trash2, AlertCircle } from "lucide-react"
import type { ABTest, ABTestVariant, SuccessMetric, AssignmentStrategy } from "@/types/ab-test"
import { useTemplates } from "@/hooks/use-templates"
import { useChannels } from "@/hooks/use-channels"

interface ABTestEditorProps {
  test?: ABTest
  onSave: (test: Omit<ABTest, "id" | "created_at" | "updated_at">) => void
  onCancel: () => void
}

export function ABTestEditor({ test, onSave, onCancel }: ABTestEditorProps) {
  const { data: templatesData } = useTemplates({ limit: 100 })
  const { data: channelsData } = useChannels({ limit: 100 })
  const templates = templatesData?.data || []
  const channels = channelsData?.data || []

  const { register, handleSubmit, control, watch, formState: { errors } } = useForm({
    defaultValues: {
      name: test?.name || "",
      description: test?.description || "",
      workflow_id: test?.workflow_id || "",
      success_metric: (test?.success_metric || "open_rate") as SuccessMetric,
      assignment_strategy: (test?.assignment_strategy || "random") as AssignmentStrategy,
      duration_days: test?.duration_days || 7,
      min_sample_size: test?.min_sample_size || 1000,
      variants: (test?.variants || [
        { id: "variant-a", name: "Variant A", label: "A", traffic_percentage: 50 },
        { id: "variant-b", name: "Variant B", label: "B", traffic_percentage: 50 },
      ]) as ABTestVariant[],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: "variants",
  })

  const watchedVariants = watch("variants")
  const totalTraffic = watchedVariants.reduce((sum, v) => sum + (v.traffic_percentage || 0), 0)

  const onSubmit = (data: Record<string, unknown>) => {
    const variants = (data.variants as ABTestVariant[]).map((v, index) => ({
      ...v,
      label: String.fromCharCode(65 + index), // A, B, C, etc.
    }))

    onSave({
      name: data.name as string,
      description: data.description as string,
      workflow_id: data.workflow_id as string,
      status: test?.status || "draft",
      variants,
      success_metric: data.success_metric as SuccessMetric,
      assignment_strategy: (data.assignment_strategy || "random") as AssignmentStrategy,
      duration_days: Number(data.duration_days) || 7,
      min_sample_size: Number(data.min_sample_size) || 1000,
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>A/B Test Information</CardTitle>
          <CardDescription>Basic test details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Test Name *</Label>
            <Input
              id="name"
              {...register("name", { required: "Name is required" })}
              placeholder="Enter test name"
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
              placeholder="Enter test description"
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="workflow_id">Workflow ID *</Label>
            <Input
              id="workflow_id"
              {...register("workflow_id", { required: "Workflow ID is required" })}
              placeholder="workflow-123"
            />
            {errors.workflow_id && (
              <p className="text-sm text-error-600">{errors.workflow_id.message as string}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="success_metric">Success Metric *</Label>
              <Controller
                name="success_metric"
                control={control}
                rules={{ required: "Success metric is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select success metric" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="open_rate">Open Rate</SelectItem>
                      <SelectItem value="click_rate">Click Rate</SelectItem>
                      <SelectItem value="conversion_rate">Conversion Rate</SelectItem>
                      <SelectItem value="engagement_rate">Engagement Rate</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="assignment_strategy">Assignment Strategy</Label>
              <Controller
                name="assignment_strategy"
                control={control}
                render={({ field }) => (
                  <Select value={field.value || "random"} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select assignment strategy" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="random">Random Assignment</SelectItem>
                      <SelectItem value="consistent">Consistent Assignment</SelectItem>
                      <SelectItem value="stratified">Stratified Sampling</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="duration_days">Duration (days)</Label>
              <Input
                id="duration_days"
                type="number"
                {...register("duration_days", { valueAsNumber: true, min: 1 })}
                placeholder="7"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="min_sample_size">Min Sample Size</Label>
              <Input
                id="min_sample_size"
                type="number"
                {...register("min_sample_size", { valueAsNumber: true, min: 100 })}
                placeholder="1000"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Variants */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Variants</CardTitle>
              <CardDescription>Configure test variants and traffic split</CardDescription>
            </div>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                const nextLabel = String.fromCharCode(65 + fields.length)
                append({
                  id: `variant-${nextLabel.toLowerCase()}`,
                  name: `Variant ${nextLabel}`,
                  label: nextLabel,
                  traffic_percentage: 0,
                })
              }}
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Variant
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {totalTraffic !== 100 && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                Total traffic split must equal 100%. Current: {totalTraffic}%
              </AlertDescription>
            </Alert>
          )}

          {fields.map((field, index) => {
            const variant = watchedVariants[index]
            const label = String.fromCharCode(65 + index)

            return (
              <Card key={field.id} className="border-2">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">Variant {label}</CardTitle>
                    {fields.length > 2 && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => remove(index)}
                      >
                        <Trash2 className="h-4 w-4 text-error-600" />
                      </Button>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor={`variants.${index}.name`}>Variant Name *</Label>
                    <Input
                      {...register(`variants.${index}.name`, { required: "Variant name is required" })}
                      placeholder={`Variant ${label}`}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor={`variants.${index}.template_id`}>Template</Label>
                    <Controller
                      name={`variants.${index}.template_id`}
                      control={control}
                      render={({ field }) => (
                        <Select value={field.value || ""} onValueChange={field.onChange}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select template" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="">None</SelectItem>
                            {(templates as Array<{ id: string; name: string }>).map((template) => (
                              <SelectItem key={template.id} value={template.id}>
                                {template.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      )}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor={`variants.${index}.channel`}>Channel</Label>
                    <Controller
                      name={`variants.${index}.channel`}
                      control={control}
                      render={({ field }) => (
                        <Select value={field.value || ""} onValueChange={field.onChange}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select channel" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="">None</SelectItem>
                            {(channels as Array<{ id: string; name: string; type: string }>).map((channel) => (
                              <SelectItem key={channel.id} value={channel.type}>
                                {channel.name} ({channel.type})
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      )}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor={`variants.${index}.traffic_percentage`}>
                      Traffic Percentage * ({variant?.traffic_percentage || 0}%)
                    </Label>
                    <Input
                      type="number"
                      min="0"
                      max="100"
                      {...register(`variants.${index}.traffic_percentage`, {
                        required: "Traffic percentage is required",
                        valueAsNumber: true,
                        min: 0,
                        max: 100,
                      })}
                      placeholder="50"
                    />
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="flex justify-end space-x-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={totalTraffic !== 100}>
          Save A/B Test
        </Button>
      </div>
    </form>
  )
}

