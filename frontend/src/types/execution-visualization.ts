import type { ExecutionDetails, NodeExecution } from "./execution"

export interface ExecutionVisualizationData {
  execution: ExecutionDetails
  workflow: {
    id: string
    name: string
    definition: {
      nodes: Array<{
        id: string
        type: string
        position: { x: number; y: number }
        data: {
          label: string
          config?: Record<string, unknown>
        }
      }>
      edges: Array<{
        id: string
        source: string
        target: string
        sourceHandle?: string
        targetHandle?: string
      }>
    }
  }
  trigger: {
    type: string
    data: Record<string, unknown>
  }
  current_step: number
  total_steps: number
  nodes: Array<{
    id: string
    type: string
    status: "pending" | "running" | "completed" | "failed"
    execution?: NodeExecution
  }>
  context: Record<string, unknown>
}

export interface StepExecutionRequest {
  direction: "forward" | "backward"
}

export interface StepExecutionResponse {
  step_number: number
  node_id: string
  node_type: string
  status: "running" | "completed" | "failed"
  execution?: NodeExecution
  context: Record<string, unknown>
  next_node?: string
  has_next: boolean
  has_previous: boolean
}

export interface StepState {
  step_number: number
  node_id: string
  node_type: string
  status: "pending" | "running" | "completed" | "failed"
  execution?: NodeExecution
  context: Record<string, unknown>
  has_next: boolean
  has_previous: boolean
}

