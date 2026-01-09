import type { TemplateVariable } from "@/types/template"

export const TEMPLATE_VARIABLES: TemplateVariable[] = [
  // User Variables
  { name: "user.id", description: "User ID", type: "user", example: "123" },
  { name: "user.name", description: "User name", type: "user", example: "John Doe" },
  { name: "user.email", description: "User email", type: "user", example: "john@example.com" },
  { name: "user.phone", description: "User phone number", type: "user", example: "+1234567890" },
  
  // Workflow Variables
  { name: "workflow.id", description: "Workflow ID", type: "workflow", example: "wf-123" },
  { name: "workflow.name", description: "Workflow name", type: "workflow", example: "Welcome Email" },
  { name: "execution.id", description: "Execution ID", type: "workflow", example: "exec-456" },
  { name: "execution.timestamp", description: "Execution timestamp", type: "workflow", example: "2024-01-01T00:00:00Z" },
  
  // Custom Variables
  { name: "custom.field", description: "Custom field from workflow context", type: "custom", example: "custom.value" },
  { name: "data.key", description: "Data from trigger", type: "custom", example: "trigger.data" },
  
  // System Variables
  { name: "date.now", description: "Current date/time", type: "system", example: "2024-01-01T00:00:00Z" },
  { name: "date.format", description: "Formatted date", type: "system", example: "January 1, 2024" },
  { name: "url.base", description: "Base URL", type: "system", example: "https://example.com" },
]

export const VARIABLES_BY_TYPE = {
  user: TEMPLATE_VARIABLES.filter((v) => v.type === "user"),
  workflow: TEMPLATE_VARIABLES.filter((v) => v.type === "workflow"),
  custom: TEMPLATE_VARIABLES.filter((v) => v.type === "custom"),
  system: TEMPLATE_VARIABLES.filter((v) => v.type === "system"),
}

