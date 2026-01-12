import { useState, useEffect } from "react"
import { useForm, Controller } from "react-hook-form"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { ChevronDown, ChevronRight, HelpCircle } from "lucide-react"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { KeyValuePairsEditor } from "./KeyValuePairsEditor"
import { JsonEditor } from "./JsonEditor"
import { MvelInputEditor } from "./MvelInputEditor"
import { cn } from "@/lib/utils"
import type { SchemaDefinition } from "./SchemaEditor"

export interface ApiCallConfig {
  url?: string
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
  headers?: Record<string, string>
  body?: any
  authentication?: {
    type?: "api-key" | "bearer-token"
    apiKey?: string
    headerName?: string
    token?: string
  }
  timeout?: number
  retry?: {
    maxAttempts?: number
    backoffStrategy?: "exponential" | "linear" | "fixed"
  }
}

interface ApiCallConfigFieldsProps {
  config: ApiCallConfig
  onChange: (config: ApiCallConfig) => void
  inputSchema?: SchemaDefinition[]
}

export function ApiCallConfigFields({ config, onChange, inputSchema = [] }: ApiCallConfigFieldsProps) {
  const [showAuth, setShowAuth] = useState(!!config.authentication?.type)
  const [showRetry, setShowRetry] = useState(!!config.retry)

  const { register, control, watch, setValue, reset, formState: { errors } } = useForm<ApiCallConfig>({
    defaultValues: {
      url: config.url || "",
      method: config.method || "GET",
      headers: config.headers || {},
      body: config.body || {},
      authentication: config.authentication || { type: undefined },
      timeout: config.timeout || 5000,
      retry: config.retry || {
        maxAttempts: 3,
        backoffStrategy: "exponential",
      },
    },
  })

  // Update form values when config prop changes (e.g., when loading from backend)
  useEffect(() => {
    reset({
      url: config.url || "",
      method: config.method || "GET",
      headers: config.headers || {},
      body: config.body || {},
      authentication: config.authentication || { type: undefined },
      timeout: config.timeout || 5000,
      retry: config.retry || {
        maxAttempts: 3,
        backoffStrategy: "exponential",
      },
    })
    setShowAuth(!!config.authentication?.type)
    setShowRetry(!!config.retry)
  }, [config, reset])

  // Watch only specific fields to avoid unnecessary re-renders
  const urlValue = watch("url")
  const methodValue = watch("method")
  const headersValue = watch("headers")
  const bodyValue = watch("body")
  const authValue = watch("authentication")
  const timeoutValue = watch("timeout")
  const retryValue = watch("retry")

  // Update parent when form changes
  const handleChange = (field: keyof ApiCallConfig, value: any) => {
    setValue(field, value, { shouldDirty: true })
    // Get current values and update
    const currentValues = {
      url: urlValue,
      method: methodValue,
      headers: headersValue,
      body: bodyValue,
      authentication: authValue,
      timeout: timeoutValue,
      retry: retryValue,
    }
    const newConfig = { ...currentValues, [field]: value }
    onChange(newConfig)
  }

  const handleHeadersChange = (headers: Record<string, string>) => {
    handleChange("headers", headers)
  }

  // Debounce body changes to avoid lag
  const handleBodyChange = (body: any) => {
    handleChange("body", body)
  }

  const handleAuthTypeChange = (type: "api-key" | "bearer-token" | undefined) => {
    if (!type) {
      handleChange("authentication", undefined)
      setShowAuth(false)
    } else {
      const auth = {
        type,
        apiKey: type === "api-key" ? config.authentication?.apiKey : undefined,
        headerName: type === "api-key" ? (config.authentication?.headerName || "X-API-Key") : undefined,
        token: type === "bearer-token" ? config.authentication?.token : undefined,
      }
      handleChange("authentication", auth)
      setShowAuth(true)
    }
  }

  const handleAuthFieldChange = (field: "apiKey" | "headerName" | "token", value: string) => {
    const auth = {
      ...config.authentication,
      [field]: value,
    }
    handleChange("authentication", auth)
  }

  const handleRetryChange = (field: "maxAttempts" | "backoffStrategy", value: number | string) => {
    const retry = {
      ...config.retry,
      [field]: value,
    }
    handleChange("retry", retry)
  }

  const validateUrl = (url: string): boolean => {
    if (!url) return false
    try {
      new URL(url)
      return true
    } catch {
      return false
    }
  }

  const urlError = urlValue && !validateUrl(urlValue) ? "Must be a valid URL" : ""

  return (
    <div className="space-y-6">
      {/* URL */}
      <MvelInputEditor
        value={urlValue || ""}
        onChange={(value) => handleChange("url", value)}
        inputSchema={inputSchema}
        label="URL"
        required
        placeholder="https://api.example.com/endpoint or @{input.apiUrl}"
        description="Full URL for the API endpoint. Type @{ to see available input fields."
        errors={errors.url?.message as string || (urlError || undefined)}
      />

      {/* Method */}
      <div className="space-y-2">
        <Label htmlFor="api-method">
          Method <span className="text-error-600">*</span>
        </Label>
        <Controller
          name="method"
          control={control}
          rules={{ required: "Method is required" }}
          render={({ field }) => (
            <Select
              value={field.value || methodValue}
              onValueChange={(value) => {
                field.onChange(value)
                handleChange("method", value as ApiCallConfig["method"])
              }}
            >
              <SelectTrigger id="api-method">
                <SelectValue placeholder="Select HTTP method" />
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
        <p className="text-xs text-secondary-500">
          HTTP method for the API request.
        </p>
      </div>

      {/* Headers */}
      <div className="space-y-2">
        <Label>Headers</Label>
        <KeyValuePairsEditor
          pairs={headersValue || {}}
          onChange={handleHeadersChange}
          keyPlaceholder="Header name (e.g., Content-Type)"
          valuePlaceholder="Header value or @{input.headerValue}"
          emptyMessage="No headers. Click 'Add Pair' to add headers."
          inputSchema={inputSchema}
        />
        <p className="text-xs text-secondary-500">
          Optional HTTP headers. Type @{`{`} in value field to see available input fields.
        </p>
      </div>

      {/* Body */}
      <div className="space-y-2">
        <JsonEditor
          value={bodyValue || {}}
          onChange={handleBodyChange}
          label="Request Body"
          description="Optional request body. Can be JSON object or template variable."
          placeholder='{"key": "value"} or use ${variable}'
        />
      </div>

      {/* Authentication - Collapsible */}
      <Card className="border-secondary-200">
        <CardHeader
          className="cursor-pointer"
          onClick={() => setShowAuth(!showAuth)}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              {showAuth ? (
                <ChevronDown className="h-4 w-4 text-secondary-500" />
              ) : (
                <ChevronRight className="h-4 w-4 text-secondary-500" />
              )}
              <CardTitle className="text-base">Authentication</CardTitle>
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <HelpCircle className="h-4 w-4 text-secondary-400 cursor-help" />
                  </TooltipTrigger>
                  <TooltipContent>
                    <p className="max-w-xs">
                      Configure API authentication. Choose API Key or Bearer Token authentication.
                    </p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            </div>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={(e) => {
                e.stopPropagation()
                handleAuthTypeChange(undefined)
              }}
              className="cursor-pointer"
            >
              {showAuth ? "Remove" : "Add"}
            </Button>
          </div>
          <CardDescription className="text-xs">
            Optional authentication configuration
          </CardDescription>
        </CardHeader>
        {showAuth && (
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>Authentication Type</Label>
              <Select
                value={authValue?.type || ""}
                onValueChange={(value) => {
                  if (value === "none") {
                    handleAuthTypeChange(undefined)
                  } else {
                    handleAuthTypeChange(value as "api-key" | "bearer-token")
                  }
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select authentication type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">None</SelectItem>
                  <SelectItem value="api-key">API Key</SelectItem>
                  <SelectItem value="bearer-token">Bearer Token</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {authValue?.type === "api-key" && (
              <div className="space-y-4 pl-4 border-l-2 border-secondary-200">
                <div className="space-y-2">
                  <Label htmlFor="api-key">
                    API Key <span className="text-error-600">*</span>
                  </Label>
                  <Input
                    id="api-key"
                    type="password"
                    value={authValue?.apiKey || ""}
                    onChange={(e) => handleAuthFieldChange("apiKey", e.target.value)}
                    placeholder="Enter API key"
                  />
                  <p className="text-xs text-secondary-500">
                    API key value. Supports template variables.
                  </p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="api-key-header">Header Name</Label>
                  <Input
                    id="api-key-header"
                    value={authValue?.headerName || "X-API-Key"}
                    onChange={(e) => handleAuthFieldChange("headerName", e.target.value)}
                    placeholder="X-API-Key"
                  />
                  <p className="text-xs text-secondary-500">
                    HTTP header name for the API key (default: X-API-Key).
                  </p>
                </div>
              </div>
            )}

            {authValue?.type === "bearer-token" && (
              <div className="space-y-2 pl-4 border-l-2 border-secondary-200">
                <Label htmlFor="bearer-token">
                  Bearer Token <span className="text-error-600">*</span>
                </Label>
                <Input
                  id="bearer-token"
                  type="password"
                  value={authValue?.token || ""}
                  onChange={(e) => handleAuthFieldChange("token", e.target.value)}
                  placeholder="Enter bearer token"
                />
                <p className="text-xs text-secondary-500">
                  Bearer token value. Supports template variables.
                </p>
              </div>
            )}
          </CardContent>
        )}
      </Card>

      {/* Timeout */}
      <div className="space-y-2">
        <Label htmlFor="api-timeout">Timeout (milliseconds)</Label>
        <Input
          id="api-timeout"
          type="number"
          min={1}
          {...register("timeout", {
            min: { value: 1, message: "Timeout must be greater than 0" },
            valueAsNumber: true,
          })}
          onChange={(e) => {
            register("timeout").onChange(e)
            handleChange("timeout", parseInt(e.target.value) || 5000)
          }}
        />
        {errors.timeout && (
          <p className="text-sm text-error-600">{errors.timeout.message as string}</p>
        )}
        <p className="text-xs text-secondary-500">
          Request timeout in milliseconds (default: 5000).
        </p>
      </div>

      {/* Retry - Collapsible */}
      <Card className="border-secondary-200">
        <CardHeader
          className="cursor-pointer"
          onClick={() => setShowRetry(!showRetry)}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              {showRetry ? (
                <ChevronDown className="h-4 w-4 text-secondary-500" />
              ) : (
                <ChevronRight className="h-4 w-4 text-secondary-500" />
              )}
              <CardTitle className="text-base">Retry Configuration</CardTitle>
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <HelpCircle className="h-4 w-4 text-secondary-400 cursor-help" />
                  </TooltipTrigger>
                  <TooltipContent>
                    <p className="max-w-xs">
                      Configure retry behavior for failed requests. Exponential backoff increases delay between retries.
                    </p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            </div>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={(e) => {
                e.stopPropagation()
                if (showRetry) {
                  handleChange("retry", undefined)
                  setShowRetry(false)
                } else {
                  handleChange("retry", { maxAttempts: 3, backoffStrategy: "exponential" })
                  setShowRetry(true)
                }
              }}
              className="cursor-pointer"
            >
              {showRetry ? "Remove" : "Add"}
            </Button>
          </div>
          <CardDescription className="text-xs">
            Optional retry configuration
          </CardDescription>
        </CardHeader>
        {showRetry && (
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="retry-attempts">Max Attempts</Label>
              <Input
                id="retry-attempts"
                type="number"
                min={1}
                value={retryValue?.maxAttempts || 3}
                onChange={(e) => handleRetryChange("maxAttempts", parseInt(e.target.value) || 3)}
              />
              <p className="text-xs text-secondary-500">
                Maximum number of retry attempts (default: 3).
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="retry-strategy">Backoff Strategy</Label>
              <Select
                value={retryValue?.backoffStrategy || "exponential"}
                onValueChange={(value) => handleRetryChange("backoffStrategy", value)}
              >
                <SelectTrigger id="retry-strategy">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="exponential">Exponential</SelectItem>
                  <SelectItem value="linear">Linear</SelectItem>
                  <SelectItem value="fixed">Fixed</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-xs text-secondary-500">
                Retry delay strategy (default: exponential).
              </p>
            </div>
          </CardContent>
        )}
      </Card>
    </div>
  )
}

