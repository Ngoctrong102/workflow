import { useState } from "react"
import { Controller, type UseFormReturn } from "react-hook-form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { HelpTooltip } from "@/components/common/HelpTooltip"
import { Plus, X, Info } from "lucide-react"

interface KeyValuePair {
  key: string
  value: string
}

interface WaitForEventsNodePropertiesProps {
  errors?: Record<string, { message?: string }>
  register: UseFormReturn<any>["register"]
  watch: UseFormReturn<any>["watch"]
  control: UseFormReturn<any>["control"]
  setValue: (name: string, value: unknown, options?: { shouldValidate?: boolean; shouldDirty?: boolean; shouldTouch?: boolean }) => void
  formState: UseFormReturn<any>["formState"]
}

export function WaitForEventsNodeProperties({ 
  errors, 
  register, 
  watch, 
  control, 
  setValue, 
  formState: { errors: formErrors } 
}: WaitForEventsNodePropertiesProps) {
  
  const apiCallEnabled = watch("apiCall.enabled") || false
  const kafkaEventEnabled = watch("kafkaEvent.enabled") || false
  const aggregationStrategy = watch("aggregationStrategy") || "all"
  const apiCallHeaders = watch("apiCall.headers") || {}
  const kafkaEventFilter = watch("kafkaEvent.filter") || {}

  const [apiHeaders, setApiHeaders] = useState<KeyValuePair[]>(
    Object.entries(apiCallHeaders).map(([key, value]) => ({ key, value: String(value) }))
  )
  const [kafkaFilters, setKafkaFilters] = useState<KeyValuePair[]>(
    Object.entries(kafkaEventFilter).map(([key, value]) => ({ key, value: String(value) }))
  )

  const updateHeaders = (pairs: KeyValuePair[]) => {
    const headers: Record<string, string> = {}
    pairs.forEach((pair) => {
      if (pair.key && pair.value) {
        headers[pair.key] = pair.value
      }
    })
    setValue("apiCall.headers", headers)
  }

  const updateFilters = (pairs: KeyValuePair[]) => {
    const filters: Record<string, string> = {}
    pairs.forEach((pair) => {
      if (pair.key && pair.value) {
        filters[pair.key] = pair.value
      }
    })
    setValue("kafkaEvent.filter", filters)
  }

  const addHeader = () => {
    const newHeaders = [...apiHeaders, { key: "", value: "" }]
    setApiHeaders(newHeaders)
    updateHeaders(newHeaders)
  }

  const removeHeader = (index: number) => {
    const newHeaders = apiHeaders.filter((_, i) => i !== index)
    setApiHeaders(newHeaders)
    updateHeaders(newHeaders)
  }

  const updateHeader = (index: number, field: "key" | "value", value: string) => {
    const newHeaders = [...apiHeaders]
    newHeaders[index][field] = value
    setApiHeaders(newHeaders)
    updateHeaders(newHeaders)
  }

  const addFilter = () => {
    const newFilters = [...kafkaFilters, { key: "", value: "" }]
    setKafkaFilters(newFilters)
    updateFilters(newFilters)
  }

  const removeFilter = (index: number) => {
    const newFilters = kafkaFilters.filter((_, i) => i !== index)
    setKafkaFilters(newFilters)
    updateFilters(newFilters)
  }

  const updateFilter = (index: number, field: "key" | "value", value: string) => {
    const newFilters = [...kafkaFilters]
    newFilters[index][field] = value
    setKafkaFilters(newFilters)
    updateFilters(newFilters)
  }

  return (
    <div className="space-y-4">
      {/* API Call Configuration */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-base">API Call Configuration</CardTitle>
              <CardDescription>Configure API call to wait for response</CardDescription>
            </div>
            <Controller
              name="apiCall.enabled"
              control={control}
              render={({ field }) => (
                <Checkbox
                  checked={field.value || false}
                  onCheckedChange={field.onChange}
                  aria-label="Enable API Call Configuration"
                />
              )}
            />
          </div>
        </CardHeader>
        {apiCallEnabled && (
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="apiCall.url">
                URL *
                <HelpTooltip content="The API endpoint URL to call. Supports template variables like {{variable_name}}">
                  <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                </HelpTooltip>
              </Label>
              <Input
                id="apiCall.url"
                {...register("apiCall.url", {
                  required: apiCallEnabled ? "URL is required" : false,
                  validate: (value) => {
                    if (!apiCallEnabled) return true
                    if (!value) return "URL is required"
                    try {
                      new URL(value)
                      return true
                    } catch {
                      return "Invalid URL format"
                    }
                  },
                })}
                placeholder="https://api.example.com/webhook"
                aria-describedby={formErrors.apiCall?.url ? "apiCall-url-error" : undefined}
                aria-invalid={!!formErrors.apiCall?.url}
              />
              {formErrors.apiCall?.url && (
                <p 
                  id="apiCall-url-error" 
                  className="text-sm text-error-600" 
                  role="alert"
                  aria-live="polite"
                >
                  {formErrors.apiCall.url.message as string}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="apiCall.method">HTTP Method *</Label>
              <Controller
                name="apiCall.method"
                control={control}
                rules={{ required: apiCallEnabled ? "Method is required" : false }}
                render={({ field }) => (
                  <Select value={field.value || "POST"} onValueChange={field.onChange}>
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

            <div className="space-y-2">
              <Label>Headers</Label>
              <div className="space-y-2">
                {apiHeaders.map((header, index) => (
                  <div key={index} className="flex gap-2">
                    <Input
                      placeholder="Header name"
                      value={header.key}
                      onChange={(e) => updateHeader(index, "key", e.target.value)}
                      className="flex-1"
                    />
                    <Input
                      placeholder="Header value"
                      value={header.value}
                      onChange={(e) => updateHeader(index, "value", e.target.value)}
                      className="flex-1"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeHeader(index)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                <Button type="button" variant="outline" size="sm" onClick={addHeader}>
                  <Plus className="h-4 w-4 mr-1" />
                  Add Header
                </Button>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="apiCall.body">
                Request Body
                <HelpTooltip content="Request body in JSON format. Supports template variables. Execution ID and Correlation ID will be auto-injected.">
                  <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                </HelpTooltip>
              </Label>
              <Textarea
                id="apiCall.body"
                {...register("apiCall.body")}
                placeholder='{"key": "value", "execution_id": "{{_execution_id}}", "correlation_id": "{{_correlation_id}}"}'
                rows={4}
                className="font-mono text-sm"
              />
              <p className="text-xs text-secondary-500">
                Tip: Use <code className="bg-secondary-100 px-1 rounded">{"{{_execution_id}}"}</code> and <code className="bg-secondary-100 px-1 rounded">{"{{_correlation_id}}"}</code> to auto-inject correlation data
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="apiCall.correlationIdField">
                  Correlation ID Field *
                  <HelpTooltip content="Field name in response that contains correlation ID">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="apiCall.correlationIdField"
                  {...register("apiCall.correlationIdField", {
                    required: apiCallEnabled ? "Correlation ID field is required" : false,
                  })}
                  placeholder="correlationId"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="apiCall.correlationIdHeader">Correlation ID Header</Label>
                <Input
                  id="apiCall.correlationIdHeader"
                  {...register("apiCall.correlationIdHeader")}
                  placeholder="X-Correlation-ID"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="apiCall.executionIdField">
                  Execution ID Field *
                  <HelpTooltip content="Field name in response that contains execution ID">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="apiCall.executionIdField"
                  {...register("apiCall.executionIdField", {
                    required: apiCallEnabled ? "Execution ID field is required" : false,
                  })}
                  placeholder="executionId"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="apiCall.executionIdHeader">Execution ID Header</Label>
                <Input
                  id="apiCall.executionIdHeader"
                  {...register("apiCall.executionIdHeader")}
                  placeholder="X-Execution-ID"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="apiCall.timeout">
                  Timeout (seconds) *
                  <HelpTooltip content="Maximum time to wait for API response">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="apiCall.timeout"
                  type="number"
                  min={1}
                  {...register("apiCall.timeout", {
                    required: apiCallEnabled ? "Timeout is required" : false,
                    valueAsNumber: true,
                    min: { value: 1, message: "Timeout must be at least 1 second" },
                  })}
                  placeholder="30"
                />
              </div>
              <div className="flex items-center space-x-2 pt-8">
                <Controller
                  name="apiCall.required"
                  control={control}
                  render={({ field }) => (
                    <Checkbox
                      checked={field.value || false}
                      onCheckedChange={field.onChange}
                    />
                  )}
                />
                <Label className="cursor-pointer">
                  Required
                  <HelpTooltip content="If checked, workflow will fail if API call times out">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                  </HelpTooltip>
                </Label>
              </div>
            </div>
          </CardContent>
        )}
      </Card>

      {/* Kafka Event Configuration */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-base">Kafka Event Configuration</CardTitle>
              <CardDescription>Configure Kafka event to wait for</CardDescription>
            </div>
            <Controller
              name="kafkaEvent.enabled"
              control={control}
              render={({ field }) => (
                <Checkbox
                  checked={field.value || false}
                  onCheckedChange={field.onChange}
                />
              )}
            />
          </div>
        </CardHeader>
        {kafkaEventEnabled && (
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="kafkaEvent.topic">
                Topic *
                <HelpTooltip content="Kafka topic to listen for events">
                  <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                </HelpTooltip>
              </Label>
              <Input
                id="kafkaEvent.topic"
                {...register("kafkaEvent.topic", {
                  required: kafkaEventEnabled ? "Topic is required" : false,
                })}
                placeholder="workflow-events"
              />
              {formErrors.kafkaEvent?.topic && (
                <p className="text-sm text-error-600">
                  {formErrors.kafkaEvent.topic.message as string}
                </p>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="kafkaEvent.correlationIdField">
                  Correlation ID Field *
                  <HelpTooltip content="Field name in event that contains correlation ID">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="kafkaEvent.correlationIdField"
                  {...register("kafkaEvent.correlationIdField", {
                    required: kafkaEventEnabled ? "Correlation ID field is required" : false,
                  })}
                  placeholder="correlationId"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="kafkaEvent.executionIdField">
                  Execution ID Field *
                  <HelpTooltip content="Field name in event that contains execution ID">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="kafkaEvent.executionIdField"
                  {...register("kafkaEvent.executionIdField", {
                    required: kafkaEventEnabled ? "Execution ID field is required" : false,
                  })}
                  placeholder="executionId"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label>
                Event Filter
                <HelpTooltip content="Key-value pairs to filter events. Only events matching all filters will be processed.">
                  <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                </HelpTooltip>
              </Label>
              <div className="space-y-2">
                {kafkaFilters.map((filter, index) => (
                  <div key={index} className="flex gap-2">
                    <Input
                      placeholder="Filter key"
                      value={filter.key}
                      onChange={(e) => updateFilter(index, "key", e.target.value)}
                      className="flex-1"
                    />
                    <Input
                      placeholder="Filter value"
                      value={filter.value}
                      onChange={(e) => updateFilter(index, "value", e.target.value)}
                      className="flex-1"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeFilter(index)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                <Button type="button" variant="outline" size="sm" onClick={addFilter}>
                  <Plus className="h-4 w-4 mr-1" />
                  Add Filter
                </Button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="kafkaEvent.timeout">
                  Timeout (seconds) *
                  <HelpTooltip content="Maximum time to wait for Kafka event">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" aria-hidden="true" />
                  </HelpTooltip>
                </Label>
                <Input
                  id="kafkaEvent.timeout"
                  type="number"
                  min={1}
                  {...register("kafkaEvent.timeout", {
                    required: kafkaEventEnabled ? "Timeout is required" : false,
                    valueAsNumber: true,
                    min: { value: 1, message: "Timeout must be at least 1 second" },
                  })}
                  placeholder="30"
                />
              </div>
              <div className="flex items-center space-x-2 pt-8">
                <Controller
                  name="kafkaEvent.required"
                  control={control}
                  render={({ field }) => (
                    <Checkbox
                      checked={field.value || false}
                      onCheckedChange={field.onChange}
                    />
                  )}
                />
                <Label className="cursor-pointer">
                  Required
                  <HelpTooltip content="If checked, workflow will fail if Kafka event times out">
                    <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
                  </HelpTooltip>
                </Label>
              </div>
            </div>
          </CardContent>
        )}
      </Card>

      {/* Aggregation Strategy */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Aggregation Strategy</CardTitle>
          <CardDescription>Configure how to aggregate multiple events</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="aggregationStrategy">
              Strategy *
              <HelpTooltip content="How to wait for events: All (wait for all enabled), Any (wait for any), Required Only (wait only for required events), Custom (wait for selected events)">
                <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
              </HelpTooltip>
            </Label>
            <Controller
              name="aggregationStrategy"
              control={control}
              rules={{ required: "Strategy is required" }}
              render={({ field }) => (
                <Select value={field.value || "all"} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select strategy" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Wait for All</SelectItem>
                    <SelectItem value="any">Wait for Any</SelectItem>
                    <SelectItem value="required_only">Wait for Required Only</SelectItem>
                    <SelectItem value="custom">Custom</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          {aggregationStrategy === "custom" && (
            <div className="space-y-2">
              <Label>Required Events *</Label>
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <Controller
                    name="requiredEvents"
                    control={control}
                    render={({ field }) => {
                      const value = field.value || []
                      return (
                        <>
                          <Checkbox
                            checked={value.includes("api_response")}
                            onCheckedChange={(checked) => {
                              const newValue = checked
                                ? [...value, "api_response"]
                                : value.filter((v: string) => v !== "api_response")
                              field.onChange(newValue)
                            }}
                          />
                          <Label className="cursor-pointer">API Response</Label>
                        </>
                      )
                    }}
                  />
                </div>
                <div className="flex items-center space-x-2">
                  <Controller
                    name="requiredEvents"
                    control={control}
                    render={({ field }) => {
                      const value = field.value || []
                      return (
                        <>
                          <Checkbox
                            checked={value.includes("kafka_event")}
                            onCheckedChange={(checked) => {
                              const newValue = checked
                                ? [...value, "kafka_event"]
                                : value.filter((v: string) => v !== "kafka_event")
                              field.onChange(newValue)
                            }}
                          />
                          <Label className="cursor-pointer">Kafka Event</Label>
                        </>
                      )
                    }}
                  />
                </div>
              </div>
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="timeout">
              Overall Timeout (seconds) *
              <HelpTooltip content="Maximum time to wait for all events">
                <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
              </HelpTooltip>
            </Label>
            <Input
              id="timeout"
              type="number"
              min={1}
              {...register("timeout", {
                required: "Overall timeout is required",
                valueAsNumber: true,
                min: { value: 1, message: "Timeout must be at least 1 second" },
              })}
              placeholder="60"
            />
            {(errors?.timeout || formErrors.timeout) && (
              <p className="text-sm text-error-600">
                {(errors?.timeout?.message || formErrors.timeout?.message) as string}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="onTimeout">
              On Timeout Behavior *
              <HelpTooltip content="What to do when timeout is reached">
                <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
              </HelpTooltip>
            </Label>
            <Controller
              name="onTimeout"
              control={control}
              rules={{ required: "On timeout behavior is required" }}
              render={({ field }) => (
                <Select value={field.value || "fail"} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select behavior" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="fail">Fail</SelectItem>
                    <SelectItem value="continue">Continue</SelectItem>
                    <SelectItem value="continue_with_partial">Continue with Partial</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>
        </CardContent>
      </Card>

      {/* Output Mapping */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Output Mapping</CardTitle>
          <CardDescription>Configure how to map event data to workflow context</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="outputMapping.apiResponse">
              API Response Path
              <HelpTooltip content="Path in workflow context where API response will be stored (e.g., 'api_response')">
                <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
              </HelpTooltip>
            </Label>
            <Input
              id="outputMapping.apiResponse"
              {...register("outputMapping.apiResponse")}
              placeholder="api_response"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="outputMapping.kafkaEvent">
              Kafka Event Path
              <HelpTooltip content="Path in workflow context where Kafka event will be stored (e.g., 'kafka_event')">
                <Info className="h-3 w-3 ml-1 inline text-secondary-400" />
              </HelpTooltip>
            </Label>
            <Input
              id="outputMapping.kafkaEvent"
              {...register("outputMapping.kafkaEvent")}
              placeholder="kafka_event"
            />
          </div>
        </CardContent>
      </Card>

      {/* Validation Summary */}
      {(!apiCallEnabled && !kafkaEventEnabled) && (
        <div className="rounded-lg border border-warning-200 bg-warning-50 p-3">
          <p className="text-sm text-warning-800">
            At least one event type (API Call or Kafka Event) must be enabled.
          </p>
        </div>
      )}
    </div>
  )
}

