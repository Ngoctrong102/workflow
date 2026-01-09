import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ChevronLeft } from "lucide-react"
import { useForm, Controller } from "react-hook-form"
import { useActionRegistry } from "@/hooks/use-action-registry"
import { useToast } from "@/hooks/use-toast"
import { Skeleton } from "@/components/ui/skeleton"
import type { ActionRegistryItem } from "@/services/action-registry-service"

export default function ActionEditorPage() {
  const { id } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const { toast } = useToast()
  const isEditMode = id !== "new" && id !== undefined

  const { data: actionRegistry, isLoading } = useActionRegistry()
  const action = actionRegistry?.actions.find((a) => a.id === id)

  const { register, handleSubmit, control, formState: { errors } } = useForm<ActionRegistryItem>({
    defaultValues: action || {
      id: "",
      name: "",
      type: "custom-action",
      description: "",
      configTemplate: {},
      metadata: {},
    },
  })

  const onSubmit = async (data: ActionRegistryItem) => {
    try {
      // TODO: Implement API call to create/update action
      // For now, just show success message
      toast({
        title: "Success",
        description: isEditMode ? "Action updated successfully" : "Action created successfully",
      })
      navigate("/actions")
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to save action",
      })
    }
  }

  const handleCancel = () => {
    navigate("/actions")
  }

  if (isEditMode && isLoading) {
    return (
      <div className="container mx-auto p-6 max-w-4xl">
        <Card>
          <CardHeader>
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-64 mt-2" />
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <div className="mb-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={handleCancel}
          className="mb-4"
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">
          {isEditMode ? "Edit Action" : "Create Custom Action"}
        </h1>
        <p className="text-secondary-600">
          {isEditMode ? "Update action configuration" : "Create a new custom action for workflows"}
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Action Details</CardTitle>
          <CardDescription>Configure action properties and settings</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="id">Action ID *</Label>
              <Input
                id="id"
                {...register("id", { required: "Action ID is required" })}
                placeholder="my-custom-action"
                disabled={isEditMode}
                className="font-mono"
              />
              {errors.id && (
                <p className="text-sm text-error-600">{errors.id.message}</p>
              )}
              <p className="text-xs text-secondary-500">
                Unique identifier for this action (cannot be changed after creation)
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="name">Action Name *</Label>
              <Input
                id="name"
                {...register("name", { required: "Action name is required" })}
                placeholder="My Custom Action"
              />
              {errors.name && (
                <p className="text-sm text-error-600">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="type">Action Type *</Label>
              <Controller
                name="type"
                control={control}
                rules={{ required: "Action type is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange} disabled={isEditMode}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select action type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="api-call">API Call</SelectItem>
                      <SelectItem value="publish-event">Publish Event</SelectItem>
                      <SelectItem value="function">Function</SelectItem>
                      <SelectItem value="custom-action">Custom Action</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.type && (
                <p className="text-sm text-error-600">{errors.type.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                {...register("description")}
                placeholder="Describe what this action does..."
                rows={3}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="configTemplate">Configuration Template (JSON)</Label>
              <Textarea
                id="configTemplate"
                {...register("configTemplate")}
                placeholder='{"endpoint": "https://api.example.com", "method": "POST"}'
                rows={6}
                className="font-mono text-sm"
              />
              <p className="text-xs text-secondary-500">
                JSON template for action configuration. This will be used as default when adding the action to a workflow.
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="icon">Icon</Label>
                <Input
                  id="icon"
                  {...register("metadata.icon")}
                  placeholder="📧"
                />
                <p className="text-xs text-secondary-500">Icon emoji or symbol</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="color">Color</Label>
                <Input
                  id="color"
                  type="color"
                  {...register("metadata.color")}
                  defaultValue="#64748b"
                />
                <p className="text-xs text-secondary-500">Action color</p>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="version">Version</Label>
              <Input
                id="version"
                {...register("metadata.version")}
                placeholder="1.0.0"
              />
            </div>

            <div className="flex justify-end space-x-2 pt-4">
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
              <Button type="submit">
                {isEditMode ? "Update Action" : "Create Action"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

