export type ExecutionStatus = "running" | "waiting" | "completed" | "failed" | "cancelled"

export interface Execution {
  id: string
  workflow_id: string
  workflow_name?: string
  trigger_id?: string
  status: ExecutionStatus
  started_at: string
  completed_at?: string
  duration?: number // in milliseconds
  nodes_executed: number
  notifications_sent: number
  context?: Record<string, unknown>
  error?: string
  created_at?: string
}

export interface ExecutionLog {
  id: string
  execution_id: string
  node_id?: string
  level: "info" | "warning" | "error" | "debug"
  message: string
  data?: Record<string, unknown>
  timestamp: string
}

export interface ExecutionListParams {
  workflow_id?: string
  status?: ExecutionStatus
  start_date?: string
  end_date?: string
  limit?: number
  offset?: number
  search?: string
}

export interface ExecutionListResponse {
  executions: Execution[]
  total: number
  limit: number
  offset: number
}

export interface WaitState {
  nodeId: string
  nodeLabel?: string
  correlationId: string
  enabledEvents: string[]
  receivedEvents: string[]
  status: "waiting" | "completed" | "timeout"
  expiresAt: string
  startedAt: string
  eventData?: {
    apiResponse?: Record<string, unknown>
    kafkaEvent?: Record<string, unknown>
  }
}

export interface ExecutionDetails extends Execution {
  node_executions?: NodeExecution[]
  logs?: ExecutionLog[]
  waitState?: WaitState
}

export interface NodeExecution {
  id: string
  execution_id: string
  node_id: string
  node_label?: string
  node_type?: string
  status: "running" | "waiting" | "completed" | "failed"
  started_at: string
  completed_at?: string
  duration?: number // in milliseconds
  input_data?: Record<string, unknown>
  output_data?: Record<string, unknown>
  error?: string
  wait_state?: WaitState
  created_at?: string
}

