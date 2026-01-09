export interface WorkflowDashboardOverview {
  workflow: {
    id: string
    name: string
    status: string
    last_execution?: string
  }
  metrics: {
    total_executions: number
    successful_executions: number
    failed_executions: number
    success_rate: number
    average_execution_time: number
    total_actions_executed?: number
    action_success_rate?: number
    total_notifications_sent?: number
    error_rate?: number
  }
  trends: {
    executions_change: number // Change from previous period
    success_rate_change: number
    execution_time_change: number
  }
  period: {
    start: string
    end: string
  }
}

export interface ExecutionTrendData {
  data: Array<{
    timestamp: string
    total: number
    successful: number
    failed: number
  }>
  executions?: Array<{
    id: string
    started_at: string
    completed_at?: string
    status: string
    duration?: number
  }>
}

export interface ExecutionStatusDistribution {
  status: "success" | "failed" | "running" | "cancelled"
  count: number
  percentage: number
}

export interface ExecutionTimeData {
  date: string
  average: number
  min: number
  max: number
  p50: number
  p95: number
  p99: number
}

export interface ChannelPerformanceData {
  channels: Array<{
    channel: string
    sent: number
    delivered: number
    delivery_rate: number
  }>
}

export interface NodePerformanceData {
  nodes: Array<{
    node_id: string
    node_name: string
    node_type: string
    execution_count: number
    average_execution_time: number
    success_rate: number
  }>
}

export interface ErrorAnalysis {
  summary: {
    total_errors: number
    error_rate: number
  }
  errors: Array<{
    id: string
    timestamp: string
    error_type: string
    error_message: string
    execution_id: string
  }>
  errorTimeline?: Array<{
    date: string
    count: number
  }>
}

export interface PerformanceAlert {
  id: string
  type: "slow_execution" | "high_error_rate" | "low_delivery_rate" | "node_failure"
  severity: "low" | "medium" | "high" | "critical"
  message: string
  timestamp: string
  dismissed?: boolean
}

export interface PerformanceRecommendation {
  id: string
  type: "optimization" | "best_practice" | "warning"
  title: string
  description: string
  action?: string
  link?: string
}

export interface WorkflowDashboardData {
  overview: WorkflowDashboardOverview
  executionTrends?: ExecutionTrendData
  executionStatusDistribution?: ExecutionStatusDistribution[]
  executionTimeData?: ExecutionTimeData[]
  channelPerformance?: ChannelPerformanceData
  nodePerformance?: NodePerformanceData
  errorAnalysis?: ErrorAnalysis
  alerts?: PerformanceAlert[]
  recommendations?: PerformanceRecommendation[]
}

