import { useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ChevronLeft, RefreshCw } from "lucide-react"
import { useForm, Controller } from "react-hook-form"
import { useActionRegistryById, useCreateActionRegistry, useUpdateActionRegistry } from "@/hooks/use-action-registry"
import { Skeleton } from "@/components/ui/skeleton"
import { SchemaEditor, type SchemaDefinition } from "@/components/registry/SchemaEditor"
import { OutputSchemaEditor } from "@/components/registry/OutputSchemaEditor"
import { ApiCallConfigFields } from "@/components/registry/ApiCallConfigFields"
import { PublishEventConfigFields } from "@/components/registry/PublishEventConfigFields"
import { FunctionConfigFields } from "@/components/registry/FunctionConfigFields"
import { generateDefaultOutputMapping, generateConfigTemplateSchema } from "@/utils/generate-config-template-schema"
import type { ActionRegistryItem } from "@/services/action-registry-service"
import type { ActionType, ActionConfigTemplate } from "@/components/registry/types"

export default function ActionEditorPage() {
  const { id } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const isEditMode = id !== "new" && id !== undefined

  const { data: action, isLoading } = useActionRegistryById(isEditMode ? id : undefined)
  const createAction = useCreateActionRegistry()
  const updateAction = useUpdateActionRegistry()

  const { register, handleSubmit, control, watch, reset, setValue, formState: { errors } } = useForm<ActionRegistryItem & { configTemplate: ActionConfigTemplate }>({
    defaultValues: {
      id: "",
      name: "",
      type: "custom-action",
      description: "",
      configTemplate: {
        inputSchema: [],
        outputSchema: [],
        configTemplate: [],
        outputMapping: {},
      },
      metadata: {
        icon: "",
        color: "#64748b",
        version: "1.0.0",
      },
    },
  })

  // Get actionType from form - backend uses "type" field (not "actionType")
  const actionType = (watch("type") || action?.type) as ActionType

  // Initialize config template based on action type
  const initializeConfigTemplate = (type: ActionType, existingConfig?: ActionConfigTemplate | Record<string, any>): ActionConfigTemplate => {
    // Backend stores config fields (url, method, headers, etc.) at top level of configTemplate
    // We need to preserve these fields while also maintaining the schema structure
    
    // Generate outputMapping if not exists
    let outputMapping: Record<string, string> = existingConfig?.outputMapping || {}
    if (!outputMapping || Object.keys(outputMapping).length === 0) {
      const outputSchema = existingConfig?.outputSchema || []
      outputMapping = generateDefaultOutputMapping(type, outputSchema)
    }

    // Extract schema fields separately to avoid overwriting
    const inputSchema = existingConfig?.inputSchema || []
    const outputSchema = existingConfig?.outputSchema || []
    // Use existing configTemplate schema if exists, otherwise generate default based on action type
    let configTemplate = existingConfig?.configTemplate || []
    if (!configTemplate || configTemplate.length === 0 || (configTemplate.length === 1 && configTemplate[0].fields.length === 0)) {
      // Auto-generate config template schema if not exists
      configTemplate = generateConfigTemplateSchema(type)
    }

    // Preserve all existing config fields (url, method, headers, body, etc.)
    // Spread existingConfig first, then override with schema fields
    const baseConfig: ActionConfigTemplate = {
      ...(existingConfig || {}),
      inputSchema,
      outputSchema,
      configTemplate,
      outputMapping,
    }

    return baseConfig
  }

  // Load action data when editing
  useEffect(() => {
    if (action && isEditMode) {
      // Ensure type is in correct format (backend returns "api-call", "publish-event", etc.)
      const actionType = (action.type || "").toLowerCase() as ActionType
      
      const configTemplate = initializeConfigTemplate(
        actionType,
        action.configTemplate as unknown as ActionConfigTemplate
      )
      const formData = {
        id: action.id || "",
        name: action.name || "",
        type: actionType,
        description: action.description || "",
        configTemplate: configTemplate as any,
        metadata: action.metadata || {
          icon: "",
          color: "#64748b",
          version: "1.0.0",
        },
      }
      // Use reset with shouldValidate: false to avoid validation errors during load
      reset(formData, { keepDefaultValues: false })
    }
  }, [action, isEditMode, reset])

  // Initialize config template when action type changes (only for new actions)
  useEffect(() => {
    if (!isEditMode && actionType) {
      const currentConfig = watch("configTemplate") as any
      // Reset config when action type changes for new actions
      // Preserve inputSchema and outputSchema if user already defined them
      const newConfig = initializeConfigTemplate(actionType, {
        inputSchema: currentConfig?.inputSchema || [],
        outputSchema: currentConfig?.outputSchema || [],
        configTemplate: currentConfig?.configTemplate || [],
        outputMapping: currentConfig?.outputMapping || {},
      } as ActionConfigTemplate)
      setValue("configTemplate", newConfig as any, { shouldDirty: false })
    }
  }, [actionType, isEditMode, setValue, watch])

  // Auto-update output mapping when output schema changes (only if mapping is empty)
  const outputSchema = watch("configTemplate.outputSchema")
  const outputMapping = watch("configTemplate.outputMapping")
  useEffect(() => {
    if (outputSchema && outputSchema.length > 0) {
      const currentMapping = outputMapping || {}
      // Only auto-update if mapping is empty
      if (Object.keys(currentMapping).length === 0) {
        const generatedMapping = generateDefaultOutputMapping(actionType, outputSchema)
        setValue("configTemplate.outputMapping", generatedMapping, { shouldDirty: false })
      }
    }
  }, [outputSchema, actionType, setValue])

  const onSubmit = async (data: ActionRegistryItem & { configTemplate: any }) => {
    try {
      if (isEditMode && id) {
        await updateAction.mutateAsync({
          id,
          data: {
            name: data.name,
            type: data.type,
            description: data.description,
            configTemplate: data.configTemplate,
            metadata: data.metadata,
          },
        })
        navigate("/actions")
      } else {
        await createAction.mutateAsync({
          id: data.id,
          name: data.name,
          type: data.type,
          description: data.description,
          configTemplate: data.configTemplate,
          metadata: data.metadata,
        })
        navigate("/actions")
      }
    } catch (error) {
      // Error handling is done in mutation hooks
    }
  }

  const handleCancel = () => {
    navigate("/actions")
  }

  const handleInputSchemaChange = (schemas: SchemaDefinition[]) => {
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      inputSchema: schemas,
    }, { shouldDirty: true })
  }

  const handleConfigTemplateSchemaChange = (schemas: SchemaDefinition[]) => {
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      configTemplate: schemas,
    }, { shouldDirty: true })
  }

  const handleOutputSchemaChange = (schemas: SchemaDefinition[]) => {
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      outputSchema: schemas,
    }, { shouldDirty: true })
  }

  const handleConfigChange = (config: Partial<ActionConfigTemplate>) => {
    // Merge config fields with existing schemas and mappings
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      ...config,
      // Preserve schemas and mappings
      inputSchema: currentConfig.inputSchema || [],
      configTemplate: currentConfig.configTemplate || [],
      outputSchema: currentConfig.outputSchema || [],
      outputMapping: currentConfig.outputMapping || {},
    }, { shouldDirty: true })
  }

  const handleOutputMappingChange = (outputMapping: Record<string, string>) => {
    const currentConfig = watch("configTemplate") || {}
    setValue("configTemplate", {
      ...currentConfig,
      outputMapping,
    }, { shouldDirty: true })
  }


  if (isEditMode && isLoading) {
    return (
      <div className="container mx-auto p-6 max-w-7xl">
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

  const currentInputSchemas = (watch("configTemplate")?.inputSchema || []) as SchemaDefinition[]
  const currentConfigTemplateSchemas = (watch("configTemplate")?.configTemplate || []) as SchemaDefinition[]
  const currentOutputSchemas = (watch("configTemplate")?.outputSchema || []) as SchemaDefinition[]
  const currentOutputMapping = (watch("configTemplate")?.outputMapping || {}) as Record<string, string>
  const currentConfigTemplate = (watch("configTemplate") || {}) as any as ActionConfigTemplate
  
  // Extract config fields for specific action types (url, method, headers, etc.)
  // These fields are stored at top level of configTemplate from backend
  const currentConfig = {
    url: currentConfigTemplate?.url,
    method: currentConfigTemplate?.method,
    headers: currentConfigTemplate?.headers,
    body: currentConfigTemplate?.body,
    authentication: currentConfigTemplate?.authentication,
    timeout: currentConfigTemplate?.timeout,
    retry: currentConfigTemplate?.retry,
    kafka: currentConfigTemplate?.kafka,
    message: currentConfigTemplate?.message,
    expression: currentConfigTemplate?.expression,
    outputField: currentConfigTemplate?.outputField,
  }

  // Ensure at least one schema exists for SchemaEditor
  const inputSchemas = currentInputSchemas.length > 0 
    ? currentInputSchemas 
    : [{ schemaId: "input-schema", fields: [] }]
  const configTemplateSchemas = currentConfigTemplateSchemas.length > 0 
    ? currentConfigTemplateSchemas 
    : [{ schemaId: "config-template-schema", fields: [] }]
  const outputSchemas = currentOutputSchemas.length > 0 
    ? currentOutputSchemas 
    : [{ schemaId: "output-schema", fields: [] }]

  return (
    <div className="container mx-auto p-6 max-w-7xl">
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
          {isEditMode ? "Edit Action" : "Create Custom Action"}
        </h1>
        <p className="text-secondary-600">
          {isEditMode ? "Update action configuration" : "Create a new custom action for workflows"}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Info Section */}
        <Card>
          <CardHeader>
            <CardTitle>Basic Information</CardTitle>
            <CardDescription>Configure action properties and settings</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="id">
                  Action ID <span className="text-error-600">*</span>
                </Label>
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
                <Label htmlFor="name">
                  Action Name <span className="text-error-600">*</span>
                </Label>
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
                <Label htmlFor="type">
                  Action Type <span className="text-error-600">*</span>
                </Label>
                <Controller
                  name="type"
                  control={control}
                  rules={{ required: "Action type is required" }}
                  render={({ field }) => {
                    const value = field.value || ""
                    return (
                      <Select 
                        key={value} // Force re-render when value changes
                        value={value} 
                        onValueChange={(newValue) => {
                          field.onChange(newValue)
                          setValue("type", newValue as ActionType, { shouldValidate: true })
                        }} 
                        disabled={isEditMode}
                      >
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
                    )
                  }}
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
            </div>
          </CardContent>
        </Card>

        {/* Input Schema Section */}
        <Card>
          <CardHeader>
            <CardTitle>Input Schema</CardTitle>
            <CardDescription className="text-xs">
              Define input data structure from previous nodes. Fields defined here will be available for autocomplete in configuration fields when typing <code className="px-1 py-0.5 bg-secondary-100 rounded">{"@{"}</code>.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <SchemaEditor
              schemas={inputSchemas}
              onChange={handleInputSchemaChange}
              allowMultiple={false}
            />
          </CardContent>
        </Card>

        {/* Config Template Schema Section */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Config Template Schema <span className="text-error-600">*</span></CardTitle>
                <CardDescription className="text-xs">
                  Define the structure of configuration fields (url, method, headers, etc.) that users will fill in when using this action in workflows. Users can provide static values or MVEL expressions for these fields.
                </CardDescription>
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => {
                  const generated = generateConfigTemplateSchema(actionType)
                  setValue("configTemplate.configTemplate", generated as any, { shouldDirty: true })
                }}
                className="cursor-pointer"
              >
                <RefreshCw className="h-4 w-4 mr-2" />
                Auto-generate
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <SchemaEditor
              schemas={configTemplateSchemas}
              onChange={handleConfigTemplateSchemaChange}
              allowMultiple={false}
            />
          </CardContent>
        </Card>

        {/* Output Schema Section */}
        <Card>
          <CardHeader>
            <CardTitle>Output Schema</CardTitle>
            <CardDescription className="text-xs">
              Define output fields with MVEL expressions to map from action response. Each field can have an expression to extract its value.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <OutputSchemaEditor
              schemas={outputSchemas}
              outputMapping={currentOutputMapping}
              onChange={handleOutputSchemaChange}
              onMappingChange={handleOutputMappingChange}
              allowMultiple={false}
            />
          </CardContent>
        </Card>

        {/* Configuration Section - Full Width with Specific Components */}
        <Card>
          <CardHeader>
            <CardTitle>Configuration</CardTitle>
            <CardDescription>
              Configure action-specific settings. For <span className="font-medium">{actionType}</span> action type.
              Type <code className="px-1 py-0.5 bg-secondary-100 rounded">{"@{"}</code> in any field to see autocomplete suggestions from Input Schema.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {!actionType || actionType === "custom-action" ? (
              actionType === "custom-action" ? (
                <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
                  Custom actions use their own configuration structure. Define config fields in the schema above.
                </div>
              ) : (
                <div className="text-sm text-secondary-500 py-4 text-center border border-dashed border-secondary-200 rounded-md">
                  Please select an action type to configure settings.
                </div>
              )
            ) : (
              <>
                {actionType === "api-call" && (
                  <ApiCallConfigFields
                    config={currentConfig as any}
                    onChange={(config) => handleConfigChange(config)}
                    inputSchema={currentInputSchemas}
                  />
                )}
                {actionType === "publish-event" && (
                  <PublishEventConfigFields
                    config={currentConfig as any}
                    onChange={(config) => handleConfigChange(config)}
                    inputSchema={currentInputSchemas}
                  />
                )}
                {actionType === "function" && (
                  <FunctionConfigFields
                    config={currentConfig as any}
                    onChange={(config) => handleConfigChange(config)}
                    inputSchema={currentInputSchemas}
                  />
                )}
              </>
            )}
          </CardContent>
        </Card>

        {/* Metadata Section */}
        <Card>
          <CardHeader>
            <CardTitle>Metadata</CardTitle>
            <CardDescription>Configure visual and version information</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
              <div className="space-y-2">
                <Label htmlFor="version">Version</Label>
                <Input
                  id="version"
                  {...register("metadata.version")}
                  placeholder="1.0.0"
                  defaultValue="1.0.0"
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="flex justify-end space-x-2">
          <Button type="button" variant="outline" onClick={handleCancel} className="cursor-pointer">
            Cancel
          </Button>
          <Button type="submit" disabled={createAction.isPending || updateAction.isPending} className="cursor-pointer">
            {createAction.isPending || updateAction.isPending ? "Saving..." : isEditMode ? "Update Action" : "Create Action"}
          </Button>
        </div>
      </form>
    </div>
  )
}
