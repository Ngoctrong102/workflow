import { useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ChevronLeft } from "lucide-react"
import { useForm, Controller } from "react-hook-form"
import { useTriggerRegistryById, useCreateTriggerRegistry, useUpdateTriggerRegistry } from "@/hooks/use-trigger-registry"
import { useToast } from "@/hooks/use-toast"
import { Skeleton } from "@/components/ui/skeleton"
import { SchemaEditor, type SchemaDefinition } from "@/components/registry/SchemaEditor"
import type { TriggerRegistryItem } from "@/services/trigger-service"

export default function TriggerRegistryEditorPage() {
  const { id } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const { toast } = useToast()
  const isEditMode = id !== "new" && id !== undefined

  const { data: trigger, isLoading, error } = useTriggerRegistryById(isEditMode ? id : undefined)
  const createTrigger = useCreateTriggerRegistry()
  const updateTrigger = useUpdateTriggerRegistry()

  // Log trigger data and loading state for debugging
  if (import.meta.env.DEV) {
    console.log('[TriggerRegistryEditor] State:', {
      id,
      isEditMode,
      isLoading,
      error,
      trigger,
      hasTrigger: !!trigger,
    })
  }

  const { register, handleSubmit, control, watch, reset, setValue, formState: { errors } } = useForm<TriggerRegistryItem & { configTemplate: any }>({
    defaultValues: {
      id: "",
      name: "",
      type: "api-call",
      description: "",
      configTemplate: {},
      metadata: {
        icon: "",
        color: "#0ea5e9",
        version: "1.0.0",
      },
    },
  })

  const triggerType = watch("type")
  const isEventTrigger = triggerType === "event"
  // Watch configTemplate at top level to ensure re-renders when it changes
  const configTemplate = watch("configTemplate") || {}

  // Load trigger data when editing
  useEffect(() => {
    if (trigger && isEditMode) {
      // Log trigger data for debugging
      if (import.meta.env.DEV) {
        console.log('[TriggerRegistryEditor] Loading trigger data:', trigger)
        console.log('[TriggerRegistryEditor] trigger.configTemplate:', trigger.configTemplate)
        console.log('[TriggerRegistryEditor] trigger.configTemplate type:', typeof trigger.configTemplate)
        console.log('[TriggerRegistryEditor] trigger.configTemplate is null:', trigger.configTemplate === null)
        console.log('[TriggerRegistryEditor] trigger.configTemplate is undefined:', trigger.configTemplate === undefined)
      }
      
      // Ensure configTemplate is properly structured
      // Backend returns config with kafka, schemas, etc.
      // Make sure we preserve the full structure
      // Handle null, undefined, or empty object cases
      let configTemplate: Record<string, unknown> = {}
      if (trigger.configTemplate) {
        // If configTemplate exists, use it directly
        // Ensure it's an object (not null, not undefined)
        if (typeof trigger.configTemplate === 'object' && trigger.configTemplate !== null) {
          configTemplate = trigger.configTemplate as Record<string, unknown>
        }
      }
      
      const formData = {
        id: trigger.id,
        name: trigger.name,
        type: trigger.type,
        description: trigger.description || "",
        configTemplate: configTemplate, // Always an object, never null or undefined
        metadata: trigger.metadata || {
          icon: "",
          color: "#0ea5e9",
          version: "1.0.0",
        },
      }
      
      // Log form data for debugging
      if (import.meta.env.DEV) {
        console.log('[TriggerRegistryEditor] Resetting form with data:', formData)
        console.log('[TriggerRegistryEditor] formData.configTemplate:', formData.configTemplate)
        console.log('[TriggerRegistryEditor] formData.configTemplate type:', typeof formData.configTemplate)
        console.log('[TriggerRegistryEditor] formData.configTemplate keys:', Object.keys(formData.configTemplate))
        console.log('[TriggerRegistryEditor] formData.configTemplate.schemas:', formData.configTemplate.schemas)
        console.log('[TriggerRegistryEditor] formData.configTemplate.kafka:', formData.configTemplate.kafka)
      }
      
      // Reset form with the data
      // Use reset with options to ensure all fields are properly set
      reset(formData, {
        keepDefaultValues: false, // Don't keep default values, use the new data
      })
      
      // Also explicitly set configTemplate to ensure it's in the form state
      // This is a workaround for cases where reset might not properly set nested objects
      setValue("configTemplate", configTemplate, { shouldDirty: false })
    }
  }, [trigger, isEditMode, reset, setValue])

  const onSubmit = async (data: TriggerRegistryItem & { configTemplate: any }) => {
    try {
      if (isEditMode && id) {
        await updateTrigger.mutateAsync({
          id,
          data: {
            name: data.name,
            type: data.type,
            description: data.description,
            configTemplate: data.configTemplate,
            metadata: data.metadata,
          },
        })
        navigate("/trigger-registry")
      } else {
        await createTrigger.mutateAsync({
          id: data.id,
          name: data.name,
          type: data.type,
          description: data.description,
          configTemplate: data.configTemplate,
          metadata: data.metadata,
        })
        navigate("/trigger-registry")
      }
    } catch (error) {
      // Error handling is done in mutation hooks
    }
  }

  const handleCancel = () => {
    navigate("/trigger-registry")
  }

  const handleSchemasChange = (schemas: SchemaDefinition[]) => {
    // Preserve all existing config fields (kafka, etc.) when updating schemas
    // Use the watched configTemplate to ensure we have the latest value
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      schemas,
    }, { shouldDirty: true })
    
    // Debug logging
    if (import.meta.env.DEV) {
      console.log('[TriggerRegistryEditor] Schemas changed:', {
        schemas,
        currentConfig,
        updatedConfig: { ...currentConfig, schemas }
      })
    }
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

  // Show error if loading failed
  if (isEditMode && error && !isLoading) {
    return (
      <div className="container mx-auto p-6 max-w-4xl">
        <Card>
          <CardHeader>
            <CardTitle>Error Loading Trigger</CardTitle>
            <CardDescription>Failed to load trigger definition</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-error-600">
              {error instanceof Error ? error.message : "Unknown error occurred"}
            </p>
            <Button
              variant="outline"
              onClick={handleCancel}
              className="mt-4 cursor-pointer"
            >
              <ChevronLeft className="h-4 w-4 mr-2" />
              Back
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  // Get schemas from configTemplate, ensure it's always an array
  // Note: configTemplate is already watched at top level, so we can use it directly here
  const currentSchemas = (Array.isArray(configTemplate.schemas) ? configTemplate.schemas : []) as SchemaDefinition[]
  
  // Debug logging
  if (import.meta.env.DEV && isEditMode) {
    console.log('[TriggerRegistryEditor] Current schemas:', {
      configTemplate,
      schemas: configTemplate.schemas,
      currentSchemas,
      isArray: Array.isArray(configTemplate.schemas),
      configTemplateKeys: Object.keys(configTemplate)
    })
  }

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      <div className="mb-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={handleCancel}
          className="mb-4 cursor-pointer"
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <h1 className="text-3xl font-bold text-secondary-900 mb-2">
          {isEditMode ? "Edit Trigger Definition" : "Create Trigger Definition"}
        </h1>
        <p className="text-secondary-600">
          {isEditMode ? "Update trigger definition configuration" : "Create a new trigger definition for workflows"}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Info Section */}
        <Card>
          <CardHeader>
            <CardTitle>Basic Information</CardTitle>
            <CardDescription>Configure trigger definition properties</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="id">
                Trigger ID <span className="text-error-600">*</span>
              </Label>
              <Input
                id="id"
                {...register("id", { required: "Trigger ID is required" })}
                placeholder="kafka-event-trigger-standard"
                disabled={isEditMode}
                className="font-mono"
              />
              {errors.id && (
                <p className="text-sm text-error-600">{errors.id.message}</p>
              )}
              <p className="text-xs text-secondary-500">
                Unique identifier for this trigger definition (cannot be changed after creation)
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="name">
                Trigger Name <span className="text-error-600">*</span>
              </Label>
              <Input
                id="name"
                {...register("name", { required: "Trigger name is required" })}
                placeholder="Kafka Event Trigger"
              />
              {errors.name && (
                <p className="text-sm text-error-600">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="type">
                Trigger Type <span className="text-error-600">*</span>
              </Label>
              <Controller
                name="type"
                control={control}
                rules={{ required: "Trigger type is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange} disabled={isEditMode}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select trigger type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="api-call">API Call</SelectItem>
                      <SelectItem value="scheduler">Scheduler</SelectItem>
                      <SelectItem value="event">Event (Kafka)</SelectItem>
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
                placeholder="Describe what this trigger does..."
                rows={3}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="icon">Icon</Label>
                <Input
                  id="icon"
                  {...register("metadata.icon")}
                  placeholder="📡"
                />
                <p className="text-xs text-secondary-500">Icon emoji or symbol</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="color">Color</Label>
                <Input
                  id="color"
                  type="color"
                  {...register("metadata.color")}
                  defaultValue="#0ea5e9"
                />
                <p className="text-xs text-secondary-500">Trigger color</p>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="version">Version</Label>
              <Input
                id="version"
                {...register("metadata.version")}
                placeholder="1.0.0"
                defaultValue="1.0.0"
              />
            </div>
          </CardContent>
        </Card>

        {/* Config Template Section */}
        <Card>
          <CardHeader>
            <CardTitle>Configuration Template</CardTitle>
            <CardDescription>Configure default settings for this trigger type</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {triggerType === "event" && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="kafka-brokers">Kafka Brokers <span className="text-error-600">*</span></Label>
                  <Textarea
                    id="kafka-brokers"
                    value={(() => {
                      const brokers = (configTemplate.kafka as any)?.brokers || []
                      return Array.isArray(brokers) ? brokers.join('\n') : ''
                    })()}
                    onChange={(e) => {
                      const brokers = e.target.value.split('\n').map(s => s.trim()).filter(s => s.length > 0)
                      setValue("configTemplate", {
                        ...configTemplate,
                        kafka: {
                          ...(configTemplate.kafka || {}),
                          brokers,
                        },
                      }, { shouldDirty: true })
                    }}
                    placeholder="localhost:9092&#10;broker2:9092"
                    rows={3}
                    className="font-mono text-sm"
                  />
                  <p className="text-xs text-secondary-500">
                    Enter one broker address per line (e.g., localhost:9092)
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="kafka-topic">Topic/Queue Name</Label>
                  <Input
                    id="kafka-topic"
                    value={(configTemplate.kafka as any)?.topic || ""}
                    onChange={(e) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        kafka: {
                          ...(configTemplate.kafka || {}),
                          topic: e.target.value,
                        },
                      }, { shouldDirty: true })
                    }}
                    placeholder="user-events"
                  />
                </div>


                <div className="space-y-2">
                  <Label htmlFor="kafka-offset">Offset Reset</Label>
                  <Select
                    value={(configTemplate.kafka as any)?.offset || "latest"}
                    onValueChange={(value) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        kafka: {
                          ...(configTemplate.kafka || {}),
                          offset: value,
                        },
                      }, { shouldDirty: true })
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="earliest">Earliest (from beginning)</SelectItem>
                      <SelectItem value="latest">Latest (from now)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </>
            )}

            {triggerType === "scheduler" && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="cron-expression">Cron Expression</Label>
                  <Input
                    id="cron-expression"
                    value={(configTemplate as any).cronExpression || ""}
                    onChange={(e) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        cronExpression: e.target.value,
                      }, { shouldDirty: true })
                    }}
                    placeholder="0 9 * * *"
                  />
                  <p className="text-xs text-secondary-500">
                    Examples: 0 9 * * * (daily at 9 AM), 0 */6 * * * (every 6 hours)
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="timezone">Timezone</Label>
                  <Input
                    id="timezone"
                    value={(configTemplate as any).timezone || "UTC"}
                    onChange={(e) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        timezone: e.target.value,
                      }, { shouldDirty: true })
                    }}
                    placeholder="UTC"
                  />
                </div>
              </>
            )}

            {triggerType === "api-call" && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="endpoint-path">Endpoint Path</Label>
                  <Input
                    id="endpoint-path"
                    value={(configTemplate as any).endpointPath || ""}
                    onChange={(e) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        endpointPath: e.target.value,
                      }, { shouldDirty: true })
                    }}
                    placeholder="/api/v1/trigger/{workflowId}"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="http-method">HTTP Method</Label>
                  <Select
                    value={(configTemplate as any).httpMethod || "POST"}
                    onValueChange={(value) => {
                      setValue("configTemplate", {
                        ...configTemplate,
                        httpMethod: value,
                      }, { shouldDirty: true })
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="GET">GET</SelectItem>
                      <SelectItem value="POST">POST</SelectItem>
                      <SelectItem value="PUT">PUT</SelectItem>
                      <SelectItem value="PATCH">PATCH</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Schema Section (for event triggers) */}
        {isEventTrigger && (
          <Card>
            <CardHeader>
              <CardTitle>Event Schemas</CardTitle>
              <CardDescription>Define schemas for different event types in the Kafka topic</CardDescription>
            </CardHeader>
            <CardContent>
              <SchemaEditor
                schemas={currentSchemas}
                onChange={handleSchemasChange}
                allowMultiple={true}
              />
            </CardContent>
          </Card>
        )}

        {/* Action Buttons */}
        <div className="flex justify-end space-x-2">
          <Button type="button" variant="outline" onClick={handleCancel} className="cursor-pointer">
            Cancel
          </Button>
          <Button type="submit" disabled={createTrigger.isPending || updateTrigger.isPending} className="cursor-pointer">
            {createTrigger.isPending || updateTrigger.isPending ? "Saving..." : isEditMode ? "Update Trigger" : "Create Trigger"}
          </Button>
        </div>
      </form>
    </div>
  )
}

