import type { ActionType } from "@/components/registry/types"
import type { SchemaDefinition, FieldDefinition } from "@/components/registry/SchemaEditor"

/**
 * Auto-generate config template schema from action type
 * This defines the structure of config fields (url, method, headers, etc.)
 * Users can provide static values or MVEL expressions in workflow builder
 */
export function generateConfigTemplateSchema(
  actionType: ActionType
): SchemaDefinition[] {
  switch (actionType) {
    case "api-call": {
      const urlField: FieldDefinition = {
        name: "url",
        type: "url",
        required: true,
        description: "API endpoint URL. Can be static or MVEL expression: /users/@{userID}",
      }

      const methodField: FieldDefinition = {
        name: "method",
        type: "string",
        required: true,
        validation: {
          enum: ["GET", "POST", "PUT", "PATCH", "DELETE"],
        },
        defaultValue: "GET",
        description: "HTTP method",
      }

      const headersField: FieldDefinition = {
        name: "headers",
        type: "object",
        required: false,
        description: "HTTP headers. Can use MVEL expressions",
        fields: [
          {
            name: "key",
            type: "string",
            required: true,
            description: "Header name",
          },
          {
            name: "value",
            type: "string",
            required: true,
            description: "Header value. Can use MVEL: Bearer @{getToken.token}",
          },
        ],
      }

      const bodyField: FieldDefinition = {
        name: "body",
        type: "json",
        required: false,
        description: "Request body. Can use MVEL expressions",
      }

      const authenticationField: FieldDefinition = {
        name: "authentication",
        type: "object",
        required: false,
        description: "Authentication configuration",
        fields: [
          {
            name: "type",
            type: "string",
            required: false,
            validation: {
              enum: ["none", "api-key", "bearer-token"],
            },
            defaultValue: "none",
            description: "Authentication type",
          },
          {
            name: "apiKey",
            type: "string",
            required: false,
            description: "API key. Can use MVEL: @{apiKey}",
          },
          {
            name: "headerName",
            type: "string",
            required: false,
            description: "Header name for API key (e.g., X-API-Key)",
          },
          {
            name: "token",
            type: "string",
            required: false,
            description: "Bearer token. Can use MVEL: @{bearerToken}",
          },
        ],
      }

      const timeoutField: FieldDefinition = {
        name: "timeout",
        type: "number",
        required: false,
        defaultValue: 5000,
        description: "Request timeout in milliseconds",
      }

      return [
        {
          schemaId: "api-call-config",
          description: "API Call configuration fields",
          fields: [urlField, methodField, headersField, bodyField, authenticationField, timeoutField],
        },
      ]
    }

    case "publish-event": {
      const kafkaField: FieldDefinition = {
        name: "kafka",
        type: "object",
        required: true,
        description: "Kafka configuration",
        fields: [
          {
            name: "brokers",
            type: "array",
            required: true,
            description: "Kafka broker addresses",
            fields: [
              {
                name: "item",
                type: "string",
                required: true,
                validation: {
                  pattern: "^[^:]+:[0-9]+$",
                },
                description: "Broker address (host:port)",
              },
            ],
          },
          {
            name: "topic",
            type: "string",
            required: true,
            description: "Kafka topic name. Can use MVEL: events-@{eventType}",
          },
          {
            name: "key",
            type: "string",
            required: false,
            description: "Message key. Can use MVEL: @{userId}",
          },
          {
            name: "headers",
            type: "object",
            required: false,
            description: "Kafka message headers",
            fields: [
              {
                name: "key",
                type: "string",
                required: true,
                description: "Header name",
              },
              {
                name: "value",
                type: "string",
                required: true,
                description: "Header value. Can use MVEL expressions",
              },
            ],
          },
        ],
      }

      const messageField: FieldDefinition = {
        name: "message",
        type: "json",
        required: false,
        description: "Message payload. Can use MVEL expressions",
      }

      return [
        {
          schemaId: "publish-event-config",
          description: "Publish Event (Kafka) configuration fields",
          fields: [kafkaField, messageField],
        },
      ]
    }

    case "function": {
      const expressionField: FieldDefinition = {
        name: "expression",
        type: "string",
        required: true,
        description: "MVEL expression to evaluate. Can reference previous nodes: @{user.firstName} + ' ' + @{user.lastName}",
      }

      const outputField: FieldDefinition = {
        name: "outputField",
        type: "string",
        required: false,
        defaultValue: "result",
        description: "Output field name",
      }

      return [
        {
          schemaId: "function-config",
          description: "Function configuration fields",
          fields: [expressionField, outputField],
        },
      ]
    }

    default:
      return [
        {
          schemaId: "custom-config",
          description: "Custom action configuration fields",
          fields: [],
        },
      ]
  }
}

/**
 * Auto-generate default output mapping from action type
 * Maps raw response to output schema structure using MVEL expressions
 */
export function generateDefaultOutputMapping(
  actionType: ActionType,
  outputSchema: SchemaDefinition[]
): Record<string, string> {
  if (!outputSchema || outputSchema.length === 0) {
    return {}
  }

  const mapping: Record<string, string> = {}

  // Extract all fields from output schema
  const extractFields = (fields: FieldDefinition[], prefix = ""): void => {
    fields.forEach((field) => {
      const fieldPath = prefix ? `${prefix}.${field.name}` : field.name

      switch (actionType) {
        case "api-call":
          // Default mapping for API call: map common response fields
          if (field.name === "statusCode") {
            mapping[fieldPath] = "@{_response.statusCode}"
          } else if (field.name === "body") {
            mapping[fieldPath] = "@{_response.body}"
          } else if (field.name === "headers") {
            mapping[fieldPath] = "@{_response.headers}"
          } else {
            // Try to map from response body
            mapping[fieldPath] = `@{_response.body.${field.name}}`
          }
          break

        case "publish-event":
          // Default mapping for publish event
          if (field.name === "topic") {
            mapping[fieldPath] = "@{_response.topic}"
          } else if (field.name === "partition") {
            mapping[fieldPath] = "@{_response.partition}"
          } else if (field.name === "offset") {
            mapping[fieldPath] = "@{_response.offset}"
          } else {
            mapping[fieldPath] = `@{_response.${field.name}}`
          }
          break

        case "function":
          // Default mapping for function: result is in _response.result
          mapping[fieldPath] = `@{_response.result}`
          break

        default:
          // Default: try to map from response
          mapping[fieldPath] = `@{_response.${field.name}}`
      }

      // Handle nested fields
      if (field.fields && field.fields.length > 0) {
        extractFields(field.fields, fieldPath)
      }
    })
  }

  outputSchema.forEach((schema) => {
    if (schema.fields && schema.fields.length > 0) {
      extractFields(schema.fields)
    }
  })

  return mapping
}

