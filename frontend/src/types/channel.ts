export type ChannelType = "email" | "sms" | "push" | "in-app" | "slack" | "discord" | "teams" | "webhook"

export type ChannelStatus = "active" | "inactive" | "error"

export interface Channel {
  id: string
  name: string
  type: ChannelType
  status: ChannelStatus
  description?: string
  config: Record<string, unknown> // Channel-specific configuration
  createdAt?: string
  updatedAt?: string
  lastTestedAt?: string
}

// Channel-specific configurations
export interface EmailChannelConfig {
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword: string
  fromEmail: string
  fromName?: string
  useTls?: boolean
}

export interface SMSChannelConfig {
  provider: "twilio" | "aws-sns" | "custom"
  apiKey: string
  apiSecret?: string
  fromNumber?: string
  accountSid?: string
}

export interface PushChannelConfig {
  provider: "fcm" | "apns" | "custom"
  fcmServerKey?: string
  apnsKeyId?: string
  apnsTeamId?: string
  apnsBundleId?: string
  apnsKey?: string
}

export interface WebhookChannelConfig {
  url: string
  method: "GET" | "POST" | "PUT" | "PATCH"
  headers?: Record<string, string>
  authType?: "none" | "basic" | "bearer" | "api-key"
  authConfig?: Record<string, unknown>
}

export interface SlackChannelConfig {
  botToken: string
  defaultChannel?: string
  workspace?: string
}

export interface DiscordChannelConfig {
  botToken: string
  serverId?: string
  defaultChannelId?: string
}

export interface TeamsChannelConfig {
  webhookUrl: string
  connector?: string
}

export interface InAppChannelConfig {
  apiEndpoint?: string
  apiKey?: string
  defaultExpiry?: number // in seconds
}

