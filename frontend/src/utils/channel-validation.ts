import type { ChannelType, SlackChannelConfig, DiscordChannelConfig, TeamsChannelConfig, InAppChannelConfig, WebhookChannelConfig } from "@/types/channel"

export interface ValidationResult {
  valid: boolean
  errors: string[]
}

/**
 * Validate channel configuration based on channel type
 */
export function validateChannelConfig(
  type: ChannelType,
  config: Record<string, unknown>
): ValidationResult {
  const errors: string[] = []

  switch (type) {
    case "slack": {
      const slackConfig = config as unknown as SlackChannelConfig
      if (!slackConfig.botToken || typeof slackConfig.botToken !== "string" || slackConfig.botToken.trim() === "") {
        errors.push("Bot token is required")
      } else if (!slackConfig.botToken.startsWith("xoxb-") && !slackConfig.botToken.startsWith("xoxp-")) {
        errors.push("Bot token should start with 'xoxb-' or 'xoxp-'")
      }
      break
    }

    case "discord": {
      const discordConfig = config as unknown as DiscordChannelConfig
      if (!discordConfig.botToken || typeof discordConfig.botToken !== "string" || discordConfig.botToken.trim() === "") {
        errors.push("Bot token is required")
      }
      if (discordConfig.serverId && typeof discordConfig.serverId === "string" && discordConfig.serverId.length < 17) {
        errors.push("Server ID should be at least 17 characters")
      }
      if (discordConfig.defaultChannelId && typeof discordConfig.defaultChannelId === "string" && discordConfig.defaultChannelId.length < 17) {
        errors.push("Channel ID should be at least 17 characters")
      }
      break
    }

    case "teams": {
      const teamsConfig = config as unknown as TeamsChannelConfig
      if (!teamsConfig.webhookUrl || typeof teamsConfig.webhookUrl !== "string" || teamsConfig.webhookUrl.trim() === "") {
        errors.push("Webhook URL is required")
      } else {
        try {
          new URL(teamsConfig.webhookUrl)
        } catch {
          errors.push("Webhook URL must be a valid URL")
        }
        if (!teamsConfig.webhookUrl.includes("office.com/webhook")) {
          errors.push("Webhook URL should be a Microsoft Teams webhook URL")
        }
      }
      break
    }

    case "webhook": {
      const webhookConfig = config as unknown as WebhookChannelConfig
      if (!webhookConfig.url || typeof webhookConfig.url !== "string" || webhookConfig.url.trim() === "") {
        errors.push("Webhook URL is required")
      } else {
        try {
          new URL(webhookConfig.url)
        } catch {
          errors.push("Webhook URL must be a valid URL")
        }
      }
      if (!webhookConfig.method || !["GET", "POST", "PUT", "PATCH"].includes(webhookConfig.method)) {
        errors.push("HTTP method must be GET, POST, PUT, or PATCH")
      }
      if (webhookConfig.authType === "basic") {
        if (!webhookConfig.authConfig?.username || !webhookConfig.authConfig?.password) {
          errors.push("Username and password are required for Basic Auth")
        }
      }
      if (webhookConfig.authType === "bearer") {
        if (!webhookConfig.authConfig?.token) {
          errors.push("Token is required for Bearer Auth")
        }
      }
      if (webhookConfig.authType === "api-key") {
        if (!webhookConfig.authConfig?.apiKey) {
          errors.push("API Key is required for API Key Auth")
        }
      }
      break
    }

    case "in-app": {
      const inAppConfig = config as InAppChannelConfig
      if (inAppConfig.apiEndpoint) {
        try {
          new URL(inAppConfig.apiEndpoint as string)
        } catch {
          errors.push("API Endpoint must be a valid URL")
        }
      }
      if (inAppConfig.defaultExpiry !== undefined) {
        const expiry = Number(inAppConfig.defaultExpiry)
        if (isNaN(expiry) || expiry < 0) {
          errors.push("Default expiry must be a positive number")
        }
      }
      break
    }

    case "email":
    case "sms":
    case "push":
      // These are handled elsewhere
      break
  }

  return {
    valid: errors.length === 0,
    errors,
  }
}

/**
 * Get validation rules for a channel type
 */
export function getChannelValidationRules(type: ChannelType): Record<string, unknown> {
  switch (type) {
    case "slack":
      return {
        slackBotToken: {
          required: "Bot token is required",
          validate: (value: string) => {
            if (!value) return "Bot token is required"
            if (!value.startsWith("xoxb-") && !value.startsWith("xoxp-")) {
              return "Bot token should start with 'xoxb-' or 'xoxp-'"
            }
            return true
          },
        },
      }

    case "discord":
      return {
        discordBotToken: {
          required: "Bot token is required",
        },
        discordServerId: {
          validate: (value: string) => {
            if (value && value.length < 17) {
              return "Server ID should be at least 17 characters"
            }
            return true
          },
        },
        discordDefaultChannelId: {
          validate: (value: string) => {
            if (value && value.length < 17) {
              return "Channel ID should be at least 17 characters"
            }
            return true
          },
        },
      }

    case "teams":
      return {
        teamsWebhookUrl: {
          required: "Webhook URL is required",
          validate: (value: string) => {
            if (!value) return "Webhook URL is required"
            try {
              new URL(value)
              if (!value.includes("office.com/webhook")) {
                return "Webhook URL should be a Microsoft Teams webhook URL"
              }
              return true
            } catch {
              return "Webhook URL must be a valid URL"
            }
          },
        },
      }

    case "webhook":
      return {
        webhookUrl: {
          required: "Webhook URL is required",
          validate: (value: string) => {
            if (!value) return "Webhook URL is required"
            try {
              new URL(value)
              return true
            } catch {
              return "Webhook URL must be a valid URL"
            }
          },
        },
        webhookMethod: {
          required: "HTTP method is required",
        },
      }

    case "in-app":
      return {
        inAppDefaultExpiry: {
          validate: (value: number) => {
            if (value !== undefined && (isNaN(value) || value < 0)) {
              return "Default expiry must be a positive number"
            }
            return true
          },
        },
      }

    default:
      return {}
  }
}

