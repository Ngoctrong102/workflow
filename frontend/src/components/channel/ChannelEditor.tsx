import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useForm, Controller } from "react-hook-form"
import { Loader2, CheckCircle2, XCircle } from "lucide-react"
import type { Channel, ChannelType } from "@/types/channel"
import { getChannelValidationRules } from "@/utils/channel-validation"

interface ChannelEditorProps {
  channel?: Channel
  onSave: (channel: Omit<Channel, "id" | "createdAt" | "updatedAt">) => void
  onCancel: () => void
  onTest?: (config: Record<string, unknown>) => Promise<boolean>
}

export function ChannelEditor({ channel, onSave, onCancel, onTest }: ChannelEditorProps) {
  const [isTesting, setIsTesting] = useState(false)
  const [testResult, setTestResult] = useState<{ success: boolean; message: string } | null>(null)

  const { register, handleSubmit, watch, control, formState: { errors } } = useForm({
    defaultValues: {
      name: channel?.name || "",
      description: channel?.description || "",
      type: (channel?.type || "email") as ChannelType,
      status: channel?.status || "inactive",
      // Email config
      smtpHost: (channel?.config as { smtpHost?: string })?.smtpHost || "",
      smtpPort: (channel?.config as { smtpPort?: number })?.smtpPort || 587,
      smtpUsername: (channel?.config as { smtpUsername?: string })?.smtpUsername || "",
      smtpPassword: (channel?.config as { smtpPassword?: string })?.smtpPassword || "",
      fromEmail: (channel?.config as { fromEmail?: string })?.fromEmail || "",
      fromName: (channel?.config as { fromName?: string })?.fromName || "",
      useTls: (channel?.config as { useTls?: boolean })?.useTls ?? true,
      // SMS config
      smsProvider: (channel?.config as { provider?: string })?.provider || "twilio",
      smsApiKey: (channel?.config as { apiKey?: string })?.apiKey || "",
      smsApiSecret: (channel?.config as { apiSecret?: string })?.apiSecret || "",
      smsFromNumber: (channel?.config as { fromNumber?: string })?.fromNumber || "",
      smsAccountSid: (channel?.config as { accountSid?: string })?.accountSid || "",
      // Push config
      pushProvider: (channel?.config as { provider?: string })?.provider || "fcm",
      fcmServerKey: (channel?.config as { fcmServerKey?: string })?.fcmServerKey || "",
      // Webhook config
      webhookUrl: (channel?.config as { url?: string })?.url || "",
      webhookMethod: (channel?.config as { method?: string })?.method || "POST",
      webhookAuthType: (channel?.config as { authType?: string })?.authType || "none",
      webhookHeaders: (channel?.config as { headers?: Record<string, string> })?.headers || {},
      webhookAuthUsername: (channel?.config as { authConfig?: { username?: string } })?.authConfig?.username || "",
      webhookAuthPassword: (channel?.config as { authConfig?: { password?: string } })?.authConfig?.password || "",
      webhookAuthToken: (channel?.config as { authConfig?: { token?: string } })?.authConfig?.token || "",
      webhookAuthApiKey: (channel?.config as { authConfig?: { apiKey?: string } })?.authConfig?.apiKey || "",
      // Slack config
      slackBotToken: (channel?.config as { botToken?: string })?.botToken || "",
      slackDefaultChannel: (channel?.config as { defaultChannel?: string })?.defaultChannel || "",
      slackWorkspace: (channel?.config as { workspace?: string })?.workspace || "",
      // Discord config
      discordBotToken: (channel?.config as { botToken?: string })?.botToken || "",
      discordServerId: (channel?.config as { serverId?: string })?.serverId || "",
      discordDefaultChannelId: (channel?.config as { defaultChannelId?: string })?.defaultChannelId || "",
      // Teams config
      teamsWebhookUrl: (channel?.config as { webhookUrl?: string })?.webhookUrl || "",
      teamsConnector: (channel?.config as { connector?: string })?.connector || "",
      // In-App config
      inAppApiEndpoint: (channel?.config as { apiEndpoint?: string })?.apiEndpoint || "",
      inAppApiKey: (channel?.config as { apiKey?: string })?.apiKey || "",
      inAppDefaultExpiry: (channel?.config as { defaultExpiry?: number })?.defaultExpiry || 86400,
    },
  })

  const channelType = watch("type")
  const validationRules = getChannelValidationRules(channelType || "email")

  const handleTest = async () => {
    if (!onTest) return

    setIsTesting(true)
    setTestResult(null)

    try {
      const formData = watch()
      const config = getConfigForType(channelType, formData)
      const success = await onTest(config)
      setTestResult({
        success,
        message: success
          ? "Connection test successful"
          : "Connection test failed. Please check your configuration.",
      })
    } catch (error) {
      setTestResult({
        success: false,
        message: error instanceof Error ? error.message : "Connection test failed",
      })
    } finally {
      setIsTesting(false)
    }
  }

  const getConfigForType = (type: ChannelType, formData: Record<string, unknown>) => {
    switch (type) {
      case "email":
        return {
          smtpHost: formData.smtpHost,
          smtpPort: Number(formData.smtpPort),
          smtpUsername: formData.smtpUsername,
          smtpPassword: formData.smtpPassword,
          fromEmail: formData.fromEmail,
          fromName: formData.fromName,
          useTls: formData.useTls,
        }
      case "sms":
        return {
          provider: formData.smsProvider,
          apiKey: formData.smsApiKey,
          apiSecret: formData.smsApiSecret,
          fromNumber: formData.smsFromNumber,
          accountSid: formData.smsAccountSid,
        }
      case "push":
        return {
          provider: formData.pushProvider,
          fcmServerKey: formData.fcmServerKey,
        }
      case "webhook":
        const webhookConfig: Record<string, unknown> = {
          url: formData.webhookUrl,
          method: formData.webhookMethod,
          authType: formData.webhookAuthType,
        }
        if (formData.webhookHeaders && Object.keys(formData.webhookHeaders as Record<string, string>).length > 0) {
          webhookConfig.headers = formData.webhookHeaders
        }
        if (formData.webhookAuthType !== "none") {
          webhookConfig.authConfig = {}
          if (formData.webhookAuthType === "basic") {
            webhookConfig.authConfig = {
              username: formData.webhookAuthUsername,
              password: formData.webhookAuthPassword,
            }
          } else if (formData.webhookAuthType === "bearer") {
            webhookConfig.authConfig = {
              token: formData.webhookAuthToken,
            }
          } else if (formData.webhookAuthType === "api-key") {
            webhookConfig.authConfig = {
              apiKey: formData.webhookAuthApiKey,
            }
          }
        }
        return webhookConfig
      case "slack":
        return {
          botToken: formData.slackBotToken,
          defaultChannel: formData.slackDefaultChannel,
          workspace: formData.slackWorkspace,
        }
      case "discord":
        return {
          botToken: formData.discordBotToken,
          serverId: formData.discordServerId,
          defaultChannelId: formData.discordDefaultChannelId,
        }
      case "teams":
        return {
          webhookUrl: formData.teamsWebhookUrl,
          connector: formData.teamsConnector,
        }
      case "in-app":
        return {
          apiEndpoint: formData.inAppApiEndpoint,
          apiKey: formData.inAppApiKey,
          defaultExpiry: Number(formData.inAppDefaultExpiry) || 86400,
        }
      default:
        return {}
    }
  }

  const onSubmit = (data: Record<string, unknown>) => {
    const config = getConfigForType(channelType, data)
    onSave({
      name: data.name as string,
      description: data.description as string,
      type: channelType,
      status: (data.status as Channel["status"]) || "inactive",
      config,
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Basic Info */}
      <Card>
        <CardHeader>
          <CardTitle>Channel Information</CardTitle>
          <CardDescription>Basic channel details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Channel Name *</Label>
            <Input
              id="name"
              {...register("name", { required: "Name is required" })}
              placeholder="Enter channel name"
            />
            {errors.name && (
              <p className="text-sm text-error-600">{errors.name.message as string}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Input
              id="description"
              {...register("description")}
              placeholder="Enter channel description"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="type">Channel Type *</Label>
            <Controller
              name="type"
              control={control}
              rules={{ required: "Type is required" }}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select channel type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="email">Email</SelectItem>
                    <SelectItem value="sms">SMS</SelectItem>
                    <SelectItem value="push">Push Notification</SelectItem>
                    <SelectItem value="in-app">In-App</SelectItem>
                    <SelectItem value="slack">Slack</SelectItem>
                    <SelectItem value="discord">Discord</SelectItem>
                    <SelectItem value="teams">Teams</SelectItem>
                    <SelectItem value="webhook">Webhook</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.type && (
              <p className="text-sm text-error-600">{errors.type.message as string}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Channel-Specific Configuration */}
      <Card>
        <CardHeader>
          <CardTitle>Configuration</CardTitle>
          <CardDescription>Channel-specific settings</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {channelType === "email" && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smtpHost">SMTP Host *</Label>
                  <Input
                    id="smtpHost"
                    {...register("smtpHost", { required: "SMTP host is required" })}
                    placeholder="smtp.example.com"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="smtpPort">SMTP Port *</Label>
                  <Input
                    id="smtpPort"
                    type="number"
                    {...register("smtpPort", { required: "SMTP port is required", valueAsNumber: true })}
                    placeholder="587"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smtpUsername">SMTP Username *</Label>
                  <Input
                    id="smtpUsername"
                    {...register("smtpUsername", { required: "SMTP username is required" })}
                    placeholder="username"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="smtpPassword">SMTP Password *</Label>
                  <Input
                    id="smtpPassword"
                    type="password"
                    {...register("smtpPassword", { required: "SMTP password is required" })}
                    placeholder="password"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="fromEmail">From Email *</Label>
                  <Input
                    id="fromEmail"
                    type="email"
                    {...register("fromEmail", { required: "From email is required" })}
                    placeholder="noreply@example.com"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="fromName">From Name</Label>
                  <Input
                    id="fromName"
                    {...register("fromName")}
                    placeholder="Company Name"
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register("useTls")}
                    className="rounded"
                  />
                  <span>Use TLS</span>
                </Label>
              </div>
            </>
          )}

          {channelType === "sms" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="smsProvider">Provider *</Label>
                <Controller
                  name="smsProvider"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select provider" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="twilio">Twilio</SelectItem>
                        <SelectItem value="aws-sns">AWS SNS</SelectItem>
                        <SelectItem value="custom">Custom</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smsApiKey">API Key *</Label>
                  <Input
                    id="smsApiKey"
                    {...register("smsApiKey", { required: "API key is required" })}
                    placeholder="API key"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="smsApiSecret">API Secret</Label>
                  <Input
                    id="smsApiSecret"
                    type="password"
                    {...register("smsApiSecret")}
                    placeholder="API secret"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="smsFromNumber">From Number</Label>
                  <Input
                    id="smsFromNumber"
                    {...register("smsFromNumber")}
                    placeholder="+1234567890"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="smsAccountSid">Account SID</Label>
                  <Input
                    id="smsAccountSid"
                    {...register("smsAccountSid")}
                    placeholder="Account SID"
                  />
                </div>
              </div>
            </>
          )}

          {channelType === "push" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="pushProvider">Provider *</Label>
                <Controller
                  name="pushProvider"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select provider" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="fcm">Firebase Cloud Messaging (FCM)</SelectItem>
                        <SelectItem value="apns">Apple Push Notification Service (APNs)</SelectItem>
                        <SelectItem value="custom">Custom</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
              {watch("pushProvider") === "fcm" && (
                <div className="space-y-2">
                  <Label htmlFor="fcmServerKey">FCM Server Key *</Label>
                  <Textarea
                    id="fcmServerKey"
                    {...register("fcmServerKey", { required: "FCM server key is required" })}
                    placeholder="Enter FCM server key"
                    rows={4}
                  />
                </div>
              )}
            </>
          )}

          {channelType === "slack" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="slackBotToken">Bot Token *</Label>
                <Input
                  id="slackBotToken"
                  type="password"
                  {...register("slackBotToken", validationRules.slackBotToken as any)}
                  placeholder="xoxb-your-bot-token"
                />
                {errors.slackBotToken && (
                  <p className="text-sm text-error-600">{errors.slackBotToken.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Get your bot token from{" "}
                  <a
                    href="https://api.slack.com/apps"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary-600 hover:underline"
                  >
                    Slack API
                  </a>
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="slackDefaultChannel">Default Channel</Label>
                <Input
                  id="slackDefaultChannel"
                  {...register("slackDefaultChannel")}
                  placeholder="#general or C1234567890"
                />
                <p className="text-xs text-secondary-500">
                  Channel name (e.g., #general) or channel ID
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="slackWorkspace">Workspace</Label>
                <Input
                  id="slackWorkspace"
                  {...register("slackWorkspace")}
                  placeholder="workspace-name"
                />
                <p className="text-xs text-secondary-500">
                  Optional workspace identifier
                </p>
              </div>
            </>
          )}

          {channelType === "discord" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="discordBotToken">Bot Token *</Label>
                <Input
                  id="discordBotToken"
                  type="password"
                  {...register("discordBotToken", validationRules.discordBotToken as any)}
                  placeholder="your-discord-bot-token"
                />
                {errors.discordBotToken && (
                  <p className="text-sm text-error-600">{errors.discordBotToken.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Get your bot token from{" "}
                  <a
                    href="https://discord.com/developers/applications"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary-600 hover:underline"
                  >
                    Discord Developer Portal
                  </a>
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="discordServerId">Server ID</Label>
                <Input
                  id="discordServerId"
                  {...register("discordServerId", validationRules.discordServerId as any)}
                  placeholder="123456789012345678"
                />
                {errors.discordServerId && (
                  <p className="text-sm text-error-600">{errors.discordServerId.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Optional Discord server (guild) ID
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="discordDefaultChannelId">Default Channel ID</Label>
                <Input
                  id="discordDefaultChannelId"
                  {...register("discordDefaultChannelId", validationRules.discordDefaultChannelId as any)}
                  placeholder="123456789012345678"
                />
                {errors.discordDefaultChannelId && (
                  <p className="text-sm text-error-600">{errors.discordDefaultChannelId.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Optional default channel ID for notifications
                </p>
              </div>
            </>
          )}

          {channelType === "teams" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="teamsWebhookUrl">Webhook URL *</Label>
                <Input
                  id="teamsWebhookUrl"
                  type="url"
                  {...register("teamsWebhookUrl", validationRules.teamsWebhookUrl as any)}
                  placeholder="https://outlook.office.com/webhook/..."
                />
                {errors.teamsWebhookUrl && (
                  <p className="text-sm text-error-600">{errors.teamsWebhookUrl.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Get your webhook URL from Microsoft Teams connector settings
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="teamsConnector">Connector</Label>
                <Input
                  id="teamsConnector"
                  {...register("teamsConnector")}
                  placeholder="Incoming Webhook"
                />
                <p className="text-xs text-secondary-500">
                  Optional connector name
                </p>
              </div>
            </>
          )}

          {channelType === "in-app" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="inAppApiEndpoint">API Endpoint</Label>
                <Input
                  id="inAppApiEndpoint"
                  type="url"
                  {...register("inAppApiEndpoint")}
                  placeholder="https://api.example.com/notifications"
                />
                <p className="text-xs text-secondary-500">
                  Optional API endpoint for external notification service
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="inAppApiKey">API Key</Label>
                <Input
                  id="inAppApiKey"
                  type="password"
                  {...register("inAppApiKey")}
                  placeholder="API key for authentication"
                />
                <p className="text-xs text-secondary-500">
                  Optional API key if using external service
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="inAppDefaultExpiry">Default Expiry (seconds)</Label>
                <Input
                  id="inAppDefaultExpiry"
                  type="number"
                  {...register("inAppDefaultExpiry", { 
                    valueAsNumber: true, 
                    ...(validationRules.inAppDefaultExpiry as any)
                  })}
                  placeholder="86400"
                />
                {errors.inAppDefaultExpiry && (
                  <p className="text-sm text-error-600">{errors.inAppDefaultExpiry.message as string}</p>
                )}
                <p className="text-xs text-secondary-500">
                  Default expiration time in seconds (default: 86400 = 24 hours)
                </p>
              </div>
            </>
          )}

          {channelType === "webhook" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="webhookUrl">Webhook URL *</Label>
                <Input
                  id="webhookUrl"
                  type="url"
                  {...register("webhookUrl", validationRules.webhookUrl as any)}
                  placeholder="https://api.example.com/webhook"
                />
                {errors.webhookUrl && (
                  <p className="text-sm text-error-600">{errors.webhookUrl.message as string}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="webhookMethod">HTTP Method *</Label>
                <Controller
                  name="webhookMethod"
                  control={control}
                  rules={validationRules.webhookMethod as any}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select method" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="GET">GET</SelectItem>
                        <SelectItem value="POST">POST</SelectItem>
                        <SelectItem value="PUT">PUT</SelectItem>
                        <SelectItem value="PATCH">PATCH</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="webhookAuthType">Authentication Type</Label>
                <Controller
                  name="webhookAuthType"
                  control={control}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select auth type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="none">None</SelectItem>
                        <SelectItem value="basic">Basic Auth</SelectItem>
                        <SelectItem value="bearer">Bearer Token</SelectItem>
                        <SelectItem value="api-key">API Key</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.webhookMethod && (
                  <p className="text-sm text-error-600">{errors.webhookMethod.message as string}</p>
                )}
              </div>
              {watch("webhookAuthType") === "basic" && (
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="webhookAuthUsername">Username</Label>
                    <Input
                      id="webhookAuthUsername"
                      {...register("webhookAuthUsername")}
                      placeholder="username"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="webhookAuthPassword">Password</Label>
                    <Input
                      id="webhookAuthPassword"
                      type="password"
                      {...register("webhookAuthPassword")}
                      placeholder="password"
                    />
                  </div>
                </div>
              )}
              {watch("webhookAuthType") === "bearer" && (
                <div className="space-y-2">
                  <Label htmlFor="webhookAuthToken">Bearer Token</Label>
                  <Input
                    id="webhookAuthToken"
                    type="password"
                    {...register("webhookAuthToken")}
                    placeholder="Bearer token"
                  />
                </div>
              )}
              {watch("webhookAuthType") === "api-key" && (
                <div className="space-y-2">
                  <Label htmlFor="webhookAuthApiKey">API Key</Label>
                  <Input
                    id="webhookAuthApiKey"
                    type="password"
                    {...register("webhookAuthApiKey")}
                    placeholder="API key"
                  />
                </div>
              )}
            </>
          )}

          {/* Test Connection */}
          {onTest && (
            <div className="pt-4 border-t">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold">Test Connection</h3>
                  <p className="text-sm text-secondary-500">
                    Test the channel configuration before saving
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleTest}
                  disabled={isTesting}
                >
                  {isTesting ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Testing...
                    </>
                  ) : (
                    "Test Connection"
                  )}
                </Button>
              </div>
              {testResult && (
                <Alert
                  variant={testResult.success ? "default" : "destructive"}
                  className="mt-4"
                >
                  {testResult.success ? (
                    <CheckCircle2 className="h-4 w-4" />
                  ) : (
                    <XCircle className="h-4 w-4" />
                  )}
                  <AlertDescription>{testResult.message}</AlertDescription>
                </Alert>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="flex justify-end space-x-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">Save Channel</Button>
      </div>
    </form>
  )
}

