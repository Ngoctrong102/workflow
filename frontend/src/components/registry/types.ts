/**
 * TypeScript types for action configuration templates
 */

export type ActionType = "api-call" | "publish-event" | "function" | "custom-action"

export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE"

export type AuthenticationType = "api-key" | "bearer-token"

export type BackoffStrategy = "exponential" | "linear" | "fixed"

/**
 * API Call Action Configuration
 */
export interface ApiCallConfig {
  url?: string
  method?: HttpMethod
  headers?: Record<string, string>
  body?: any
  authentication?: {
    type?: AuthenticationType
    apiKey?: string
    headerName?: string
    token?: string
  }
  timeout?: number
  retry?: {
    maxAttempts?: number
    backoffStrategy?: BackoffStrategy
  }
}

/**
 * Publish Event Action Configuration (Kafka)
 */
export interface PublishEventConfig {
  kafka?: {
    brokers?: string[]
    topic?: string
    key?: string
    headers?: Record<string, string>
  }
  message?: any
}

/**
 * Function Action Configuration
 */
export interface FunctionConfig {
  expression?: string
  outputField?: string
}

import type { SchemaDefinition } from "./SchemaEditor"

/**
 * Complete Action Config Template
 * Contains schemas and configuration structure
 */
export interface ActionConfigTemplate {
  // Optional: Data từ previous nodes để map
  inputSchema?: SchemaDefinition[]
  
  // Required: Output structure
  outputSchema: SchemaDefinition[]
  
  // Required: Config fields structure (url, method, headers, etc.)
  // Defines the structure of config fields that users will fill in workflow builder
  // Users can provide static values or MVEL expressions
  configTemplate: SchemaDefinition[]
  
  // Required: MVEL expressions để map từ _response vào output schema
  // Key: field name trong output schema
  // Value: MVEL expression để evaluate với context có `_response`
  outputMapping: Record<string, string>
  
  // Legacy fields (deprecated, kept for backward compatibility)
  // These will be migrated to configTemplate schema
  url?: string
  method?: HttpMethod
  headers?: Record<string, string>
  body?: any
  authentication?: {
    type?: AuthenticationType
    apiKey?: string
    headerName?: string
    token?: string
  }
  timeout?: number
  retry?: {
    maxAttempts?: number
    backoffStrategy?: BackoffStrategy
  }
  kafka?: {
    brokers?: string[]
    topic?: string
    key?: string
    headers?: Record<string, string>
  }
  message?: any
  expression?: string
  outputField?: string
}

