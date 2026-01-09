import { useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useForm, Controller } from "react-hook-form"
import type { Node } from "reactflow"
import type { WorkflowNodeType } from "@/types/workflow"
import { NODE_DEFINITIONS, NODE_ICONS } from "@/constants/workflow-nodes"
import { X, GitBranch, Shuffle, Repeat, GitMerge, Map as MapIcon, Filter, Settings, FileText, FolderUp, Radio } from "lucide-react"
import { HelpTooltip } from "@/components/common/HelpTooltip"
import { WaitForEventsNodeProperties } from "./WaitForEventsNodeProperties"
import { FieldSelector } from "./FieldSelector"
import { ContextFieldSelector } from "./ContextFieldSelector"
import { TemplateInput } from "./TemplateInput"
import { useFieldReference } from "@/providers/FieldReferenceContext"
import { parseFieldReference, formatFieldReference, type FieldReference } from "@/utils/fieldReferenceUtils"
import { useActionRegistryById } from "@/hooks/use-action-registry"

interface PropertiesPanelProps {
  selectedNode: Node | null
  nodes?: Node[]
  onSave: (nodeId: string, config: Record<string, unknown>) => void
  onCancel: () => void
  onClose?: () => void
}

export function PropertiesPanel({
  selectedNode,
  nodes = [],
  onSave,
  onCancel,
  onClose,
}: PropertiesPanelProps) {
  const { register, handleSubmit, reset, watch, control, setValue, formState } = useForm()
  const { errors } = formState
  const { objectTypes } = useFieldReference()
  
  // Convert objectTypes Map to the format expected by FieldSelector
  const objectTypesMap = objectTypes as Map<string, { name: string; fields: any[] }>

  const nodeDef = selectedNode
    ? NODE_DEFINITIONS.find((n) => n.type === selectedNode.data.type)
    : null
  
  // Load action config template from registry if node has registryId
  const registryId = (selectedNode?.data as any)?.registryId
  const { data: actionRegistryItem } = useActionRegistryById(registryId)
  
  // Use config template from registry if available, otherwise use node config
  const configTemplate = actionRegistryItem?.configTemplate || (selectedNode?.data as any)?.configTemplate
  
  const IconComponent = nodeDef ? NODE_ICONS[nodeDef.icon as keyof typeof NODE_ICONS] : null

  useEffect(() => {
    if (selectedNode) {
      // Reset form with node data
      const nodeConfig = selectedNode.data.config || {}
      
      // Set default values for wait-events node
      if (selectedNode.data.type === "wait-events") {
        reset({
          label: selectedNode.data.label || nodeDef?.label || "",
          apiCall: {
            enabled: nodeConfig.apiCall?.enabled || false,
            url: nodeConfig.apiCall?.url || "",
            method: nodeConfig.apiCall?.method || "POST",
            headers: nodeConfig.apiCall?.headers || {},
            body: nodeConfig.apiCall?.body || "",
            correlationIdField: nodeConfig.apiCall?.correlationIdField || "",
            correlationIdHeader: nodeConfig.apiCall?.correlationIdHeader || "",
            executionIdField: nodeConfig.apiCall?.executionIdField || "",
            executionIdHeader: nodeConfig.apiCall?.executionIdHeader || "",
            timeout: nodeConfig.apiCall?.timeout || 30,
            required: nodeConfig.apiCall?.required || false,
          },
          kafkaEvent: {
            enabled: nodeConfig.kafkaEvent?.enabled || false,
            topic: nodeConfig.kafkaEvent?.topic || "",
            correlationIdField: nodeConfig.kafkaEvent?.correlationIdField || "",
            executionIdField: nodeConfig.kafkaEvent?.executionIdField || "",
            filter: nodeConfig.kafkaEvent?.filter || {},
            timeout: nodeConfig.kafkaEvent?.timeout || 30,
            required: nodeConfig.kafkaEvent?.required || false,
          },
          aggregationStrategy: nodeConfig.aggregationStrategy || "all",
          requiredEvents: nodeConfig.requiredEvents || [],
          timeout: nodeConfig.timeout || 60,
          onTimeout: nodeConfig.onTimeout || "fail",
          outputMapping: {
            apiResponse: nodeConfig.outputMapping?.apiResponse || "api_response",
            kafkaEvent: nodeConfig.outputMapping?.kafkaEvent || "kafka_event",
          },
        })
      } else {
        // For event-trigger, handle special fields
        const defaultValues: Record<string, unknown> = {
          label: selectedNode.data.label || nodeDef?.label || "",
          ...nodeConfig,
        }
        
        // Ensure objectTypeId is set correctly (null becomes "none" for Select)
        if (defaultValues.objectTypeId === null || defaultValues.objectTypeId === undefined) {
          defaultValues.objectTypeId = "none"
        }
        
        // Convert brokers array to string (for textarea)
        if (selectedNode.data.type === "event-trigger" && defaultValues.brokers) {
          if (Array.isArray(defaultValues.brokers)) {
            defaultValues.brokers = defaultValues.brokers.join('\n')
          }
        }
        
        // Parse eventFilter if it's a string
        if (selectedNode.data.type === "event-trigger" && defaultValues.eventFilter) {
          if (typeof defaultValues.eventFilter === 'string') {
            try {
              defaultValues.eventFilter = JSON.parse(defaultValues.eventFilter)
            } catch {
              // Keep as string if invalid JSON
            }
          }
        }
        
        reset(defaultValues)
      }
    }
  }, [selectedNode, reset, nodeDef])

  // Watch for changes and update node state immediately
  const formValues = watch()
  useEffect(() => {
    if (!selectedNode) return
    
    // Debounce updates to avoid too many state updates
    const timeoutId = setTimeout(() => {
      // Transform data before saving (same logic as onSubmit)
      const transformedData = { ...formValues }
      
      // Convert objectTypeId from "none" to null for all trigger nodes
      if (transformedData.objectTypeId === "none") {
        transformedData.objectTypeId = null
      }
      
      // Transform data for event-trigger
      if (selectedNode?.data.type === "event-trigger") {
        // Convert brokers string to array
        if (transformedData.brokers && typeof transformedData.brokers === 'string') {
          transformedData.brokers = transformedData.brokers
            .split('\n')
            .map((s: string) => s.trim())
            .filter((s: string) => s.length > 0)
        }
        
        // Convert eventFilter to JSON string if it's an object
        if (transformedData.eventFilter && typeof transformedData.eventFilter === 'object') {
          try {
            transformedData.eventFilter = JSON.stringify(transformedData.eventFilter)
          } catch {
            // Keep as object if stringify fails
          }
        }
      }
      
      // Update node state immediately
      onSave(selectedNode.id, transformedData)
    }, 300) // 300ms debounce
    
    return () => clearTimeout(timeoutId)
  }, [formValues, selectedNode, onSave])

  if (!selectedNode || !nodeDef) {
    return (
      <Card className="h-full">
        <CardContent className="flex items-center justify-center h-full text-secondary-500">
          <div className="text-center">
            <p className="text-sm">No node selected</p>
            <p className="text-xs mt-1">Click on a node to configure it</p>
          </div>
        </CardContent>
      </Card>
    )
  }


  return (
    <Card className="h-full flex flex-col border-0 shadow-none">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg flex items-center space-x-2">
              {IconComponent && (
                <IconComponent className="w-5 h-5" style={{ color: nodeDef.color }} />
              )}
              <span>{nodeDef.label}</span>
            </CardTitle>
            <CardDescription className="mt-1">{nodeDef.description}</CardDescription>
          </div>
          {onClose && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="h-8 w-8 p-0"
            >
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto pb-4">
        <div className="space-y-4">
          {/* Common Fields */}
          <div className="space-y-2">
            <Label htmlFor="label">Node Label</Label>
            <Input
              id="label"
              {...register("label", { required: "Label is required" })}
              placeholder="Enter node label"
            />
            {errors.label && (
              <p className="text-sm text-error-600">{errors.label.message as string}</p>
            )}
          </div>

          {/* Node-specific fields based on type */}
          {renderNodeSpecificFields(selectedNode.data.type, register, control, errors, watch, setValue, objectTypesMap, formState, nodes, selectedNode.id)}
        </div>
      </CardContent>
    </Card>
  )
}

function renderNodeSpecificFields(
  nodeType: WorkflowNodeType,
  register: ReturnType<typeof useForm>["register"],
  control: ReturnType<typeof useForm>["control"],
  errors: ReturnType<typeof useForm>["formState"]["errors"],
  watch: ReturnType<typeof useForm>["watch"],
  setValue: ReturnType<typeof useForm>["setValue"],
  objectTypesMap: Map<string, { name: string; fields: any[] }>,
  formState: ReturnType<typeof useForm>["formState"],
  nodes: Node[],
  currentNodeId: string
) {
  // Get trigger object type from workflow (find trigger node)
  const triggerNode = nodes.find((n) => 
    n.data?.type === "api-trigger" || 
    n.data?.type === "event-trigger" || 
    n.data?.type === "schedule-trigger"
  )
  const triggerObjectTypeId = triggerNode?.data?.config?.objectTypeId || null

  switch (nodeType) {
    case "api-trigger":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="objectTypeId">Object Type (Trigger Data Structure)</Label>
            <Controller
              name="objectTypeId"
              control={control}
              render={({ field }) => {
                const availableObjectTypes = Array.from(objectTypesMap.entries()).map(([id, def]) => ({
                  id,
                  name: def.name,
                }))
                return (
                  <Select 
                    value={field.value === null || field.value === undefined || field.value === "" ? "none" : String(field.value)} 
                    onValueChange={(value) => {
                      field.onChange(value)
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select object type for trigger data" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">None (no structured data)</SelectItem>
                      {availableObjectTypes.map((type) => (
                        <SelectItem key={type.id} value={type.id}>
                          {type.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )
              }}
            />
            <p className="text-xs text-secondary-500">
              Select the object type that represents the structure of data this trigger will receive. This helps other nodes suggest fields from trigger data.
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="path">API Path</Label>
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
            <Label htmlFor="method">HTTP Method</Label>
            <Controller
              name="method"
              control={control}
              defaultValue="POST"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select method" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="GET">GET</SelectItem>
                    <SelectItem value="POST">POST</SelectItem>
                    <SelectItem value="PUT">PUT</SelectItem>
                    <SelectItem value="DELETE">DELETE</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>
        </>
      )

    case "schedule-trigger":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="cron">Cron Expression</Label>
            <Input
              id="cron"
              {...register("cron", { required: "Cron expression is required" })}
              placeholder="0 0 * * *"
            />
            {errors.cron && (
              <p className="text-sm text-error-600">{errors.cron.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="timezone">Timezone</Label>
            <Input
              id="timezone"
              {...register("timezone")}
              placeholder="UTC"
            />
          </div>
        </>
      )

    case "send-email":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="template">Template ID</Label>
            <Input
              id="template"
              {...register("template")}
              placeholder="template-id"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="recipients">Recipients</Label>
            <Textarea
              id="recipients"
              {...register("recipients", { required: "Recipients are required" })}
              placeholder="user@example.com"
            />
            {errors.recipients && (
              <p className="text-sm text-error-600">{errors.recipients.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Controller
              name="subject"
              control={control}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Subject"
                  description="Email subject with template variables"
                  placeholder="Enter subject with {{variables}}..."
                  rows={2}
                />
              )}
            />
          </div>
        </>
      )

    case "send-sms":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="template">Template ID</Label>
            <Input
              id="template"
              {...register("template")}
              placeholder="template-id"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="recipients">Phone Numbers</Label>
            <Textarea
              id="recipients"
              {...register("recipients", { required: "Recipients are required" })}
              placeholder="+1234567890"
            />
            {errors.recipients && (
              <p className="text-sm text-error-600">{errors.recipients.message as string}</p>
            )}
          </div>
        </>
      )

    case "condition":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <GitBranch className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Condition Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                This node has 2 outputs: <strong>True</strong> (left) and <strong>False</strong> (right)
              </p>
            </div>

            <Controller
              name="field"
              control={control}
              rules={{ required: "Field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    // Convert to string format for backward compatibility
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Field"
                  description="Select the field to check in the condition"
                  required
                  placeholder="user.status"
                  showManualEntry={true}
                  objectTypes={objectTypesMap}
                />
              )}
            />
            {errors.field && (
              <p className="text-sm text-error-600">{errors.field.message as string}</p>
            )}

            <div className="space-y-2">
              <Label htmlFor="operator">Operator</Label>
              <Controller
                name="operator"
                control={control}
                defaultValue="equals"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select operator" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="equals">Equals (=)</SelectItem>
                      <SelectItem value="not-equals">Not Equals (≠)</SelectItem>
                      <SelectItem value="greater-than">Greater Than (&gt;)</SelectItem>
                      <SelectItem value="greater-than-or-equal">Greater Than or Equal (≥)</SelectItem>
                      <SelectItem value="less-than">Less Than (&lt;)</SelectItem>
                      <SelectItem value="less-than-or-equal">Less Than or Equal (≤)</SelectItem>
                      <SelectItem value="contains">Contains</SelectItem>
                      <SelectItem value="not-contains">Not Contains</SelectItem>
                      <SelectItem value="starts-with">Starts With</SelectItem>
                      <SelectItem value="ends-with">Ends With</SelectItem>
                      <SelectItem value="is-empty">Is Empty</SelectItem>
                      <SelectItem value="is-not-empty">Is Not Empty</SelectItem>
                      <SelectItem value="is-null">Is Null</SelectItem>
                      <SelectItem value="is-not-null">Is Not Null</SelectItem>
                      <SelectItem value="in">In (list)</SelectItem>
                      <SelectItem value="not-in">Not In (list)</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="value">Compare Value</Label>
              <Controller
                name="value"
                control={control}
                render={({ field }) => (
                  <ContextFieldSelector
                    value={field.value || ""}
                    onChange={field.onChange}
                    nodes={nodes}
                    currentNodeId={currentNodeId}
                    triggerObjectTypeId={triggerObjectTypeId}
                    objectTypes={objectTypesMap}
                    label="Compare Value"
                    description="Select a field from context or enter a literal value to compare"
                    placeholder="Select field or enter value"
                    required={false}
                  />
                )}
              />
              <p className="text-xs text-secondary-500">
                Leave empty for "Is Empty", "Is Not Empty", "Is Null", "Is Not Null" operators
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="trueLabel">True Branch Label</Label>
              <Input
                id="trueLabel"
                {...register("trueLabel")}
                placeholder="True (default)"
                defaultValue="True"
              />
              <p className="text-xs text-secondary-500">
                Label for the output handle when condition is true
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="falseLabel">False Branch Label</Label>
              <Input
                id="falseLabel"
                {...register("falseLabel")}
                placeholder="False (default)"
                defaultValue="False"
              />
              <p className="text-xs text-secondary-500">
                Label for the output handle when condition is false
              </p>
            </div>
          </div>
        </>
      )

    case "delay":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="duration">Duration (seconds)</Label>
            <Input
              id="duration"
              type="number"
              {...register("duration", {
                required: "Duration is required",
                min: { value: 1, message: "Duration must be at least 1 second" },
              })}
              placeholder="60"
            />
            {errors.duration && (
              <p className="text-sm text-error-600">{errors.duration.message as string}</p>
            )}
          </div>
        </>
      )

    case "send-push":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="template">Template ID</Label>
            <Input
              id="template"
              {...register("template")}
              placeholder="template-id"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="recipients">Device Tokens / User IDs</Label>
            <Textarea
              id="recipients"
              {...register("recipients", { required: "Recipients are required" })}
              placeholder="device-token-1, device-token-2"
            />
            {errors.recipients && (
              <p className="text-sm text-error-600">{errors.recipients.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Controller
              name="title"
              control={control}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Title"
                  description="Notification title with template variables"
                  placeholder="Enter title with {{variables}}..."
                  rows={2}
                />
              )}
            />
          </div>
        </>
      )

    case "send-in-app":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="template">Template ID</Label>
            <Input
              id="template"
              {...register("template")}
              placeholder="template-id"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="recipients">User IDs</Label>
            <Textarea
              id="recipients"
              {...register("recipients", { required: "Recipients are required" })}
              placeholder="user-id-1, user-id-2"
            />
            {errors.recipients && (
              <p className="text-sm text-error-600">{errors.recipients.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="notificationType">Notification Type</Label>
            <Controller
              name="notificationType"
              control={control}
              defaultValue="info"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="info">Info</SelectItem>
                    <SelectItem value="success">Success</SelectItem>
                    <SelectItem value="warning">Warning</SelectItem>
                    <SelectItem value="error">Error</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="expiry">Expiry (seconds)</Label>
            <Input
              id="expiry"
              type="number"
              {...register("expiry")}
              placeholder="86400"
            />
          </div>
        </>
      )

    case "send-slack":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="channel">Channel</Label>
            <Input
              id="channel"
              {...register("channel", { required: "Channel is required" })}
              placeholder="#general or channel-id"
            />
            {errors.channel && (
              <p className="text-sm text-error-600">{errors.channel.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Controller
              name="message"
              control={control}
              rules={{ required: "Message is required" }}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Message"
                  description="Slack message with template variables"
                  placeholder="Enter message with {{variables}}..."
                  rows={4}
                  required={true}
                />
              )}
            />
            {errors.message && (
              <p className="text-sm text-error-600">{errors.message.message as string}</p>
            )}
          </div>
        </>
      )

    case "send-discord":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="channelId">Channel ID</Label>
            <Input
              id="channelId"
              {...register("channelId", { required: "Channel ID is required" })}
              placeholder="123456789012345678"
            />
            {errors.channelId && (
              <p className="text-sm text-error-600">{errors.channelId.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Controller
              name="content"
              control={control}
              rules={{ required: "Content is required" }}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Message Content"
                  description="Discord message content with template variables"
                  placeholder="Enter content with {{variables}}..."
                  rows={4}
                  required={true}
                />
              )}
            />
            {errors.content && (
              <p className="text-sm text-error-600">{errors.content.message as string}</p>
            )}
          </div>
        </>
      )

    case "send-teams":
      return (
        <>
          <div className="space-y-2">
            <Controller
              name="title"
              control={control}
              rules={{ required: "Title is required" }}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Title"
                  description="Message title with template variables"
                  placeholder="Enter title with {{variables}}..."
                  rows={2}
                  required={true}
                />
              )}
            />
            {errors.title && (
              <p className="text-sm text-error-600">{errors.title.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Controller
              name="text"
              control={control}
              rules={{ required: "Text is required" }}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Message Text"
                  description="Teams message text with template variables"
                  placeholder="Enter text with {{variables}}..."
                  rows={4}
                  required={true}
                />
              )}
            />
            {errors.text && (
              <p className="text-sm text-error-600">{errors.text.message as string}</p>
            )}
          </div>
        </>
      )

    case "send-webhook":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="url">Webhook URL</Label>
            <Input
              id="url"
              {...register("url", { required: "URL is required" })}
              placeholder="https://example.com/webhook"
            />
            {errors.url && (
              <p className="text-sm text-error-600">{errors.url.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="method">HTTP Method</Label>
            <Controller
              name="method"
              control={control}
              defaultValue="POST"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select method" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="POST">POST</SelectItem>
                    <SelectItem value="PUT">PUT</SelectItem>
                    <SelectItem value="PATCH">PATCH</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>
          <div className="space-y-2">
            <Controller
              name="payload"
              control={control}
              render={({ field }) => (
                <TemplateInput
                  value={field.value || ""}
                  onChange={field.onChange}
                  nodes={nodes}
                  currentNodeId={currentNodeId}
                  triggerObjectTypeId={triggerObjectTypeId}
                  objectTypes={objectTypesMap}
                  label="Payload (JSON)"
                  description="JSON payload for the webhook request with template variables"
                  placeholder='{"key": "{{value}}"}'
                  rows={6}
                />
              )}
            />
          </div>
        </>
      )

    case "ab-test":
      return (
        <>
          <div className="space-y-2">
            <Label htmlFor="test_id">A/B Test ID</Label>
            <Input
              id="test_id"
              {...register("test_id", { required: "A/B Test ID is required" })}
              placeholder="ab-test-123"
            />
            {errors.test_id && (
              <p className="text-sm text-error-600">{errors.test_id.message as string}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="variant_a_label">Variant A Label</Label>
            <Input
              id="variant_a_label"
              {...register("variant_a_label")}
              placeholder="Variant A"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="variant_b_label">Variant B Label</Label>
            <Input
              id="variant_b_label"
              {...register("variant_b_label")}
              placeholder="Variant B"
            />
          </div>
        </>
      )

    case "wait-events":
      return (
        <WaitForEventsNodeProperties 
          errors={errors as Record<string, { message?: string }>}
          register={register}
          watch={watch}
          control={control}
          setValue={setValue}
          formState={formState}
        />
      )

    case "switch":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <Shuffle className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Switch Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Multi-case branching based on field value
              </p>
            </div>

            <Controller
              name="field"
              control={control}
              rules={{ required: "Field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Switch Field"
                  description="Field to evaluate for switch cases"
                  required
                  placeholder="user.status"
                  showManualEntry={true}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <div className="space-y-2">
              <Label htmlFor="defaultCase">Default Case Label</Label>
              <Input
                id="defaultCase"
                {...register("defaultCase")}
                placeholder="default"
              />
            </div>

            <div className="space-y-2">
              <Label>Cases</Label>
              <div className="text-xs text-secondary-500 mb-2">
                Configure cases in the workflow execution context
              </div>
            </div>
          </div>
        </>
      )

    case "loop":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <Repeat className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Loop Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Iterate over an array field
              </p>
            </div>

            <Controller
              name="arrayField"
              control={control}
              rules={{ required: "Array field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Array Field"
                  description="Field containing array to iterate over"
                  required
                  placeholder="users"
                  showManualEntry={true}
                  allowedTypes={['array']}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <div className="space-y-2">
              <Label htmlFor="itemVariable">Item Variable Name</Label>
              <Input
                id="itemVariable"
                {...register("itemVariable", { required: "Item variable name is required" })}
                placeholder="item"
                defaultValue="item"
              />
              <p className="text-xs text-secondary-500">
                Variable name for current item in loop
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="indexVariable">Index Variable Name</Label>
              <Input
                id="indexVariable"
                {...register("indexVariable")}
                placeholder="index"
                defaultValue="index"
              />
              <p className="text-xs text-secondary-500">
                Variable name for current index (optional)
              </p>
            </div>
          </div>
        </>
      )

    case "merge":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <GitMerge className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Merge Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Combine multiple branches into one
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="mergeStrategy">Merge Strategy</Label>
              <Controller
                name="mergeStrategy"
                control={control}
                defaultValue="first"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select merge strategy" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="first">First (use first branch data)</SelectItem>
                      <SelectItem value="last">Last (use last branch data)</SelectItem>
                      <SelectItem value="merge">Merge (combine all data)</SelectItem>
                      <SelectItem value="array">Array (combine as array)</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="inputCount">Number of Inputs</Label>
              <Input
                id="inputCount"
                type="number"
                min="2"
                max="10"
                {...register("inputCount", {
                  required: "Input count is required",
                  min: { value: 2, message: "At least 2 inputs required" },
                  max: { value: 10, message: "Maximum 10 inputs" },
                })}
                placeholder="2"
                defaultValue="2"
              />
              <p className="text-xs text-secondary-500">
                Number of branches to merge
              </p>
            </div>
          </div>
        </>
      )

    case "transform":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <Settings className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Transform Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Transform data from source field to target field
              </p>
            </div>

            <Controller
              name="sourceField"
              control={control}
              rules={{ required: "Source field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Source Field"
                  description="Field to read data from"
                  required
                  placeholder="user.email"
                  showManualEntry={true}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <Controller
              name="targetField"
              control={control}
              rules={{ required: "Target field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Target Field"
                  description="Field to write data to"
                  required
                  placeholder="recipient_email"
                  showManualEntry={true}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <div className="space-y-2">
              <Controller
                name="transformation"
                control={control}
                render={({ field }) => (
                  <TemplateInput
                    value={field.value || ""}
                    onChange={field.onChange}
                    nodes={nodes}
                    currentNodeId={currentNodeId}
                    triggerObjectTypeId={triggerObjectTypeId}
                    objectTypes={objectTypesMap}
                    label="Transformation Expression"
                    description="Optional: JavaScript expression with template variables (e.g., {{value}}.toUpperCase())"
                    placeholder="Optional: JavaScript expression for transformation (e.g., {{value}}.toUpperCase())"
                    rows={3}
                  />
                )}
              />
            </div>
          </div>
        </>
      )

    case "map":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <MapIcon className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Map Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Map fields from source to target structure
              </p>
            </div>

            <div className="space-y-2">
              <Label>Field Mappings</Label>
              <div className="text-xs text-secondary-500 mb-2">
                Configure field mappings in JSON format. You can use template variables in values.
              </div>
              <Controller
                name="mapping"
                control={control}
                rules={{
                  required: "Mapping is required",
                  validate: (value) => {
                    try {
                      JSON.parse(value || "{}")
                      return true
                    } catch {
                      return "Invalid JSON format"
                    }
                  },
                }}
                render={({ field }) => (
                  <TemplateInput
                    value={field.value || ""}
                    onChange={field.onChange}
                    nodes={nodes}
                    currentNodeId={currentNodeId}
                    triggerObjectTypeId={triggerObjectTypeId}
                    objectTypes={objectTypesMap}
                    label="Field Mappings"
                    description="JSON mapping with template variables in values"
                    placeholder='{"targetField1": "{{sourceField1}}", "targetField2": "{{sourceField2}}"}'
                    rows={6}
                    required={true}
                  />
                )}
              />
              {errors.mapping && (
                <p className="text-sm text-error-600">{errors.mapping.message as string}</p>
              )}
            </div>
          </div>
        </>
      )

    case "filter":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <Filter className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Filter Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Filter array based on condition
              </p>
            </div>

            <Controller
              name="arrayField"
              control={control}
              rules={{ required: "Array field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Array Field"
                  description="Array field to filter"
                  required
                  placeholder="users"
                  showManualEntry={true}
                  allowedTypes={['array']}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <Controller
              name="field"
              control={control}
              rules={{ required: "Filter field is required" }}
              render={({ field }) => (
                <FieldSelector
                  value={field.value}
                  onChange={(value) => {
                    const stringValue = typeof value === 'string' 
                      ? value 
                      : formatFieldReference(value)
                    field.onChange(stringValue)
                  }}
                  label="Filter Field"
                  description="Field in array items to filter by"
                  required
                  placeholder="user.status"
                  showManualEntry={true}
                  objectTypes={objectTypesMap}
                />
              )}
            />

            <div className="space-y-2">
              <Label htmlFor="operator">Operator</Label>
              <Controller
                name="operator"
                control={control}
                defaultValue="equals"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select operator" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="equals">Equals</SelectItem>
                      <SelectItem value="not-equals">Not Equals</SelectItem>
                      <SelectItem value="contains">Contains</SelectItem>
                      <SelectItem value="greater-than">Greater Than</SelectItem>
                      <SelectItem value="less-than">Less Than</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="value">Filter Value</Label>
              <Input
                id="value"
                {...register("value")}
                placeholder="value"
              />
            </div>
          </div>
        </>
      )

    case "read-file":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <FileText className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Read File Configuration</Label>
              </div>
              <p className="text-xs text-primary-700">
                Parse uploaded file and extract data
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="fileFormat">File Format</Label>
              <Controller
                name="fileFormat"
                control={control}
                defaultValue="csv"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select file format" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="csv">CSV</SelectItem>
                      <SelectItem value="json">JSON</SelectItem>
                      <SelectItem value="xml">XML</SelectItem>
                      <SelectItem value="excel">Excel (XLSX)</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="outputField">Output Field</Label>
              <Controller
                name="outputField"
                control={control}
                rules={{ required: "Output field is required" }}
                defaultValue="fileData"
                render={({ field }) => (
                  <FieldSelector
                    value={field.value}
                    onChange={(value) => {
                      const stringValue = typeof value === 'string' 
                        ? value 
                        : formatFieldReference(value)
                      field.onChange(stringValue)
                    }}
                    label="Output Field"
                    description="Field name to store parsed file data"
                    placeholder="fileData"
                    showManualEntry={true}
                    objectTypes={objectTypesMap}
                  />
                )}
              />
              {errors.outputField && (
                <p className="text-sm text-error-600">{errors.outputField.message as string}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="hasHeader">Has Header Row</Label>
              <Controller
                name="hasHeader"
                control={control}
                defaultValue="true"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="true">Yes</SelectItem>
                      <SelectItem value="false">No</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          </div>
        </>
      )

    case "file-trigger":
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <FolderUp className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">File Upload Trigger</Label>
              </div>
              <p className="text-xs text-primary-700">
                Trigger workflow when file is uploaded
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="acceptedFormats">Accepted File Formats</Label>
              <Textarea
                id="acceptedFormats"
                {...register("acceptedFormats")}
                placeholder="csv,json,xlsx"
                defaultValue="csv,json,xlsx"
              />
              <p className="text-xs text-secondary-500">
                Comma-separated list of accepted file formats
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="maxFileSize">Max File Size (MB)</Label>
              <Input
                id="maxFileSize"
                type="number"
                {...register("maxFileSize")}
                placeholder="10"
                defaultValue="10"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="destination">Destination Field</Label>
              <Controller
                name="destination"
                control={control}
                defaultValue="fileData"
                render={({ field }) => (
                  <FieldSelector
                    value={field.value}
                    onChange={(value) => {
                      const stringValue = typeof value === 'string' 
                        ? value 
                        : formatFieldReference(value)
                      field.onChange(stringValue)
                    }}
                    label="Destination Field"
                    description="Field name to store uploaded file data"
                    placeholder="fileData"
                    showManualEntry={true}
                    objectTypes={objectTypesMap}
                  />
                )}
              />
            </div>
          </div>
        </>
      )

    case "event-trigger":
      const selectedEventType = watch("eventType")
      
      return (
        <>
          <div className="space-y-4">
            <div className="p-3 bg-primary-50 rounded-lg border border-primary-200">
              <div className="flex items-center space-x-2 mb-2">
                <Radio className="h-4 w-4 text-primary-600" />
                <Label className="text-sm font-semibold text-primary-900">Event Trigger</Label>
              </div>
              <p className="text-xs text-primary-700">
                Trigger workflow on external event
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="objectTypeId">Object Type (Trigger Data Structure)</Label>
              <Controller
                name="objectTypeId"
                control={control}
                render={({ field }) => {
                  const availableObjectTypes = Array.from(objectTypesMap.entries()).map(([id, def]) => ({
                    id,
                    name: def.name,
                  }))
                  return (
                    <Select 
                      value={field.value === null || field.value === undefined || field.value === "" ? "none" : String(field.value)} 
                      onValueChange={(value) => {
                        field.onChange(value)
                      }}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select object type for trigger data" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">None (no structured data)</SelectItem>
                        {availableObjectTypes.map((type) => (
                          <SelectItem key={type.id} value={type.id}>
                            {type.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )
                }}
              />
              <p className="text-xs text-secondary-500">
                Select the object type that represents the structure of data this trigger will receive.
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="eventType">Event Type</Label>
              <Controller
                name="eventType"
                control={control}
                rules={{ required: "Event type is required" }}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select event type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="kafka">Kafka Event</SelectItem>
                      <SelectItem value="rabbitmq">RabbitMQ Event</SelectItem>
                      <SelectItem value="custom">Custom Event</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.eventType && (
                <p className="text-sm text-error-600">{errors.eventType.message as string}</p>
              )}
            </div>

            {selectedEventType === "kafka" && (
              <div className="space-y-2">
                <Label htmlFor="brokers">Bootstrap Servers</Label>
                <Textarea
                  id="brokers"
                  {...register("brokers", { 
                    required: "Bootstrap servers are required",
                    validate: (value) => {
                      if (!value) return "Bootstrap servers are required"
                      const servers = value.split('\n').filter((s: string) => s.trim())
                      if (servers.length === 0) return "At least one server is required"
                      return true
                    }
                  })}
                  placeholder="localhost:9092&#10;broker2:9092"
                  rows={3}
                />
                <p className="text-xs text-secondary-500">
                  Enter one broker address per line (e.g., localhost:9092)
                </p>
                {errors.brokers && (
                  <p className="text-sm text-error-600">{errors.brokers.message as string}</p>
                )}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="topic">Topic/Queue Name</Label>
              <Input
                id="topic"
                {...register("topic", { required: "Topic/Queue name is required" })}
                placeholder="user-events"
              />
              {errors.topic && (
                <p className="text-sm text-error-600">{errors.topic.message as string}</p>
              )}
            </div>

            {/* Kafka Configuration */}
            {selectedEventType === "kafka" && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="consumerGroup">Consumer Group</Label>
                  <Input
                    id="consumerGroup"
                    {...register("consumerGroup", { required: "Consumer group is required" })}
                    placeholder="notification-platform-consumer"
                  />
                  {errors.consumerGroup && (
                    <p className="text-sm text-error-600">{errors.consumerGroup.message as string}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="offset">Offset Reset</Label>
                  <Controller
                    name="offset"
                    control={control}
                    defaultValue="latest"
                    render={({ field }) => (
                      <Select value={field.value} onValueChange={field.onChange}>
                        <SelectTrigger>
                          <SelectValue placeholder="Select offset" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="earliest">Earliest (from beginning)</SelectItem>
                          <SelectItem value="latest">Latest (from now)</SelectItem>
                        </SelectContent>
                      </Select>
                    )}
                  />
                  <p className="text-xs text-secondary-500">
                    Where to start reading messages when no offset is stored
                  </p>
                </div>
              </>
            )}

            {/* RabbitMQ Configuration */}
            {selectedEventType === "rabbitmq" && (
              <>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="rabbitmqHost">Host</Label>
                    <Input
                      id="rabbitmqHost"
                      {...register("rabbitmqHost", { required: "Host is required" })}
                      placeholder="localhost"
                    />
                    {errors.rabbitmqHost && (
                      <p className="text-sm text-error-600">{errors.rabbitmqHost.message as string}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="rabbitmqPort">Port</Label>
                    <Input
                      id="rabbitmqPort"
                      type="number"
                      {...register("rabbitmqPort", { 
                        required: "Port is required",
                        valueAsNumber: true,
                        min: { value: 1, message: "Port must be greater than 0" },
                        max: { value: 65535, message: "Port must be less than 65536" }
                      })}
                      placeholder="5672"
                    />
                    {errors.rabbitmqPort && (
                      <p className="text-sm text-error-600">{errors.rabbitmqPort.message as string}</p>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="rabbitmqUsername">Username</Label>
                    <Input
                      id="rabbitmqUsername"
                      {...register("rabbitmqUsername", { required: "Username is required" })}
                      placeholder="guest"
                    />
                    {errors.rabbitmqUsername && (
                      <p className="text-sm text-error-600">{errors.rabbitmqUsername.message as string}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="rabbitmqPassword">Password</Label>
                    <Input
                      id="rabbitmqPassword"
                      type="password"
                      {...register("rabbitmqPassword", { required: "Password is required" })}
                      placeholder="guest"
                    />
                    {errors.rabbitmqPassword && (
                      <p className="text-sm text-error-600">{errors.rabbitmqPassword.message as string}</p>
                    )}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="rabbitmqVirtualHost">Virtual Host</Label>
                  <Input
                    id="rabbitmqVirtualHost"
                    {...register("rabbitmqVirtualHost")}
                    placeholder="/"
                  />
                  <p className="text-xs text-secondary-500">
                    Optional: Virtual host name (default: /)
                  </p>
                </div>
              </>
            )}

            <div className="space-y-2">
              <Label htmlFor="eventFilter">Event Filter (JSON)</Label>
              <Textarea
                id="eventFilter"
                {...register("eventFilter")}
                placeholder='{"type": "user.created"}'
                rows={3}
              />
              <p className="text-xs text-secondary-500">
                Optional: Filter events by criteria
              </p>
            </div>
          </div>
        </>
      )

    default:
      return (
        <div className="text-sm text-secondary-500">
          Configuration options for this node type will be available soon.
        </div>
      )
  }
}

